package snowplowjavatrackertracker

import com.snowplowanalytics.snowplow.tracker.Subject
import com.snowplowanalytics.snowplow.tracker.Tracker
import com.snowplowanalytics.snowplow.tracker.events.Unstructured
import com.snowplowanalytics.snowplow.tracker.payload.SelfDescribingJson
import com.snowplowanalytics.snowplow.tracker.payload.TrackerPayload
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle

@TestInstance(Lifecycle.PER_CLASS)
class SnowplowDispatcherTest {
    @Test
    fun `should call track`() {
        val tracker: Tracker = mockk(relaxed = true)
        val dispatcher = SnowplowDispatcher(tracker)
        val json = SelfDescribingJson("schema", TrackerPayload().apply { addMap(mapOf("key" to "value")) })
        val event = Unstructured.builder()
            .eventData(json)
            .subject(Subject.SubjectBuilder().userId("my-id-1").build())
            .build()

        dispatcher.send(event)

        verify(exactly = 1) { tracker.track(event) }
    }

    @Test
    fun `should not call track`() {
        val tracker: Tracker = mockk(relaxed = true)
        val dispatcher = SnowplowDispatcher(tracker)

        dispatcher.send(null)

        verify(exactly = 0) { tracker.track(any()) }
    }
}
