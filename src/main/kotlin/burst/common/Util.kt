package burst.common

import kotlin.experimental.xor

object Util {
    fun xorArray(dst: ByteArray, offset: Int, other: ByteArray) {
        for (i in other.indices) {
            dst[offset + i] = dst[offset + i] xor other[i]
        }
    }
}
