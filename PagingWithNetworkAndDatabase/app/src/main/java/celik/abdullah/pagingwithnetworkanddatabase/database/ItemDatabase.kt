package celik.abdullah.pagingwithnetworkanddatabase.database

import androidx.room.Database
import androidx.room.RoomDatabase
import celik.abdullah.pagingwithnetworkanddatabase.model.Item

/*
* Implement the Item database:
*   - create an abstract class ItemDatabase that extends RoomDatabase
*   - annotate the class with @Database, set the list of entities to contain the Item class & RemoteKeys class,
*     and set the database version to 1. For the purpose of this little project we don't need to export the schema
*   - define an abstract function that returns the ReposDao and an abstract function that returns the RemoteKeysDao
* */
@Database(entities = [Item::class, RemoteKey::class], version=1, exportSchema = false)
abstract class ItemDatabase: RoomDatabase() {
    abstract fun itemDao(): ItemDao
    abstract fun remoteKeysDao(): RemoteKeyDao
}