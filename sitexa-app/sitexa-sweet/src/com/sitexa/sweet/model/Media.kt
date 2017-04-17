package com.sitexa.sweet.model

import java.io.Serializable

/**
 * Created by open on 17/04/2017.
 *
 */

data class Media(val id: Int, val refId: Int, val fileName: String, val fileType: String, val title: String, val sortOrder: Int): Serializable
