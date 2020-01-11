package com.gusty.arousemvvm.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Image {
    @SerializedName("size")
    @Expose
    val size: String? = null
    @SerializedName("#text")
    @Expose
    val text: String? = null
}