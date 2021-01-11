package snowplowjavatrackertracker

import com.snowplowanalytics.snowplow.tracker.Subject
import com.snowplowanalytics.snowplow.tracker.events.Unstructured
import com.snowplowanalytics.snowplow.tracker.payload.SelfDescribingJson
import com.snowplowanalytics.snowplow.tracker.payload.TrackerPayload

object Fixtures {

    fun event(
        eventData: SelfDescribingJson = eventData(),
        userId: String = "my-id-1",
    ): Unstructured = Unstructured.builder()
        .eventData(eventData)
        .subject(Subject.SubjectBuilder().userId(userId).build())
        .build()

    private fun eventData(
        schema: String = "schema",
        data: Map<String, String> = mapOf("key" to "value"),
    ): SelfDescribingJson = SelfDescribingJson(schema, TrackerPayload().apply { addMap(data) })
}
