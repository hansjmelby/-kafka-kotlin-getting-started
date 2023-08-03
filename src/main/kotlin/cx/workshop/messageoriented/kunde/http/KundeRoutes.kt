

package cx.workshop.messageoriented.kunde.http


import com.fasterxml.jackson.databind.JsonNode
import cx.workshop.messageoriented.*

import cx.workshop.messageoriented.kafka.KafkaMessageProducer
import io.ktor.http.*

import io.ktor.server.application.*
import io.ktor.server.plugins.callid.*

import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import mu.KotlinLogging


import java.util.*

private val logger = KotlinLogging.logger { }


val KLIENT_ID="Kunde-rest"

fun Routing.KundeRoutes(userRepo: MutableMap<String, JsonNode>) {
    route("/person") {
        get{

            val callId = call.callId ?: UUID.randomUUID().toString()
            //call.request.headers.entries().forEach{ logger.info(" ---->Header --> ${it.key} , ${it.value}")}
            val person_Id = call.request.header("Person-Id")
            if (person_Id.isNullOrBlank()){
                call.respond(HttpStatusCode.BadRequest,"Person-Id ikke funnet i headers")
            }
            if (userRepo.containsKey(person_Id)){
                val res = userRepo.get(person_Id)
                call.respond(HttpStatusCode.OK, res?.toPrettyString() ?: "")
            }

        }
        post{

            val callId = call.callId ?: UUID.randomUUID().toString()
            try{

                val kundeID = call.receive<KundeID>()
                if (userRepo.containsKey(kundeID.id)){
                    call.respond(HttpStatusCode.BadRequest,  "User already exits")
                }
                KafkaMessageProducer("client.properties",KLIENT_ID).sendMessageOfTypeTekst(
                    Topics.KUNDE_OPPRETTET.name,kundeID.id,
                    JacksonParser().ToJson(kundeID).toPrettyString())
                logger.info { "Publisert hendelse ${Topics.KUNDE_OPPRETTET.name} " }
                call.respond(HttpStatusCode.Created,  JacksonParser().ToJson(kundeID).toPrettyString())

            }
            catch (t:Throwable){
                call.respond(t.stackTrace)

            }
        }
        put{

                val callId = call.callId ?: UUID.randomUUID().toString()
            try{

                val kunde = call.receive<KundeDTO>()
                if (!userRepo.containsKey(kunde.id)){
                    call.respond(HttpStatusCode.BadRequest,  "kunde finnes ikke")
                }
                val OldValue = userRepo.get(kunde.id)
                if (OldValue!=null)
                {
                    val oldKundeObject:KundeDTO = JacksonParser().toDomainObject(OldValue)
                    if (oldKundeObject.kundeStatus != kunde.kundeStatus){
                        logger.info { "Endrer status på kunde fra ${oldKundeObject.kundeStatus} til ${kunde.kundeStatus}" }
                        KafkaMessageProducer("client.properties",KLIENT_ID).sendMessageOfTypeTekst(
                            Topics.KUNDE_STATUS_ENDRET.name,kunde.id,
                            JacksonParser().ToJson(KundeStatusEndret(kunde.id, status = kunde.kundeStatus)).toPrettyString())
                    }
                    userRepo[kunde.id] = JacksonParser().ToJson(kunde)
                }
                else{
                    call.respond(HttpStatusCode.InternalServerError,  "noe rart skjedde")
                }


                call.respond(HttpStatusCode.OK,  kunde)

            }
            catch (t:Throwable){
                call.respond(HttpStatusCode.InternalServerError,t.stackTrace)

            }
        }
    }
}
