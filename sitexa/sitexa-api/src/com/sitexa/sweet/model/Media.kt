package com.sitexa.sweet.model

import java.io.Serializable

/**
 * Created by open on 17/04/2017.
 *
 */

data class Media(val id: Int,
                 val refId: Int? = -1,
                 val fileName: String,
                 val fileType: String? = "unknown",
                 val title: String? = null,
                 val sortOrder: Int? = 0) : Serializable
