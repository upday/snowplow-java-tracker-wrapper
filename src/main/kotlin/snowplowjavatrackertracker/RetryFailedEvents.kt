package snowplowjavatrackertracker

import com.snowplowanalytics.snowplow.tracker.events.Event
import kotlin.math.pow
import kotlin.random.Random
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

internal class RetryFailedEvents(
    private val snowplowAppProperties: SnowplowAppProperties,
    private val retryCount: Int,
    private val successCallback: SuccessCallback,
    private val finalFailureCallback: FailureCallback
) {
    private var retryAttemptCounter = retryCount

    suspend fun sendEvent(event: Event) =
        with(snowplowAppProperties) {
            SnowplowDispatcher(tracker(nameSpace, appId, true,
                emitter(collectorUrl,
                    EMITTER_SIZE,
                    THREAD_COUNT,
                    { retrySuccess(it) },
                    { successCount, failedEvents ->
                        runBlocking { retryFailure(successCount, failedEvents) }
                    })))
                .send(event)
        }

    private fun retrySuccess(successCount: Int) = successCallback(successCount)

    private suspend fun retryFailure(successCount: Int, failedEvents: List<Event>) {
        when {
            retryAttemptCounter > 1 -> {
                delay(retryAttemptCounter.delay().toLong())
                failedEvents.forEach { sendEvent(it) }
                retryAttemptCounter--
            }
            else -> {
                finalFailureCallback(successCount, failedEvents)
            }
        }
    }

    private fun Int.delay() = INITIAL_DELAY * EXPONENTIAL_BASE.pow(retryCount - this + 1) + RANDOM_FACTOR

    companion object {
        private const val EMITTER_SIZE = 1
        private const val THREAD_COUNT = 50
        private const val INITIAL_DELAY = 300
        private const val EXPONENTIAL_BASE = 2.0
        private val RANDOM_FACTOR = Random.nextInt(100, 500)
    }
}
