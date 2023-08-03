package cx.workshop.messageoriented.kunde
import com.fasterxml.jackson.databind.JsonNode
import cx.workshop.messageoriented.Topics
import cx.workshop.messageoriented.http.createHttpServer
import cx.workshop.messageoriented.http.healthRoutes
import cx.workshop.messageoriented.kafka.GenericKafkaConsumer
import cx.workshop.messageoriented.kunde.http.KundeRoutes
import io.ktor.server.routing.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.launchIn
import org.slf4j.Logger
import org.slf4j.LoggerFactory

fun main(){
 KundeApplication().start()
}
class KundeApplication() {
    companion object {
        val log: Logger = LoggerFactory.getLogger(KundeApplication::class.java)
    }

    fun start() {
        var kundeRepo:MutableMap<String, JsonNode> = mutableMapOf<String, JsonNode>()
        log.info("Start application")

        @OptIn(DelicateCoroutinesApi::class)
        val consumeJob = GenericKafkaConsumer("client.properties", listOf(Topics.KUNDE_OPPRETTET.name), KundeService(kundeRepo),"kunde").flow().launchIn(GlobalScope)
        val httpserver = createHttpServer(consumeJob,kundeRepo,8080)
        httpserver.application.routing {
            healthRoutes(consumeJob)
            KundeRoutes(kundeRepo)
        }
        httpserver.start(wait = true)
        while(consumeJob.isActive){
            Thread.sleep(5000)
        }
        log.info("application stopped")
        println(" isActive : "+consumeJob.isActive)
        println(" isCancelled "+consumeJob.isCancelled)
    }
}