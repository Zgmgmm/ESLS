package dev.zgmgmm.esls.bean

import java.io.Serializable

data class Good(
    var id: Long,//主键
    var name: String,//商品名称
    var origin: String,//商品产地
    var provider: String,//商品供应商
    var unit: String,//商品单位
    var barcode: String,//条形码
    var qrCode: String,//二维码
    var operator: String,//操作员
    var timestamp: String,//操作时间
    var promotion: Int,//是否促销
    var promotionReason: String,//促销原因
    var status: Int,//绑定状态
    var price: Double,//价格
    var promotePrice: Double,//促销价格
    var spec: String,//规格
    var shelfNumber: String="",
    var category: String="",
    var rfu01: String="",
    var rfu02: String="",
    var rfus01: String="",
    var rfus02: String=""
//    var tagIdList: ArrayList<String>
) : Serializable