package pfhb.damian.inwentaryzacja_kotlin.activities

import android.Manifest
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.android.synthetic.main.activity_main.*
import pfhb.damian.inwentaryzacja_kotlin.FirestoreExt.Companion.fs
import pfhb.damian.inwentaryzacja_kotlin.R
import pfhb.damian.inwentaryzacja_kotlin.activities.LoginActivity.Companion.loginEmail
import pfhb.damian.inwentaryzacja_kotlin.activities.LoginActivity.Companion.loginName
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {

    var barcode = ""

    companion object{
        var userType = "Gość"
        var location = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btn_stack.setOnClickListener{
            intent = Intent(this, StackActivity::class.java)
            startActivity(intent)
        }

        // HIDE BUTTONS BEFORE LOGIN
        btn_dodaj.visibility = View.INVISIBLE
        start_menu_container.visibility = View.INVISIBLE
        ////////////////////////////
        btn_dodaj.setOnClickListener{
            requestPermissions(arrayOf(Manifest.permission.CAMERA.toString()), 100)
            val integrator = IntentIntegrator(this)
            integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)
            integrator.setPrompt("Scan Code")
            integrator.setCameraId(0)
            integrator.setBeepEnabled(true)
            integrator.setBarcodeImageEnabled(false)
            integrator.setOrientationLocked(false)
            integrator.initiateScan()
        }

        btn_drukarki.setOnClickListener {
            startActivity(Intent(this, PrintersActivity::class.java))
        }
        if(loginEmail == "" || loginEmail.isEmpty())
            startActivity(Intent(this, LoginActivity::class.java))

        takeLoginType()

        start_menu_logout.setOnClickListener {logOut()}
    }

    fun logOut(){
        val intent = Intent(this, LoginActivity::class.java)
        intent.putExtra("wyloguj", true)
        startActivity(intent)
    }

    private fun takeLoginType() {
        fs.dbPrefix = ""
        fs.getData("Inwentaryzacja_users", loginEmail, ::continueLoginType, ::continueLoginTypeFailure)

    }

    fun continueLoginTypeFailure(){
        userType = "Gość"

        val data = hashMapOf<String, Any>(
            "name" to loginName,
            "email" to loginEmail,
            "type" to userType,
            "location" to ""
        )
        fs.putData("Inwentaryzacja_users", loginEmail, data, ::continueAfterCreateUser, ::continueAfterCreateUserFailure)
    }

    private fun continueAfterCreateUserFailure() {
        Toast.makeText(baseContext, "Tworzenie nowego konta nie powiodło się, na pewno Twoje konto należy do domeny pfhb.pl?", Toast.LENGTH_LONG).show()
        logOut()
    }

    fun continueLoginType(){
        if(fs.result.isEmpty()){
            continueLoginTypeFailure()
        }
        else {
            userType = fs.result["type"].toString()
            location = fs.result["location"].toString()
            fs.dbPrefix = location
            start_menu_info.text = "Zalogowany: ${userType} \n${loginName} \n${loginEmail} \nLokacja: ${location}"
        }

        if((userType == "Administrator" || userType == "Użytkownik") && location.isNotEmpty()){
            btn_dodaj.visibility = View.VISIBLE
            start_menu_container.visibility = View.VISIBLE
        }
    }

    fun continueAfterCreateUser(){
        Toast.makeText(baseContext, "Utworzono nowe konto", Toast.LENGTH_SHORT).show()
        takeLoginType()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (intentResult != null) {
            if (intentResult.contents == null) {
                Log.d("MainActivity", "Cancelled")
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show()
            } else {
                Log.d("MainActivity", "Scanned")
                Toast.makeText(this, "Scanned: " + intentResult.contents, Toast.LENGTH_LONG).show()
                barcode = intentResult.contents.replace("/", "-")

                fs.getData("Inwentaryzacja_testy", barcode, ::continueAfterScanBarcode, ::continueAfterScanBarcodeOnFailure, null)
            }
        }
    }

    fun continueAfterScanBarcode(){
        val dataText = "$barcode \nNazwa: ${fs.result["Item"]} \nSztuki na magazynie: ${fs.result["quantity"]} \nMinimalna: ${fs.result["min"]}"
        popUp("Dodaj sztukę", dataText)
    }

    fun continueAfterScanBarcodeOnFailure(){
        popUpYesNo("Brak przedmiotu w bazie, czy chcesz utworzyć nowy?")
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
            setTextColor(Color.WHITE)
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

        var quantity = 1
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
            val data: HashMap<String, Any> = fs.result as HashMap<String, Any>
            val sum = data["quantity"].toString().toInt() + quantity
            if(sum >= 0) {
                data["quantity"] = data["quantity"].toString().toInt() + quantity
                fs.putData(
                    "Inwentaryzacja_testy",
                    barcode,
                    data,
                    ::onPutDataSuccess,
                    ::onPutDataFailure
                )
                mPopupWindow.dismiss()
            }
            else {
                Toast.makeText(baseContext, "Ilość sztuk po zmianie nie moża być niższa od 0!", Toast.LENGTH_LONG).show()
            }
        }


        // Close window with button
        btn.setOnClickListener {
            mPopupWindow.dismiss()
        }
    }

    fun popUpYesNo(_title : String){
        val mLayoutInflater : LayoutInflater = LayoutInflater.from(baseContext)
        val mView = mLayoutInflater.inflate(
            R.layout.window_pop_up_yes_no,
            null)
        mView.findViewById<TextView>(R.id.popup_window_title).text = _title


        val mPopupWindow = PopupWindow(
            mView,
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, false
        )
        windowDragging(mView, mPopupWindow)
        mPopupWindow.showAtLocation(findViewById(R.id.mainView), Gravity.CENTER, 0, 0);

        mView.findViewById<Button>(R.id.popup_window_yes).setOnClickListener {
            val intent = Intent(this, NewItemActivity::class.java)
            intent.putExtra("barcode", barcode)
            startActivity(intent)
            mPopupWindow.dismiss()
        }
        mView.findViewById<Button>(R.id.popup_window_no).setOnClickListener {
            mPopupWindow.dismiss()
        }
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

    fun onPutDataSuccess(){
        Toast.makeText(baseContext, "Successfully added data.", Toast.LENGTH_LONG).show()
    }

    fun onPutDataFailure(){
        Toast.makeText(baseContext, "Failed...", Toast.LENGTH_LONG).show()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }

}
