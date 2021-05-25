package celik.abdullah.pagingwithnetworkanddatabase.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import celik.abdullah.pagingwithnetworkanddatabase.R
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.paging.LoadState
import androidx.recyclerview.widget.DividerItemDecoration
import celik.abdullah.pagingwithnetworkanddatabase.adapter.ItemsAdapter
import celik.abdullah.pagingwithnetworkanddatabase.adapter.ItemsLoadStateAdapter
import celik.abdullah.pagingwithnetworkanddatabase.databinding.FragmentSearchItemsBinding
import celik.abdullah.pagingwithnetworkanddatabase.viewmodel.SearchItemsFragmentViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchItemsFragment : Fragment() {

    private lateinit var binding : FragmentSearchItemsBinding
    private val searchItemsFragmentViewModel by viewModels<SearchItemsFragmentViewModel>()
    private lateinit var searchView : SearchView
    private val navController by lazy{findNavController()}

    private val args by navArgs<SearchItemsFragmentArgs>()
    private val itemsAdapter = ItemsAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchItemsBinding.inflate(inflater, container, false)

        initAdapter()
        setupScrolling()

        binding.apply {
            retryButton.setOnClickListener{ itemsAdapter.retry() }
            searchedItems.apply{
                addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
            }
        }
        search()
        setHasOptionsMenu(true)

        // inflate the layout for this fragment
        return binding.root
    }

    private fun search() =
        lifecycleScope.launch{
            searchItemsFragmentViewModel.searchItems(args.query).collectLatest {
                itemsAdapter.submitData(it)
            }
        }

    private fun initAdapter() {
        /* withLoadStateFooter : if we only want to display a footer—this should be used when your list only supports adding items at the end of the list.*/
        binding.searchedItems.adapter = itemsAdapter.withLoadStateFooter(footer = ItemsLoadStateAdapter{itemsAdapter.retry()})
        itemsAdapter.addLoadStateListener { loadState ->
            /* The list is empty when the refresh state of CombinedLoadStates is NotLoading and adapter.itemCount == 0 */
            val isListEmpty = loadState.refresh is LoadState.NotLoading && itemsAdapter.itemCount == 0
            showEmptyList(isListEmpty)

            /*
            * the list of items is visible when either the mediator's or the source's load state is NotLoading
            * otherwise, the list of items invisible. Here, I have add loadState.source here (as opposed
            * to the official Google Sample) because when you only select loadState.mediator (as it is done in the
            * official sample) then the cached data is not shown in offline mode.
            * The reason for this is that "loadState.mediator?.refresh is LoadState.Error" in offline mode and therefore,
            * the expression of "loadState.mediator?.refresh is LoadState.NotLoading" would be false which then results
            * in hiding the list although cached data is present. By also looking at loadState.source, we take
            * the cached data into account because the expression "loadState.source.refresh is LoadState.NotLoading" is true
            * in offline mode which then would result in showing the list.
            * */
            binding.searchedItems.isVisible = loadState.mediator?.refresh is LoadState.NotLoading || loadState.source.refresh is LoadState.NotLoading
            /* show the progressbar as long as the mediator's load state is Loading */
            binding.progressBar.isVisible = loadState.mediator?.refresh is LoadState.Loading
            /* show the retry button when an error is occurred and the number of items is 0. */
            binding.retryButton.isVisible = loadState.refresh is LoadState.Error && itemsAdapter.itemCount == 0

            // Toast on any error, regardless of whether it came from RemoteMediator or PagingSource
            val errorState = loadState.source.append as? LoadState.Error
                ?: loadState.source.prepend as? LoadState.Error
                ?: loadState.append as? LoadState.Error
                ?: loadState.prepend as? LoadState.Error
            errorState?.let {
                Toast.makeText(
                    requireContext(),
                    "\uD83D\uDE28 Wooops ${it.error}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    /*
    * Because we wanted to make sure that the scroll position is reset for each new search, we had:
    * binding.list.scrollToPosition(0). But, instead of resetting the position on new search, we
    * should reset the position when the list adapter is updated with the result of a new search.
    * To achieve this, we can use the PagingDataAdapter.loadStateFlow API. This Flow emits us every
    * time there's a change in the load state via a CombinedLoadStates object.
    *
    * CombinedLoadStates allows us to get the load state for the 3 different types of load operations:
    *       - CombinedLoadStates.refresh - represents the load state for loading the PagingData for the first time.
    *       - CombinedLoadStates.prepend - represents the load state for loading data at the start of the list.
    *       - CombinedLoadStates.append - represents the load state for loading data at the end of the list.
    *
    * In our case, we want to reset the scroll position only when the refresh has completed i.e LoadState is
    * refresh, NotLoading.
    * */
    private fun setupScrolling() =
        // Scroll to top when the list is refreshed from network.
        lifecycleScope.launch {
            itemsAdapter.loadStateFlow
                // Only emit when REFRESH LoadState for RemoteMediator changes.
                .distinctUntilChangedBy { it.refresh }
                // Only react to cases where Remote REFRESH completes i.e., NotLoading.
                .filter { it.refresh is LoadState.NotLoading }
                .collect { binding.searchedItems.scrollToPosition(0) }
        }


    private fun showEmptyList(show: Boolean) =
        if (show) {
            binding.emptyList.visibility = View.VISIBLE
            binding.searchedItems.visibility = View.GONE
        } else {
            binding.emptyList.visibility = View.GONE
            binding.searchedItems.visibility = View.VISIBLE
        }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {

        // add a search view to the menu
        inflater.inflate(R.menu.my_menu, menu)
        val searchItem = menu.findItem(R.id.search)
        searchView = searchItem.actionView as SearchView

        searchView.apply {
            queryHint = "Search items ..."
            isIconified = false

            /* this shows the query the user has typed (no submission) so that the user can see which search query was used */
            setQuery(args.query, false)

            setOnQueryTextListener(object : SearchView.OnQueryTextListener{
                override fun onQueryTextSubmit(query: String): Boolean {
                    // navigate to SearchItemsFragment again but with a new query
                    // that way, the fragments are stacked on top of each other so that
                    // we are simulating a "search history"; meaning that when the user navigates
                    // back he/she sees the previous search query results
                    navController.navigate(SearchItemsFragmentDirections.actionSearchItemsFragmentSelf(query))
                    return false
                }
                override fun onQueryTextChange(newText: String): Boolean = false
            })
        }
    }
}