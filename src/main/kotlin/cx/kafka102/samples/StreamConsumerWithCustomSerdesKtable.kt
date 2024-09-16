package cx.workshop.messageoriented.cx.kafka102.samples



import cx.workshop.messageoriented.kafka.loadLensesConfig
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.kstream.*
import org.apache.kafka.streams.state.Stores



fun main(){
    val kvstore = simpleSampleStreamWithSerdesLENSESKtable()

}

fun simpleSampleStreamWithSerdesLENSESKtable() {
    val props = loadLensesConfig()
    props.put(StreamsConfig.APPLICATION_ID_CONFIG, "sample-serdes-stream")
    val builder = StreamsBuilder()
    val purchaseEventSerde = Serdes.serdeFrom(PurchaseEventSerializer(), PurchaseEventDeserializer())

    // Source topic: where we read the purchase events from
    val purchaseStream: KStream<String, PurchaseEvent> =
        builder.stream("purchase-events", Consumed.with(Serdes.String(), purchaseEventSerde))

    // Group by category field
    val groupedByCategory: KGroupedStream<String, PurchaseEvent> = purchaseStream
        .groupBy({ _, value -> value.category }, Grouped.with(Serdes.String(), purchaseEventSerde))

    // Count the number of purchases in each category
    val categoryCounts: KTable<String, Long> = groupedByCategory
        .count(Materialized.`as`<String, Long>(Stores.inMemoryKeyValueStore("category-count-store"))
            .withKeySerde(Serdes.String())
            .withValueSerde(Serdes.Long()))

    // Output the counts to a topic
    categoryCounts.toStream().to("category-counts", Produced.with(Serdes.String(), Serdes.Long()))

    // Start the Kafka Streams application
    val streams = KafkaStreams(builder.build(), props)
    streams.start()

    // Add shutdown hook to gracefully stop the application
    Runtime.getRuntime().addShutdownHook(Thread {
        streams.close()
    })
}