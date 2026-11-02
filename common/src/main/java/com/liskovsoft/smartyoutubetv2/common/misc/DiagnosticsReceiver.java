package com.liskovsoft.smartyoutubetv2.common.misc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.io.File;

/**
 * Lets a computer generate the diagnostics report over adb:<br/>
 * adb shell am broadcast -a org.smarthub.app.DIAGNOSTICS -n org.smarthub.app/com.liskovsoft.smartyoutubetv2.common.misc.DiagnosticsReceiver<br/>
 * The manifest requires the DUMP permission, which only adb/system hold: other apps cannot trigger it.
 */
public class DiagnosticsReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        final PendingResult pending = goAsync();
        final Context app = context.getApplicationContext();

        DiagnosticsHelper.runChecks(app, checks -> new Thread(() -> {
            try {
                String report = DiagnosticsHelper.buildReport(app, checks);
                File file = DiagnosticsHelper.saveReport(app, report);
                pending.setResultData(file != null ? file.getAbsolutePath() : "ERROR: could not save the report");
            } catch (Throwable e) {
                pending.setResultData("ERROR: " + e);
            } finally {
                pending.finish();
            }
        }, "DiagnosticsReceiver").start());
    }
}
