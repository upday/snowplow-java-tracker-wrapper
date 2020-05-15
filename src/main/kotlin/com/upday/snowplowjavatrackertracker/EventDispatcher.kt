package com.upday.snowplowjavatrackertracker

import com.snowplowanalytics.snowplow.tracker.payload.TrackerPayload

interface EventDispatcher <T> {

    fun send(failedEvents: List<TrackerPayload>)

    fun send(event: T, userId: String)
}