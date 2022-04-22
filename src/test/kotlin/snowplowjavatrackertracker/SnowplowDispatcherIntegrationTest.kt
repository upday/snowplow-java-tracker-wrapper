package snowplowjavatrackertracker

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.common.Slf4jNotifier
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder
import org.awaitility.kotlin.await
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS

@TestInstance(PER_CLASS)
class SnowplowDispatcherIntegrationTest {

    private val wireMockServer = WireMockServer(
        WireMockConfiguration.wireMockConfig()
            .notifier(Slf4jNotifier(false))
            .dynamicPort()
    )

    @BeforeAll
    fun startServer() {
        this.wireMockServer.start()
        WireMock.configureFor(this.wireMockServer.port())
    }

    @AfterAll
    fun stopServer() {
        wireMockServer.stop()
    }

    @Test
    fun `should successfully send a valid event to snowplow`() {
        val dispatcher = snowplowDispatcher(
            appId = "my-app-id",
            nameSpace = "app-namespace",
            collectorUrl = "$LOCALHOST:${wireMockServer.port()}",
        )

        dispatcher.send(Fixtures.event(userId = "my-id-1"))
        dispatcher.send(Fixtures.event(userId = "my-id-2"))

        await.untilAsserted {
            wireMockServer.verify(2, RequestPatternBuilder().withPort(wireMockServer.port()))
        }
    }

    companion object {
        private const val LOCALHOST = "http://localhost"
    }
}
