package org.jetbrains.ktor.nio


import java.nio.ByteBuffer
import java.util.*

class DefaultByteBufferPool(val size: Int, val partSize: Int = 8192, concurrency: Int = Runtime.getRuntime().availableProcessors()) : ByteBufferPool {
    private val blocksPerBucket = (size / concurrency) / partSize
    private val buckets = Array(concurrency) { Bucket(size / concurrency, blocksPerBucket) }

    private class TicketImpl(buffer: ByteBuffer, val bucket: Bucket, val index: Int) : ReleasablePoolTicket(buffer) {
        override fun release() {
            super.release()
            bucket.free(index)
        }
    }

    private class NoPoolTicketImpl(override val buffer: ByteBuffer) : PoolTicket

    override fun allocate(size: Int): PoolTicket {
        if (size > partSize) {
            return NoPoolTicketImpl(ByteBuffer.allocate(size))
        }

        val bucketIndex = Thread.currentThread().id % buckets.size

        buckets[bucketIndex.toInt()].allocate(size)?.let { return it }

        for (i in buckets.indices) {
            val ticket = buckets[i].allocate(size)
            if (ticket != null) {
                return ticket
            }
        }

        return NoPoolTicketImpl(ByteBuffer.allocate(size))
    }

    override fun release(buffer: PoolTicket) {
        when (buffer) {
            is NoPoolTicketImpl -> {}
            is TicketImpl -> buffer.release()
            else -> throw IllegalArgumentException("Unsupported pool ticket")
        }
    }

    private class Bucket(size: Int, val count: Int) {
        private val partSize = size / count
        private val buffer = ByteBuffer.allocate(size)!!

        private val parts = Array(count) { idx ->
            buffer.limit(partSize * (idx + 1))
            buffer.position(partSize * idx)

            buffer.slice()!!
        }

        private val bits = BitSet(count)
        private var next: Int = 0

        fun allocate(size: Int): TicketImpl? {
            val index = allocateLocked(allocateBit())
            if (index == -1) return null

            val buffer = parts[index].apply { clear(); limit(size) }

            return TicketImpl(buffer, this, index)
        }

        @Synchronized
        private fun allocateLocked(guess: Int): Int {
            val index = if (guess == -1 || bits.get(guess)) {
                allocateBit()
            } else {
                guess
            }

            if (index >= 0) {
                bits.set(index)
            }
            return index
        }

        @Synchronized
        fun free(index: Int) {
            bits.clear(index)
            next = Math.min(next, index)
        }

        private fun allocateBit(): Int {
            val b1 = bits.nextClearBit(next)
            if (b1 < count) {
                return b1
            }

            if (next > 0) {
                val b2 = bits.nextClearBit(0)
                if (b2 < count) {
                    return b2
                }
            }

            return -1
        }
    }
}
