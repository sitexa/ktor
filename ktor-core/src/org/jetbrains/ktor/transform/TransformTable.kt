package org.jetbrains.ktor.transform

import java.util.*
import java.util.concurrent.atomic.*
import java.util.concurrent.locks.*
import kotlin.concurrent.*

class TransformTable<C : Any>(val parent: TransformTable<C>? = null, val key: KeyType = KeyType.FROM) {
    private val handlersCounter: AtomicInteger = parent?.handlersCounter ?: AtomicInteger()

    private val superTypesCacheLock: ReentrantReadWriteLock = parent?.superTypesCacheLock ?: ReentrantReadWriteLock()
    private val superTypesCache: MutableMap<Class<*>, Array<Class<*>>> = parent?.superTypesCache ?: HashMap()

    private val handlersLock = ReentrantReadWriteLock()
    private val handlers = HashMap<Class<*>, MutableList<Handler<C, *, *>>>()

    private val handlersCacheLock = ReentrantReadWriteLock()
    private val handlersCache = HashMap<Class<*>, List<Handler<C, *, *>>>()

    inline fun <reified T : Any> register(noinline handler: C.(T) -> Any) {
    	registerBound({ true }, handler)
    }
    
    inline fun <reified T : Any, reified R : Any> registerBound(noinline handler: C.(T) -> R) {
        register({ true }, handler)
    }

    inline fun <reified T : Any> register(noinline predicate: C.(T) -> Boolean, noinline handler: C.(T) -> Any) {
    	registerBound(predicate, handler)
    }
    
    inline fun <reified T : Any, reified R : Any> registerBound(noinline predicate: C.(T) -> Boolean, noinline handler: C.(T) -> R) {
    	val type = when (key) {
    		KeyType.FROM -> T::class.javaObjectType
    		KeyType.TO -> R::class.javaObjectType
    	}
        register(type, predicate, handler)
    }

    fun <T : Any, R : Any> register(type: Class<*>, predicate: C.(T) -> Boolean, handler: C.(T) -> R) {
        superTypes(type)
        addHandler(type, Handler(handlersCounter.getAndIncrement(), predicate, handler))

        handlersCacheLock.write {
            handlersCache.keys.filter { type.isAssignableFrom(it) }.forEach {
                handlersCache.remove(it)
            }
        }
    }

    fun <T : Any> handlers(type: Class<T>): List<Handler<C, T, *>> {
        require(key == KeyType.FROM)

        @Suppress("UNCHECKED_CAST")
        return handlers0(type) as List<Handler<C, T, *>>
    }

    fun <R : Any> handlersFor(type: Class<R>): List<Handler<C, *, R>> {
        require(key == KeyType.TO)

        @Suppress("UNCHECKED_CAST")
        return handlers0(type) as List<Handler<C, *, R>>
    }

    private fun handlers0(type: Class<*>): List<Handler<C, *, *>> {
        val cached = handlersCacheLock.read { handlersCache[type] }
        val partialResult = if (cached == null) {
            val collected = collectHandlers(type)

            handlersCacheLock.write {
                handlersCache[type] = collected
            }

            collected
        } else {
            cached
        }

        return if (parent != null) {
            val parentResult = parent.handlers0(type)
            when {
                parentResult.isEmpty() -> partialResult
                partialResult.isEmpty() -> parentResult
                else -> partialResult + parentResult
            }
        } else
            partialResult
    }

    class Handler<in C : Any, in T, out R : Any> internal constructor(val id: Int, val predicate: C.(T) -> Boolean, val handler: C.(T) -> R) {
        override fun toString() = handler.toString()
    }

    fun newHandlersSet() = HandlersSet<C>()

    class HandlersSet<out C : Any> {
        private val bitSet = BitSet()

        fun add(element: Handler<C, *, *>): Boolean {
            if (bitSet[element.id]) {
                return false
            }

            bitSet[element.id] = true
            return true
        }

        fun remove(element: Handler<C, *, *>): Boolean {
            if (bitSet[element.id]) {
                bitSet[element.id] = false
                return true
            }

            return false
        }

        operator fun contains(element: Handler<C, *, *>) = bitSet[element.id]
    }

    private fun collectHandlers(type: Class<*>): List<Handler<C, *, *>> {
        val result = ArrayList<Handler<C, *, *>>(2)
        val superTypes = superTypes(type)

        handlersLock.read {
            for (superType in superTypes) {
                val hh = handlers[superType]
                if (hh != null && hh.isNotEmpty()) {
                    result.addAll(hh as List<Handler<C, *, *>>)
                }
            }
        }

        return if (result.isEmpty()) emptyList() else result
    }

    private fun addHandler(type: Class<*>, handler: Handler<C, *, *>) {
        handlersLock.write {
            handlers.getOrPut(type) { ArrayList(2) }.add(handler)
        }
    }

    private fun superTypes(type: Class<*>): Array<Class<*>> = superTypesCacheLock.read {
        superTypesCache[type] ?: superTypesCacheLock.write {
            buildSuperTypes(type).apply { superTypesCache[type] = this }
        }
    }

    private fun buildSuperTypes(type: Class<*>) = dfs(type).asReversed().toTypedArray()
    
    enum class KeyType {
    	FROM, TO
    }
}
