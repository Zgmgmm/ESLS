package dev.zgmgmm.esls.service

import dev.zgmgmm.esls.model.*
import io.reactivex.Observable
import retrofit2.http.*

interface Service {

    // 登录
    @POST("user/login")
    fun login(@Body user: User): Observable<Response<UserInfo>>


    // 查询指定ID商品
    @GET("goods/{id}")
    fun good(@Path("id") id: Int): Observable<Response<List<Good>>>


    // 根据条件查询商品
    @GET("goods")
    fun goods(
        @Query("query") query: String, @Query("queryString") queryString: String, @Query("page") page: Int = 0, @Query(
            "count"
        ) count: Int = 99999
    ): Observable<Response<List<Good>>>

    // 搜索商品
    @POST("goods/search")
    fun searchGood(@Query("connection") connection: String, @Query("page") page: Int, @Query("count") count: Int, @Body requestBean: RequestBean): Observable<Response<List<Good>>>

    @PUT("good/update")
    fun goodUpdate(@Body requestBean: RequestBean): Observable<Response<Stat>>

    @POST("good")
    fun good(@Body good: Good): Observable<Response<Good>>

    @GET("tags/{id}")
    fun tag(@Path("id") id: Int): Observable<Response<List<Label>>>

    @POST("tags/search")
    fun searchTag(@Query("connection") connection: String, @Query("page") page: Int?, @Query("count") count: Int?, @Body requestBean: RequestBean): Observable<Response<List<Label>>>

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
    @PUT("tag/flush")
    fun flush(@Query("mode") mode: Int, @Body requestBean: RequestBean): Observable<Response<Stat>>

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

    @POST("tag/compute")
    fun manualCount(@Query("goodNumber") goodNumber: Int, @Body requestBean: RequestBean): Observable<Response<String>>


    // 电子秤
    @POST("/api/balance")
    fun weigher(@Query("mode") mode: Int, @Body requestBean: RequestBean, @Query("weight") weight: Int? = 0): Observable<Response<String>>

}