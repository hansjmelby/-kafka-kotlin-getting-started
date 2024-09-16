package cx.workshop.messageoriented.cx.kafka102.samples

import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import java.util.*

class Produser {
}

fun main(){
    val produser = createLensesProducer()
    val produser2 = createLensesProducerPurchaseEvent()
    while (true){
        val gadget =Gadget(color = picColor(),id= (1..10).random().toString(),temp=(10..30).random(),)
        val purchaseEvent = PurchaseEvent(category = picColor(), amount = (1..100).random().toDouble())
        sendMessageOfTypePurchaseEvent(produser2,"purchase-events",UUID.randomUUID().toString(),purchaseEvent)
        //sendMessageOfTypeTekst(produser,"gadgets_input", gadget.id, objectMapper.writeValueAsString(gadget))
        //sendMessageOfTypeTekst(produser,"word-count-input", gadget.id, "One Two Three")
        //sendMessageOfTypeTekst(produser,"word-count-input", gadget.id, "One Blind mouse")
        println("event created")
        Thread.sleep(1000)
    }


}

fun picColor():String {
    val rnds = (0..10).random()
    when (rnds) {
        1 -> return "red"
        2 -> return "blue"
        3 -> return "green"
        4 -> return "black"
        5 -> return "yellow"
        6 -> return "pink"
        7 -> return "brown"
        8 -> return "purple"
        9 -> return "white"
        10 -> return "orange"
        else -> return "unknown"
    }
}
fun createLensesProducer(): KafkaProducer<String, String> {
    val props = Properties()
    props[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = "127.0.0.1:9092"
    props[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
    props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
    props[ProducerConfig.CLIENT_ID_CONFIG] = "SerdesProducer"
    return KafkaProducer(props)
}
fun createLensesProducerPurchaseEvent(): KafkaProducer<String, PurchaseEvent> {
    val props = Properties()
    props[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = "127.0.0.1:9092"
    props[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
    props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = PurchaseEventSerializer::class.java
    props[ProducerConfig.CLIENT_ID_CONFIG] = "SerdesProducer"
    return KafkaProducer(props)
}
fun sendMessageOfTypeTekst(producer:KafkaProducer<String,String>,topic:String,key:String,value:String){
    val futureResult = producer.send(
        ProducerRecord(
            topic,
            key, value
        )
    )
    val v = futureResult.get()


}
fun sendMessageOfTypePurchaseEvent(producer:KafkaProducer<String,PurchaseEvent>, topic:String, key:String, value:PurchaseEvent){
    val futureResult = producer.send(
        ProducerRecord(
            topic,
            key, value
        )
    )
    val v = futureResult.get()


}