package dev.zgmgmm.esls

fun parseWeigherResp(s: String?): Map<String, String> {
    if(s==null){
        return emptyMap()
    }
    val begin = s.indexOf("{")
    val end = s.lastIndexOf("}")
    if(begin==-1||end==-1)
        return emptyMap()

    val data = s.substring(begin+1, end)
    val map = hashMapOf<String, String>()
    data.split(",")
        .map(String::trim)
        .map { rawKV ->
            rawKV.split("=")
        }
        .forEach {item->
            val k=item[0]
            val v=item[1]
            map[k] = v
        }
//    if(map.containsKey("key")){
//        val res=map["key"]!!
//        if(res!="成功"){
//            map["key"]=res.substring(res.indexOf(" ")+1)
//        }
//    }
    return map
}
