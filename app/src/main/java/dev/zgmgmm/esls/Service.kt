package dev.zgmgmm.esls

import dev.zgmgmm.esls.bean.*
import io.reactivex.Observable
import retrofit2.http.*
import retrofit2.http.Query

interface Service {
    @FormUrlEncoded
    @POST("login")
    fun login(@Field("user") user: String, @Field("password") password: String): Observable<Response<User>>


    @GET("goods/{id}")
    fun good(@Path("id") id: String): Observable<Response<Good>>

    @GET("goods")
    fun goods(@Query("query") query: String,@Query("queryString") queryString:String,@Query("page") page:String="",@Query("count") count:String=""): Observable<Response<List<Good>>>

    @POST("goods/search")
    fun searchGood(@Query("connection") connecion: String,@Query("page") page:Int,@Query("count") count:Int,@Body requestBean:RequestBean): Observable<Response<List<Good>>>

    @POST("good")
    fun good(@Body good:Good):Observable<Response<String>>

    @GET("tags/{id}")
    fun tag(@Path("id") id: String): Observable<Response<Label>>

    @POST("tags/search")
    fun searchTag(@Query("connection") connecion: String, @Query("page") page: Int?, @Query("count") count: Int?, @Body requestBean:RequestBean): Observable<Response<List<Label>>>

    @PUT("tag/bind")
    fun bind(@Query("sourceArgs1") sourceArgs1: String, @Query("ArgsString1") ArgsString1:String, @Query ("sourceArgs2") sourceArgs2:String, @Query("ArgsString2") ArgsString2:String, @Query("mode") mode:Int): Observable<Response<String>>

    /**
     *  DO_BY_TAG = 0;
     *  DO_BY_ROUTER = 1;
     *  DO_BY_CYCLE = 2;
     */
    @PUT("tag/update")
    fun updateTag(@Query("mode") mode:Int,@Body requestBean: RequestBean):Observable<Response<String>>

    /**
     *  DO_BY_TAG = 0;
     *  DO_BY_ROUTER = 1;
     *  DO_BY_CYCLE = 2;
     */
    @PUT("tag/scan")
    fun scanTag(@Query("mode") mode:Int,@Body requestBean: RequestBean):Observable<Response<String>>

    @PUT("tag/light")
    fun light(@Query("mode") mode:Int,@Body requestBean: RequestBean):Observable<Response<String>>


}