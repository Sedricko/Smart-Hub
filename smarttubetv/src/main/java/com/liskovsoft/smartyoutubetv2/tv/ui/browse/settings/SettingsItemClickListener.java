package com.liskovsoft.smartyoutubetv2.tv.ui.browse.settings;

import android.content.Context;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.leanback.widget.OnItemViewClickedListener;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.Row;
import androidx.leanback.widget.RowPresenter;
import com.liskovsoft.smartyoutubetv2.common.app.models.data.SettingsItem;
import com.liskovsoft.smartyoutubetv2.common.prefs.GeneralData;
import com.liskovsoft.smartyoutubetv2.common.utils.SimpleEditDialog;
import com.liskovsoft.smartyoutubetv2.common.utils.Utils;
import com.liskovsoft.smartyoutubetv2.tv.R;

/**
 * Opens a settings screen. Asks for the settings password first when the user set one.
 * Shared by the grid (phone) and the grouped rows (TV).
 */
class SettingsItemClickListener implements OnItemViewClickedListener {
    private final Fragment mFragment;

    SettingsItemClickListener(Fragment fragment) {
        mFragment = fragment;
    }

    @Override
    public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Row row) {
        Context context = mFragment.getContext();

        if (context == null) { // fragment already detached
            return;
        }

        if (!(item instanceof SettingsItem)) {
            Toast.makeText(context, String.valueOf(item), Toast.LENGTH_SHORT).show();
            return;
        }

        SettingsItem settingsItem = (SettingsItem) item;
        String password = GeneralData.instance(context).getSettingsPassword();

        if (password == null) {
            settingsItem.onClick.run();
            return;
        }

        SimpleEditDialog.showPassword(
                context,
                context.getString(R.string.enter_settings_password),
                null,
                newValue -> {
                    if (Utils.passwordMatch(password, newValue)) {
                        settingsItem.onClick.run();
                        return true;
                    }
                    return false;
                });
    }
}
