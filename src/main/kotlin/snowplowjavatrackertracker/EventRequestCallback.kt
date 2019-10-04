package snowplowjavatrackertracker

import com.snowplowanalytics.snowplow.tracker.emitter.RequestCallback
import com.snowplowanalytics.snowplow.tracker.payload.TrackerPayload
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class EventRequestCallback(
    private val successCallback: (Int) -> Unit,
    private val failureCallback: (List<TrackerPayload>) -> Unit
) : RequestCallback {
    private val logger: Logger = LoggerFactory.getLogger(EventRequestCallback::class.java)

    override fun onSuccess(successCount: Int) {
        logger.info("Successfully emitted $successCount events")
        successCallback(successCount)
    }

    override fun onFailure(successCount: Int, failedEvents: List<TrackerPayload>) {
        logger.error("Error when emitting events: $failedEvents")
        failureCallback(failedEvents)
    }
}
