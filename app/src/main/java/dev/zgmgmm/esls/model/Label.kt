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
    var goodNumber: Int = 0,
    var hardwareVersion: String? = "",
    var isReplenish: String?="",
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
    var waitUpdate: String? = ""
) : Serializable {
    val isBound: Boolean
        get() {
            return goodId != 0
        }
    val needReplenish:Boolean
        get(){
            return isReplenish=="1"
        }
}
