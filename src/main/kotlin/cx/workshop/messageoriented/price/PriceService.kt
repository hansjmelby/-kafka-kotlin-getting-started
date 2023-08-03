package cx.workshop.messageoriented.cx.workshop.messageoriented.price

import com.fasterxml.jackson.databind.JsonNode
import cx.workshop.messageoriented.*
import mu.KotlinLogging
import java.lang.Exception


class PriceService(val price_kunderepo:MutableMap<String, KundeStatus>,val produkt_repo:MutableMap<String, ProduktDto>) : IHandleRecords {
    private val logger = KotlinLogging.logger { }
    override fun handle(record: KafkaMessage) {
      when(record.topic){
         Topics.KUNDE_OPPRETTET.name -> {kundeOpprettet(record)}
         Topics.KUNDE_STATUS_ENDRET.name -> {kundeStatusEndret(record)}
      }
    }


    fun kundeOpprettet(kafkaMessage:KafkaMessage){
      try {
          val v = JacksonParser().ToJson(kafkaMessage.json); val kundeID = KundeID(v.get("id").asText())
          price_kunderepo.put(kundeID.id,KundeStatus.STANDARD)
          logger.info { "Har registrert kunde med id ${kundeID.id} som eksisterende kunde i price databasen"  }
      }
      catch (e:Exception){
          //her logger vi og kaster meldingen.. er det så lurt?
          e.printStackTrace()
      }
    }
    fun kundeStatusEndret(kafkaMessage:KafkaMessage){
        try {
            val kundeStatusEndret:KundeStatusEndret = JacksonParser().toDomainObject(kafkaMessage.json)
            price_kunderepo.put(kundeStatusEndret.kundeID,kundeStatusEndret.status!!)
            logger.info { "Har registrert kunde med id ${kundeStatusEndret.kundeID} med status ${kundeStatusEndret.status} i price databasen"  }
        }
        catch (e:Exception){
            //her logger vi og kaster meldingen.. er det så lurt?
            e.printStackTrace()
        }
    }
    fun kalkulerPris(produktDto: ProduktDto,kundeStatus: KundeStatus) :Double{
        when (kundeStatus){
            KundeStatus.STANDARD ->return produktDto.pris
            KundeStatus.PREMIUM -> produktDto.pris*0.7
            KundeStatus.DODGY ->  produktDto.pris*2
            KundeStatus.DOD -> return produktDto.pris
        }
        return produktDto.pris
    }
}

