package celik.abdullah.pagingwithnetworkanddatabase.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/*
* Immutable model class that holds all the information about an 'Item'.
* Objects of this type are received from the a REST API, therefore all the fields are annotated
* with the serialized name.
* This class also defines the Room 'items' table, where the item [id] is the primary key.
*
* For simplicity, I kept the model for this project very small:
* every item is only equipped with an ID and a name.
* * */
@Entity(tableName="items")
data class Item(
    @PrimaryKey
    @field:SerializedName("id") val id: Long,
    @field:SerializedName("name") val name:String
)
