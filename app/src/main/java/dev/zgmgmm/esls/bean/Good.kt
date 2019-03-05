package dev.zgmgmm.esls.bean

import java.io.Serializable

data class Good(
    var id: String = "",//主键
    var name: String? = "",//商品名称
    var origin: String? = "",//商品产地
    var provider: String? = "",//商品供应商
    var unit: String? = "",//商品单位
    var barCode: String? = "",//条形码
    var qrCode: String? = "",//二维码
    var operator: String? = "",//操作员
    var importTime: String? = "",//操作时间
    var promotionReason: String? = "",//促销原因
    var status: String? = "",//绑定状态
    var price: Double? = 0.0,//价格
    var promotePrice: Double? = 0.0,//促销价格
    var photo: String? = "",//图片
    var waitUpdate: String? = "",
    var spec: String? = "",//规格
    var category: String? = "",
    var rfu01: String? = "",
    var rfu02: String? = "",
    var rfus01: String? = "",
    var rfus02: String? = "",
    var regionNames: String? = "",
    var shelfNumber: String? = "",
    var tagIdList: List<Long> = listOf()
) : Serializable