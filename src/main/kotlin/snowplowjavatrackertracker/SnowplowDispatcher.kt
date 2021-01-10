package snowplowjavatrackertracker

import com.snowplowanalytics.snowplow.tracker.Tracker
import com.snowplowanalytics.snowplow.tracker.events.Event

class SnowplowDispatcher(private val tracker: Tracker) {
    fun send(event: Event?) {
            event?.let { tracker.track(event) }
    }
}
