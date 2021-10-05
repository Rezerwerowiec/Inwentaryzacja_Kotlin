package pfhb.damian.inwentaryzacja_kotlin.activities

import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FieldValue
import pfhb.damian.inwentaryzacja_kotlin.FirestoreExt.Companion.fs
import pfhb.damian.inwentaryzacja_kotlin.StackRecyclerAdapter
import pfhb.damian.inwentaryzacja_kotlin.StackViewModel
import pfhb.damian.inwentaryzacja_kotlin.R

class StackActivity : AppCompatActivity() {
    lateinit var barcode : String
    lateinit var name : String
    var _quantity = 0
    var quantity = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stack)
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
            val data = ArrayList<StackViewModel>()
            val recyclerview = findViewById<RecyclerView>(R.id.recyclerView)
            recyclerview.layoutManager = GridLayoutManager(this, 1)
            for(i in fs.arrayResult){
                if(i.isNotEmpty()) {
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
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    fun onClickShowWindow(view: android.view.View) {
        name = view.findViewById<TextView>(R.id.itemType).text.toString()
        barcode = view.findViewById<TextView>(R.id.isEnough).text.toString()
        _quantity = view.findViewById<TextView>(R.id.itemQuantity).text.toString().toInt()
        popUp("Zmiana stanu przedmiotu", "$barcode \n" +
                "Nazwa: $name \n" +
                "Sztuki na magazynie: $_quantity \n")
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

        mPopupWindow.showAtLocation(findViewById(R.id.mainView), Gravity.CENTER, 0, 0);
        windowDragging(mView, mPopupWindow)

        val btn_plus = mView.findViewById<Button>(R.id.popup_window_plus)
        val btn_minus = mView.findViewById<Button>(R.id.popup_window_minus)
        val btn_dodaj = mView.findViewById<Button>(R.id.popup_window_dodaj)
        val text_quantity = mView.findViewById<TextView>(R.id.popup_window_quantity)

        btn_plus.setOnClickListener{
            quantity++
            if(quantity == 0) quantity = 1
            text_quantity.text = quantity.toString()
        }
        btn_minus.setOnClickListener {
            quantity--
            if(quantity == 0) quantity = -1
            text_quantity.text = quantity.toString()
        }

        btn_dodaj.setOnClickListener {


            fs.getData("Inwentaryzacja_testy", barcode, ::continueLoadData2)
            mPopupWindow.dismiss()
        }


        // Close window with button
        btn.setOnClickListener {
            mPopupWindow.dismiss()
        }
    }

    fun continueLoadData2(){
        var data = HashMap<String, Any>()
        data = fs.result as HashMap<String, Any>
        data["quantity"] = quantity + _quantity
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
