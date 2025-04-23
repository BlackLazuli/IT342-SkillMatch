package com.example.skillmatch.models

import android.os.Parcel
import android.os.Parcelable

data class Service(
    val id: Long? = null,
    val name: String,
    val description: String? = null,
    val pricing: String? = null,
    val time: String? = null,
    val daysOfTheWeek: List<String> = emptyList()
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readValue(Long::class.java.classLoader) as? Long,
        parcel.readString() ?: "",
        parcel.readString(),
        parcel.readString(),  // Changed to match pricing field
        parcel.readString(),
        parcel.createStringArrayList() ?: emptyList()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(id)
        parcel.writeString(name)
        parcel.writeString(description)
        parcel.writeString(pricing)  // Changed to match pricing field
        parcel.writeString(time)
        parcel.writeStringList(daysOfTheWeek)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Service> {
        override fun createFromParcel(parcel: Parcel): Service {
            return Service(parcel)
        }

        override fun newArray(size: Int): Array<Service?> {
            return arrayOfNulls(size)
        }
    }
}