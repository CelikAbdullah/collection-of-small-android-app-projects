package celik.abdullah.pagingwithnetworkanddatabase.remotemediator

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import celik.abdullah.pagingwithnetworkanddatabase.database.ItemDatabase
import celik.abdullah.pagingwithnetworkanddatabase.database.RemoteKey
import celik.abdullah.pagingwithnetworkanddatabase.model.Item
import celik.abdullah.pagingwithnetworkanddatabase.network.ItemApi
import celik.abdullah.pagingwithnetworkanddatabase.utils.Const.STARTING_PAGE_INDEX
import retrofit2.HttpException
import java.io.IOException

/*
* The Paging library uses the database as a source of truth for the data that needs to be displayed
* in the UI. Whenever we don't have any more data in the database, we need to request more from
* the network.
* To help with this, Paging 3.0 defines the RemoteMediator abstract class, with one method that needs
* to be implemented: load(). This method will be called whenever we need to load more data from the
* network. This class returns a MediatorResult object, that can either be:
*       - Error : if we got an error while requesting data from the network
*       - Success : If we successfully got data from the network. Here, we also need to pass in a
*                   signal that tells whether more data can be loaded or not. For example, if the
*                   network response was successful but we got an empty list of repositories, it
*                   means that there is no more data to be loaded
*
* To be able to build the network request, the load method has 2 parameters that should give us all
* the information we need:
*       - PagingState: this gives us information about the pages that were loaded before, the
*         most recently accessed index in the list, and the PagingConfig we defined when initializing
*         the paging stream
*       - LoadType: this tells us whether we need to load data at the end (LoadType.APPEND) or at the
*                   beginning of the data (LoadType.PREPEND) that we previously loaded, or if this the
*                   first time we're loading data (LoadType.REFRESH)
*
* For example, if the load type is LoadType.APPEND then we retrieve the last item that was loaded from
* the PagingState. Based on that we should be able to find out how to load the next batch of Item
* objects, by computing the next page to be loaded
* */
@OptIn(ExperimentalPagingApi::class)
class ItemsRemoteMediator(
    private val service: ItemApi,
    private val itemDatabase: ItemDatabase
) : RemoteMediator<Int, Item>() {

    override suspend fun initialize(): InitializeAction {
        // Launch remote refresh as soon as paging starts and do not trigger remote prepend or
        // append until refresh has succeeded. In cases where we don't mind showing out-of-date,
        // cached offline data, we can return SKIP_INITIAL_REFRESH instead to prevent paging
        // triggering remote refresh.
        return InitializeAction.LAUNCH_INITIAL_REFRESH
    }

    /*
    * The load() method in a nutshell:
    *   - Find out what page we need to load from the network, based on the LoadType
    *   - Trigger the network request
    *   - Once the network request completes, if the received list of repositories is
    *     not empty, then do the following:
    *   - We compute the RemoteKeys for every Item
    *   - If this a new query (loadType = REFRESH) then we clear the database.
    *   - Save the RemoteKeys and Items in the database
    *   - Return MediatorResult.Success(endOfPaginationReached = false)
    *   - If the list of items was empty then we return MediatorResult.Success(endOfPaginationReached = true).
    *     If we get an error requesting data we return MediatorResult.Error
    * */
    override suspend fun load(loadType: LoadType, state: PagingState<Int, Item>): MediatorResult {

        /*
        * Determining the page number depends on the LoadType. This can be REFRESH, PREPEND or APPEND.
        * */
        val page = when (loadType) {

            /*
            * LoadType.REFRESH gets called when it's the first time we're loading data, or
            * when PagingDataAdapter.refresh() is called; so now the point of reference for loading
            * our data is the state.anchorPosition. If this is the first load, then the anchorPosition
            * is null. When PagingDataAdapter.refresh() is called, the anchorPosition is the first
            * visible position in the displayed list, so we will need to load the page that contains
            * that specific item.
            * */
            LoadType.REFRESH -> {
                /*
                * Based on the anchorPosition from the state, we can get the closest Item to that
                * position by calling state.closestItemToPosition(). Based on the Item, we can get
                * the RemoteKeys from the database.
                * */
                val remoteKeys = getRemoteKeyClosestToCurrentPosition(state)

                /*
                * If remoteKey is not null, then we can get the nextKey from it. In the REST API
                * the page keys are incremented sequentially. So to get the page that contains the
                * current item, we just subtract 1 from remoteKey.nextKey.
                *
                * If RemoteKey is null (because the anchorPosition was null), then the page we need
                * to load is the initial one: STARTING_PAGE_INDEX
                * */
                remoteKeys?.nextKey?.minus(1) ?: STARTING_PAGE_INDEX
            }
            /*
            * When we need to load data at the beginning of the currently loaded data set, the load
            * parameter is LoadType.PREPEND. Based on the first item in the database we need to compute
            * the network page key.
            * */
            LoadType.PREPEND -> {
                /*
                * We need to get the remote key of the first Item loaded from the database.
                * This happens in the getRemoteKeyForFirstItem() function.
                * */
                val remoteKeys = getRemoteKeyForFirstItem(state)

                /*
                * If remoteKeys is null, that means the refresh result is not in the database yet.
                * We can return Success with 'endOfPaginationReached = false' because Paging will call
                * this method again if RemoteKeys becomes non-null. If remoteKeys is not null but its
                * prevKey is null, that means we've reached the end of pagination for append.
                * */
                val prevKey = remoteKeys?.prevKey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                prevKey
            }

            /*
            * When we need to load data at the end of the currently loaded data set, the load parameter
            * is LoadType.APPEND. So now, based on the last item in the database we need to compute
            * the network page key.
            * */
            LoadType.APPEND -> {
                /*
                * We need to get the remote key of the last Item loaded from the database.
                * This happens in the getRemoteKeyForLastItem() function.
                * */
                val remoteKeys = getRemoteKeyForLastItem(state)

                /*
                * If remoteKeys is null, that means the refresh result is not in the database yet.
                * We can return Success with endOfPaginationReached = false because Paging will call
                * this method again if RemoteKeys becomes non-null. If remoteKeys is not null but its
                * prevKey is null, that means we've reached the end of pagination for append.
                * */
                val nextKey = remoteKeys?.nextKey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                nextKey
            }
        }

        try {
            // trigger the network request
            val apiResponse = service.getItems(page)

            val items = apiResponse.listOfItems
            val endOfPaginationReached = items.isEmpty()
            itemDatabase.withTransaction {
                // If this a new query (loadType = REFRESH) then we clear the database
                if (loadType == LoadType.REFRESH) {
                    itemDatabase.remoteKeysDao().clearRemoteKeys()
                    itemDatabase.itemDao().clearItems()
                }
                val prevKey = if (page == STARTING_PAGE_INDEX) null else page - 1
                val nextKey = if (endOfPaginationReached) null else page + 1

                // compute the RemoteKeys for every Item
                val keys = items.map {
                    RemoteKey(itemId = it.id, prevKey = prevKey, nextKey = nextKey)
                }
                // Save the RemoteKeys and Items in the database
                itemDatabase.remoteKeysDao().insertAll(keys)
                itemDatabase.itemDao().insertAllItems(items)
            }
            /*
            * If the list of Items is not empty, then we return MediatorResult.Success(endOfPaginationReached = false)
            * If the list of items was empty then we return MediatorResult.Success(endOfPaginationReached = true)
            * */
            return MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        }
        // If we get an error requesting data we return MediatorResult.Error
        catch (exception: IOException) {
            return MediatorResult.Error(exception)
        } catch (exception: HttpException) {
            return MediatorResult.Error(exception)
        }
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, Item>): RemoteKey? {
        // Get the last page that was retrieved, that contained items.
        // From that last page, get the last item
        return state.pages.lastOrNull() { it.data.isNotEmpty() }?.data?.lastOrNull()
            ?.let { item ->
                // Get the remote keys of the last item retrieved
                itemDatabase.remoteKeysDao().remoteKeysByItemId(item.id)
            }
    }

    private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int, Item>): RemoteKey? {
        // Get the first page that was retrieved, that contained items.
        // From that first page, get the first item
        return state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()
            ?.let { item ->
                // Get the remote keys of the first items retrieved
                itemDatabase.remoteKeysDao().remoteKeysByItemId(item.id)
            }
    }

    private suspend fun getRemoteKeyClosestToCurrentPosition(
        state: PagingState<Int, Item>
    ): RemoteKey? {
        // The paging library is trying to load data after the anchor position
        // Get the item closest to the anchor position
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.id?.let { itemId ->
                itemDatabase.remoteKeysDao().remoteKeysByItemId(itemId)
            }
        }
    }
}