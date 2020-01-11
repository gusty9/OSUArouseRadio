package com.gusty.arousemvvm.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * inner layer of retrofit java object
 */
class RecentTracksInfo {
    @SerializedName("@attr")
    @Expose
    val attr: Attr? = null
    @SerializedName("track")
    @Expose
    val tracks: List<Track>? = null

    override fun toString(): String {
        return attr.toString()
    }
}