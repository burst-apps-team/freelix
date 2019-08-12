package burst.miner

import io.reactivex.Observable

interface PlotReader {
    fun fetchBestDeadlines(generationSignature: ByteArray, scoop: Int, baseTarget: Long, height: Long, pocVersion: Int): Observable<Deadline>
}

class Deadline(val deadline: Long, val nonce: Long)
