package com.liskovsoft.smartyoutubetv2.tv.ui.browse.settings;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.leanback.app.RowsSupportFragment;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.HeaderItem;
import androidx.leanback.widget.ListRow;
import com.liskovsoft.smartyoutubetv2.common.app.models.data.SettingsGroup;
import com.liskovsoft.smartyoutubetv2.common.app.models.data.SettingsItem;
import com.liskovsoft.smartyoutubetv2.tv.R;
import com.liskovsoft.smartyoutubetv2.tv.presenter.CustomListRowPresenter;
import com.liskovsoft.smartyoutubetv2.tv.presenter.SettingsCardPresenter;
import com.liskovsoft.smartyoutubetv2.tv.ui.browse.interfaces.SettingsSection;

import java.util.ArrayList;
import java.util.List;

/**
 * Settings grouped by what the user is doing (account, playback, app, system), one titled row per group.
 */
public class SettingsRowsFragment extends RowsSupportFragment implements SettingsSection {
    private static final int[] CATEGORY_ORDER = {
            SettingsItem.CATEGORY_ACCOUNT, SettingsItem.CATEGORY_PLAYBACK, SettingsItem.CATEGORY_APP,
            SettingsItem.CATEGORY_SYSTEM, SettingsItem.CATEGORY_OTHER
    };
    private ArrayObjectAdapter mRowsAdapter;
    private final List<SettingsGroup> mPendingUpdates = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mRowsAdapter = new ArrayObjectAdapter(new CustomListRowPresenter());
        setAdapter(mRowsAdapter);
        setOnItemViewClickedListener(new SettingsItemClickListener(this));

        for (SettingsGroup group : mPendingUpdates) {
            update(group);
        }
        mPendingUpdates.clear();

        if (getMainFragmentAdapter().getFragmentHost() != null) {
            getMainFragmentAdapter().getFragmentHost().notifyDataReady(getMainFragmentAdapter());
        }
    }

    @Override
    public void setExpand(boolean expand) {
        // Leanback hides the row titles while the side menu has the focus; the video rows already force this
        super.setExpand(true);
    }

    @Override
    public void update(SettingsGroup group) {
        if (mRowsAdapter == null) {
            mPendingUpdates.add(group);
            return;
        }

        clear(); // continuation is not supported

        if (group == null || group.getItems() == null) {
            return;
        }

        for (int category : CATEGORY_ORDER) {
            ArrayObjectAdapter cards = new ArrayObjectAdapter(new SettingsCardPresenter());

            for (SettingsItem item : group.getItems()) {
                if (item.category == category) {
                    cards.add(item);
                }
            }

            if (cards.size() > 0) {
                mRowsAdapter.add(new ListRow(new HeaderItem(getString(titleOf(category))), cards));
            }
        }
    }

    @Override
    public void clear() {
        if (mRowsAdapter != null) {
            mRowsAdapter.clear();
        }
    }

    @Override
    public boolean isEmpty() {
        return mRowsAdapter == null ? mPendingUpdates.isEmpty() : mRowsAdapter.size() == 0;
    }

    private static int titleOf(int category) {
        switch (category) {
            case SettingsItem.CATEGORY_ACCOUNT:
                return R.string.settings_category_account;
            case SettingsItem.CATEGORY_PLAYBACK:
                return R.string.settings_category_playback;
            case SettingsItem.CATEGORY_APP:
                return R.string.settings_category_app;
            case SettingsItem.CATEGORY_SYSTEM:
                return R.string.settings_category_system;
            default:
                return R.string.settings_category_other;
        }
    }
}
