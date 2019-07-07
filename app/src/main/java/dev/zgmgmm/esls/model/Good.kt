package dev.zgmgmm.esls.model

import java.io.Serializable

data class Good(
    var id: Int = 0,
    var barCode: String? = "",
    var category: String? = "",
    var computeNumber: String? = "",
    var imageUrl: String? = "",
    var isComputeOpen: Int = 0,
    var isPromote: String? = "",
    var isReplenish: String? = "",
    var name: String? = "",
    var operator: String? = "",
    var origin: String? = "",
    var price: String? = "",
    var promotePrice: String? = "",
    var promoteTimeGap: String? = "",
    var promotionReason: String? = "",
    var provider: String? = "",
    var qrCode: String? = "",
    var regionNames: String? = "",
    var replenishNumber: String? = "",
    var rfu01: String? = "",
    var rfu02: String? = "",
    var rfus01: String? = "",
    var rfus02: String? = "",
    var shelfNumber: String? = "",
    var spec: String? = "",
    var stock: String? = "",
    var unit: String? = "",
    var waitUpdate: String? = "",
    var weightSpec: String? = "",
    var shopNameAndShopNumber: String? = "_",
    var tagIdList: List<String> = emptyList()


) : Serializable {

    val needReplenish: Boolean
        get() {
            return isReplenish == "1"
        }

    val shopName: String
        get() {
            return splitPair(shopNameAndShopNumber)[0]
        }
    val shopNumber: String
        get() {
            return splitPair(shopNameAndShopNumber)[1]
        }

    private fun splitPair(raw: String?): List<String> {
        if (raw == null || !raw.contains("_"))
            return listOf("", "")
        return raw.split("_")
    }
}

