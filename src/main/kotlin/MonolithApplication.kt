package cx.workshop.messageoriented
import com.fasterxml.jackson.databind.JsonNode
import cx.workshop.messageoriented.cx.workshop.messageoriented.price.PriceService
import cx.workshop.messageoriented.http.createHttpServer
import cx.workshop.messageoriented.http.healthRoutes
import cx.workshop.messageoriented.kafka.GenericKafkaConsumer
import cx.workshop.messageoriented.kunde.KundeService
import cx.workshop.messageoriented.kunde.http.KundeRoutes
import cx.workshop.messageoriented.price.http.priceRoutes
import cx.workshop.messageoriented.shipping.ShippingService
import io.ktor.server.routing.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.launchIn
import org.slf4j.Logger
import org.slf4j.LoggerFactory

fun main(){
 MonolothApplication().start()
}
class MonolothApplication() {
    companion object {
        val log: Logger = LoggerFactory.getLogger(MonolothApplication::class.java)
    }

    fun start() {
        var kundeRepo:MutableMap<String, JsonNode> = mutableMapOf<String, JsonNode>()
        var shippingRepo:MutableMap<String, JsonNode> = mutableMapOf<String, JsonNode>()
        var price_kunderepo:MutableMap<String, KundeStatus> = mutableMapOf<String, KundeStatus>()
        val produktRepo: MutableMap<String, ProduktDto> = mutableMapOf<String, ProduktDto>()
        produktRepo.put("1", ProduktDto("1","Bil",100.0,0.0))
        produktRepo.put("2",ProduktDto("2","Båt",200.0,0.0))
        produktRepo.put("3",ProduktDto("3","Fly",500.0,0.0))
        log.info("Start application")

        @OptIn(DelicateCoroutinesApi::class)
        val kundeConsumer = GenericKafkaConsumer("client.properties", listOf(Topics.KUNDE_OPPRETTET.name,Topics.ORDRE_STATUS_ENDRET.name), KundeService(kundeRepo),"kunde").flow().launchIn(GlobalScope)
        val shippingConsumer = GenericKafkaConsumer("client.properties", listOf(Topics.KUNDE_OPPRETTET.name), ShippingService(shippingRepo),"shipping").flow().launchIn(GlobalScope)
        val priceConsumer = GenericKafkaConsumer("client.properties", listOf(Topics.KUNDE_OPPRETTET.name,Topics.KUNDE_STATUS_ENDRET.name), PriceService(price_kunderepo,produktRepo),"pris").flow().launchIn(GlobalScope)
        val httpserver = createHttpServer(kundeConsumer,kundeRepo,8080)
        httpserver.application.routing {
            healthRoutes(kundeConsumer)
            KundeRoutes(kundeRepo)
            priceRoutes(price_kunderepo,produktRepo)
        }
        httpserver.start(wait = true)
        while(kundeConsumer.isActive && shippingConsumer.isActive && priceConsumer.isActive){
            Thread.sleep(5000)
        }
        log.info("application stopped")
        println(" isActive : "+kundeConsumer.isActive)
        println(" isCancelled "+kundeConsumer.isCancelled)
    }
}