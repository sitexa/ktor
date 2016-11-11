package org.jetbrains.ktor.nio


import java.nio.ByteBuffer
import java.util.concurrent.ArrayBlockingQueue

class DefaultByteBufferPool(val size: Int, val partSize: Int = 8192, concurrency: Int = Runtime.getRuntime().availableProcessors()) : ByteBufferPool {
    private val area = ByteBuffer.allocate(size)
    private val blocksPerBucket = (size / concurrency) / partSize
    private val subs = area.split(concurrency)
    private val buckets = Array(concurrency) { Bucket(subs[it], blocksPerBucket) }

    private class TicketImpl(buffer: ByteBuffer, val bucket: Bucket) : ReleasablePoolTicket(buffer) {
        override fun release() {
            if (!released) {
                bucket.free(buffer)
                super.release()
            }
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

    private class Bucket(buffer: ByteBuffer, val count: Int) {
        private val parts = buffer.split(count).let { ArrayBlockingQueue(count, false, it.asList()) }

        fun allocate(size: Int): TicketImpl? = parts.poll()?.let { TicketImpl(it.apply {
            clear()
            limit(size)
        }, this) }

        fun free(buffer: ByteBuffer) {
            parts.offer(buffer)
        }
    }

}

private fun ByteBuffer.split(parts: Int): Array<ByteBuffer> {
    val partSize = capacity() / parts

    return Array(parts) { idx ->
        limit(Math.min((idx + 1) * partSize, capacity()))
        position(idx * partSize)
        slice()
    }
}
