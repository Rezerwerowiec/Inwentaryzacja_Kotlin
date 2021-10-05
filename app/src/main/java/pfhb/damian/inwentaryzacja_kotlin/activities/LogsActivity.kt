package pfhb.damian.inwentaryzacja_kotlin.activities

import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import pfhb.damian.inwentaryzacja_kotlin.FirestoreExt.Companion.fs
import pfhb.damian.inwentaryzacja_kotlin.LogsRecyclerAdapter
import pfhb.damian.inwentaryzacja_kotlin.LogsViewModel
import pfhb.damian.inwentaryzacja_kotlin.R

class LogsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logs)
    }

    override fun onResume() {
        super.onResume()
        loadData()

    }

    private fun loadData() {
        fs.getData("Inwentaryzacja_testy", ::continueLoadData)
    }

    private fun continueLoadData(){

        Log.d(ContentValues.TAG, "RESULT: AFTER ${fs.arrayResult}")

        runOnUiThread {
            val data = ArrayList<LogsViewModel>()
            val recyclerview = findViewById<RecyclerView>(R.id.recyclerView)
            recyclerview.layoutManager = GridLayoutManager(this, 1)
            for(i in fs.arrayResult){
                if(i.isNotEmpty()) {
                    val isEnough = i["quantity"].toString().toInt() > i["min"].toString().toInt()
                    data.add(LogsViewModel(i["Item"].toString(), i["quantity"].toString().toInt(), isEnough))

                }
            }

            val adapter = LogsRecyclerAdapter(data)
            recyclerview.adapter = adapter
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(baseContext, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}
