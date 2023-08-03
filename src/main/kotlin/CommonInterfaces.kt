package cx.workshop.messageoriented

interface IHandleRecords {
    fun handle(record: KafkaMessage)

}