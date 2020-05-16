package snowplowjavatrackertracker

import com.snowplowanalytics.snowplow.tracker.emitter.RequestCallback
import com.snowplowanalytics.snowplow.tracker.payload.TrackerPayload
import mu.KotlinLogging

class EventRequestCallback(
    private val successCallback: (Int) -> Unit,
    private val failureCallback: (List<TrackerPayload>) -> Unit
) : RequestCallback {

    override fun onSuccess(successCount: Int) {
        logger.info("Successfully emitted $successCount events")
        successCallback(successCount)
    }

    override fun onFailure(successCount: Int, failedEvents: List<TrackerPayload>) {
        logger.error("Error when emitting events: $failedEvents")
        failureCallback(failedEvents)
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
