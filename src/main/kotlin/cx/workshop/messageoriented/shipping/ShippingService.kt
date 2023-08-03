package cx.workshop.messageoriented.shipping

import com.fasterxml.jackson.databind.JsonNode
import cx.workshop.messageoriented.*
import mu.KotlinLogging
import java.lang.Exception


class ShippingService(val shipping_kunderepo:MutableMap<String, JsonNode>) : IHandleRecords {
    private val logger = KotlinLogging.logger { }
    override fun handle(record: KafkaMessage) {
      when(record.topic){
         Topics.KUNDE_OPPRETTET.name -> {kundeOpprettet(record)}
      }
    }


    fun kundeOpprettet(Kafkamessage:KafkaMessage){
      try {
          val v = JacksonParser().ToJson(Kafkamessage.json); val kundeID = KundeID(v.get("id").asText())
          shipping_kunderepo.put(kundeID.id,v)
          logger.info { "Har registrert kunde med id ${kundeID.id} som eksisterende kunde i shipping databasen"  }
      }
      catch (e:Exception){
          //her logger vi og kaster meldingen.. er det så lurt?
          e.printStackTrace()
      }

}
}

