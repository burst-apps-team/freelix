package burst.plotter.pocxor

import burst.kit.crypto.BurstCrypto
import burst.miner.pocxor.MiningPlot
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.io.OutputStream
import kotlin.math.roundToLong

class XorPlotter(private val id: Long) {
    private var burstCrypto = BurstCrypto.getInstance()

    fun plot(output: OutputStream, startNonces: Array<Long>): Completable {
        var nextStartNonceIndex = 0
        val selectNextStartNonceLock = Any()
        val writeDiskLock = Any()
        return Single.fromCallable {
            // Allocate memory.
            val buffers = mutableListOf<ByteArray>()
            val setSize = MiningPlot.SCOOPS_PER_PLOT * MiningPlot.PLOT_SIZE

            println("Allocating memory...")
            while (true) {
                try {
                    buffers.add(ByteArray(setSize) { (it % 0xFF).toByte() })
                } catch (e: OutOfMemoryError) {
                    println("Allocated ${buffers.size} buffers. Let's go!")
                    break
                }
            }
            return@fromCallable buffers
        }
                .subscribeOn(Schedulers.io())
                .flattenAsObservable { it }
                .flatMapMaybe { buffer ->
                    Maybe.fromCallable {
                        // Choose a startNonce
                        synchronized(selectNextStartNonceLock) {
                            if (nextStartNonceIndex >= startNonces.size) {
                                return@fromCallable null
                            }
                            val startNonce = startNonces[nextStartNonceIndex]
                            nextStartNonceIndex++
                            return@fromCallable Pair(startNonce, buffer)
                        }
                    }.subscribeOn(Schedulers.single()) // This only needs to run on one thread
                }
                .flatMapSingle {
                    Single.fromCallable {
                        calculatePlotData(it.second, it.first)
                        return@fromCallable it
                    }.subscribeOn(Schedulers.computation()) // This is computationally intensive
                }
                .flatMapCompletable {
                    Completable.fromAction {
                        synchronized(writeDiskLock) {
                            output.write(it.second, (MiningPlot.SCOOPS_PER_PLOT * MiningPlot.PLOT_SIZE * it.first).toInt(), it.second.size)
                        }
                    }.subscribeOn(Schedulers.single()) // This only needs to run on one thread
                }
    }

    private fun calculatePlotData(buffer: ByteArray, startNonce: Long) {
        println("Calculating plot data for start nonce $startNonce")

        fun printProgress(currentNonce: Int) {
            // Update every 1%
            if (currentNonce % (MiningPlot.SCOOPS_PER_PLOT / 50 /* scoopsPerPlot*2/100 */) == 0) {
                println("Start nonce $startNonce: ${(currentNonce.toDouble() / (MiningPlot.SCOOPS_PER_PLOT * 2).toDouble() * 100).roundToLong()}% (currentNonce: $currentNonce)")
            }
        }

        /*for (nonce in 0 until MiningPlot.SCOOPS_PER_PLOT) { // first set is straight copied
            printProgress(nonce)
            val plot = MiningPlot(Supplier { burstCrypto.shabal256 }, id, nonce + startNonce, 2)
            System.arraycopy(plot.data, 0, buffer, nonce * MiningPlot.PLOT_SIZE, MiningPlot.PLOT_SIZE) // TODO plot directly into the buffer
        }

        println("starting 2nd")

        for (nonce in 0 until MiningPlot.SCOOPS_PER_PLOT) { // second set is nonce + SCOOPS_PER_PLOT and is xored
            printProgress(nonce + MiningPlot.SCOOPS_PER_PLOT)
            val plot = MiningPlot(Supplier { burstCrypto.shabal256 }, id, nonce + MiningPlot.SCOOPS_PER_PLOT + startNonce, 2)
            for (scoop in 0 until MiningPlot.SCOOPS_PER_PLOT) {
                XorUtil.xorArray(buffer, (nonce * MiningPlot.SCOOP_SIZE + scoop * MiningPlot.PLOT_SIZE), plot.getScoop(scoop))
            }
        }*/

        println("Start nonce $startNonce finished!")
    }
}
