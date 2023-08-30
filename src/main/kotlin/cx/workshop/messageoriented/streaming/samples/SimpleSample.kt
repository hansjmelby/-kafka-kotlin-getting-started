package cx.workshop.messageoriented.cx.workshop.messageoriented.streaming.samples

import cx.workshop.messageoriented.kafka.loadConfig
import cx.workshop.messageoriented.kafka.loadLensesConfig
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.common.serialization.Serializer
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.Produced

class SimpleSample {
    fun simpleSampleStream(){
        val props = loadConfig("client.properties")
        props.put(StreamsConfig.APPLICATION_ID_CONFIG,"sample-stream")
        val  builder =  StreamsBuilder()
        builder.stream("streams_input",Consumed.with(Serdes.String(),Serdes.String()))
            .filter { key, value -> key!=null && value.contains("red") }
            .peek { key, value -> println("key:$key, value:$value") }
            .to("red", Produced.with(Serdes.String(), Serdes.String()))
        val ks = KafkaStreams(builder.build(),props)
        ks.start()
    }

    fun simpleSampleStreamWithSerdes(){
        val props = loadConfig("client.properties")
        val gadgetSerde = Serdes.serdeFrom(GadgetSerializer(), GadgetDeserializer())
        props.put(StreamsConfig.APPLICATION_ID_CONFIG,"sample-serdes-stream")
        val  builder =  StreamsBuilder()
        builder.stream("gadgets_input",Consumed.with(Serdes.String(),gadgetSerde))
            .filter { key, value -> key!=null && value.color=="red" }
            .peek { key, value -> println("key:$key, value:$value") }
            .to("gadgets_red", Produced.with(Serdes.String(), gadgetSerde))
        val ks = KafkaStreams(builder.build(),props)
        ks.start()
    }
    fun simpleSampleStreamWithSerdesLENSES(){
        val props = loadLensesConfig()
        val gadgetSerde = Serdes.serdeFrom(GadgetSerializer(), GadgetDeserializer())
        props.put(StreamsConfig.APPLICATION_ID_CONFIG,"sample-serdes-stream")
        val  builder =  StreamsBuilder()
        builder.stream("gadgets_input",Consumed.with(Serdes.String(),gadgetSerde))
            .filter { key, value -> key!=null && value.color=="red" }
            .peek { key, value -> println("key:$key, value:$value") }
            .to("gadgets_red", Produced.with(Serdes.String(), gadgetSerde))
        val ks = KafkaStreams(builder.build(),props)
        ks.start()
    }
}

fun main(){
    SimpleSample().simpleSampleStreamWithSerdesLENSES()
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
