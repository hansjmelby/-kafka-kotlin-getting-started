import cx.workshop.messageoriented.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class JacksonParserTest {
    @Test
    fun `mapping til request objekt for status Ja med ikke norsk statsborger`(){
        val kn = KundeNavn("1","ole olesen")
        val json = JacksonParser().ToJson(kn)
        val parsed = JacksonParser().toDomainObject<KundeNavn>(json.toPrettyString())
        Assertions.assertNotNull(parsed)
    }
    @Test
    fun `mapping `(){
        val kn = KundeNavn("1","ole olesen")
        val json = JacksonParser().ToJson(kn)
        val parsed = JacksonParser().toDomainObject<KundeNavn>(json)
        Assertions.assertNotNull(parsed)
    }
    @Test
    fun `mapping2 `(){
        val kundeID = KundeID("1234")
        val node= JacksonParser().ToJson(kundeID)
        val kundeDTO:KundeDTO = JacksonParser().toDomainObject(node)
        val kn = KundeDTO("1","ole olesen","adresse","1234",KundeStatus.STANDARD)
        if (kundeDTO.kundeStatus != KundeStatus.STANDARD){
            println("test")
        }
    }
}