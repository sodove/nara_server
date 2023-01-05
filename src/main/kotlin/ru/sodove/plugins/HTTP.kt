package ru.sodove.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.request.*
import org.slf4j.event.Level

fun Application.configureHTTP() {
    install(Compression) {
        gzip {
            priority = 1.0
        }
        deflate {
            priority = 10.0
            minimumSize(1024) // condition
        }
    }

    install(CallLogging) {
        level = Level.INFO
        format { call ->
            //status code, method, path, query
            "${call.response.status()}: ${call.request.httpMethod.value} - ${call.request.path()}?${call.request.queryString()}"
        }
    }

//    install(CachingHeaders) {
//        options { call, outgoingContent ->
//            when (outgoingContent.contentType?.withoutParameters()) {
//                ContentType("application", "json") -> CachingOptions(CacheControl.MaxAge(maxAgeSeconds = 600))
//                ContentType.Any -> CachingOptions(CacheControl.MaxAge(maxAgeSeconds = 600))
//                else -> null
//            }
//        }
//    }

}
