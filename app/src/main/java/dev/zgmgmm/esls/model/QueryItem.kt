package dev.zgmgmm.esls.model

data class QueryItem(
    val query: String,
    val queryString: String,
    val cron: String = ""
)