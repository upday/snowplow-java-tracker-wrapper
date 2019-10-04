package snowplowjavatrackertracker

import com.snowplowanalytics.snowplow.tracker.payload.TrackerPayload
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

class EventRequestCallbackTest {

    @Test
    fun `make sure provided failure callback is called on failure`() {
        val events = listOf(TrackerPayload())
        val successCallback = mockk<(Int) -> Unit>(relaxed = true)
        val failureCallback = mockk<(List<TrackerPayload>) -> Unit>(relaxed = true)

        EventRequestCallback(successCallback, failureCallback).onFailure(0, events)

        verify(exactly = 1) { failureCallback(events) }
    }

    @Test
    fun `make sure provided success callback is called on success`() {
        val successCallback = mockk<(Int) -> Unit>(relaxed = true)
        val failureCallback = mockk<(List<TrackerPayload>) -> Unit>(relaxed = true)

        EventRequestCallback(successCallback, failureCallback).onSuccess(42)

        verify(exactly = 1) { successCallback(42) }
    }
}