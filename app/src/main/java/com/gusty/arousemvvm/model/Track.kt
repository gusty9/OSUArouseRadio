package com.gusty.arousemvvm.model

import android.graphics.Bitmap
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Track object based on api response
 */
class Track {
    @SerializedName("artist")
    @Expose
    val artist: Artist? = null
    @SerializedName("mbid")
    @Expose
    val mbid: String? = null
    @SerializedName("album")
    @Expose
    val album: Album? = null
    @SerializedName("streamable")
    @Expose
    val streamable: String? = null
    @SerializedName("url")
    @Expose
    val url: String? = null
    @SerializedName("name")
    @Expose
    val name: String? = null
    @SerializedName("image")
    @Expose
    val image: List<Image>? = null
    //album art image, not stored from json response and retrofit
    var albumArt: Bitmap? = null
}