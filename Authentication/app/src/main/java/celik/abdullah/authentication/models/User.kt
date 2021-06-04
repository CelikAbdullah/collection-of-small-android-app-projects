package celik.abdullah.authentication.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName="users")
data class User(
    @PrimaryKey
    @ColumnInfo(name="user_id")
    val userId : Long,
    @ColumnInfo(name="user_email")
    val userEmail: String,
    @ColumnInfo(name="user_name")
    val userName: String,
    @ColumnInfo(name="user_date_joined")
    val userDateJoined: String,
    @ColumnInfo(name="user_picture")
    val userPicture: String
)
