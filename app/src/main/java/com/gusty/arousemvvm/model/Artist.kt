package com.gusty.arousemvvm.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Artist json response
 */
class Artist {
    @SerializedName("mbid")
    @Expose
    val mbid: String? = null

    @SerializedName("#text")
    @Expose
    val text: String? = null

}