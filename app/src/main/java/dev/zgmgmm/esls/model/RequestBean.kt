package dev.zgmgmm.esls.model

data class RequestBean(
    val items: List<QueryItem>
) {
    constructor(queryItem: QueryItem) : this(listOf(queryItem))
    constructor(query: String, queryString: String) : this(QueryItem(query, queryString))
}


fun Good.toRequestBean(): RequestBean {
    return RequestBean("id", id.toString())
}

fun Label.toBarcodeRequestBean(): RequestBean {
    return RequestBean("barCode", barCode.toString())
}