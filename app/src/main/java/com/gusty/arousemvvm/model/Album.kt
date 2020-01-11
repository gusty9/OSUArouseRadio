package com.gusty.arousemvvm.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Album Json Response
 */
class Album {
    @SerializedName("mbid")
    @Expose
    val mbid: String? = null

    @SerializedName("#text")
    @Expose
    val text: String? = null

}