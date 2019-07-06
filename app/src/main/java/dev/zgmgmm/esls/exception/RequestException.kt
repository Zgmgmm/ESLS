package dev.zgmgmm.esls.exception

class RequestException(any: Any?, cause: Throwable? = null) : Exception(any.toString(), cause) {
    override val message: String = when( any){
        null->"未知错误"
        else->any.toString()
    }
}

