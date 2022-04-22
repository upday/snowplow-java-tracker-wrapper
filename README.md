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
With `snowplow-java-tracker` version `0.12.0`, tracker automatically retries when response code is not `2xx` and users no longer have to handle retry on failed events. 

### How to use

The following code showcases the usage or the wrapper in your service.
1. usage of the dispatcher.
2. example mapper with subject

```kotlin
@Component
class SnowplowEventDispatcher(private val snowplowMapper: SnowplowMapper) {
    
    private val dispatcher: SnowplowDispatcher = snowplowDispatcher(
        appId = "my-app-id",
        nameSpace = "app-namespace",
        collectorUrl = "http://localhost:1080"
    )

    fun send(myEvent: MyEvent) = dispatcher.send(myEvent.toSnowplowEvent())

    private fun MyEvent.toSnowplowEvent(): Event? = snowplowMapper.map(this)
}

/**
 *  Snowplow mapper example to include the userId on subject
 */
class SnowplowMapper(private val snowplowSchemaProvider: SnowplowSchemaProvider) {

    fun map(myEvent: MyEvent): Event? = mapToUnstructured(buildContext(myEvent), myEvent)

    private fun mapToUnstructured(context: List<SelfDescribingJson>, myEvent: MyEvent): Event? = 
        try {
            Unstructured.builder()
                .customContext(context)
                .eventData(createEventData(myEvent))
                .subject(Subject.SubjectBuilder().userId(myEvent.userId).build())
                .build()
        } catch (e: Exception) {
            logger.error("Error parsing event ${e.localizedMessage}: $myEvent")
            null
        }

    private fun createEventData(myEvent: MyEvent): SelfDescribingJson =
        SelfDescribingJson(
            snowplowSchemaProvider.getEventSchema(eventName = event.schemaName()),
            myEvent.attributeToMap()
        )

    private fun buildContext(myEvent: MyEvent) =
        listOf(SelfDescribingJson(
            snowplowSchemaProvider.getEnvironmentContextSchema(),
            mapOf("my_app_context_name" to myEvent.attributes.appName)
        ))
}
```

### _snowplowDispatcher_ doc
```kotlin
/**
 * @param appId snowplow application ID
 * @param nameSpace snowplow tracker name
 * @param collectorUrl snowplow URL
 * @param bufferSize Specifies how many events go into a POST, default 1
 * @param threadCount The number of Threads that can be used to send events, default 50
 * @param base64 enable base 64 encoding, default true
 */
fun snowplowDispatcher(
    appId: String,
    nameSpace: String,
    collectorUrl: String,
    bufferSize: Int = 1,
    threadCount: Int = 50,
    base64: Boolean = true
): SnowplowDispatcher
```

## Contributors

Any contribution is appreciated. See the contributors list in: https://github.com/upday/snowplow-java-tracker-wrapper/graphs/contributors

### Contributing 

Pull requests are welcome.
[Show your ❤ with a ★](https://github.com/upday/snowplow-java-tracker-wrapper/stargazers)

