package dev.zgmgmm.esls.model

import java.io.Serializable

data class Label(
    var id: Int = 0,
    var apRssi: String? = "",
    var barCode: String? = "",
    var completeTime: String? = "",
    var computeTime: String? = "",
    var execTime: String? = "",
    var forbidState: String? = "",
    var goodId: Int = 0,
    var goodNumber: String? = "",
    var hardwareVersion: String? = "",
    var isReplenish: String? = "",
    var isWorking: String? = "",
    var measurePower: String? = "",
    var power: String? = "",
    var resolutionHeight: String? = "",
    var resolutionWidth: String? = "",
    var routerId: String? = "",
    var screenType: String? = "",
    var softwareVersion: String? = "",
    var state: String? = "",
    var styleId: String? = "",
    var tagAddress: String? = "",
    var tagRssi: String? = "",
    var totalWeight: String? = "",
    var waitUpdate: String? = "",
    var goodIsComputeOpen: String? = "",
    var goodBarCodeAndName: String? = "_",
    var routerBarCodeAndChannelId: String? = "_",
    var shopNameAndShopNumber: String? = "_"
) : Serializable {
    val isBound: Boolean
        get() {
            return goodId != 0
        }
    val needReplenish: Boolean
        get() {
            return isReplenish == "1"
        }
    val isComputeOpen: Boolean
        get() {
            return goodIsComputeOpen == "1"
        }
    val goodBarCode: String
        get() {
            return splitPair(goodBarCodeAndName)[0]
        }

    val goodName: String
        get() {
            return splitPair(goodBarCodeAndName)[1]
        }

    val routerBarCode: String
        get() {
            return splitPair(routerBarCodeAndChannelId)[0]
        }

    val channelId: String
        get() {
            return splitPair(routerBarCodeAndChannelId)[1]
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
