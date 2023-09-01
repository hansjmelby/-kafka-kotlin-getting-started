package cx.workshop.messageoriented.cx.workshop.messageoriented.streaming.samples

import cx.workshop.messageoriented.kafka.loadConfig
import cx.workshop.messageoriented.kafka.loadLensesConfig
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.common.serialization.Serdes.StringSerde
import org.apache.kafka.common.serialization.Serializer
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.kstream.*
import java.util.*


class SimpleSample {
    fun simpleSampleStream() {
        val props = loadConfig("client.properties")
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "sample-stream")
        val builder = StreamsBuilder()
        builder.stream("streams_input", Consumed.with(Serdes.String(), Serdes.String()))
            .filter { key, value -> key != null && value.contains("red") }
            .peek { key, value -> println("key:$key, value:$value") }
            .to("red", Produced.with(Serdes.String(), Serdes.String()))
        val ks = KafkaStreams(builder.build(), props)
        ks.start()
    }

    fun simpleSampleStreamWithSerdes() {
        val props = loadConfig("client.properties")
        val gadgetSerde = Serdes.serdeFrom(GadgetSerializer(), GadgetDeserializer())
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "sample-serdes-stream")
        val builder = StreamsBuilder()
        builder.stream("gadgets_input", Consumed.with(Serdes.String(), gadgetSerde))
            .filter { key, value -> key != null && value.color == "red" }
            .peek { key, value -> println("key:$key, value:$value") }
            .to("gadgets_red", Produced.with(Serdes.String(), gadgetSerde))
        val ks = KafkaStreams(builder.build(), props)
        ks.start()
    }

    fun simpleSampleStreamWithSerdesLENSES() {
        val props = loadLensesConfig()
        val gadgetSerde = Serdes.serdeFrom(GadgetSerializer(), GadgetDeserializer())
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "sample-serdes-stream")
        val builder = StreamsBuilder()
        builder.stream("gadgets_input", Consumed.with(Serdes.String(), gadgetSerde))
            .filter { key, value -> key != null && value.color == "red" }
            .peek { key, value -> println("key:$key, value:$value") }
            .to("gadgets_red", Produced.with(Serdes.String(), gadgetSerde))

        val ks = KafkaStreams(builder.build(), props)
        ks.start()
    }
    fun simpleSampleStreamKtableWithSerdesLENSES(){
        val config = loadLensesConfig()
        config[StreamsConfig.APPLICATION_ID_CONFIG] = "wordcount-application"
        config[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"
        config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, StringSerde().javaClass);
        config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, StringSerde().javaClass);



        val builder = StreamsBuilder()

        val textLines = builder.stream<String, String>("word-count-input",Consumed.with(StringSerde(),StringSerde())).mapValues { _,value ->value.lowercase() }
           // .peek { key, value -> println("mapValues result: ${key}:${value}") }
            .flatMapValues { value -> value.split(" ") }
            //.peek { key, value -> println("flatMapValues result: ${key}:${value}") }
            .selectKey { _, value -> value }
            //.peek { key, value -> println("selectKey result: ${key}:${value}") }
            .groupByKey(Grouped.with(Serdes.String(), Serdes.String()))
            .count()
            .toStream()
            .print(Printed.toSysOut<String, Long>().withLabel("WC result: "))

        val streams = KafkaStreams(builder.build(), config)
        streams.start()

        }

}
fun main(){
    val kvstore = SimpleSample().simpleSampleStreamKtableWithSerdesLENSES()

}



class GadgetSerializer : Serializer<Gadget> {
    override fun serialize(topic: String?, data: Gadget?): ByteArray? {
        if (data == null) return null
        return jsonMapper.writeValueAsBytes(data)
    }

}

class GadgetDeserializer : Deserializer<Gadget> {
    override fun deserialize(topic: String?, data: ByteArray?): Gadget? {
        if (data == null) return null
        return jsonMapper.readValue(data, Gadget::class.java)
    }

}

data class Gadget(val color:String,val id:String,val temp:Int)
