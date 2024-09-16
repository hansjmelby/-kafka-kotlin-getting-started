package cx.workshop.messageoriented.cx.kafka102.samples

import cx.workshop.messageoriented.cx.workshop.messageoriented.streaming.samples.GadgetDeserializer
import cx.workshop.messageoriented.cx.workshop.messageoriented.streaming.samples.GadgetSerializer
import cx.workshop.messageoriented.cx.workshop.messageoriented.streaming.samples.SimpleSample
import cx.workshop.messageoriented.kafka.loadLensesConfig
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.Produced



fun main(){
    val kvstore = simpleSampleStreamWithSerdesLENSES()

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