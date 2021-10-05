package pfhb.damian.inwentaryzacja_kotlin.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import pfhb.damian.inwentaryzacja_kotlin.*
import pfhb.damian.inwentaryzacja_kotlin.FirestoreExt.Companion.fs

class PrintersActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_printers)


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
}