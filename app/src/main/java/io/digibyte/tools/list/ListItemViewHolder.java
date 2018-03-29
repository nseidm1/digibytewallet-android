package io.digibyte.tools.list;

import android.support.v7.widget.RecyclerView;
import android.view.View;

public class ListItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener
{
    private View view;
    protected ListItemData theItemData;
    public ListItemData getItemData() { return theItemData; }

    public ListItemViewHolder(View itemView)
    {
        super(itemView);
        view = itemView;
        this.itemView.setOnClickListener(this);
        this.itemView.setOnLongClickListener(this);
    }

    public View getView() {
        return view;
    }

    public void process(ListItemData aListItemData)
    {
        theItemData = aListItemData;
    }

    @Override
    public void onClick(View view)
    {
        if(null != theItemData)
        {
            theItemData.onClick();
        }
    }

    @Override
    public boolean onLongClick(View v)
    {
        if(null != theItemData)
        {
            return theItemData.onLongClick();
        }

        return false;
    }
}
