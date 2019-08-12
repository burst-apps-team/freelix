package burst.miner

import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

// TODO is this even necessary
fun <T> Single<T>.defaultSubscribe(): Single<T> {
    return this.subscribeOn(Schedulers.io())
}