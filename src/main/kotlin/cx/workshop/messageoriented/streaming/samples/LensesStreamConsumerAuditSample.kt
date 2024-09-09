package cx.workshop.messageoriented.cx.workshop.messageoriented.streaming.samples

import cx.workshop.messageoriented.JacksonParser
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.Consumed

import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class LensesStreamConsumerAuditSample {


    fun stream() {
        val props = Properties()
        val store = mutableMapOf(Pair(String,String))
        val properties = Properties()
        properties["application.id"] = "EVENT-consumer"
        properties["bootstrap.servers"] = "127.0.0.1:9092"
        //properties["schema.registry.url"] = "http://127.0.0.1:8081"

        val builder = StreamsBuilder()

        val keys = mutableSetOf<String>()
        mDoThisJob(keys)
        builder.stream("EVENT", Consumed.with(Serdes.String(), Serdes.String()))
            .filterNot{key,value->duplicate(keys,key!!)}
            .peek { key, value -> logit(key, JacksonParser().ToJson(value).toPrettyString()) }
        val streams = KafkaStreams(builder.build(), properties)
        streams.start()

        Runtime.getRuntime().addShutdownHook(Thread(streams::close))
    }

    private fun duplicate(keys: MutableSet<String>, key: String): Boolean {
        if(keys.contains(key)){
            println("Key found. Do not log")
            return true
        }
        else{
            keys.add(key)
            println("Key not found. Do log")

        }
        return false
    }

    private fun process2() {
        TODO("Not yet implemented")
    }

    private fun logit(key: String?, value: String?) {
        println("Received Avro object with key: $key, value: $value")
    }
    private fun mDoThisJob(keys: MutableSet<String>){

        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate({
            println("Cleaning up cache!!")
            keys.clear()

        }, 1, 1, TimeUnit.MINUTES)
    }

}


fun main() {
    LensesStreamConsumerAuditSample().stream()
}