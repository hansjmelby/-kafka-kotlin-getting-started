

package cx.workshop.messageoriented.price.http


import cx.workshop.messageoriented.*

import io.ktor.http.*

import io.ktor.server.application.*
import io.ktor.server.plugins.callid.*

import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import mu.KotlinLogging


import java.util.*

private val logger = KotlinLogging.logger { }


val KLIENT_ID="pris-rest"

fun Routing.priceRoutes(userRepo: MutableMap<String, KundeStatus>,produktRepo:MutableMap<String,ProduktDto>) {
    route("/pris") {
        get{

            val callId = call.callId ?: UUID.randomUUID().toString()
            val person_Id = call.request.header("Person-Id")
            val produkt_Id = call.request.header("Produkt-Id")
            var kundeStatus:KundeStatus = KundeStatus.STANDARD
            if (produkt_Id.isNullOrBlank()){
                call.respond(HttpStatusCode.BadRequest,"produkt_Id ikke funnet i headers")
            }
            if (userRepo.containsKey(person_Id)){
                kundeStatus = userRepo.get(person_Id)!!
            }
            else{
                logger.info { "Ingen kunde med kunde id $person_Id funnet i pris databasen. Defaulter til Kundestatus:  ${KundeStatus.STANDARD.name}" }
            }
            if (!produktRepo.containsKey(produkt_Id)){
                call.respond(HttpStatusCode.NotFound,  "Produkt ikke funnet")
            }

            val produkt = produktRepo.get(produkt_Id)
            if (produkt!=null){
                produkt.rabatt = kalkulerRabatt(produkt,kundeStatus)
                call.respond(HttpStatusCode.OK,  produkt)
            }
            else{
                call.respond(HttpStatusCode.NotFound,  "Produkt ikke funnet.. id finnes")
            }




        }
    }
}
fun kalkulerRabatt(produktDto: ProduktDto, kundeStatus: KundeStatus) :Double{
    when (kundeStatus){
        KundeStatus.STANDARD ->return 0.0
        KundeStatus.PREMIUM -> return produktDto.pris*0.3
        KundeStatus.DODGY ->  return produktDto.pris*-2
        KundeStatus.DOD -> return return 0.0
    }
    return 0.0
}