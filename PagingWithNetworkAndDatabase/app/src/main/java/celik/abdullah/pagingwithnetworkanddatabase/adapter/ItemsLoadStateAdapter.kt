package celik.abdullah.pagingwithnetworkanddatabase.adapter

import android.view.ViewGroup
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import celik.abdullah.pagingwithnetworkanddatabase.viewholder.ItemsLoadStateViewHolder

/*
* In our app, we want to be able to display a footer based on the load status:
* when the list is loading, we want to show a progress spinner. In case of an error,
* we want to show the error and a retry button.
*
* The header/footer that we need to build follows the idea of a list that needs to
* be appended either at the beginning (as a header) or at the end (as a footer) of
* the actual list of items we're displaying. The header/footer is a list with only
* one element: a view that displays a progress bar or an error with a retry button,
* based on the Paging LoadState.
*
* As displaying a header/footer based on the loading state and implementing a retry
* mechanism are common tasks, the Paging 3.0 API helps us with both of these.
* For header/footer implementation we'll use a LoadStateAdapter. This implementation
* of RecyclerView.Adapter is automatically notified of changes in load state.
* It makes sure that only Loading and Error states lead to items being displayed and
* notifies the RecyclerView when an item is removed, inserted, or changed, depending
* on the LoadState.
*
* The adapter should receive the retry function as a parameter, since the retry function will be
* passed to the ViewHolder when constructed. As with any Adapter, we need to implement the onBind()
* and onCreate() methods. LoadStateAdapter makes it easier as it passes the LoadState in both of
* these functions. In onBindViewHolder(), bind your ViewHolder. In onCreateViewHolder(), define
* how to create the ReposLoadStateViewHolder based on the parent ViewGroup and the retry function
* */
class ItemsLoadStateAdapter(
    private val retry: () -> Unit
) : LoadStateAdapter<ItemsLoadStateViewHolder>() {
    override fun onBindViewHolder(holder: ItemsLoadStateViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): ItemsLoadStateViewHolder {
        return ItemsLoadStateViewHolder.create(parent, retry)
    }
}