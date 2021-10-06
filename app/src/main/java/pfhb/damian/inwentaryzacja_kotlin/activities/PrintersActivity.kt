package pfhb.damian.inwentaryzacja_kotlin.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_printers.*
import kotlinx.android.synthetic.main.activity_printers_edit.*
import pfhb.damian.inwentaryzacja_kotlin.*
import pfhb.damian.inwentaryzacja_kotlin.FirestoreExt.Companion.fs

class PrintersActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_printers)


        printer_add.setOnClickListener {
            startActivity(Intent(this, PrintersEdit::class.java))
        }
        loadData()
    }

    fun loadData(){
        fs.getData("Inwentaryzacja_drukarki", ::continueLoadData)
    }

    fun continueLoadData(){
        runOnUiThread {
            val data = ArrayList<PrintersViewModel>()
            val recyclerview = findViewById<RecyclerView>(R.id.recyclerView)
            recyclerview.layoutManager = GridLayoutManager(this, 1)
            for(i in fs.arrayResult){
                if(i.isNotEmpty()) {
                    data.add(PrintersViewModel(i["docId"].toString(), 0))

                }
            }

            val adapter = PrinterRecyclerAdapter(data)
            recyclerview.adapter = adapter
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this, MainActivity::class.java))
    }
}