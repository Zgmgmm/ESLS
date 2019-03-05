package dev.zgmgmm.esls.bean

import java.io.Serializable

data class Label(
    var id: String = "",//主键
    var power: String? = "",//电量
    var tagRssi: String? = "",
    var apRssi: String? = "",
    var state: String? = "",//状态
    var hardwareVersion: String? = "",
    var softwareVersion: String? = "",
    var updateStatus: String? = "",
    var forbidState: String? = "",
    var execTime: String? = "",
    var completeTime: String? = "",
    var barCode: String? = "",
    var screenType: String? = "",
    var resolutionWidth: String? = "",
    var resolutionHeight: String? = "",
    var goodId: String? = "",
    var styleId: String? = "",
    var routerId: String? = ""
) : Serializable {
    fun isBound(): Boolean {
        return goodId?.equals(0) ?: (false)
    }
}