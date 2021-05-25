package celik.abdullah.pagingwithnetworkanddatabase.model

/*
* Represents a result from a network query, which contains List<Item> holding query data,
* and a String of network error state.
* */
sealed class NetworkResult{
    data class Success(val data: List<Item>): NetworkResult()
    data class Error(val error: Exception): NetworkResult()
}
