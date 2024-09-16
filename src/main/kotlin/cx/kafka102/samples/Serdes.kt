package cx.workshop.messageoriented.cx.kafka102.samples

import cx.workshop.messageoriented.cx.workshop.messageoriented.streaming.samples.Gadget
import cx.workshop.messageoriented.cx.workshop.messageoriented.streaming.samples.jsonMapper
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.Serializer

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


// Custom Serializer and Deserializer for PurchaseEvent (for simplicity)
class PurchaseEventSerializer : org.apache.kafka.common.serialization.Serializer<PurchaseEvent> {
    override fun serialize(topic: String?, data: PurchaseEvent?): ByteArray? {
        return data?.let { "${it.category},${it.amount}".toByteArray() }
    }
}

class PurchaseEventDeserializer : org.apache.kafka.common.serialization.Deserializer<PurchaseEvent> {
    override fun deserialize(topic: String?, data: ByteArray?): PurchaseEvent? {
        return data?.let {
            val fields = String(it).split(",")
            PurchaseEvent(fields[0], fields[1].toDouble())
        }
    }
}