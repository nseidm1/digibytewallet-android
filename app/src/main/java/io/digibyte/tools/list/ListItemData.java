package io.digibyte.tools.list;

public class ListItemData
{
    public interface OnListItemClickListener
    {
        void onListItemClick(ListItemData aListItemData);
    }

    public final int resourceId;
    public final OnListItemClickListener onClickListener;
    public final OnListItemClickListener onLongClickListener;

    public ListItemData(int aResourceId)
    {
        this(aResourceId, null);
    }

    public ListItemData(int aResourceId, OnListItemClickListener aClickListener)
    {
        this(aResourceId, aClickListener, null);
    }

    public ListItemData(int aResourceId, OnListItemClickListener aClickListener, OnListItemClickListener aLongClickListener)
    {
        this.resourceId = aResourceId;
        this.onClickListener = aClickListener;
        this.onLongClickListener = aLongClickListener;
    }

    public void onClick()
    {
        if(null != this.onClickListener)
        {
            this.onClickListener.onListItemClick(this);
        }
    }

    public boolean onLongClick()
    {
        if(null != this.onLongClickListener)
        {
            this.onLongClickListener.onListItemClick(this);

            return true;
        }
        return false;
    }
}
