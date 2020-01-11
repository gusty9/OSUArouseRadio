package com.gusty.arousemvvm.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * json attribute response
 */
class Attr {
    @SerializedName("page")
    @Expose
    val page: String? = null
    @SerializedName("perPage")
    @Expose
    val perPage: String? = null
    @SerializedName("user")
    @Expose
    val user: String? = null
    @SerializedName("total")
    @Expose
    val total: String? = null
    @SerializedName("totalPages")
    @Expose
    val totalPages: String? = null

    override fun toString(): String {
        if (total == null) {
            return "null"
        }
        return total
    }
}