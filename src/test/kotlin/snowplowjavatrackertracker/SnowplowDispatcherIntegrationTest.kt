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

        verify(timeout = 500, exactly = 2) { successCallback(1) }
    }

    @Test
    fun `should not throw an exception`() {
        val event = Fixtures.event()

        val dispatcher = snowplowDispatcher(
            "my-name",
            "app",
            "http://localhost:1080",
            1, 1
        )
        dispatcher.send(event)
    }

    @Test
    fun `should call success callback after successfully re-sending failed event`() {
        val event1: Event = Fixtures.event(userId = "user-1")

        retryFailedEvent(
            failedEvents = listOf(event1),
            retryCount = 2,
            appProperties = SnowplowAppProperties("app-test", "http://localhost:1080", "test"),
            onFailure = failureCallback,
            onSuccess = successCallback
        )

        verify(timeout = 500, exactly = 1) { successCallback(1) }
        verify(timeout = 500, exactly = 0) { failureCallback(any(), any()) }
    }

    @Test
    fun `should call failure callback after failures with retry attempts`() {
        val event1: Event = Fixtures.event(userId = "user-1")
        val event2: Event = Fixtures.event(userId = "user-2")

        retryFailedEvent(
            failedEvents = listOf(event1, event2),
            retryCount = 2,
            appProperties = SnowplowAppProperties("app-test", "http://localhost:1081", "test"),
            onFailure = failureCallback,
            onSuccess = successCallback
        )

        verify(timeout = 400, exactly = 0) { failureCallback(any(), any()) }
        verify(timeout = 1200, exactly = 1) { failureCallback(0, listOf(event1)) }
        verify(timeout = 1200, exactly = 1) { failureCallback(0, listOf(event2)) }
    }
}
