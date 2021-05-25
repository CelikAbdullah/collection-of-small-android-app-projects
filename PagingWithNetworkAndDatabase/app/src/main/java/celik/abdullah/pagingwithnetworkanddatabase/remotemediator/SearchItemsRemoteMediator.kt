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

// For detailed comments, see ItemsRemoteMediator.kt
@OptIn(ExperimentalPagingApi::class)
class SearchItemsRemoteMediator(
    private val query: String,
    private val service: ItemApi,
    private val itemDatabase: ItemDatabase
) : RemoteMediator<Int, Item>() {

    override suspend fun initialize(): InitializeAction =
        InitializeAction.LAUNCH_INITIAL_REFRESH


    override suspend fun load(loadType: LoadType, state: PagingState<Int, Item>): MediatorResult {

        val page = when (loadType) {
            LoadType.REFRESH -> {
                val remoteKeys = getRemoteKeyClosestToCurrentPosition(state)
                remoteKeys?.nextKey?.minus(1) ?: STARTING_PAGE_INDEX
            }
            LoadType.PREPEND -> {
                val remoteKeys = getRemoteKeyForFirstItem(state)
                val prevKey = remoteKeys?.prevKey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                prevKey
            }
            LoadType.APPEND -> {
                val remoteKeys = getRemoteKeyForLastItem(state)
                val nextKey = remoteKeys?.nextKey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                nextKey
            }
        }
        try {
            val apiResponse = service.getItemsBySearchQuery(query, page)

            val items = apiResponse.listOfItems
            val endOfPaginationReached = items.isEmpty()
            itemDatabase.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    itemDatabase.remoteKeysDao().clearRemoteKeys()
                    itemDatabase.itemDao().clearItems()
                }
                val prevKey = if (page == STARTING_PAGE_INDEX) null else page - 1
                val nextKey = if (endOfPaginationReached) null else page + 1
                val keys = items.map {
                    RemoteKey(itemId = it.id, prevKey = prevKey, nextKey = nextKey)
                }
                itemDatabase.remoteKeysDao().insertAll(keys)
                itemDatabase.itemDao().insertAllItems(items)
            }
            return MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (exception: IOException) {
            return MediatorResult.Error(exception)
        } catch (exception: HttpException) {
            return MediatorResult.Error(exception)
        }
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, Item>): RemoteKey? =
        state.pages.lastOrNull() { it.data.isNotEmpty() }?.data?.lastOrNull()
            ?.let { item -> itemDatabase.remoteKeysDao().remoteKeysByItemId(item.id) }


    private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int, Item>): RemoteKey? =
        state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()
            ?.let { repo ->  itemDatabase.remoteKeysDao().remoteKeysByItemId(repo.id) }


    private suspend fun getRemoteKeyClosestToCurrentPosition(
        state: PagingState<Int, Item>
    ): RemoteKey? =
        state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.id?.let { itemId ->
                itemDatabase.remoteKeysDao().remoteKeysByItemId(itemId)
            }
        }
}