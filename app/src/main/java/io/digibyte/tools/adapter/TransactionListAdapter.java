package io.digibyte.tools.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.lang.reflect.Constructor;
import java.util.ArrayList;

import io.digibyte.tools.list.ListItemData;
import io.digibyte.tools.list.ListItemViewHolder;


/**
 * BreadWallet
 * <p>
 * Created by Mihail Gutan <mihail@breadwallet.com> on 7/27/15.
 * Copyright (c) 2016 breadwallet LLC
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

public class TransactionListAdapter extends RecyclerView.Adapter<ListItemViewHolder>
{
    public static final String TAG = TransactionListAdapter.class.getName();

    private SparseArray<ArrayList<ListItemData>> theItemDataList;

    public TransactionListAdapter()
    {
        theItemDataList = new SparseArray<>();
    }

    public void addSection(int aSectionId)
    {
        theItemDataList.put(aSectionId, new ArrayList<ListItemData>());
    }

    public void removeSection(int aSectionId)
    {
        theItemDataList.remove(aSectionId);
        this.notifyDataSetChanged(); // TODO: Notify correct range position
    }

    public ArrayList<ListItemData> getItemsInSection(int aSectionId)
    {
        return theItemDataList.get(aSectionId, new ArrayList<ListItemData>());
    }

    public void updateSection(int aSectionId)
    {
        this.notifyDataSetChanged(); // TODO: Notify correct range position
    }

    public void addItemsInSection(int aSectionId, ArrayList<ListItemData> anItemDataList)
    {
        theItemDataList.put(aSectionId, anItemDataList);
        this.notifyDataSetChanged(); // TODO: Notify correct range position
    }

    public void removeItemInSection(int aSectionId, ListItemData aListItem)
    {
        if(null != theItemDataList.get(aSectionId))
        {
            theItemDataList.get(aSectionId).remove(aListItem);
            this.notifyDataSetChanged(); // TODO: Notify correct item position
        }
    }

    @Override
    public ListItemViewHolder onCreateViewHolder(ViewGroup aParent, int aResourceId)
    {
        ListItemViewHolder holder;
        LayoutInflater layoutInflater = LayoutInflater.from(aParent.getContext());
        View view = layoutInflater.inflate(aResourceId, aParent, false);

        try
        {
            Class<?> viewHolder = ListItemData.getViewHolder(aResourceId);
            Constructor<?> constructors = viewHolder.getConstructor(View.class);
            holder = (ListItemViewHolder) constructors.newInstance(view);
        }
        catch (Exception ignore)
        {
            holder = new ListItemViewHolder(view);
        }

        return holder;
    }

    @Override
    public void onBindViewHolder(ListItemViewHolder holder, int aPosition)
    {
        holder.process(this.getListItemDataForPosition(aPosition));
    }

    @Override
    public int getItemViewType(int aPosition)
    {
        return this.getListItemDataForPosition(aPosition).resourceId;
    }

    @Override
    public int getItemCount()
    {
        int itemCount = 0;
        for(int sectionIndex = 0; sectionIndex < theItemDataList.size(); sectionIndex++)
        {
            itemCount += theItemDataList.valueAt(sectionIndex).size();
        }
        return itemCount;
    }

    private ListItemData getListItemDataForPosition(int aPosition)
    {
        int currentPosition = -1;
        for(int sectionIndex = 0; sectionIndex < theItemDataList.size(); sectionIndex++)
        {
            for(int index = 0; index < theItemDataList.get(sectionIndex).size(); index++)
            {
                if(aPosition == ++currentPosition)
                {
                    return theItemDataList.get(sectionIndex).get(index);
                }
            }
        }
        return null;
    }
}
