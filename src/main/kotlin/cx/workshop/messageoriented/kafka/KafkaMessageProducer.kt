package cx.workshop.messageoriented.kafka;



import mu.KotlinLogging

import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord

import org.apache.kafka.common.serialization.StringSerializer
import java.util.*


fun loadProduceConfig(configFile:String): Properties {
    val properties = Properties()
    properties.load(ClassLoader.getSystemResourceAsStream(configFile))
    return properties

}

class KafkaMessageProducer(val file:String,val clientID:String){
    val producer = createProducer()
    private val logger = KotlinLogging.logger { }


    fun createProducer(): KafkaProducer<String, String> {
        val props = loadProduceConfig("client.properties")
        props[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        props[ProducerConfig.CLIENT_ID_CONFIG] = clientID

        return KafkaProducer(props)
    }
    fun sendMessageOfTypeTekst(topic:String,key:String,value:String){
        val futureResult = producer.send(
            ProducerRecord(
                topic,
                key, value
            )
        )
        val v = futureResult.get()
        logger.info { "message sent to topic $topic" }

    }
}