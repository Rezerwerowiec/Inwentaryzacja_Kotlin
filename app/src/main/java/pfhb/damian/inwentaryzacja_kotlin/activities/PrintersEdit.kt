package pfhb.damian.inwentaryzacja_kotlin.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import kotlinx.android.synthetic.main.activity_printers_edit.*
import kotlinx.android.synthetic.main.recyclerview_printers.*
import kotlinx.android.synthetic.main.recyclerview_printers.printerName
import pfhb.damian.inwentaryzacja_kotlin.FirestoreExt.Companion.fs
import pfhb.damian.inwentaryzacja_kotlin.R

class PrintersEdit : AppCompatActivity() {
    lateinit var printer_name : String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_printers_edit)
        printer_name = intent.getStringExtra("printerName").toString()

        loadData()
    }

    fun loadData(){
        printerName.text = "Nazwa: $printer_name"

        fs.getData("Inwentaryzacja_testy", ::continueLoadData)

    }
    fun continueLoadData(){

        for(item in fs.arrayResult){
            if(item.isNotEmpty()) {
                val checkBox = CheckBox(this)
                checkBox.text = item["Item"].toString()

                printer_container.addView(checkBox)
            }
        }

        fs.getData("Inwentaryzacja_drukarki", printer_name, ::continueLoadDataSetCheckboxes)
    }

    fun continueLoadDataSetCheckboxes(){
        val allCheckboxes = findViewById<View>(R.id.printer_container).touchables

        for(cb in allCheckboxes){
            var checkbox = cb as CheckBox
            val list : List<String> = fs.result["array.kompatybilne"] as List<String>
            for(s in list){
                if(checkbox.text.equals(s)) checkbox.isChecked = true
            }

        }
    }
}