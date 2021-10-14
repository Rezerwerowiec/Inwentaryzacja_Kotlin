package pfhb.damian.inwentaryzacja_kotlin.activities

import android.app.Activity
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_printers.*
import kotlinx.android.synthetic.main.activity_printers.search_btn_delete
import kotlinx.android.synthetic.main.activity_printers.search_tv_text
import kotlinx.android.synthetic.main.activity_stack.*
import pfhb.damian.inwentaryzacja_kotlin.FirestoreExt.Companion.fs
import pfhb.damian.inwentaryzacja_kotlin.R
import pfhb.damian.inwentaryzacja_kotlin.StackRecyclerAdapter
import pfhb.damian.inwentaryzacja_kotlin.StackViewModel
import java.io.File
import java.util.*
import kotlin.collections.ArrayList
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider


class StackActivity : AppCompatActivity() {
    lateinit var barcode : String
    lateinit var name : String
    var sortBy = "quantity"
    var quantity = 0
    val items = ArrayList<Map<String, Any>>()


    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stack)

        // SEARCH ENGINE BUTTONS
        search_btn_delete.setOnClickListener{
            search_tv_text.setText("")
            loadData()
            val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(this@StackActivity.findViewById<ConstraintLayout>(R.id.mainView).windowToken, 0)

        }   // TEXT CLEAR BUTTON
        search_btn_search.setOnClickListener{
            if(sortBy == "quantity"){
                sortBy = "Item"
                loadData()
                search_btn_search.setImageResource(R.drawable.sort_a_z)
            }
            else {
                sortBy = "quantity"
                loadData()
                search_btn_search.setImageResource(R.drawable.sort_1_9)
            }
        }
        search_tv_text.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                //loadData()
                val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(this@StackActivity.findViewById<ConstraintLayout>(R.id.mainView).windowToken, 0)
                true
            } else false
        }
        search_tv_text.doOnTextChanged { _, _, _, _ -> changedText() }
        setSupportActionBar(findViewById(R.id.my_toolbar))
        my_toolbar.title = "STAN MAGAZYNOWY"

        MainActivity.applyCustomTheme(findViewById(R.id.mainView))
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        menuInflater.inflate(R.menu.send_email, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId){
        R.id.action_send_email -> {
            sendEmail()
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
            Toast.makeText(baseContext, "Uruchamiam skrypt mailowy...", Toast.LENGTH_SHORT).show()
            false
        }
    }

    private fun sendEmail() {
        Log.d(TAG, "ACTION BAR?")
//        if(items.isEmpty()) return
        val builder = VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())
        var emailText = "AUTOMATYCZNIE WYGENEROWANA WIADOMOSC"
        var csvTextBraki = "Nazwa,Kod Kreskowy,Ilosc,Minimum"
        var csvTextWszystko = "Nazwa,Kod Kreskowy,Ilosc,Minimum"

        val path = baseContext.filesDir
        val fileBraki = File( path,"raport_braki.csv")
        val fileWszystko = File( path, "raport_wszystko.csv")
        for(item in items){
            val calc = item["quantity"].toString().toInt() - item["min"].toString().toInt()
            if(calc < 0){
                emailText += "\n----------------------------------" +
                        "\nNazwa: ${item["Item"]}  |  ${item["Barcode"]}" +
                        "\nIlość: ${item["quantity"]}   |  Brakuje: ${calc*(-1)} sztuk."
                csvTextBraki +="\n${item["Item"]},${item["Barcode"]},${item["quantity"]},${item["min"]}"


                fileBraki.writeText(csvTextBraki, charset("windows-1250"))
            }
            csvTextWszystko +="\n${item["Item"]},${item["Barcode"]},${item["quantity"]},${item["min"]}"
            fileWszystko.writeText(csvTextWszystko, charset("windows-1250"))

        }
        val mIntent = Intent(Intent.ACTION_SEND_MULTIPLE)
        mIntent.data = Uri.parse("mailto:")
        mIntent.type = "message/rfc822"
        mIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf("d.piszka@pfhb.pl"))
        mIntent.putExtra(Intent.EXTRA_SUBJECT, "Inwentaryzacja_raport_brakujace")
        mIntent.putExtra(Intent.EXTRA_TEXT, emailText)
        val fileURI = FileProvider.getUriForFile(baseContext, "$packageName.fileprovider", fileBraki)
        val fileURIWszystko = FileProvider.getUriForFile(baseContext, "$packageName.fileprovider", fileWszystko)
        val uris = ArrayList<Uri>()
        uris.add(fileURI)
        uris.add(fileURIWszystko)
        mIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);


        try {
            //start email intent
            startActivity(Intent.createChooser(mIntent, "Choose Email Client..."))
        }
        catch (e: Exception){
            //if any thing goes wrong for example no email client application or any exception
            //get and show exception message
            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
        }

    }

    private fun changedText() {
        runOnUiThread {
            val data = ArrayList<StackViewModel>()
            val recyclerview = findViewById<RecyclerView>(R.id.recyclerView)

            recyclerview.layoutManager = GridLayoutManager(this, 1)
            for(i in items){
                if(i.isNotEmpty()) {
                    if(i["Item"].toString().lowercase()
                            .contains(search_tv_text.text.toString().lowercase())) {
                        val isEnough =
                            i["quantity"].toString().toInt() >= i["min"].toString().toInt()
                        data.add(
                            StackViewModel(
                                i["Item"].toString(),
                                i["quantity"].toString().toInt(),
                                isEnough,
                                i["Barcode"].toString()
                            )
                        )
                    }
                }
            }


            val adapter = StackRecyclerAdapter(data)
            recyclerview.adapter = adapter
        }
    }

    override fun onResume() {
        super.onResume()
        loadData()

    }

    private fun loadData() {
        fs.getData("Inwentaryzacja_testy", ::continueLoadData, ::failedLoadData, sortBy)
    }

    fun failedLoadData(){
        items.clear()
        Toast.makeText(baseContext, "Failed load data, check internet connection!", Toast.LENGTH_LONG).show()
        startActivity(Intent(this, MainActivity::class.java))
    }

    private fun continueLoadData(){
        items.clear()
        Log.d(ContentValues.TAG, "RESULT: AFTER ${fs.arrayResult}")

        runOnUiThread {
            val data = ArrayList<StackViewModel>()
            val recyclerview = findViewById<RecyclerView>(R.id.recyclerView)

            recyclerview.layoutManager = GridLayoutManager(this, 1)
            for(i in fs.arrayResult){
                if(i.isNotEmpty()) {
                    items.add(i)
                    val isEnough = i["quantity"].toString().toInt() >= i["min"].toString().toInt()
                    data.add(StackViewModel(i["Item"].toString(), i["quantity"].toString().toInt(), isEnough, i["Barcode"].toString()))
                }
            }


            val adapter = StackRecyclerAdapter(data)
            recyclerview.adapter = adapter
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(baseContext, MainActivity::class.java)
        startActivity(intent)
    }

    fun onClickShowWindow(view: View) {
        name = view.findViewById<TextView>(R.id.itemType).text.toString()
        barcode = view.findViewById<TextView>(R.id.isEnough).text.toString()
        quantity = view.findViewById<TextView>(R.id.itemQuantity).text.toString().toInt()
        popUp("Zmiana stanu przedmiotu", "$barcode \n" +
                "Nazwa: $name \n" +
                "Sztuki na magazynie: $quantity \n")
    }

    fun popUp(_title : String, _text : String ){
        val mLayoutInflater : LayoutInflater = LayoutInflater.from(baseContext)
        val mView = mLayoutInflater.inflate(
            R.layout.main_acitivity_pop_up_quick,
            null)
        mView.findViewById<TextView>(R.id.popup_window_title).text = _title
        val llView = mView.findViewById<LinearLayout>(R.id.window_ll_desc)

        val newTextView = TextView(this)
        with(newTextView) {
            text = _text
            setTextColor(Color.WHITE)
            textSize = 21F
            textAlignment = View.TEXT_ALIGNMENT_CENTER
        }

        val btn = mView.findViewById<Button>(R.id.popup_window_button)
        btn.text = "x"
        llView.addView(newTextView)

        val mPopupWindow = PopupWindow(
            mView,
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, false
        )

        mPopupWindow.showAtLocation(findViewById(R.id.mainView), Gravity.CENTER, 0, 0)
        windowDragging(mView, mPopupWindow)
        val btn_plus = mView.findViewById<Button>(R.id.popup_window_plus)
        val btn_minus = mView.findViewById<Button>(R.id.popup_window_minus)
        val btn_dodaj = mView.findViewById<Button>(R.id.popup_window_dodaj)
        val text_quantity = mView.findViewById<TextView>(R.id.popup_window_quantity)

        text_quantity.text = quantity.toString()
        var quantity = this.quantity
        btn_plus.setOnClickListener{
            quantity++
            text_quantity.text = quantity.toString()
        }
        btn_minus.setOnClickListener {
            if(quantity < 0) return@setOnClickListener
            quantity--
            if(quantity == 0) quantity = -1
            text_quantity.text = quantity.toString()
        }

        btn_dodaj.setOnClickListener {
            this.quantity = quantity
            fs.getData("Inwentaryzacja_testy", barcode, ::continueLoadData2)
            mPopupWindow.dismiss()
        }


        // Close window with button
        btn.setOnClickListener {
            mPopupWindow.dismiss()
        }
    }

    fun continueLoadData2(){
        val data: HashMap<String, Any> = fs.result as HashMap<String, Any>
        data["quantity"] = quantity
            fs.putData("Inwentaryzacja_testy", barcode, data, ::onPutDataSuccess, ::onPutDataFailure)

    }

    fun onPutDataSuccess(){
        Toast.makeText(baseContext, "Successfull", Toast.LENGTH_LONG).show()
        startActivity(Intent(this, this@StackActivity::class.java))
    }

    fun onPutDataFailure(){
        Toast.makeText(baseContext, "Failed", Toast.LENGTH_LONG).show()
    }

    fun windowDragging(mView : View, mPopupWindow:PopupWindow){
        mView.setOnTouchListener(object : View.OnTouchListener {
            private var dx = 0
            private var dy = 0
            private var ox = 0
            private var oy = 0
            override fun onTouch(view: View?, motionEvent: MotionEvent): Boolean {
                when (motionEvent.action) {
                    MotionEvent.ACTION_DOWN -> {
                        dx +=  motionEvent.rawX.toInt() -ox
                        dy +=  motionEvent.rawY.toInt() -oy
                    }
                    MotionEvent.ACTION_UP -> {
                        ox = motionEvent.rawX.toInt()
                        oy = motionEvent.rawY.toInt()
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val x = motionEvent.rawX.toInt()
                        val y = motionEvent.rawY.toInt()
                        val left = x - dx
                        val top = y - dy
                        mPopupWindow.update(left, top, -1, -1, true)
                    }
                }
                return true
            }
        })
    }
}
