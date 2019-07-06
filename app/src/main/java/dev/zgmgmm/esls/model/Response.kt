package dev.zgmgmm.esls.model

data class Response<T>(var msg: String, var code: Int, var data: T) {
    fun isSuccess(): Boolean {
        return code != 0
    }
}