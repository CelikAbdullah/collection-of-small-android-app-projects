package celik.abdullah.pagingwithnetworkanddatabase.adapter

import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import celik.abdullah.pagingwithnetworkanddatabase.model.Item
import celik.abdullah.pagingwithnetworkanddatabase.viewholder.ItemViewHolder

/*
* Adapter for the list of items.
* To bind a PagingData to a RecyclerView, use a PagingDataAdapter. The PagingDataAdapter gets
* notified whenever the PagingData content is loaded and then it signals the RecyclerView to update.
* */
class ItemsAdapter : PagingDataAdapter<Item, ItemViewHolder>(ITEM_COMPARATOR) {
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = getItem(position)
        if (item != null) holder.bind(item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder =
        ItemViewHolder.create(parent)

    companion object{
        private val ITEM_COMPARATOR = object: DiffUtil.ItemCallback<Item>(){
            override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean =
                oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean =
                oldItem == newItem
        }
    }
}