package cx.workshop.messageoriented.kunde

import com.fasterxml.jackson.databind.JsonNode
import cx.workshop.messageoriented.*
import mu.KotlinLogging
import java.lang.Exception


class KundeService(val repo:MutableMap<String, JsonNode>) : IHandleRecords {
    private val logger = KotlinLogging.logger { }
    override fun handle(record: KafkaMessage) {
      when(record.topic){
         Topics.KUNDE_OPPRETTET.name -> {kundeOpprettet(record)}
         Topics.NAVN_REGISTRERT.name -> {navnRegistrert(record)}
         Topics.ORDRE_STATUS_ENDRET.name -> {ordreStatusEndret(record)}
      }
    }

    fun navnRegistrert (kafkaMessage: KafkaMessage){

    }
    fun ordreStatusEndret (kafkaMessage: KafkaMessage){
        val v:OrdreStatusEndret = JacksonParser().toDomainObject(kafkaMessage.json)
        //her vil man typisk hente ut relevant kunde indormasjon (epost, språk osv) til å skreddersy riktig kanal og tekst til bruker
        logger.info { "Informer bruker ${v.kundeID} om at ordre ${v.ordreID} har endret status til ${v.status.name}" }
    }
    fun kundeOpprettet(Kafkamessage:KafkaMessage){
      try {
          val v = JacksonParser().ToJson(Kafkamessage.json); val kundeID = KundeID(v.get("id").asText())
          repo.put(kundeID.id,v)
      }
      catch (e:Exception){
          //her logger vi og kaster meldingen.. er det så lurt?
          e.printStackTrace()
      }
  }
}

