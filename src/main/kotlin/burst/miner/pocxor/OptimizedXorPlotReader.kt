package burst.miner.pocxor

import burst.miner.Deadline
import burst.miner.PlotReader
import io.reactivex.Observable
import java.io.RandomAccessFile

/**
 * In theory, what we need to do to be as efficient as possible is:
 * 1. Check which nonces we are reading and figure out which ones we need to generate on the fly.
 * 2. Start reading the disk in the right places
 * 3. Have some sort of system so that when when a certain set's nonces and data is ready, we start mashing them together and get the nonces
 * 4. Calculate deadlines from nonces
 * 5. Filter deadlines
 *
 * In practice, we are constrained by memory, etc. TODO work out exactly how much memory we need per set
 * There is no point reading the disk or calculating nonces faster than we are actually using any of the obtained data.
 * Additionally, there is no point trying to read the disk on multiple threads at the same time - it is slower than series due to head flying time.
 * Therefore we need to
 * 1. Start reading the disk for a certain set and simultaneously, on 2 separate cores, calculate the 2 nonces needed to obtain the original data.
 * 2. Once we have all the data, start the process again, and on a separate thread, start calculating the deadline for each retrieved nonce. This could take advantage of any remaining cores.
 * 3. If it turns out we are CPU-bound rather than IO bound, we could use other threads to calculate the next set's 2 nonces in advance.
 */
class OptimizedXorPlotReader : PlotReader {
    override fun fetchBestDeadlines(generationSignature: ByteArray, scoop: Int, baseTarget: Long, height: Long, pocVersion: Int): Observable<Deadline> {
        TODO()
    }

    private fun readOneSet(plotFile: RandomAccessFile) {
        val buffer = ByteArray(MiningPlot.SCOOPS_PER_PLOT * 2 * MiningPlot.SCOOP_SIZE)
        for (i in 0 until MiningPlot.SCOOPS_PER_PLOT * 2) {
            plotFile.seek((i * MiningPlot.PLOT_SIZE + i * MiningPlot.SCOOP_SIZE).toLong())
            plotFile.read()
        }
    }
}