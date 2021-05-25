package celik.abdullah.pagingwithnetworkanddatabase.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import celik.abdullah.pagingwithnetworkanddatabase.database.ItemDatabase
import celik.abdullah.pagingwithnetworkanddatabase.model.Item
import celik.abdullah.pagingwithnetworkanddatabase.network.ItemApi
import celik.abdullah.pagingwithnetworkanddatabase.remotemediator.ItemsRemoteMediator
import celik.abdullah.pagingwithnetworkanddatabase.remotemediator.SearchItemsRemoteMediator
import celik.abdullah.pagingwithnetworkanddatabase.utils.Const.NETWORK_PAGE_SIZE
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/*
* Repository class that works with local and remote data sources.
* We use the @Inject annotation to inject them into this class.
* Nothing else needs to be done since Hilt will provide the local &
* remote data source instances for us.
* */
@OptIn(ExperimentalPagingApi::class)
class ItemsRepository @Inject constructor(private val service: ItemApi,         // remote data source
                                          private val database: ItemDatabase    // local data source
) {

    /*
    * Get all items, exposed as a stream of data that will emit every time we get more data
    * from the network.
    * */
    fun getItems(): Flow<PagingData<Item>> =
        Pager(
            /*
            * PagingConfig: This class sets options regarding how to load content from a PagingSource
            *               such as how far ahead to load, the size request for the initial load, and
            *               others. The only mandatory parameter you have to define is the page size—
            *               how many items should be loaded in each page. By default, Paging will keep
            *               in memory all the pages you load. To ensure that you're not wasting memory
            *               as the user scrolls, set the maxSize parameter in PagingConfig. By default
            *               Paging will return null items as a placeholder for content that is not yet
            *               loaded if Paging can count the unloaded items and if the enablePlaceholders
            *               config flag is true. Like this, you will be able to display a placeholder
            *               view in your adapter. To simplify the work in this codelab, let's disable
            *               the placeholders by passing enablePlaceholders = false.
            * */
            config = PagingConfig(pageSize = NETWORK_PAGE_SIZE, enablePlaceholders = false),
            remoteMediator = ItemsRemoteMediator(service, database),
            /* A function that defines how to create the PagingSource */
            pagingSourceFactory = {database.itemDao().retrieveItems()}
        ).flow

    /*
    * Search items whose names match the query, exposed as a stream of data that will emit
    * every time we get more data from the network.
    * */
    fun searchItems(query:String): Flow<PagingData<Item>> =
        Pager(
            config = PagingConfig(pageSize = NETWORK_PAGE_SIZE, enablePlaceholders = false),
            remoteMediator = SearchItemsRemoteMediator(query, service, database),
            pagingSourceFactory = {database.itemDao().retrieveItemsByName("%${query.replace(' ', '%')}%")}
        ).flow

}