package snowplowjavatrackertracker

import com.snowplowanalytics.snowplow.tracker.events.Unstructured
import com.snowplowanalytics.snowplow.tracker.payload.SelfDescribingJson
import com.snowplowanalytics.snowplow.tracker.payload.TrackerPayload
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import org.mockserver.integration.ClientAndServer
import snowplowjavatrackertracker.mockserver.MockServer

@TestInstance(Lifecycle.PER_CLASS)
class SnowplowEventDispatcherTest {

    private lateinit var server: ClientAndServer

    @BeforeAll
    fun startServer() {
        server = ClientAndServer.startClientAndServer(1080)
        MockServer().setupSnowplowOk()
        print("hh")
    }

    @AfterAll
    fun stopServer() {
        server.stop()
    }

    private class Event(val content: String)

    @Test
    fun `should call mapper and success callback when sending valid event`() {
        val json = SelfDescribingJson("schema", TrackerPayload().apply { addMap(mapOf("key" to "value")) })
        val successCallback = mockk<(Int) -> Unit>(relaxed = true)
        val failureCallback = mockk<(List<TrackerPayload>) -> Unit>(relaxed = true)
        val mapper = mockk<(Event) -> List<Unstructured>?>()
        every { mapper(any()) } returns listOf(Unstructured.builder().eventData(json).build())
        val event = Event("hi")

        val dispatcher = SnowplowEventDispatcher(
            successCallback,
            failureCallback,
            TrackerConfiguration("name", "app", "http://localhost:1080", 1, 1),
            mapper
        )
        dispatcher.send(event, "id")

        verify(timeout = 1000) { mapper(event) }
        verify(timeout = 1000) { successCallback(1) }
    }

}