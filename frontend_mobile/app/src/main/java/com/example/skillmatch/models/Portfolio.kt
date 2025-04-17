package com.example.skillmatch.models

import android.os.Parcel
import android.os.Parcelable

data class Portfolio(
    val id: String?,
    val workExperience: String?,
    val clientTestimonials: String?,
    val servicesOffered: List<Service>?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.createTypedArrayList(Service.CREATOR)
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(workExperience)
        parcel.writeString(clientTestimonials)
        parcel.writeTypedList(servicesOffered)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Portfolio> {
        override fun createFromParcel(parcel: Parcel): Portfolio {
            return Portfolio(parcel)
        }

        override fun newArray(size: Int): Array<Portfolio?> {
            return arrayOfNulls(size)
        }
    }
}