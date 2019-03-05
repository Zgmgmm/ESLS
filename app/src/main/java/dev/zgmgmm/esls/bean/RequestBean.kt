package dev.zgmgmm.esls.bean

data class RequestBean(
    val items: List<QueryItem>
) {
    constructor(queryItem: QueryItem) : this(listOf(queryItem))
    constructor(query: String, queryString: String) : this(QueryItem(query, queryString))
}

