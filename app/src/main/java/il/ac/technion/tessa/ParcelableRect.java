package il.ac.technion.tessa;

import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by arietal on 1/11/16.
 */
class ParcelableRect implements Parcelable {
    int mLeft, mTop, mRight, mBottom;

    protected ParcelableRect(Parcel in) {
        mLeft = in.readInt();
        mTop = in.readInt();
        mRight = in.readInt();
        mBottom = in.readInt();
    }

    public ParcelableRect(Rect r) {
        mLeft = r.left;
        mTop = r.top;
        mRight = r.right;
        mBottom = r.bottom;
    }

    public Rect toRect() {
        return new Rect(mLeft, mTop, mRight, mBottom);
    }

    public static final Creator<ParcelableRect> CREATOR = new Creator<ParcelableRect>() {
        @Override
        public ParcelableRect createFromParcel(Parcel in) {
            return new ParcelableRect(in);
        }

        @Override
        public ParcelableRect[] newArray(int size) {
            return new ParcelableRect[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mLeft);
        dest.writeInt(mTop);
        dest.writeInt(mRight);
        dest.writeInt(mBottom);
    }
}
