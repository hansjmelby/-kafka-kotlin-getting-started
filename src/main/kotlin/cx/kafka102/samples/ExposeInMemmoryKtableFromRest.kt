package cx.workshop.messageoriented.cx.kafka102.samples

import cx.workshop.messageoriented.http.objectMapper
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.common.utils.Bytes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StoreQueryParameters
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.kstream.*
import org.apache.kafka.streams.state.KeyValueStore
import org.apache.kafka.streams.state.QueryableStoreTypes
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore
import org.apache.kafka.streams.state.Stores
import java.time.Duration
import java.util.*


fun main() {
    // Configure Kafka Streams
    val props = Properties()
    props[StreamsConfig.APPLICATION_ID_CONFIG] = "kotlin-kafka-streams-example"
    props[StreamsConfig.BOOTSTRAP_SERVERS_CONFIG] = "localhost:9092"
    props[StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG] ="org.apache.kafka.common.serialization.Serdes\$StringSerde"
    props[StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG] = "org.apache.kafka.common.serialization.Serdes\$StringSerde"
    /*
    * Add this to enable exaclly once
    props[StreamsConfig.PROCESSING_GUARANTEE_CONFIG] = StreamsConfig.EXACTLY_ONCE_V2
    */
    val builder = StreamsBuilder()

    val purchaseEventSerde = Serdes.serdeFrom(PurchaseEventSerializer(), PurchaseEventDeserializer())

    // Source topic: where we read the purchase events from
    val purchaseStream: KStream<String, PurchaseEvent> =
        builder.stream("purchase-events", Consumed.with(Serdes.String(), purchaseEventSerde))
    /*
    * if you want to debug
    * */
    purchaseStream.peek { key, value ->  println("key = $key value = ${value.toString()}")}

    // Group by category field
    val groupedByCategory: KGroupedStream<String, PurchaseEvent> = purchaseStream
        .groupBy({ _, value -> value.category }, Grouped.with(Serdes.String(), purchaseEventSerde))

    // Count the number of purchases in each category
    val categoryCounts: KTable<String, Long> = groupedByCategory
        .count(
            Materialized.`as`<String, Long>(Stores.inMemoryKeyValueStore("category-count-store"))
            .withKeySerde(Serdes.String())
            .withValueSerde(Serdes.Long()))


    // Define initializer for the aggregation (start with sum = 0 and count = 0)
    val initializer: () -> CountAndSum = { CountAndSum(0, 0) }
    //Define agregate function
    val countAndSumAgregator: (String, PurchaseEvent, CountAndSum) -> CountAndSum = { _, event, aggregate ->
        CountAndSum(
            sum = aggregate.sum + event.amount.toLong(),
            count = aggregate.count + 1
        )
    }
    val countandSumStream = groupedByCategory.aggregate(
        initializer,
        countAndSumAgregator,
        Materialized.`as`<String, CountAndSum, KeyValueStore<Bytes, ByteArray>>("purchase-aggregates-store")
            .withKeySerde(Serdes.String())
            .withValueSerde(CountAndSumSerde())
    )

    // Output the counts to a topic
    categoryCounts.toStream().to("category-counts", Produced.with(Serdes.String(), Serdes.Long()))


    // Start Kafka Streams
    val streams = KafkaStreams(builder.build(), props)
    streams.start()

    // Add shutdown hook
    Runtime.getRuntime().addShutdownHook(
        Thread {
        streams.close()
    })

    // Start Ktor server to expose API
    embeddedServer(Netty, port = 8080) {
        install(ContentNegotiation) {
            register(ContentType.Application.Json, JacksonConverter(objectMapper))
        }
        install(CallLogging)

        routing {
            // Querying the Kafka Streams state store
            get("/category-count/{category}") {
                val category = call.parameters["category"]
                if (category == null) {
                    call.respond(HttpStatusCode.BadRequest,"Category is missing")
                } else {
                    // Query the store for the category count
                    val store: ReadOnlyKeyValueStore<String, Long> = streams
                        .store(
                            StoreQueryParameters.fromNameAndType(
                                "category-count-store",
                                QueryableStoreTypes.keyValueStore<String, Long>()
                            )
                        )

                    val count = store[category]
                    if (count != null) {
                        call.respond(mapOf("category" to category, "count" to count))
                    } else {
                        call.respond(HttpStatusCode.NotFound,"Category not found")
                    }
                }
            }
            get("/category-agregates") {

                    // Query the store for the category count
                    val store: ReadOnlyKeyValueStore<String, CountAndSum> = streams
                        .store(
                            StoreQueryParameters.fromNameAndType(
                                "purchase-aggregates-store",
                                QueryableStoreTypes.keyValueStore<String, CountAndSum>()
                            )
                        )

                val allCounts = mutableMapOf<String, CountAndSum>()
                store.all().use { iterator ->
                    while (iterator.hasNext()) {
                        val entry = iterator.next()
                        allCounts[entry.key] = entry.value
                    }
                }
                call.respond(allCounts)
                }


            // List all categories and counts
            get("/category-counts") {
                val store: ReadOnlyKeyValueStore<String, Long> = streams
                    .store(
                        StoreQueryParameters.fromNameAndType(
                            "category-count-store",
                            QueryableStoreTypes.keyValueStore<String, Long>()
                        )
                    )

                val allCounts = mutableMapOf<String, Long>()
                store.all().use { iterator ->
                    while (iterator.hasNext()) {
                        val entry = iterator.next()
                        allCounts[entry.key] = entry.value
                    }
                }
                call.respond(allCounts)
            }
        }
    }.start(wait = true)
}
