package celik.abdullah.pagingwithnetworkanddatabase.database

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import celik.abdullah.pagingwithnetworkanddatabase.model.Item

/*
* Implement the data access object (DAO) to access the items table by creating a ItemDao interface, annotated with @Dao.
* */
@Dao
interface ItemDao {

    /* Insert a list of Item objects. If the Item objects are already in the table, then replace them. */
    @Insert(onConflict= OnConflictStrategy.REPLACE)
    suspend fun insertAllItems(items:List<Item>)

    /*
    * Query for item that contain the query string in the name and sort those
    * results alphabetically by name. Instead of returning a List<Item>, return PagingSource<Int, Item>.
    * That way, the items table becomes the source of data for Paging.
    * */
    @Query("SELECT * FROM items WHERE name LIKE :queryString ORDER BY name ASC" )
    fun retrieveItemsByName(queryString: String): PagingSource<Int, Item>

    /* Just retrieve all available Items in the local cache.*/
    @Query("SELECT * FROM items ORDER BY name ASC" )
    fun retrieveItems(): PagingSource<Int, Item>

    /* Clear all data in the items table.*/
    @Query("DELETE FROM items")
    suspend fun clearItems()
}