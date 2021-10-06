package pfhb.damian.inwentaryzacja_kotlin.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_printers_edit.*
import kotlinx.android.synthetic.main.recyclerview_printers.*
import kotlinx.android.synthetic.main.recyclerview_printers.printerName
import pfhb.damian.inwentaryzacja_kotlin.FirestoreExt.Companion.fs
import pfhb.damian.inwentaryzacja_kotlin.R
import java.util.ArrayList
import java.util.HashMap

class PrintersEdit : AppCompatActivity() {
    lateinit var printer_name : String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_printers_edit)
        printer_name = intent.getStringExtra("printerName").toString()

        btn_save.setOnClickListener { saveData() }
        loadData()
    }

    private fun saveData() {
        val allCheckboxes: ArrayList<View> = findViewById<View>(R.id.printer_container).touchables
        val ls: ArrayList<String> = ArrayList<String>()
        for (v in allCheckboxes) {
            val cb: CheckBox = v as CheckBox
            if (cb.isChecked) {
                ls.add(cb.text.toString())
            }
        }
        val data: HashMap<String, List<String>> = HashMap()
        data["array.kompatybilne"] = ls
        fs.putListedData("Inwentaryzacja_drukarki", printer_name, data, ::onSuccessSaveData, ::onFailureSaveData)

    }

    private fun onFailureSaveData() {
        Toast.makeText(baseContext, "Failed...", Toast.LENGTH_LONG).show()
    }

    private fun onSuccessSaveData() {
        Toast.makeText(baseContext, "Successfully changed data", Toast.LENGTH_LONG).show()
        startActivity(Intent(this, PrintersActivity::class.java))
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

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this, PrintersActivity::class.java))
    }
}