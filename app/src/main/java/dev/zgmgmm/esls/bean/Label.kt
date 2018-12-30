package dev.zgmgmm.esls.bean

import java.io.Serializable

data class Label(
    var id: String,//主键
    var mac: String,//商品名称
    var shopNumber: String,//店铺标号
    var width: Int,//宽度
    var height: Int,//高度
    var power: Int,//电量
    var rssi: Double,//信号强度
    var state: Boolean,//状态
    var name: String,//价签名称
    var serialNumber: String,//路由器序列号
    var hardwareVersion: Int,//硬件版本号
    var softwareVersion: Int,//软件版本号
    var productionBatch: String,//生产批次
    var manufacture: String,//制造商
    var updateProgress: Int,//更新进程
    var type: String,//屏类型
    var wakeState: Int,//唤醒状态
    var styleid: Long,//外键（总样式实体）
    var routerid: Long,//外键（路由器实体）
    var goodid: Long//外键（商品实体）

):Serializable