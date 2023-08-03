import cx.workshop.messageoriented.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class JacksonParserTest {
    @Test
    fun `mapping til JsonNode og tilbake til domene objekt igjen`(){
        val kn = KundeNavn("1","ole olesen")
        val json = JacksonParser().ToJson(kn)
        val parsed = JacksonParser().toDomainObject<KundeNavn>(json.toPrettyString())
        Assertions.assertNotNull(parsed)
    }
    @Test
    fun `mapping fra kunde id til mer komplekst kundedto med test av sammenlikning `(){
        val kundeID = KundeID("1234")
        val node= JacksonParser().ToJson(kundeID)
        val kundeDTO:KundeDTO = JacksonParser().toDomainObject(node)
        val kn = KundeDTO("1","ole olesen","adresse","1234",KundeStatus.STANDARD)
        Assertions.assertTrue(kundeDTO.kundeStatus != KundeStatus.STANDARD)
    }
}