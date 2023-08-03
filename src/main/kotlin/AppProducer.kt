package cx.workshop.messageoriented

import cx.workshop.messageoriented.kafka.KafkaMessageProducer


class AppProducer {

}

fun main(){

    val kafkaMessageProducer = KafkaMessageProducer("client.properties","TEST-PRODUSER")
    val k = KundeID("12345678901")
    val kn = KundeNavn(k.id,"Navn Navnesen")
    val ordrestatus = OrdreStatusEndret(1,"12345678901",OrdreStatus.REGISTRERT)
    println(JacksonParser().ToJson(ordrestatus).toPrettyString())

    //kafkaMessageProducer.sendMessageOfTypeTekst(Topics.KUNDE_OPPRETTET.name,k.id,JacksonParser().ToJson(k).toPrettyString())
    //producer.sendMessageOfTypeTekst(Topics.NAVN_REGISTRERT.name,k.id,JacksonParser().ToJson(k).toPrettyString())
    //kafkaMessageProducer.sendMessageOfTypeTekst(Topics.ORDRE_STATUS_ENDRET.name,"12345678901",JacksonParser().ToJson(ordrestatus).toPrettyString())

}