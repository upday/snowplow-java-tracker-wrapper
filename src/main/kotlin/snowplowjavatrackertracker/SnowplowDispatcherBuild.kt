package snowplowjavatrackertracker

import com.snowplowanalytics.snowplow.tracker.DevicePlatform
import com.snowplowanalytics.snowplow.tracker.Tracker
import com.snowplowanalytics.snowplow.tracker.emitter.BatchEmitter
import com.snowplowanalytics.snowplow.tracker.emitter.Emitter
import com.snowplowanalytics.snowplow.tracker.emitter.RequestCallback
import com.snowplowanalytics.snowplow.tracker.events.Event
import com.snowplowanalytics.snowplow.tracker.http.ApacheHttpClientAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.apache.http.impl.client.HttpClients

typealias SuccessCallback = (successCount: Int) -> Unit
typealias FailureCallback = (successCount: Int, failedEvents: List<Event>) -> Unit

/**
 * @param appId snowplow application ID
 * @param nameSpace snowplow tracker name
 * @param collectorUrl snowplow URL
 * @param bufferSize Specifies how many events go into a POST, default 1
 * @param threadCount The number of Threads that can be used to send events, default 50
 * @param base64 enable base 64 encoding, default true
 * @param onSuccess [SuccessCallback] called to each success request, default null
 * @param onFailure [FailureCallback] called to each failed request, default null
 */
fun snowplowDispatcher(
    appId: String,
    nameSpace: String,
    collectorUrl: String,
    bufferSize: Int = 1,
    threadCount: Int = 50,
    base64: Boolean = true,
    onSuccess: SuccessCallback? = null,
    onFailure: FailureCallback? = null
): SnowplowDispatcher = SnowplowDispatcher(
    tracker(
        nameSpace, appId, base64,
        emitter(collectorUrl, bufferSize, threadCount, onSuccess, onFailure)
    )
)

/**
 * Retries emitting a list of failed events
 * @param failedEvents all events failed on submission to snowplow collector
 * @param appProperties Snowplow app details like appID, tracker name etc
 * @param retryCount Number of retry attempts, default 5
 * @param onSuccess [SuccessCallback] called to each success request
 * @param onFailure [FailureCallback] called to each failed request
 */
fun retryFailedEvent(
    failedEvents: List<Event>,
    appProperties: SnowplowAppProperties,
    retryCount: Int = 5,
    onSuccess: SuccessCallback,
    onFailure: FailureCallback
) = CoroutineScope(Dispatchers.IO).launch {
    failedEvents.forEach { RetryFailedEvents(appProperties, retryCount, onSuccess, onFailure).sendEvent(it) }
}

internal fun tracker(
    nameSpace: String,
    appId: String,
    base64: Boolean,
    emitter: Emitter
): Tracker = Tracker
    .TrackerBuilder(emitter, nameSpace, appId)
    .base64(base64)
    .platform(DevicePlatform.General)
    .build()

internal fun emitter(
    collectorUrl: String,
    emitterSize: Int,
    threadCount: Int,
    onSuccess: SuccessCallback?,
    onFailure: FailureCallback?
) = BatchEmitter
    .builder()
    .threadCount(threadCount)
    .httpClientAdapter(apacheHttpClientAdapter(collectorUrl))
    .requestCallback(requestCallback(onSuccess, onFailure))
    .bufferSize(emitterSize)
    .build()

private fun apacheHttpClientAdapter(collectorUrl: String): ApacheHttpClientAdapter =
    ApacheHttpClientAdapter
        .builder()
        .httpClient(HttpClients.custom().disableCookieManagement().build())
        .url(collectorUrl)
        .build()

private fun requestCallback(onSuccess: SuccessCallback?, onFailure: FailureCallback?): RequestCallback =
    object : RequestCallback {
        override fun onSuccess(successCount: Int) {
            onSuccess?.let { it(successCount) }
        }

        override fun onFailure(successCount: Int, failedEvents: List<Event>) {
            onFailure?.let { it(successCount, failedEvents) }
        }
    }
