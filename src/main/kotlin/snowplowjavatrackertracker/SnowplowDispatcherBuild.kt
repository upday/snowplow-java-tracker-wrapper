package snowplowjavatrackertracker

import com.snowplowanalytics.snowplow.tracker.DevicePlatform
import com.snowplowanalytics.snowplow.tracker.Tracker
import com.snowplowanalytics.snowplow.tracker.emitter.BatchEmitter
import com.snowplowanalytics.snowplow.tracker.emitter.Emitter
import com.snowplowanalytics.snowplow.tracker.http.ApacheHttpClientAdapter
import org.apache.http.impl.client.HttpClients

/**
 * Sends Snowplow events to the Snowplow collector
 *
 * @param appId snowplow application ID
 * @param nameSpace snowplow tracker name
 * @param collectorUrl snowplow URL
 * @param batchSize Specifies how many events go into a POST, default 1
 * @param threadCount The number of Threads that can be used to send events, default 50
 * @param base64 enable base 64 encoding, default true
 */
fun snowplowDispatcher(
    appId: String,
    nameSpace: String,
    collectorUrl: String,
    batchSize: Int = 1,
    threadCount: Int = 50,
    base64: Boolean = true,
): SnowplowDispatcher = SnowplowDispatcher(
    tracker(
        nameSpace, appId, base64,
        emitter(collectorUrl, batchSize, threadCount)
    )
)

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
    emitterBatchSize: Int,
    threadCount: Int
) = BatchEmitter
    .builder()
    .threadCount(threadCount)
    .httpClientAdapter(apacheHttpClientAdapter(collectorUrl))
    .batchSize(emitterBatchSize)
    .build()

private fun apacheHttpClientAdapter(collectorUrl: String): ApacheHttpClientAdapter =
    ApacheHttpClientAdapter
        .builder()
        .httpClient(HttpClients.custom().disableCookieManagement().build())
        .url(collectorUrl)
        .build()
