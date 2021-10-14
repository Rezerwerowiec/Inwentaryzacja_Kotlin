package pfhb.damian.inwentaryzacja_kotlin.activities

import android.app.Activity
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_printers.*
import kotlinx.android.synthetic.main.activity_printers_edit.*
import pfhb.damian.inwentaryzacja_kotlin.*
import pfhb.damian.inwentaryzacja_kotlin.FirestoreExt.Companion.fs
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager

import android.widget.TextView

import android.widget.EditText
import android.widget.TextView.OnEditorActionListener
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.doOnTextChanged
import pfhb.damian.inwentaryzacja_kotlin.activities.MainActivity.Companion.applyCustomTheme


class PrintersActivity : AppCompatActivity() {
    val printers = ArrayList<Map<String, Any>>()
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_printers)


        printer_add.setOnClickListener {
            val intent = Intent(this, PrintersEdit::class.java)
            intent.putExtra("printerName", "")
            startActivity(intent)
        }
        loadData()


        // SEARCH ENGINE BUTTONS
        search_btn_delete.setOnClickListener{
            search_tv_text.setText("")
            loadData()
            val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(this@PrintersActivity.findViewById<ConstraintLayout>(R.id.mainView).windowToken, 0)

        }   // TEXT CLEAR BUTTON
//        search_btn_search.setOnClickListener{ loadData()}//
        search_tv_text.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                //loadData()
                val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(this@PrintersActivity.findViewById<ConstraintLayout>(R.id.mainView).windowToken, 0)
                true
            } else false
        }
        search_tv_text.doOnTextChanged { _, _, _, _ -> changedText() }

        applyCustomTheme(findViewById(R.id.mainView))
    }

    private fun changedText() {
        runOnUiThread {
            val data = ArrayList<PrintersViewModel>()
            val recyclerview = findViewById<RecyclerView>(R.id.recyclerView)
            recyclerview.layoutManager = GridLayoutManager(this, 1)

            for (i in printers) {
                if (i.isNotEmpty()) {
                    if (i["docId"].toString().lowercase()
                            .contains(search_tv_text.text.toString().lowercase())
                    )
                        data.add(PrintersViewModel(i["docId"].toString(), 0))
                }
            }
            val adapter = PrinterRecyclerAdapter(data)
            recyclerview.adapter = adapter
        }
    }

    fun loadData(){
        fs.getData("Inwentaryzacja_drukarki", ::continueLoadData)
    }

    fun continueLoadData(){
        printers.clear()
        runOnUiThread {
            val data = ArrayList<PrintersViewModel>()
            val recyclerview = findViewById<RecyclerView>(R.id.recyclerView)
            recyclerview.layoutManager = GridLayoutManager(this, 1)
            for(i in fs.arrayResult){
                if(i.isNotEmpty()) {
                    printers.add(i)
                    if(i["docId"].toString().lowercase().contains(search_tv_text.text.toString().lowercase()))
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