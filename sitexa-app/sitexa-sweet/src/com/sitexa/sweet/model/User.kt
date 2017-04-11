package com.sitexa.sweet.model

import java.io.Serializable

/**
 * Created by open on 03/04/2017.
 *
 */

data class User(val userId: String, val mobile: String, val email: String, val displayName: String, val passwordHash: String) : Serializable