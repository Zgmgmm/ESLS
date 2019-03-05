package dev.zgmgmm.esls.service

import dev.zgmgmm.esls.bean.*
import io.reactivex.Observable
import retrofit2.http.*

interface Service {

    // 登录
    @FormUrlEncoded
//    @POST("login")
    @POST("login")
    fun login(@Field("user") user: String, @Field("password") password: String): Observable<Response<User>>


    // 查询指定ID商品
    @GET("goods/{id}")
    fun good(@Path("id") id: String): Observable<Response<Good>>


    // 根据条件查询商品
    @GET("goods")
    fun goods(
        @Query("query") query: String, @Query("queryString") queryString: String, @Query("page") page: Int = 0, @Query(
            "count"
        ) count: Int = 99999
    ): Observable<Response<List<Good>>>

    // 搜索商品
    @POST("goods/search")
    fun searchGood(@Query("connection") connecion: String, @Query("page") page: Int, @Query("count") count: Int, @Body requestBean: RequestBean): Observable<Response<List<Good>>>


    @POST("good")
    fun good(@Body good: Good): Observable<Response<Good>>

    @GET("tags/{id}")
    fun tag(@Path("id") id: String): Observable<Response<Label>>

    @POST("tags/search")
    fun searchTag(@Query("connection") connecion: String, @Query("page") page: Int?, @Query("count") count: Int?, @Body requestBean: RequestBean): Observable<Response<List<Label>>>

    @PUT("tag/bind")
    fun bind(
        @Query("sourceArgs1") goodAttr: String, @Query("ArgsString1") goodAttrValue: String, @Query("sourceArgs2") labelAttr: String, @Query(
            "ArgsString2"
        ) labelAttrValue: String, @Query("mode") mode: Int
    ): Observable<Response<String>>

    /**
     *  DO_BY_TAG = 0;
     *  DO_BY_ROUTER = 1;
     *  DO_BY_CYCLE = 2;
     */
    @PUT("tag/update")
    fun updateTag(@Query("mode") mode: Int, @Body requestBean: RequestBean): Observable<Response<Stat>>

    /**
     *  DO_BY_TAG = 0;
     *  DO_BY_ROUTER = 1;
     *  DO_BY_CYCLE = 2;
     */
    @PUT("tag/scan")
    fun scan(@Query("mode") mode: Int, @Body requestBean: RequestBean): Observable<Response<Stat>>

    @PUT("tag/light")
    fun light(@Query("mode") mode: Int, @Query("typeMode") typeMode: Int, @Body requestBean: RequestBean): Observable<Response<Stat>>

    @PUT("tag/remove")
    fun remove(@Query("mode") mode: Int, @Body requestBean: RequestBean): Observable<Response<Stat>>

    @PUT("tag/status")
    fun status(@Query("mode") mode: Int, @Body requestBean: RequestBean): Observable<Response<Stat>>


}