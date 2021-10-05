package pfhb.damian.inwentaryzacja_kotlin

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Color.GREEN
import android.graphics.Color.RED
import android.util.Printer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.common.io.BaseEncoding
import kotlinx.coroutines.withContext
import pfhb.damian.inwentaryzacja_kotlin.activities.PrintersActivity
import pfhb.damian.inwentaryzacja_kotlin.activities.PrintersEdit

data class PrinterRecyclerAdapter(private val mList: List<PrintersViewModel>): RecyclerView.Adapter<PrinterRecyclerAdapter.ViewHolder>()  {
    lateinit var context : Context
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PrinterRecyclerAdapter.ViewHolder {
        context = parent.context
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recyclerview_printers, parent, false)
        return ViewHolder(view)
    }


    override fun onBindViewHolder(holder: PrinterRecyclerAdapter.ViewHolder, position: Int) {
        val data = mList[position]
        holder.printerName.text = data.printerName
        holder.clickable.setOnClickListener {
            val intent = Intent(context, PrintersEdit::class.java)
            intent.putExtra("printerName", data.printerName)
            context.startActivity(intent)
        }

    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    override fun getItemCount(): Int = mList.size


    class ViewHolder(iView : View) : RecyclerView.ViewHolder(iView){
        val image : ImageView = iView.findViewById(R.id.printerImage)
        val printerName : TextView = iView.findViewById(R.id.printerName)
        val clickable : ConstraintLayout = iView.findViewById(R.id.printerClickable)
    }
}
