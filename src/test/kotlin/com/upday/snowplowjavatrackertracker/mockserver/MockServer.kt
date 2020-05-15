package com.upday.snowplowjavatrackertracker.mockserver

import org.mockserver.client.server.MockServerClient
import org.mockserver.model.HttpRequest.request
import org.mockserver.model.HttpResponse.response

class MockServer {

    fun setupSnowplowOk() {
        MockServerClient("localhost", 1080)
            .`when`(
                request()
                    .withMethod("POST")
                    .withPath("/com.snowplowanalytics.snowplow/tp2")
            ).respond(
                response()
                    .withStatusCode(200)
            )
    }
}