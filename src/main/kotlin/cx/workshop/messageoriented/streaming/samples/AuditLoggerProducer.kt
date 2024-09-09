package cx.workshop.messageoriented.cx.workshop.messageoriented.streaming.samples

import cx.workshop.messageoriented.JacksonParser
import java.time.LocalDateTime

class AuditLoggerProducer {

}

fun main(){
    val produser = createLensesProducer()
    val event = DomainEvent("Letme",
        "1.0",
        eventClass.READ.toCEF(),
        "MedlemskapsVurdering",
        true, LocalDateTime.now(),
        "FZ12321",
        "12345678901")
    println(event.createKey())
    val event2 = DomainEvent("Letme",
        "1.0",
        eventClass.READ.toCEF(),
        "MedlemskapsVurdering",
        true, LocalDateTime.now(),
        "FZ12322",
        "12345678901")
    println(event2.createKey())
    while(true){
        sendMessageOfTypeTekst(produser,"EVENT",event.createKey(),JacksonParser().parse(event))
        sendMessageOfTypeTekst(produser,"EVENT",event2.createKey(),JacksonParser().parse(event2))
        Thread.sleep(10000)
    }


}

data class DomainEvent(val applikasjon:String,
                       val versjon:String,
                       val event:String,
                       val hva:String,
                       val loggbart:Boolean,
                       val tidspunkt:LocalDateTime,
                       val saksbehandler:String,
                       val fnr:String)

enum class eventClass{
    READ,
    WRITE
}
fun eventClass.toCEF():String{
    when (this.name){
        "READ" -> return "audit:read"
        "WRITE" -> return "audit:write"
        else -> return "unknown"
    }
}
fun DomainEvent.createKey():String{
    return "${this.saksbehandler}-${this.fnr}-${this.applikasjon}-${this.tidspunkt.year}${this.tidspunkt.month}${this.tidspunkt.dayOfMonth}-${this.hva}"
}