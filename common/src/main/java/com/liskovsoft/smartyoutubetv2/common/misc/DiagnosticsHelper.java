package com.liskovsoft.smartyoutubetv2.common.misc;

import android.app.ActivityManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.StatFs;
import android.util.DisplayMetrics;

import com.liskovsoft.sharedutils.helpers.AppInfoHelpers;
import com.liskovsoft.sharedutils.helpers.FileHelpers;
import com.liskovsoft.sharedutils.mylogger.Log;
import com.liskovsoft.youtubeapi.service.YouTubeServiceManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Health checks and a shareable report (app log + system info).<br/>
 * Everything is kept on the device. Files older than 24h are removed by {@link #cleanup(Context)}.
 */
public final class DiagnosticsHelper {
    private static final String TAG = DiagnosticsHelper.class.getSimpleName();
    private static final String DIR_NAME = "diagnostics";
    private static final String REPORT_FILE = "diagnostico-atual.txt";
    private static final String CRASH_FILE = "ultimo-erro.txt";
    private static final long MAX_AGE_MS = 24L * 60 * 60 * 1000;
    private static final int MAX_REPORT_CHARS = 300_000;
    private static final int LOGCAT_LINES = 800;
    private static final Charset UTF8 = Charset.forName("UTF-8");

    public interface ChecksCallback {
        void onDone(List<String> lines);
    }

    private DiagnosticsHelper() {
    }

    /**
     * Folder reachable without root: /sdcard/Android/media/&lt;package&gt;/diagnostics (adb pull / file manager).
     */
    public static File getDiagnosticsDir(Context context) {
        File base = null;

        try {
            base = FileHelpers.getExternalMediaDirectory(context);
        } catch (Throwable e) {
            // NOP
        }

        if (base == null) {
            base = context.getFilesDir();
        }

        File dir = new File(base, DIR_NAME);
        //noinspection ResultOfMethodCallIgnored
        dir.mkdirs();
        return dir;
    }

    /**
     * Removes diagnostic files and in-memory log lines older than 24h. Cheap: call it on every start.
     */
    public static void cleanup(Context context) {
        try {
            File[] files = getDiagnosticsDir(context).listFiles();

            if (files != null) {
                long now = System.currentTimeMillis();

                for (File file : files) {
                    if (now - file.lastModified() > MAX_AGE_MS) {
                        //noinspection ResultOfMethodCallIgnored
                        file.delete();
                    }
                }
            }
        } catch (Throwable e) {
            Log.e(TAG, "Cleanup failed: %s", e.getMessage());
        }
    }

    public static void clearAll(Context context) {
        try {
            File[] files = getDiagnosticsDir(context).listFiles();

            if (files != null) {
                for (File file : files) {
                    //noinspection ResultOfMethodCallIgnored
                    file.delete();
                }
            }
        } catch (Throwable e) {
            Log.e(TAG, "Clear failed: %s", e.getMessage());
        }

        Log.clearRecent();
    }

    /**
     * Called from the uncaught exception handler. Must never throw.
     */
    public static void saveCrash(Context context, Thread thread, Throwable error) {
        try {
            StringWriter trace = new StringWriter();
            error.printStackTrace(new PrintWriter(trace));

            StringBuilder sb = new StringBuilder();
            sb.append("Quando: ").append(now()).append('\n');
            sb.append("Thread: ").append(thread != null ? thread.getName() : "?").append('\n');
            sb.append("Erro:\n").append(trace).append('\n');
            sb.append("--- Registro do app antes do erro (últimas 200 linhas) ---\n");

            List<String> recent = Log.getRecent();
            for (int i = Math.max(0, recent.size() - 200); i < recent.size(); i++) {
                sb.append(recent.get(i)).append('\n');
            }

            writeFile(new File(getDiagnosticsDir(context), CRASH_FILE), redact(sb.toString()));
        } catch (Throwable e) {
            // NOP
        }
    }

    /**
     * Runs the health checks in the background. The callback is called on the main thread.
     */
    public static void runChecks(final Context context, final ChecksCallback callback) {
        final Context appContext = context.getApplicationContext();

        new Thread(() -> {
            final List<String> lines = new ArrayList<>();

            try {
                checkNetwork(lines);
                checkStorage(appContext, lines);
                checkMemory(appContext, lines);
                checkDecoders(lines);
                checkAccount(lines);
                checkRecentErrors(appContext, lines);
            } catch (Throwable e) {
                lines.add(line("ERRO", "Falha ao executar os testes: " + e));
            }

            new Handler(Looper.getMainLooper()).post(() -> callback.onDone(lines));
        }, "Diagnostics").start();
    }

    /**
     * Full report as text (secrets are removed).
     */
    public static String buildReport(Context context, List<String> checks) {
        StringBuilder sb = new StringBuilder();

        sb.append("=== Smart Hub: relatório de diagnóstico ===\n");
        sb.append("Gerado em: ").append(now()).append("\n\n");

        sb.append("--- App ---\n");
        sb.append("Versão: ").append(AppInfoHelpers.getAppVersionName(context))
                .append(" (").append(AppInfoHelpers.getAppVersionCode(context)).append(")\n");
        sb.append("Pacote: ").append(context.getPackageName()).append("\n\n");

        sb.append("--- Aparelho ---\n");
        sb.append("Modelo: ").append(Build.MANUFACTURER).append(' ').append(Build.MODEL)
                .append(" (").append(Build.DEVICE).append(")\n");
        sb.append("Android: ").append(Build.VERSION.RELEASE).append(" (SDK ").append(Build.VERSION.SDK_INT).append(")\n");
        sb.append("Idioma: ").append(Locale.getDefault()).append('\n');
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        sb.append("Tela: ").append(metrics.widthPixels).append('x').append(metrics.heightPixels)
                .append(" @").append(metrics.densityDpi).append("dpi\n");
        sb.append("Rede: ").append(getNetworkType(context)).append("\n\n");

        if (checks != null && !checks.isEmpty()) {
            sb.append("--- Testes rápidos ---\n");
            for (String check : checks) {
                sb.append(check).append('\n');
            }
            sb.append('\n');
        }

        File crash = new File(getDiagnosticsDir(context), CRASH_FILE);
        if (crash.exists()) {
            sb.append("--- Último erro que fechou o app (últimas 24h) ---\n");
            sb.append(readFile(crash)).append('\n');
        }

        sb.append("--- Registro do app (últimas 24h) ---\n");
        List<String> recent = Log.getRecent();
        for (String line : recent) {
            sb.append(line).append('\n');
        }
        if (recent.isEmpty()) {
            sb.append("(vazio)\n");
        }

        sb.append("\n--- Registro do sistema (apenas deste app) ---\n");
        sb.append(readLogcat());

        String result = redact(sb.toString());

        if (result.length() > MAX_REPORT_CHARS) {
            result = result.substring(0, 20_000) + "\n[...cortado...]\n" + result.substring(result.length() - (MAX_REPORT_CHARS - 20_000));
        }

        return result;
    }

    /**
     * @return the saved file or null
     */
    public static File saveReport(Context context, String report) {
        try {
            File file = new File(getDiagnosticsDir(context), REPORT_FILE);
            writeFile(file, report);
            return file;
        } catch (Throwable e) {
            Log.e(TAG, "Can't save the report: %s", e.getMessage());
            return null;
        }
    }

    public static boolean copyToClipboard(Context context, String text) {
        try {
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);

            if (clipboard == null) {
                return false;
            }

            // Binder limit is ~1MB: copy only the head and tail if the report is big.
            String clip = text.length() > 150_000 ? text.substring(0, 30_000) + "\n[...cortado...]\n" + text.substring(text.length() - 100_000) : text;
            clipboard.setPrimaryClip(ClipData.newPlainText("Smart Hub diagnóstico", clip));
            return true;
        } catch (Throwable e) {
            Log.e(TAG, "Clipboard failed: %s", e.getMessage());
            return false;
        }
    }

    /**
     * Removes tokens, cookies and keys from text before it leaves the app.
     */
    public static String redact(String text) {
        if (text == null) {
            return "";
        }

        String result = HEADER_SECRETS.matcher(text).replaceAll("$1: [removido]");
        result = PARAM_SECRETS.matcher(result).replaceAll("$1[removido]");
        result = BEARER.matcher(result).replaceAll("Bearer [removido]");
        result = LONG_TOKENS.matcher(result).replaceAll("[removido]");
        return result;
    }

    private static final Pattern HEADER_SECRETS = Pattern.compile("(?i)\\b(authorization|cookie|set-cookie)\\s*[:=]\\s*[^\\r\\n]+");
    private static final Pattern PARAM_SECRETS = Pattern.compile(
            "(?i)([?&;\\s\"'](?:access_token|refresh_token|token|pot|po_token|sig|signature|sapisid|key|api_key|password|visitor_data|visitordata|client_secret|lsig)=)[^&\\s\"']+");
    private static final Pattern BEARER = Pattern.compile("(?i)bearer\\s+[A-Za-z0-9._\\-]+");
    private static final Pattern LONG_TOKENS = Pattern.compile("[A-Za-z0-9_\\-]{60,}");

    // ---- checks

    private static void checkNetwork(List<String> lines) {
        long start = System.currentTimeMillis();
        HttpURLConnection connection = null;

        try {
            connection = open("https://www.google.com/generate_204", "GET");
            int code = connection.getResponseCode();
            long took = System.currentTimeMillis() - start;

            if (code == 204 || code == 200) {
                lines.add(line("OK", "Internet funcionando (" + took + " ms)"));
            } else {
                lines.add(line("ALERTA", "Internet respondeu com código " + code));
            }

            long serverTime = connection.getDate();
            if (serverTime > 0) {
                long skewMin = Math.abs(System.currentTimeMillis() - serverTime) / 60_000;
                lines.add(skewMin > 2
                        ? line("ALERTA", "Relógio do aparelho difere " + skewMin + " min da hora real (pode causar erro nos vídeos)")
                        : line("OK", "Relógio do aparelho correto"));
            }
        } catch (Throwable e) {
            lines.add(line("ERRO", "Sem internet ou DNS com problema: " + e.getClass().getSimpleName()));
            return;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        start = System.currentTimeMillis();

        try {
            connection = open("https://www.youtube.com/", "HEAD");
            int code = connection.getResponseCode();
            long took = System.currentTimeMillis() - start;
            lines.add(code < 500
                    ? line("OK", "YouTube acessível (" + took + " ms)")
                    : line("ALERTA", "YouTube respondeu com código " + code));
        } catch (Throwable e) {
            lines.add(line("ERRO", "YouTube inacessível: " + e.getClass().getSimpleName()));
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static HttpURLConnection open(String url, String method) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod(method);
        connection.setConnectTimeout(6000);
        connection.setReadTimeout(6000);
        connection.setInstanceFollowRedirects(false);
        return connection;
    }

    private static void checkStorage(Context context, List<String> lines) {
        try {
            File dir = getDiagnosticsDir(context);
            StatFs stat = new StatFs(dir.getAbsolutePath());
            long freeMb = (long) stat.getAvailableBlocks() * stat.getBlockSize() / (1024 * 1024);

            if (freeMb < 100) {
                lines.add(line("ERRO", "Pouco espaço livre: " + freeMb + " MB"));
            } else if (freeMb < 300) {
                lines.add(line("ALERTA", "Espaço livre baixo: " + freeMb + " MB"));
            } else {
                lines.add(line("OK", "Espaço livre: " + freeMb + " MB"));
            }

            // Backups and reports are written here
            File probe = new File(dir, ".probe");
            writeFile(probe, "ok");
            //noinspection ResultOfMethodCallIgnored
            probe.delete();
            lines.add(line("OK", "Pasta de relatórios/backup gravável: " + dir.getAbsolutePath()));
        } catch (Throwable e) {
            lines.add(line("ERRO", "Não consigo gravar na pasta de relatórios/backup: " + e.getClass().getSimpleName()));
        }
    }

    private static void checkMemory(Context context, List<String> lines) {
        try {
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            ActivityManager.MemoryInfo info = new ActivityManager.MemoryInfo();
            am.getMemoryInfo(info);
            long availMb = info.availMem / (1024 * 1024);
            long totalMb = Build.VERSION.SDK_INT >= 16 ? info.totalMem / (1024 * 1024) : 0;
            String text = "Memória livre: " + availMb + " MB" + (totalMb > 0 ? " de " + totalMb + " MB" : "")
                    + " (limite do app: " + am.getMemoryClass() + " MB)";
            lines.add(info.lowMemory ? line("ALERTA", text + ", o sistema está com pouca memória") : line("OK", text));
        } catch (Throwable e) {
            lines.add(line("INFO", "Memória: não foi possível ler"));
        }
    }

    private static void checkDecoders(List<String> lines) {
        String[][] codecs = {{"H.264", "video/avc"}, {"VP9", "video/x-vnd.on2.vp9"}, {"AV1", "video/av01"}, {"HEVC", "video/hevc"}};

        for (String[] codec : codecs) {
            String hardware = null;
            String software = null;

            try {
                for (int i = 0; i < MediaCodecList.getCodecCount(); i++) {
                    MediaCodecInfo info = MediaCodecList.getCodecInfoAt(i);

                    if (info.isEncoder()) {
                        continue;
                    }

                    for (String type : info.getSupportedTypes()) {
                        if (type.equalsIgnoreCase(codec[1])) {
                            String name = info.getName();
                            boolean isSoftware = name.startsWith("OMX.google.") || name.startsWith("c2.android.") || name.contains(".sw.");

                            if (isSoftware && software == null) {
                                software = name;
                            } else if (!isSoftware && hardware == null) {
                                hardware = name;
                            }
                        }
                    }
                }
            } catch (Throwable e) {
                // NOP
            }

            if (hardware != null) {
                lines.add(line("OK", codec[0] + ": hardware (" + hardware + ")"));
            } else if (software != null) {
                lines.add(line("ALERTA", codec[0] + ": só por software (" + software + "), pode travar em alta resolução"));
            } else {
                lines.add(line("INFO", codec[0] + ": não suportado neste aparelho"));
            }
        }
    }

    private static void checkAccount(List<String> lines) {
        try {
            boolean signed = YouTubeServiceManager.instance().getSignInService().isSigned();
            lines.add(line("INFO", signed ? "Conta: logado" : "Conta: sem login"));
        } catch (Throwable e) {
            lines.add(line("INFO", "Conta: não foi possível verificar"));
        }
    }

    private static void checkRecentErrors(Context context, List<String> lines) {
        File crash = new File(getDiagnosticsDir(context), CRASH_FILE);

        if (crash.exists()) {
            lines.add(line("ALERTA", "O app fechou por erro nas últimas 24h (detalhes no relatório)"));
        } else {
            lines.add(line("OK", "Nenhum fechamento por erro nas últimas 24h"));
        }

        int errors = 0;
        String last = null;

        for (String entry : Log.getRecent()) {
            // format: "MM-dd HH:mm:ss E/Tag: message"
            if (entry.length() > 15 && entry.charAt(15) == 'E' && entry.charAt(16) == '/') {
                errors++;
                last = entry;
            }
        }

        if (errors > 0) {
            lines.add(line("ALERTA", errors + " erro(s) no registro do app. Último: " + redact(last)));
        } else {
            lines.add(line("OK", "Nenhum erro no registro do app"));
        }
    }

    // ---- helpers

    private static String line(String level, String text) {
        return "[" + level + "] " + text;
    }

    private static String now() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date());
    }

    private static String getNetworkType(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = cm.getActiveNetworkInfo();
            return info != null && info.isConnected() ? info.getTypeName() : "desconectado";
        } catch (Throwable e) {
            return "desconhecida";
        }
    }

    private static String readLogcat() {
        StringBuilder sb = new StringBuilder();
        Process process = null;

        try {
            process = Runtime.getRuntime().exec(new String[]{"logcat", "-d", "-t", String.valueOf(LOGCAT_LINES), "-v", "time"});
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), UTF8));
            String text;

            while ((text = reader.readLine()) != null) {
                sb.append(text).append('\n');
            }
        } catch (Throwable e) {
            sb.append("(não foi possível ler: ").append(e.getClass().getSimpleName()).append(")\n");
        } finally {
            if (process != null) {
                process.destroy();
            }
        }

        return sb.toString();
    }

    private static void writeFile(File file, String text) throws Exception {
        FileOutputStream out = new FileOutputStream(file, false);

        try {
            OutputStreamWriter writer = new OutputStreamWriter(out, UTF8);
            writer.write(text);
            writer.flush();
        } finally {
            out.close();
        }
    }

    private static String readFile(File file) {
        StringBuilder sb = new StringBuilder();

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new java.io.FileInputStream(file), UTF8));
            String text;

            while ((text = reader.readLine()) != null) {
                sb.append(text).append('\n');
            }

            reader.close();
        } catch (Throwable e) {
            sb.append("(não foi possível ler)\n");
        }

        return sb.toString();
    }
}
