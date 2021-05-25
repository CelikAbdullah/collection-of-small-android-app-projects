package celik.abdullah.pagingwithnetworkanddatabase.viewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import celik.abdullah.pagingwithnetworkanddatabase.R
import celik.abdullah.pagingwithnetworkanddatabase.databinding.ItemsLoadStateFooterViewItemBinding

/*
* The ViewHolder for the header/footer.
* It should receive a retry function as a parameter, to be called when the retry button is pressed.
* Create a bind() function that receives the LoadState as a parameter and sets the visibility of each
* view depending on the LoadState. An implementation of ReposLoadStateViewHolder using ViewBinding
* looks like this
* */
class ItemsLoadStateViewHolder(
    private val binding: ItemsLoadStateFooterViewItemBinding,
    retry: () -> Unit
): RecyclerView.ViewHolder(binding.root) {

    init {
        binding.retryButton.setOnClickListener{ retry.invoke()}
    }

    fun bind(loadState: LoadState) {
        if (loadState is LoadState.Error) {
            binding.errorMsg.text = loadState.error.localizedMessage
        }
        binding.progressBar.isVisible = loadState is LoadState.Loading
        binding.retryButton.isVisible = loadState is LoadState.Error
        binding.errorMsg.isVisible = loadState is LoadState.Error
    }

    companion object {
        fun create(parent: ViewGroup, retry: () -> Unit): ItemsLoadStateViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.items_load_state_footer_view_item, parent, false)
            val binding = ItemsLoadStateFooterViewItemBinding.bind(view)
            return ItemsLoadStateViewHolder(binding, retry)
        }
    }
}