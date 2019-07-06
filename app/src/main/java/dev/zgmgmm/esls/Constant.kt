package dev.zgmgmm.esls

object Constant {
    object Pref {
        const val REMEMBERED_USERNAME = "REMEMBERED_USERNAME"
        const val REMEMBERED_PASSWORD = "REMEMBERED_PASSWORD"
        const val REMEMBER_ME = "REMEMBER_ME"
        const val AUTO_LOGIN = "PREF_AUTO_LOGIN"
        const val API_BASE_URL = "API_BASE_URL"
        const val REQUEST_TIMEOUT = "REQUEST_TIMEOUT"
    }

    object HttpHeader {
        const val TOKEN = "esls"
    }

    object Net {
        const val DEFAULT_API_BASE_URL = "http://39.108.106.167:8086"
        const val DEFAULT_REQUEST_TIMEOUT = 10L
    }

}