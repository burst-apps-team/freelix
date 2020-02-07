package burst.common

import kotlin.experimental.xor

object Util {
    fun xorArray(dst: ByteArray, offset: Int, other: ByteArray, otherOffset: Int, length: Int) {
        for (i in 0 until length) {
            dst[offset + i] = dst[offset + i] xor other[i + otherOffset]
        }
    }
}

fun <T> Iterable<T>.toPairList(): List<Pair<T, T>> {
    val result = mutableListOf<Pair<T, T>>()
    val iterator = iterator()
    while (iterator.hasNext()) {
        val first = iterator.next()
        if (iterator.hasNext()) result.add(Pair(first, iterator.next()))
    }
    return result
}
