package cx.workshop.messageoriented.cx.workshop.messageoriented.streaming.samples

import io.confluent.kafka.serializers.KafkaAvroSerializer
import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde
import org.apache.avro.generic.GenericData
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.kstream.Consumed
import java.util.*

class LensesStreamConsumer {


    fun stream(){
        val props = Properties()

        val properties = Properties()
        properties["application.id"] = "avro-consumer"
        properties["bootstrap.servers"] = "127.0.0.1:9092"
        properties["schema.registry.url"] = "http://127.0.0.1:8081"

        val builder = StreamsBuilder()

        val avroSerde = GenericAvroSerde()
        avroSerde.configure(mapOf("schema.registry.url" to "http://127.0.0.1:8081"), false)

        builder.stream("telecom_italia_data", Consumed.with(avroSerde, avroSerde))
            .peek { key, value -> logit(key,value) }

        val streams = KafkaStreams(builder.build(), properties)
        streams.start()

        Runtime.getRuntime().addShutdownHook(Thread(streams::close))
    }

    private fun logit(key: GenericRecord?, value: GenericRecord?): GenericRecord? {
        println("Received Avro object with key: $key, value: $value")
        return value
    }

}

fun main(){
    LensesStreamConsumer().stream()
}