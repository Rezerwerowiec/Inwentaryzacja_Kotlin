package pfhb.damian.inwentaryzacja_kotlin

data class StackViewModel(val itemType: String, val quantity: Int, val isEnough: Boolean, val barcode: String) {
}