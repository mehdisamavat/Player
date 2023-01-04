package com.example.exomine

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Post(
    var id: String = "",
    var title: String = "",
    var artist: String = "",
    var source: String = "",
    var image: String = "",
    var playbackRes: Int=0
): Parcelable