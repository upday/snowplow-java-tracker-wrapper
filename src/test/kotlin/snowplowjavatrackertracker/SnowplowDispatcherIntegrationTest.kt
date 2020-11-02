package snowplowjavatrackertracker

import com.snowplowanalytics.snowplow.tracker.Subject
import com.snowplowanalytics.snowplow.tracker.events.Unstructured
import com.snowplowanalytics.snowplow.tracker.payload.SelfDescribingJson
import com.snowplowanalytics.snowplow.tracker.payload.TrackerPayload
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
class SnowplowDispatcherIntegrationTest {

    private lateinit var server: ClientAndServer

    @BeforeAll
    fun startServer() {
        server = ClientAndServer.startClientAndServer(1080)
        MockServer().setupSnowplowOk()
    }

    @AfterAll
    fun stopServer() {
        server.stop()
    }

    @Test
    fun `should call mapper and success callback when sending valid event`() {
        val json = SelfDescribingJson("schema", TrackerPayload().apply { addMap(mapOf("key" to "value")) })
        val successCallback = mockk<(Int) -> Unit>(relaxed = true)

        val dispatcher = snowplowDispatcher(
            appId = "my-app-id",
            nameSpace = "app-namespace",
            collectorUrl = "http://localhost:1080",
            onSuccess = successCallback
        )

        dispatcher.send(
            Unstructured.builder()
                .eventData(json)
                .subject(Subject.SubjectBuilder().userId("my-id-1").build())
                .build()
        )
        dispatcher.send(
            Unstructured.builder()
                .eventData(json)
                .subject(Subject.SubjectBuilder().userId("my-id-2").build())
                .build()
        )

        verify(timeout = 500, exactly = 2) { successCallback(1) }
    }

    @Test
    fun `should not throw an exception`() {
        val json = SelfDescribingJson("schema", TrackerPayload().apply { addMap(mapOf("key" to "value")) })
        val event = Unstructured.builder()
            .eventData(json)
            .subject(Subject.SubjectBuilder().userId("my-id").build())
            .build()

        val dispatcher = snowplowDispatcher(
            "my-name",
            "app",
            "http://localhost:1080",
            1, 1
        )
        dispatcher.send(event)
    }
}
