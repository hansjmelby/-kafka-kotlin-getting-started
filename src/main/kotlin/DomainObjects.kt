package cx.workshop.messageoriented

data class KafkaMessage(val partition:Int,val offset:Long,val value : String, val key:String,val topic:String,val json  :String)


data class KundeID(val id:String)

data class KundeDTO(val id:String, val navn:String?, val adresse:String?, val postnummer:String?, val kundeStatus: KundeStatus?)

data class KundeNavn(val id:String,val navn:String)

data class OrdreStatusEndret (val ordreID:Int, val kundeID:String, val status:OrdreStatus)

data class KundeStatusEndret (val kundeID:String, val status:KundeStatus?)

data class ProduktDto(val id:String,val navn:String,val pris:Double,var rabatt:Double)

enum class OrdreStatus(){
    REGISTRERT,
    SENDT,
    KANSELERT,
    UTSATT,
}

enum class KundeStatus(){
    PREMIUM,
    STANDARD,
    DODGY,
    DOD
}