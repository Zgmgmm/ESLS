package dev.zgmgmm.esls

import dev.zgmgmm.esls.bean.Good

object Mock {
    var id = 1
    fun getGood(): Good {
        return Good(id = id++.toString(), name = "Name-$id", provider = "provider-$id")
    }

    fun getGoods(num: Int): MutableList<Good> {
        val goods = mutableListOf<Good>()
        for (i in 1..num)
            goods.add(getGood())
        return goods
    }
}
