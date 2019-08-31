package burst.miner.pocxor

import burst.common.Quad
import burst.kit.crypto.BurstCrypto
import burst.miner.Deadline
import burst.miner.PlotReader
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import java.math.BigInteger
import java.util.concurrent.atomic.AtomicLong

/**
 * A poor, unoptimized implementation of the Xor mining algorithm.
 * Reasons for poor performance include poor use of schedulers (always subscribing on the io scheduler even when doing computational work),
 * poor use of parallelization opportunities (compute the 2 nonces on one thread whilst we read on the other etc)
 * and lots of disk seeks, an optimization which will likely require changing the format of the plot files.
 */
class UnoptimizedXorPlotReader(private val plotFiles: Array<String>, private val pocVersion: Int) : PlotReader {
    override fun fetchBestDeadlines(generationSignature: ByteArray, scoop: Int, baseTarget: Long, height: Long, pocVersion: Int): Observable<Deadline> {
        val burstCrypto = BurstCrypto.getInstance()
        val bestDeadline = AtomicLong(Long.MAX_VALUE)
        val plotFileNameRegex = Regex("([\\d]+)_([\\d]+)_([\\d]+)")
        return Observable.fromArray(*plotFiles)
                .subscribeOn(Schedulers.io())
                .map { plotFile ->
                    var id: Long = -1
                    var startSet: Long = -1
                    var setCount: Long = -1
                    // TODO what if plotFile is absolute path?
                    plotFileNameRegex.findAll(plotFile).forEach {
                        it.groups.forEachIndexed { index, match ->
                            if (match == null) return@forEachIndexed
                            when (index) {
                                1 -> id = java.lang.Long.parseUnsignedLong(match.value)
                                2 -> startSet = java.lang.Long.parseUnsignedLong(match.value)
                                3 -> setCount = java.lang.Long.parseUnsignedLong(match.value)
                            }
                        }
                    }
                    require(!(id == -1L || startSet == -1L || setCount == -1L)) { "Invalid plot file name: $plotFile" }
                    Quad(plotFile, id, startSet, setCount)
                }
                .flatMap { read(it.second, it.first, it.third, it.fourth, burstCrypto.calculateScoop(generationSignature, height)) }
                .flatMapMaybe {
                    Maybe.fromCallable {
                        val deadline = calculateDeadline(generationSignature, baseTarget, it.second)
                                ?: return@fromCallable null
                        synchronized(bestDeadline) {
                            if (deadline < bestDeadline.get()) {
                                bestDeadline.set(deadline)
                                return@fromCallable Pair(it.first, deadline)
                            } else {
                                return@fromCallable null
                            }
                        }
                    }
                }
                .map { Deadline(it.second, it.first) }
    }

    // TODO fix burstkit4j and use that instead
    private fun calculateDeadline(generationSignature: ByteArray, baseTarget: Long, scoopData: ByteArray): Long? {
        return try {
            val shabal256 = BurstCrypto.getInstance().shabal256
            shabal256.update(generationSignature)
            shabal256.update(scoopData)
            val hash = shabal256.digest()
            (BigInteger(1, byteArrayOf(hash[7], hash[6], hash[5], hash[4], hash[3], hash[2], hash[1], hash[0])) / BigInteger.valueOf(baseTarget))
                    .longValueExact()
        } catch (e: ArithmeticException) {
            null
        }
    }

    /**
     * @return an observable containing the scoops
     */
    private fun read(id: Long, plotFile: String, startSet: Long, setCount: Long, scoop: Int): Observable<Pair<Long, ByteArray>> {
        return Observable.create {
            XorGetter(id, scoop, plotFile, startSet, setCount, pocVersion).get()
                    .forEach { scoop -> if (scoop != null) it.onNext(scoop) }
        }
    }
}