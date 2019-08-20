package burst.plotter.pocxor

import burst.kit.crypto.BurstCrypto
import burst.miner.pocxor.MiningPlot
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.io.RandomAccessFile
import kotlin.math.roundToLong

class XorPlotter(private val id: Long) {
    private var burstCrypto = BurstCrypto.getInstance()

    fun plot(output: RandomAccessFile, startNonces: Array<Long>): Completable {
        var nextStartNonceIndex = 0
        val selectNextStartNonceLock = Any()
        val writeDiskLock = Any()
        return Single.fromCallable {
            val setSize = MiningPlot.SCOOPS_PER_PLOT * MiningPlot.PLOT_SIZE

            // Set file size
            output.setLength(startNonces.size * setSize.toLong())

            // Allocate memory
            val buffers = mutableListOf<ByteArray>()

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
                .flatMapCompletable { buffer ->
                    Completable.fromAction {
                        while (true) {
                            // Choose a startNonce
                            val startNonce: Long
                            synchronized(selectNextStartNonceLock) {
                                if (nextStartNonceIndex >= startNonces.size) {
                                    return@fromAction
                                }
                                startNonce = startNonces[nextStartNonceIndex]
                                nextStartNonceIndex++
                            }
                            println("Calculating data for start nonce $startNonce")
                            // Calculate the plot data
                            calculatePlotData(buffer, startNonce)
                            println("Start nonce $startNonce calculation finished!")
                            // Write it to disk
                            synchronized(writeDiskLock) {
                                println("Writing start nonce $startNonce to disk...")
                                output.seek(MiningPlot.SCOOPS_PER_PLOT * MiningPlot.PLOT_SIZE * startNonce)
                                output.write(buffer)
                                println("Finished writing start nonce $startNonce to disk...")
                            }
                        }

                    }.subscribeOn(Schedulers.computation()) // This is computationally intensive
                }
    }

    private fun calculatePlotData(buffer: ByteArray, startNonce: Long) {
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
    }
}
