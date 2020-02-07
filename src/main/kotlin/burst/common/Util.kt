package burst.common

import kotlin.experimental.xor

object Util {
    fun xorArray(dst: ByteArray, offset: Int, other: ByteArray, otherOffset: Int, length: Int) {
        for (i in 0 until length) {
            dst[offset + i] = dst[offset + i] xor other[i + otherOffset]
        }
    }
}
