package io.digibyte.tools.list.items;

import android.view.View;
import android.widget.ProgressBar;

import io.digibyte.DigiByte;
import io.digibyte.R;
import io.digibyte.presenter.customviews.BRText;
import io.digibyte.tools.list.ListItemData;
import io.digibyte.tools.list.ListItemViewHolder;
import io.digibyte.tools.manager.SyncManager;
import io.digibyte.tools.util.Utils;

public class ListItemSyncingViewHolder extends ListItemViewHolder {
    private final BRText date;
    private final ProgressBar progress;
    private View dots;

    public ListItemSyncingViewHolder(View anItemView) {
        super(anItemView);
        date = anItemView.findViewById(R.id.sync_date);
        progress = anItemView.findViewById(R.id.sync_progress);
        dots = anItemView.findViewById(R.id.dots);
    }

    @Override
    public void process(ListItemData aListItemData) {
        super.process(aListItemData);
        itemView.setBackgroundResource(R.drawable.tx_rounded);
        progress.setProgress((int) (SyncManager.getInstance().getProgress() * 100));
        dots.setVisibility(SyncManager.getInstance().getLastBlockTimestamp() == 0 ? View.VISIBLE : View.INVISIBLE);
        date.setText(SyncManager.getInstance().getLastBlockTimestamp() == 0
                ? DigiByte.getContext().getString(R.string.NodeSelector_statusLabel) + ": "
                + DigiByte.getContext().getString(R.string.SyncingView_connecting)
                : Utils.formatTimeStamp(SyncManager.getInstance().getLastBlockTimestamp() * 1000,
                        "MMM. dd, yyyy 'at' hh:mm a"));
    }
}