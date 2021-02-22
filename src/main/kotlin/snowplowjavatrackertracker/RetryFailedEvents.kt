package snowplowjavatrackertracker

import com.snowplowanalytics.snowplow.tracker.Tracker
import com.snowplowanalytics.snowplow.tracker.events.Event
import java.util.concurrent.CountDownLatch
import kotlin.math.pow
import kotlin.random.Random
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mu.KotlinLogging

internal class RetryFailedEvents(
    snowplowAppProperties: SnowplowAppProperties,
    private val retryCount: Int,
    private val successCallback: SuccessCallback? = null,
    private val finalFailureCallback: FailureCallback? = null
) {
    private val retryAttemptCountDownLatch = CountDownLatch(retryCount)

    fun sendEvent(event: Event) {
        val attemptCount = retryCount - retryAttemptCountDownLatch.count + 1
        logger.info { "Retrying to send event : $event, attemptCount: $attemptCount" }

        val dispatcher = SnowplowDispatcher(retryTracker)
        dispatcher.send(event)
    }

    private val retryTracker: Tracker = with(snowplowAppProperties) {
        tracker(
            nameSpace = nameSpace,
            appId = appId,
            base64 = isBase64Encoded,
            emitter = emitter(collectorUrl = collectorUrl,
                emitterSize = EMITTER_SIZE,
                threadCount = emitterThreadCount,
                onSuccess = successCallback,
                onFailure = { successCount, failedEvents ->
                    retryFailure(successCount, failedEvents)
                }
            )
        )
    }

    private fun retryFailure(successCount: Int, failedEvents: List<Event>) {
        logger.debug { "retryFailure: ${failedEvents.stream().map { event -> event.eventId }}" }
        val retryAttemptCounter = retryAttemptCountDownLatch.count.toInt()
        when {
            retryAttemptCounter > 1 -> {
                retryAttemptCountDownLatch.countDown()
                CoroutineScope(Dispatchers.IO).launch {
                    val retrialDelay = retryAttemptCounter.delay()
                    delay(retrialDelay.toLong())
                    logger.info { "Retrying after $retrialDelay milliseconds" }
                    sendEvent(failedEvents.first())
                }
            }
            else -> {
                logger.error { "Retrial attempts failed for events: $failedEvents" }
                finalFailureCallback?.let { it(successCount, failedEvents) }
            }
        }
    }

    private fun Int.delay() = INITIAL_DELAY_MILLISECONDS * EXPONENTIAL_BASE.pow(retryCount - this + 1) + RANDOM_FACTOR

    companion object {
        private const val INITIAL_DELAY_MILLISECONDS = 2000
        private const val EXPONENTIAL_BASE = 2.0
        private const val EMITTER_SIZE = 1
        private val RANDOM_FACTOR = Random.nextInt(100, 500)
        private val logger = KotlinLogging.logger {}
    }
}
