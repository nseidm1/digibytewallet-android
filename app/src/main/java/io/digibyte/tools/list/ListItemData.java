package io.digibyte.tools.list;

import android.os.Parcel;
import android.os.Parcelable;

public class ListItemData implements Parcelable {
    public final int resourceId;

    public ListItemData(int resourceId) {
        this.resourceId = resourceId;
    }

    protected ListItemData(Parcel in) {
        resourceId = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(resourceId);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<ListItemData> CREATOR = new Parcelable.Creator<ListItemData>() {
        @Override
        public ListItemData createFromParcel(Parcel in) {
            return new ListItemData(in);
        }

        @Override
        public ListItemData[] newArray(int size) {
            return new ListItemData[size];
        }
    };
}