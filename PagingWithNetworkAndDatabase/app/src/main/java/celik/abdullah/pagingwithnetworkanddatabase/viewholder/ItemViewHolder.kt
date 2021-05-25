package celik.abdullah.pagingwithnetworkanddatabase.viewholder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import celik.abdullah.pagingwithnetworkanddatabase.R
import celik.abdullah.pagingwithnetworkanddatabase.model.Item

class ItemViewHolder(view: View): RecyclerView.ViewHolder(view) {
    private val name: TextView = view.findViewById(R.id.item_name)

    private var item: Item? = null

    fun bind(item:Item?){
        if(item == null) name.text = itemView.resources.getString(R.string.loading)
        else showItemData(item)
    }

    private fun showItemData(item: Item) {
        this.item = item
        name.text = item.name
    }

    companion object{
        fun create(parent: ViewGroup): ItemViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_view, parent, false)
            return ItemViewHolder(view)
        }
    }
}