package pfhb.damian.inwentaryzacja_kotlin

import android.graphics.Color
import android.graphics.Color.GREEN
import android.graphics.Color.RED
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class LogsRecyclerAdapter(private val mList: List<LogsViewModel>): RecyclerView.Adapter<LogsRecyclerAdapter.ViewHolder>()  {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LogsRecyclerAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recyclerview_item_row, parent, false)
        return ViewHolder(view)
    }


    override fun onBindViewHolder(holder: LogsRecyclerAdapter.ViewHolder, position: Int) {
        val TextViewModel = mList[position]
        holder.textItemType.text = TextViewModel.itemType
        holder.textQuantity.text = "${TextViewModel.quantity.toString()} szt."
        if(TextViewModel.isEnough)
            holder.isEnough.setBackgroundColor(GREEN)
        else holder.isEnough.setBackgroundColor(RED)

    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    override fun getItemCount(): Int = mList.size


    class ViewHolder(iView : View) : RecyclerView.ViewHolder(iView){
        val textItemType : TextView = iView.findViewById(R.id.itemType)
        val textQuantity : TextView = iView.findViewById(R.id.itemQuantity)
        val isEnough : TextView = iView.findViewById(R.id.isEnough)
    }
}
