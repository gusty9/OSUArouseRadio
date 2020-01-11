package com.gusty.arousemvvm.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Entry point for retrofit java object creation
 */
class RecentTracks {
    @SerializedName("recenttracks")
    @Expose
    val recentTracksInfo: RecentTracksInfo? = null
}