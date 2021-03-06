package com.monke.monkeybook.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by GKF on 2018/2/7.
 */

public class ReplaceRuleBean implements Parcelable {
    private String regex;
    private String replacement;


    private ReplaceRuleBean(Parcel in) {
        regex = in.readString();
        replacement = in.readString();
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(regex);
        parcel.writeString(replacement);
    }

    public static final Creator<ReplaceRuleBean> CREATOR = new Creator<ReplaceRuleBean>() {
        @Override
        public ReplaceRuleBean createFromParcel(Parcel in) {
            return new ReplaceRuleBean(in);
        }

        @Override
        public ReplaceRuleBean[] newArray(int size) {
            return new ReplaceRuleBean[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

}
