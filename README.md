# snowplow-java-tracker-wrapper
[![Apache License V.2](https://img.shields.io/badge/license-Apache%20V.2-blue.svg)](https://github.com/upday/snowplow-java-tracker-wrapper/blob/b725e4a8a77ed7d5619c782b66affdec3dea05af/LICENSE)


###
Install

[![](https://jitpack.io/v/upday/snowplow-java-tracker-wrapper.svg)](https://jitpack.io/#upday/snowplow-java-tracker-wrapper)

### Main Technologies

- Kotlin
- Snowplow

### Architecture

!["Architecture Diagram"](./etc/snowplow-java-tracker-wrapper.png)

### Use-Case

You have to send events from a Java/kotlin Application to Snowplow. Maybe you have to do this from several services.
In order to not implement and configure the [Snowplow Java Tracker](https://github.com/snowplow/snowplow/wiki/Java-Tracker) several times you can use this wrapper instead.

### How to use

The following code showcases the usage or the wrapper in your service.
1. usage of the dispatcher.
2. example mapper

```kotlin
@Component
class SnowplowBatchEventDispatcher(
    private val trackerProperties: TrackerProperties,
    private val snowplowMapper: SnowplowMapper
) : BatchEventDispatcher {

    override fun send(batchEvent: BatchEvent) {

        val dispatcher = SnowplowEventDispatcher(
            successCallback = { successCallback -> logger.info { successCallback } },
            failureCallback = { failureCallback -> logger.info { failureCallback } },
            trackerConfiguration = TrackerConfiguration(
                nameSpace = trackerProperties.nameSpace,
                appId = trackerProperties.appId,
                collectorUrl = trackerProperties.url,
                emitterSize = trackerProperties.emitterSize,
                threadPoolSize = trackerProperties.threadpoolSize
            ),
            snowplowMapper = { _: BatchEvent -> snowplowMapper.map(batchEvent) }
        )

        dispatcher.send(batchEvent, batchEvent.batch.userId)

    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}

class SnowplowMapper(val snowplowSchemaProvider: SnowplowSchemaProvider) {

    fun map(batchEvent: BatchEvent): List<Unstructured?> =
        batchEvent.batch.events.mapNotNull {
            mapToUnstructured(buildContext(batchEvent), it)
        }

    private fun mapToUnstructured(
        context: List<SelfDescribingJson>,
        event: Event
    ): Unstructured? {
        return try {
            val timestamp = event.timestamp.toInstant().toEpochMilli()
            Unstructured.builder()
                .customContext(context)
                .eventData(createEventData(event))
                .timestamp(timestamp)
                .build()
        } catch (e: Exception) {
            logger.error("Error parsing event ${e.localizedMessage}: $event")
            Metrics.counter("failed.to.parse.event").increment()
            null
        }
    }

    private fun createEventData(event: Event): SelfDescribingJson? =
        SelfDescribingJson(
            snowplowSchemaProvider.getEventSchema(mapEventName(event)),
            mapEventAttributesToMap(event)
        )
}

```

## Contributors

Any contribution is appreciated. See the contributors list in: https://github.com/upday/snowplow-java-tracker-wrapper/graphs/contributors

### Contributing 

Pull requests are welcome.
[Show your ❤ with a ★](https://github.com/upday/snowplow-java-tracker-wrapper/stargazers)



### TODO

