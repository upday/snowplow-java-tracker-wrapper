package snowplowjavatrackertracker

import com.snowplowanalytics.snowplow.tracker.payload.TrackerPayload

interface EventDispatcher <T> {

    /**
     *  Sends failed events.
     *
     *  @param failedEvents represents the list of events which should be retried to be send.
     *  Whereby each event is already comprising all tracker enrichment.
     */
    fun send(failedEvents: List<TrackerPayload>)

    /**
     * Sends an event of generic type.
     *
     * @param event the event which should be send.
     * @param userId the user the event originates from.
     */
    fun send(event: T, userId: String)
}