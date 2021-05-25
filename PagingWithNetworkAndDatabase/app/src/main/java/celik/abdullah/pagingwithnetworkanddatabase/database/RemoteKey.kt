package celik.abdullah.pagingwithnetworkanddatabase.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/*
* For the purposes of the REST API, the page key that we use to request pages of
* items is just a page index that is incremented when getting the next page. This
* means that given an Item object, the next batch of Item objects can be requested
* based on page index + 1. The previous batch of Item objects can be requested
* based on page index - 1. All Item objects received on a certain page response
* will have the same next and previous keys.
*
* When we get the last item loaded from the PagingState, there's no way to know the
* index of the page it belonged to. To solve this problem, we can add another table
* that stores the next and previous page keys for each Item; we can call it
* remote_keys. While this can be done in the Item table, creating a new table for
* the next and previous remote keys associated with a Item allows us to have a
* better separation of concerns.
* */
@Entity(tableName = "remote_keys")
data class RemoteKey(
    @PrimaryKey val itemId: Long,
    val prevKey: Int?,
    val nextKey: Int?
)
