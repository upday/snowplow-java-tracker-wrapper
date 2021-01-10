package snowplowjavatrackertracker

import com.snowplowanalytics.snowplow.tracker.Tracker
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
        val event = Fixtures.event()

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
