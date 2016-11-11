package org.jetbrains.ktor.tests.nio

import org.jetbrains.ktor.nio.DefaultByteBufferPool
import org.jetbrains.ktor.nio.PoolTicket
import org.junit.Test
import java.util.*
import kotlin.test.assertEquals

class ByteBufferPoolTest {
    private val pool = DefaultByteBufferPool(8192 * 10, concurrency = 1)

    @Test
    fun testAllocate() {
        val ts = ArrayList<PoolTicket>()

        for (i in 1..10) {
            val t = pool.allocate(100)
            ts.add(t)

            assertEquals(8192, t.buffer.capacity())
        }

        val tOut = pool.allocate(100)
        assertEquals(100, tOut.buffer.capacity())

        pool.release(tOut)

        pool.release(ts[1])
        ts[1] = pool.allocate(100)
        assertEquals(8192, ts[1].buffer.capacity())
    }

    @Test
    fun testRandom() {
        val ts = ArrayList<PoolTicket>()

        for (i in 1..10) {
            val t = pool.allocate(100)
            ts.add(t)

            assertEquals(8192, t.buffer.capacity())
        }

        val rnd = Random()

        for (i in 1..10000) {
            repeat(rnd.nextInt(ts.size / 2)) {
                if (ts.isNotEmpty()) {
                    val t = ts.removeAt(rnd.nextInt(ts.size))
                    pool.release(t)
                }
            }

            kotlin.repeat(10 - ts.size) {
                if (ts.size < 10) {
                    val t = pool.allocate(100)
                    ts += t
                    assertEquals(8192, t.buffer.capacity())
                }
            }
        }
    }
}