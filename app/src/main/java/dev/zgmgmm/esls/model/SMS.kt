package dev.zgmgmm.esls.model

data class SMS(
    var phoneNumber: String,
    var smsType: String,
    var userCode: String? = null
)