package celik.abdullah.authentication.database

import androidx.room.Database
import androidx.room.RoomDatabase
import celik.abdullah.authentication.models.User

@Database(entities = [User::class], version=1, exportSchema = false)
abstract class AuthDatabase: RoomDatabase() {
    abstract fun userDao(): UserDao
}