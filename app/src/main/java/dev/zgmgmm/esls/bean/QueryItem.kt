package dev.zgmgmm.esls.bean

data class QueryItem(
    val query: String,
    val queryString: String,
    val cron: String = ""
)