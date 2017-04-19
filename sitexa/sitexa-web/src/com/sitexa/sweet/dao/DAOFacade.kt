package com.sitexa.sweet.dao

import com.sitexa.sweet.model.Media
import com.sitexa.sweet.model.Sweet
import com.sitexa.sweet.model.User
import org.joda.time.DateTime
import java.io.Closeable

/**
 * Created by open on 19/04/2017.
 *
 */


interface DAOFacade : Closeable {
    fun init()
    fun countReplies(id: Int): Int
    fun createSweet(user: String, text: String, replyTo: Int? = null, date: DateTime = DateTime.now()): Int
    fun deleteSweet(id: Int)
    fun updateSweet(user: String, id: Int, text: String, replyTo: Int? = null, date: DateTime = DateTime.now())
    fun getSweet(id: Int): Sweet
    fun getReplies(id: Int): List<Int>
    fun userSweets(userId: String): List<Int>

    fun createMedia(refId: Int? = -1, fileName: String, fileType: String? = "unknown", title: String? = null, sortOrder: Int? = 0): Int
    fun deleteMedia(id: Int)
    fun getMedia(id: Int): Media?
    fun getMedias(refId: Int): List<Int>

    fun user(userId: String, hash: String? = null): User?
    fun userByEmail(email: String): User?
    fun userByMobile(mobile: String): User?
    fun createUser(user: User)
    fun top(count: Int = 10): List<Int>
    fun latest(count: Int = 10): List<Int>
}