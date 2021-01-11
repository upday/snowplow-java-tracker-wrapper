package snowplowjavatrackertracker

/**
 * App properties to emit Snowplow events
 */
data class SnowplowAppProperties(
    val appId: String,
    val collectorUrl: String,
    val nameSpace: String,
    val isBase64Encoded: Boolean,
    val emitterBufferSize: Int,
    val emitterThreadCount: Int
)
