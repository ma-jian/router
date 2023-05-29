package com.mm.router

import android.os.Parcel
import android.os.Parcelable


/**
 * Date : 2023/5/27
 */
class ParaBean() : Parcelable {

    constructor(parcel: Parcel) : this() {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {

    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ParaBean> {
        override fun createFromParcel(parcel: Parcel): ParaBean {
            return ParaBean(parcel)
        }

        override fun newArray(size: Int): Array<ParaBean?> {
            return arrayOfNulls(size)
        }
    }

}