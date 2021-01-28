package snowplowjavatrackertracker

import com.snowplowanalytics.snowplow.tracker.events.Event
import kotlin.math.pow
import kotlin.random.Random
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mu.KotlinLogging

internal class RetryFailedEvents(
    private val snowplowAppProperties: SnowplowAppProperties,
    private val retryCount: Int,
    private val successCallback: SuccessCallback? = null,
    private val finalFailureCallback: FailureCallback? = null
) {
    private var retryAttemptCounter = retryCount

    fun sendEvent(event: Event) {
        val attemptCount = retryCount - retryAttemptCounter + 1
        logger.info { "Retrying to send event, attemptCount: $attemptCount" }

        with(snowplowAppProperties) {
            SnowplowDispatcher(tracker(nameSpace, appId, true,
                emitter(
                    collectorUrl = collectorUrl,
                    emitterSize = emitterBufferSize,
                    threadCount = emitterThreadCount,
                    onSuccess = successCallback,
                    onFailure = { successCount, failedEvents ->
                        retryFailure(successCount, failedEvents)
                    })))
                .send(event)
        }
    }

    private fun retryFailure(successCount: Int, failedEvents: List<Event>) {
        when {
            retryAttemptCounter > 1 ->
                CoroutineScope(Dispatchers.IO).launch {
                    val retrialDelay = retryAttemptCounter.delay()
                    delay(retrialDelay.toLong())
                    logger.info { "Retrying after $retrialDelay milliseconds" }
                    failedEvents.forEach { sendEvent(it) }
                    retryAttemptCounter--
                }
            else -> {
                logger.error { "Retrial attempts failed for events: $failedEvents" }
                finalFailureCallback?.let { it(successCount, failedEvents) }
            }
        }
    }

    private fun Int.delay() = INITIAL_DELAY * EXPONENTIAL_BASE.pow(retryCount - this + 1) + RANDOM_FACTOR

    companion object {
        private const val INITIAL_DELAY = 300
        private const val EXPONENTIAL_BASE = 2.0
        private val RANDOM_FACTOR = Random.nextInt(100, 500)
        private val logger = KotlinLogging.logger {}
    }
}
