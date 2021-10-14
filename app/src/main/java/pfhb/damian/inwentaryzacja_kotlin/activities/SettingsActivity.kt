package pfhb.damian.inwentaryzacja_kotlin.activities

import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.RequiresApi
import dev.sasikanth.colorsheet.ColorSheet
import kotlinx.android.synthetic.main.activity_settings.*
import pfhb.damian.inwentaryzacja_kotlin.FirestoreExt.Companion.fs
import pfhb.damian.inwentaryzacja_kotlin.R
import pfhb.damian.inwentaryzacja_kotlin.activities.LoginActivity.Companion.loginEmail

class SettingsActivity : AppCompatActivity() {
    var ccolors = hashMapOf<String, Int>()
    companion object{
        var primaryColor = Color.WHITE
        var secondaryColor = Color.BLUE
        var textColor = Color.BLACK
        val colors = intArrayOf(
            Color.RED,
            Color.BLUE,
            Color.GREEN,
            Color.WHITE,
            Color.CYAN,
            Color.DKGRAY,
            Color.GRAY,
            Color.LTGRAY,
            Color.MAGENTA,
            Color.YELLOW
        )
    }
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        settings_btn_primary_color.setOnClickListener { showColorPicker("PrimaryColor") }
        settings_btn_secondary_color.setOnClickListener { showColorPicker("SecondaryColor") }
        settings_btn_primary_text_color.setOnClickListener { showColorPicker("TextColor") }
        loadData()

        MainActivity.applyCustomTheme(mainView)
    }

    private fun loadData() {
        fs.getColorData(loginEmail, ::continueLoadData)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun continueLoadData() {
        ccolors["PrimaryColor"] = fs.result["PrimaryColor"]?.toString()?.toInt() ?: Color.WHITE
        ccolors["SecondaryColor"] = fs.result["SecondaryColor"]?.toString()?.toInt() ?: Color.GRAY
        ccolors["TextColor"] = fs.result["TextColor"]?.toString()?.toInt() ?: Color.BLACK
        updateUI()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun updateUI(){
        settings_primary_color.setBackgroundColor(ccolors["PrimaryColor"] ?: Color.WHITE)
        settings_secondary_color.setBackgroundColor(ccolors["SecondaryColor"] ?: Color.GRAY)
        settings_primary_text_color.setBackgroundColor(ccolors["TextColor"] ?: Color.BLACK)
        MainActivity.applyCustomTheme(mainView)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun showColorPicker(target : String){
        ColorSheet().colorPicker(
            colors = colors,
            listener = { color ->
                ccolors[target] = color
                fs.putColorData(loginEmail, ccolors)
                updateUI()
                recreate()
            })
            .show(supportFragmentManager)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this, MainActivity::class.java))
    }
}