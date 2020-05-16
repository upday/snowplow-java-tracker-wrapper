# snowplow-java-tracker-wrapper

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

```kotlin
@Component
class SnowplowBatchEventDispatcher(
    private val trackerProperties: TrackerProperties,
    private val snowplowMapper: SnowplowMapper
) : BatchEventDispatcher {

    override fun send(batchEvent: BatchEvent) {

        val dispatcher = SnowplowEventDispatcher(
            successCallback = { successCallback -> Unit },
            failureCallback = { failureCallback -> Unit },
            trackerConfiguration = TrackerConfiguration(
                nameSpace = trackerProperties.nameSpace,
                appId = trackerProperties.appId,
                collectorUrl = trackerProperties.url,
                emitterSize = trackerProperties.emitterSize,
                threadPoolSize = trackerProperties.threadpoolSize
            ),
            snowplowMapper = { _: Event -> snowplowMapper.map(batchEvent).first()!! }
        )

        batchEvent.batch.events.map { event ->
            dispatcher.send(event, batchEvent.batch.userId)
        }

    }
}

```



### TODO

