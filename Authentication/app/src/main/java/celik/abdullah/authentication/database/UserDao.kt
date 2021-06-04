package celik.abdullah.authentication.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import celik.abdullah.authentication.models.User

@Dao
interface UserDao {
    // used to insert an User instance into the local database
    @Insert(onConflict= OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)
}