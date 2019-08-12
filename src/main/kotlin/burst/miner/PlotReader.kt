package burst.miner

import io.reactivex.Flowable

interface PlotReader {
    fun fetchBestDeadlines(generationSignature: ByteArray, scoop: Int, baseTarget: Long, pocVersion: Int): Flowable<Deadline>
}

class Deadline(val deadline: Long, val nonce: Long)
