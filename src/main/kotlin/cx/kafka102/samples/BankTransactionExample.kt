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
import java.time.LocalTime
import java.util.*


fun main() {
    // Configure Kafka Streams
    val props = Properties()
    props[StreamsConfig.APPLICATION_ID_CONFIG] = "kotlin-kafka-bank-balance-application"
    props[StreamsConfig.BOOTSTRAP_SERVERS_CONFIG] = "localhost:9092"
    /*
    * Add this to enable exaclly once
    props[StreamsConfig.PROCESSING_GUARANTEE_CONFIG] = StreamsConfig.EXACTLY_ONCE_V2
    */
    val builder = StreamsBuilder()

    val bankTransactionSerde = Serdes.serdeFrom(BankTransactionSerializer(), BankTransactionDeserializer())

    // Source topic: where we read the purchase events from
    val bankTransactionKStream: KStream<String, BankTransaction> =
        builder.stream("bank-transactions", Consumed.with(Serdes.String(), bankTransactionSerde))
    /*
    * if you want to debug
    * */
    bankTransactionKStream.peek { key, value ->  println("key = $key value = ${value.toString()}")}

    // Group by category field
    val groupedByBankNumber: KGroupedStream<Long, BankTransaction> = bankTransactionKStream
        .groupBy({ _, value -> value.accountNumber }, Grouped.with(Serdes.Long(), bankTransactionSerde))

    // Count the number of purchases in each category


    // Define initializer for the aggregation (start with sum = 0 and count = 0)
    val initializer: () -> BankTransaction = { BankTransaction(0,0,0, LocalTime.MIN.toString()) }
    //Define agregate function
    val countAndSumAgregator: (Long, BankTransaction, BankTransaction) -> BankTransaction = { _, event, aggregate ->
        BankTransaction(
            accountNumber = 0,
            sum = aggregate.sum + event.sum.toLong(),
            count = aggregate.count +1,
            time = LocalTime.now().toString()
        )
    }
    val countandSumStream = groupedByBankNumber.aggregate(
        initializer,
        countAndSumAgregator,
        Materialized.`as`<Long, BankTransaction, KeyValueStore<Bytes, ByteArray>>("bank-Transaction-store")
            .withKeySerde(Serdes.Long())
            .withValueSerde(BankTransactionSumSerde())
    )

    // Output the counts to a topic



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
            get("/transaction-agregates") {

                    // Query the store for the category count
                    val store: ReadOnlyKeyValueStore<Long, BankTransaction> = streams
                        .store(
                            StoreQueryParameters.fromNameAndType(
                                "bank-Transaction-store",
                                QueryableStoreTypes.keyValueStore<Long, BankTransaction>()
                            )
                        )

                val allCounts = mutableMapOf<Long, BankTransaction>()
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
