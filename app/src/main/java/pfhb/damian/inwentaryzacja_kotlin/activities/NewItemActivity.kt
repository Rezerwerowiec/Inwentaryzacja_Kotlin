package pfhb.damian.inwentaryzacja_kotlin.activities

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import kotlinx.android.synthetic.main.activity_new_item.*
import pfhb.damian.inwentaryzacja_kotlin.FirestoreExt.Companion.fs
import pfhb.damian.inwentaryzacja_kotlin.R
import pfhb.damian.inwentaryzacja_kotlin.activities.MainActivity.Companion.applyCustomTheme

class NewItemActivity : AppCompatActivity() {
    lateinit var barcode : String
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_item)

        barcode = intent.getStringExtra("barcode").toString()
        tv_barcode.text = barcode
        btn_zapisz.setOnClickListener { save() }
        btn_cancel.setOnClickListener { startActivity(Intent(this, MainActivity::class.java)) }

        applyCustomTheme(mainView)
    }

    private fun save() {
        var data = HashMap<String, Any>()
        with(data) {
            put("Barcode", barcode)
            put("Item", ev_nazwa.text.toString())
            put("min", ev_min.text.toString().toInt())
            put("quantity", ev_ilosc.text.toString().toInt())
        }

            fs.putData("Inwentaryzacja_testy", barcode, data, ::onSuccess, ::onFailure)
    }

    fun onSuccess(){
        Toast.makeText(baseContext, "New item: ${ev_nazwa.text} added successfully.", Toast.LENGTH_LONG).show()
        startActivity(Intent(this, MainActivity::class.java))
    }

    fun onFailure(){
        Toast.makeText(baseContext, "New item: ${ev_nazwa.text} failed on adding.", Toast.LENGTH_LONG).show()
        startActivity(Intent(this, MainActivity::class.java))
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this, MainActivity::class.java))
    }
}