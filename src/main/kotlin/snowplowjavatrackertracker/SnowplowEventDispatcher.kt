package snowplowjavatrackertracker

import com.snowplowanalytics.snowplow.tracker.DevicePlatform
import com.snowplowanalytics.snowplow.tracker.Subject
import com.snowplowanalytics.snowplow.tracker.Tracker
import com.snowplowanalytics.snowplow.tracker.emitter.BatchEmitter
import com.snowplowanalytics.snowplow.tracker.events.Unstructured
import com.snowplowanalytics.snowplow.tracker.http.ApacheHttpClientAdapter
import com.snowplowanalytics.snowplow.tracker.payload.TrackerPayload
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager

class SnowplowEventDispatcher<T>(
    private val successCallback: (Int) -> Unit,
    private val failureCallback: (List<TrackerPayload>) -> Unit,
    private val trackerConfiguration: TrackerConfiguration,
    private val snowplowMapper: (T) -> Unstructured
) : EventDispatcher<T> {

    private val emitter = emitter()
    override fun send(failedEvents: List<TrackerPayload>) {
        val tracker: Tracker =
            Tracker.TrackerBuilder(emitter, trackerConfiguration.nameSpace, trackerConfiguration.appId)
                .base64(true)
                .platform(DevicePlatform.General)
                .build()

        failedEvents.map { trackerPayload ->
            tracker.track(trackerPayload)
        }
    }

    override fun send(event: T, userId: String) {
        val tracker: Tracker =
            Tracker.TrackerBuilder(emitter, trackerConfiguration.nameSpace, trackerConfiguration.appId)
                .base64(true)
                .subject(Subject.SubjectBuilder().userId(userId).build())
                .platform(DevicePlatform.General)
                .build()

        val eventUnstructured = snowplowMapper(event)
        tracker.track(eventUnstructured)
    }

    private fun createPoolManager(): CloseableHttpClient {
        val manager = PoolingHttpClientConnectionManager()
        manager.defaultMaxPerRoute = trackerConfiguration.threadPoolSize

        return HttpClients.custom()
            .setConnectionManager(manager)
            .disableCookieManagement()
            .build()
    }

    private fun emitter(): BatchEmitter {
        val clientAdapter = ApacheHttpClientAdapter.builder()
            .url(trackerConfiguration.collectorUrl)
            .httpClient(createPoolManager())
            .build()
        return BatchEmitter.builder()
            .httpClientAdapter(clientAdapter)
            .requestCallback(EventRequestCallback(successCallback, failureCallback))
            .bufferSize(trackerConfiguration.emitterSize)
            .build()
    }
}

data class TrackerConfiguration(
    val nameSpace: String,
    val appId: String,
    val collectorUrl: String,
    val emitterSize: Int,
    val threadPoolSize: Int
)
