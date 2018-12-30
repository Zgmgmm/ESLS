package dev.zgmgmm.esls.bean

data class RequestBean(
    val items: List<QueryItem>
)

data class QueryItem( var query: String, var queryString: String,var beginTime: String="", var cycleTime: String="")