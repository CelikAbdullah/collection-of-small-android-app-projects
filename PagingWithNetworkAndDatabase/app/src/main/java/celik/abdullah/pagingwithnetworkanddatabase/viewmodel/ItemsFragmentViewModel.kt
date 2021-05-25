package celik.abdullah.pagingwithnetworkanddatabase.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import celik.abdullah.pagingwithnetworkanddatabase.model.Item
import celik.abdullah.pagingwithnetworkanddatabase.repository.ItemsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/*
* ViewModel for the ItemsFragment screen.
* The ViewModel works with the ItemsRepository to get the data.
* */
@HiltViewModel
class ItemsFragmentViewModel @Inject constructor(private val itemsRepository: ItemsRepository): ViewModel() {

    /* Get all items */
    fun getItems(): Flow<PagingData<Item>> =
        itemsRepository
            /*
            * The procedure of searching items is delegated to the getItems() function in ItemsRepository.
            * The repository class has access to the remote data source (webservice -> REST API) and local data
            * source (cache -> Android Room).
            * */
            .getItems()
            /*
            * Flow<PagingData> has a handy cachedIn() method that allows us to cache the content of a
            * Flow<PagingData> in a CoroutineScope. Since we're in a ViewModel, we will use the
            * androidx.lifecycle.viewModelScope.
            * */
            .cachedIn(viewModelScope)
}