package com.sitexa.sweet.dao

import com.sitexa.sweet.model.*
import org.ehcache.*
import org.ehcache.config.*
import org.ehcache.config.persistence.*
import org.ehcache.config.units.*
import org.joda.time.*
import java.io.*


/**
 * Created by open on 03/04/2017.
 *
 */


class DAOFacadeCache(val delegate: DAOFacade, val storagePath: File) : DAOFacade {
    val cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
            .with(CacheManagerPersistenceConfiguration(storagePath))
            .withCache("sweetsCache",
                    CacheConfigurationBuilder.newCacheConfigurationBuilder<Int, Sweet>()
                            .withResourcePools(ResourcePoolsBuilder.newResourcePoolsBuilder()
                                    .heap(1000, EntryUnit.ENTRIES)
                                    .offheap(10, MemoryUnit.MB)
                                    .disk(100, MemoryUnit.MB, true)
                            )
                            .buildConfig(Int::class.javaObjectType, Sweet::class.java))
            .withCache("usersCache",
                    CacheConfigurationBuilder.newCacheConfigurationBuilder<String, User>()
                            .withResourcePools(ResourcePoolsBuilder.newResourcePoolsBuilder()
                                    .heap(1000, EntryUnit.ENTRIES)
                                    .offheap(10, MemoryUnit.MB)
                                    .disk(100, MemoryUnit.MB, true)
                            )
                            .buildConfig(String::class.java, User::class.java))
            .build(true)

    val sweetsCache = cacheManager.getCache("sweetsCache", Int::class.javaObjectType, Sweet::class.java)

    val usersCache = cacheManager.getCache("usersCache", String::class.java, User::class.java)

    override fun init() {
        delegate.init()
    }

    override fun countReplies(id: Int): Int {
        return delegate.countReplies(id)
    }

    override fun createSweet(user: String, text: String, replyTo: Int?, date: DateTime): Int {
        val id = delegate.createSweet(user, text, replyTo)
        val sweet = Sweet(id, user, text, date, replyTo)
        sweetsCache.put(id, sweet)
        return id
    }

    override fun deleteSweet(id: Int) {
        delegate.deleteSweet(id)
        sweetsCache.remove(id)
    }

    override fun getSweet(id: Int): Sweet {
        val cached = sweetsCache.get(id)
        if (cached != null) {
            return cached
        }

        val sweet = delegate.getSweet(id)
        sweetsCache.put(id, sweet)

        return sweet
    }

    override fun userSweets(userId: String): List<Int> {
        return delegate.userSweets(userId)
    }

    override fun user(userId: String, hash: String?): User? {
        val cached = usersCache.get(userId)
        val user = if (cached == null) {
            val dbUser = delegate.user(userId)
            if (dbUser != null) {
                usersCache.put(userId, dbUser)
            }
            dbUser
        } else {
            cached
        }

        return when {
            user == null -> null
            hash == null -> user
            user.passwordHash == hash -> user
            else -> null
        }
    }

    override fun userByMobile(mobile: String): User? {
        return delegate.userByMobile(mobile)
    }

    override fun userByEmail(email: String): User? {
        return delegate.userByEmail(email)
    }

    override fun createUser(user: User) {
        if (usersCache.get(user.userId) != null) {
            throw IllegalStateException("User already exist")
        }

        delegate.createUser(user)
        usersCache.put(user.userId, user)
    }

    override fun top(count: Int): List<Int> {
        return delegate.top(count)
    }

    override fun latest(count: Int): List<Int> {
        return delegate.latest(count)
    }

    override fun close() {
        try {
            delegate.close()
        } finally {
            cacheManager.close()
        }
    }
}
