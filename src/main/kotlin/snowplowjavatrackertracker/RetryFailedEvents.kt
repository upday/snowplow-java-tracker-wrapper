package snowplowjavatrackertracker

import com.snowplowanalytics.snowplow.tracker.events.Event
import kotlin.math.pow
import kotlin.random.Random
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

internal class RetryFailedEvents(
    private val snowplowAppProperties: SnowplowAppProperties,
    private val retryCount: Int,
    private val successCallback: SuccessCallback? = null,
    private val finalFailureCallback: FailureCallback? = null
) {
    private var retryAttemptCounter = retryCount

    suspend fun sendEvent(event: Event) =
        with(snowplowAppProperties) {
            SnowplowDispatcher(tracker(nameSpace, appId, true,
                emitter(
                    collectorUrl = collectorUrl,
                    emitterSize = emitterBufferSize,
                    threadCount = emitterThreadCount,
                    onSuccess = successCallback,
                    onFailure = { successCount, failedEvents ->
                        runBlocking { retryFailure(successCount, failedEvents) }
                    })))
                .send(event)
        }

    private suspend fun retryFailure(successCount: Int, failedEvents: List<Event>) {
        when {
            retryAttemptCounter > 1 -> {
                delay(retryAttemptCounter.delay().toLong())
                failedEvents.forEach { sendEvent(it) }
                retryAttemptCounter--
            }
            else -> {
                finalFailureCallback?.let { it(successCount, failedEvents) }
            }
        }
    }

    private fun Int.delay() = INITIAL_DELAY * EXPONENTIAL_BASE.pow(retryCount - this + 1) + RANDOM_FACTOR

    companion object {
        private const val INITIAL_DELAY = 300
        private const val EXPONENTIAL_BASE = 2.0
        private val RANDOM_FACTOR = Random.nextInt(100, 500)
    }
}
