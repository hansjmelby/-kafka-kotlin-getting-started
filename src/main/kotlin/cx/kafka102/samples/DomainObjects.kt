package cx.workshop.messageoriented.cx.kafka102.samples

data class Gadget(val color:String,val id:String,val temp:Int)

data class PurchaseEvent(val category: String, val amount: Double)

data class CountAndSum(val sum: Long, val count: Long)

data class BankTransaction(val accountNumber: Long, val sum: Long, val count: Long,val time:String)
