package burst.miner.pocxor

import burst.kit.crypto.BurstCrypto
import burst.miner.Deadline
import burst.miner.PlotReader
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import java.math.BigInteger

/**
 * A poor, unoptimized implementation of the Xor mining algorithm.
 * Reasons for poor performance include poor use of schedulers (always subscribing on the io scheduler even when doing computational work),
 * poor use of parallelization opportunities (compute the 2 nonces on one thread whilst we read on the other etc)
 * and lots of disk seeks, an optimization which will likely require changing the format of the plot files.
 */
class UnoptimizedXorPlotReader(private val plotFiles: Array<String>, private val id: Long, private val scoop: Int, private val pocVersion: Int) : PlotReader {
    override fun fetchBestDeadlines(generationSignature: ByteArray, scoop: Int, baseTarget: Long, pocVersion: Int): Observable<Deadline> {
        return Observable.fromArray(*plotFiles)
                .subscribeOn(Schedulers.io())
                .flatMap { read(it) }
                .flatMapMaybe {
                    Maybe.fromCallable {
                        Pair(it.first, calculateDeadline(generationSignature, baseTarget, it.second)
                                ?: return@fromCallable null)
                    }
                }
                .map { Deadline(it.first, it.second) }
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
    private fun read(plotFile: String): Observable<Pair<Long, ByteArray>> {
        return Observable.create {
            XorGetter(id, scoop, plotFile, pocVersion).get()
                    .forEach { scoop -> if (scoop != null) it.onNext(scoop) }
        }
    }
}