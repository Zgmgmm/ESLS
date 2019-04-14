package dev.zgmgmm.esls.exception

import java.lang.Exception

class RequestException(msg:String, cause: Throwable? =null): Exception(msg,cause) {
    override val message: String = msg
}