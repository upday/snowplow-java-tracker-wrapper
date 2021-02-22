package snowplowjavatrackertracker

import com.snowplowanalytics.snowplow.tracker.events.Event
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

    private val successCallback = mockk<(Int) -> Unit>(relaxed = true)

    private val failureCallback = mockk<(Int, List<Event>) -> Unit>(relaxed = true)

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
        val dispatcher = snowplowDispatcher(
            appId = "my-app-id",
            nameSpace = "app-namespace",
            collectorUrl = "http://localhost:1080",
            onSuccess = successCallback
        )

        dispatcher.send(Fixtures.event(userId = "my-id-1"))
        dispatcher.send(Fixtures.event(userId = "my-id-2"))

        verify(timeout = 100, exactly = 2) { successCallback(1) }
    }

    @Test
    fun `should not throw an exception`() {
        val event = Fixtures.event()
        val dispatcher = snowplowDispatcher(
            "my-name",
            "app",
            "http://localhost:1080",
            1,
            1
        )

        dispatcher.send(event)
    }

    @Test
    fun `should call failure callback after failure with retry attempts`() {
        val dispatcher = snowplowDispatcher(
            appId = "my-app-id",
            nameSpace = "app-namespace",
            collectorUrl = "http://localhost:1081",
            retryCount = 2,
            onSuccess = successCallback,
            onFailure = failureCallback
        )
        val event1 = Fixtures.event(userId = "my-id-1")
        val event2 = Fixtures.event(userId = "my-id-2")

        dispatcher.send(event1)
        dispatcher.send(event2)

        verify(timeout = 400, exactly = 0) { failureCallback(any(), any()) }
        verify(timeout = 4000, exactly = 1) { failureCallback(0, listOf(event1)) }
        verify(timeout = 4000, exactly = 1) { failureCallback(0, listOf(event2)) }
    }
}
