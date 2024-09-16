package cx.workshop.messageoriented.cx.kafka102.samples

data class Gadget(val color:String,val id:String,val temp:Int)

data class PurchaseEvent(val category: String, val amount: Double)