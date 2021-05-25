package celik.abdullah.pagingwithnetworkanddatabase.fragment

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.DividerItemDecoration
import celik.abdullah.pagingwithnetworkanddatabase.R
import celik.abdullah.pagingwithnetworkanddatabase.adapter.ItemsAdapter
import celik.abdullah.pagingwithnetworkanddatabase.adapter.ItemsLoadStateAdapter
import celik.abdullah.pagingwithnetworkanddatabase.databinding.FragmentItemsBinding
import celik.abdullah.pagingwithnetworkanddatabase.viewmodel.ItemsFragmentViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch


@AndroidEntryPoint
class ItemsFragment : Fragment() {
    private lateinit var binding : FragmentItemsBinding
    private val itemsFragmentViewModel by viewModels<ItemsFragmentViewModel>()
    private val itemsAdapter : ItemsAdapter = ItemsAdapter()
    private lateinit var searchView : SearchView
    private val navController by lazy{findNavController()}

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentItemsBinding.inflate(inflater, container, false)

        initAdapter()
        setupScrolling()

        binding.apply {
            retryButton.setOnClickListener{ itemsAdapter.retry() }
            items.apply {
                addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
            }
        }

        getItems()
        setHasOptionsMenu(true)

        // inflate the layout for this fragment
        return binding.root
    }

    private fun setupScrolling() =
        // Scroll to top when the list is refreshed from network.
        lifecycleScope.launch {
            itemsAdapter.loadStateFlow
                // Only emit when REFRESH LoadState for RemoteMediator changes.
                .distinctUntilChangedBy { it.refresh }
                // Only react to cases where Remote REFRESH completes i.e., NotLoading.
                .filter { it.refresh is LoadState.NotLoading }
                .collect { binding.items.scrollToPosition(0) }
        }


    private fun getItems() =
        lifecycleScope.launch{
            itemsFragmentViewModel.getItems().collectLatest {
                itemsAdapter.submitData(it)
            }
        }


    private fun initAdapter() {
        binding.items.adapter = itemsAdapter.withLoadStateFooter(footer = ItemsLoadStateAdapter{itemsAdapter.retry()})
        itemsAdapter.addLoadStateListener { loadState ->
            // show empty list
            val isListEmpty = loadState.refresh is LoadState.NotLoading && itemsAdapter.itemCount == 0
            showEmptyList(isListEmpty)
            binding.items.isVisible = loadState.mediator?.refresh is LoadState.NotLoading || loadState.source.refresh is LoadState.NotLoading
            binding.progressBar.isVisible = loadState.mediator?.refresh is LoadState.Loading
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        // add a search view to the menu
        inflater.inflate(R.menu.my_menu, menu)
        val searchItem = menu.findItem(R.id.search)
        searchView = searchItem.actionView as SearchView

        searchView.apply {
            queryHint = "Search"
            isIconified = true

            setOnQueryTextListener(object : SearchView.OnQueryTextListener{
                override fun onQueryTextSubmit(query: String): Boolean {
                    // navigate to SearchItemsFragment
                    navController.navigate(ItemsFragmentDirections.actionItemsFragmentToSearchItemsFragment(query))
                    return false
                }
                override fun onQueryTextChange(newText: String): Boolean = false
            })
        }
    }

    private fun showEmptyList(show: Boolean) =
        if (show) {
            binding.emptyList.visibility = View.VISIBLE
            binding.items.visibility = View.GONE
        } else {
            binding.emptyList.visibility = View.GONE
            binding.items.visibility = View.VISIBLE
        }
}