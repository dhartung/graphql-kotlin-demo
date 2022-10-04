package com.example.graphqlkotlindemo

import com.example.graphqlkotlindemo.helper.max
import com.example.graphqlkotlindemo.helper.mean
import com.example.graphqlkotlindemo.helper.min
import com.example.graphqlkotlindemo.helper.std
import kotlin.time.Duration
import kotlin.time.Duration.Companion.nanoseconds
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import mu.KotlinLogging
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitExchange

@Component
class LoadTest {
    private val queryA = """query Test {
            example {
                one: number
                two: number
                three: number
            } 
        }"""
        .trimIndent()
        .replace("\n", "")

    private val queryB = """query Test {
            example {
               number
               one: addition(a: 500, b: 600)
               two: addition(a: 400, b: 700)
               three: addition(a: 300, b: 900)
            } 
        }"""
        .trimIndent()
        .replace("\n", "")

    @EventListener(ApplicationReadyEvent::class)
    fun startupAsync() = GlobalScope.async {
        log.info { "Warmup phase, so we can reduce effects of the Java JIT compiler" }
        queryEndpoint(queryA, 1000)
        queryEndpoint(queryB, 1000)

        log.info { "Starting test phase" }
        val a = queryEndpoint(queryA, 1000)
        log.info { String.format("[Query A] min: %-15s max: %-15s mean: %-15s std: %-15s", a.min(), a.max(), a.mean(), a.std()) }

        val b = queryEndpoint(queryB, 1000)
        log.info { String.format("[Query B] min: %-15s max: %-15s mean: %-15s std: %-15s", b.min(), b.max(), b.mean(), b.std()) }
    }

    private suspend fun queryEndpoint(escapedQuery: String, requestCount: Int): List<Duration> = coroutineScope {
        (1..requestCount).flatMap {
            // 10 requests in parallel
            val requests = (1..10).map { async { request(escapedQuery) } }
            requests.awaitAll()
        }
    }

    private suspend fun request(escapedQuery: String): Duration {
        val before = System.nanoTime()
        WebClient.create("http://localhost:8080/graphql").post()
            .header(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .bodyValue(
                """{
                   "query": "$escapedQuery",
                   "operationName": "Test",
                   "variables": {}
                }""".trimIndent()
            )
            .awaitExchange { it.statusCode() }

        val after = System.nanoTime()
        return (after - before).nanoseconds
    }

    companion object {
        private val log = KotlinLogging.logger { }
    }
}
