package com.github.kotlintelegrambot.network

import com.github.kotlintelegrambot.network.Response as TelegramResponse
import com.github.kotlintelegrambot.network.serialization.GsonFactory
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response

fun <T> Call<T>.call(): Pair<Response<T?>?, Exception?> = try {
    Pair(execute(), null)
} catch (exception: Exception) {
    Pair(null, exception)
}

class ResponseError(val errorBody: ResponseBody?, val exception: Exception?)

fun <T> Pair<Response<T?>?, Exception?>.fold(response: (T?) -> Unit = {}, error: (ResponseError) -> Unit = {}) {
    if (first?.isSuccessful == true && first?.body() != null) response(first!!.body()!!)
    else error(ResponseError(first?.errorBody(), second))
}

/**
 * call the error function [errorFun] if an exception occurred during the request
 */
fun <T> Pair<Response<TelegramResponse<T>?>?, Exception?>.onException(
    errorFun: (e: Exception) -> Unit
): Pair<Response<TelegramResponse<T>?>?, Exception?> {
    if (second != null)  {
        errorFun(second!!)
    }
    return this
}

/**
 * call the error function [errorFun] if an API HTTP error occurred during the request
 */
fun <T> Pair<Response<TelegramResponse<T>?>?, Exception?>.onHttpError(
    errorFun: (errorResponse: TelegramResponse<Nothing>) -> Unit
): Pair<Response<TelegramResponse<T>?>?, Exception?> {
    if (first != null && !first!!.isSuccessful) {
        errorFun(GsonFactory.createForApiClient().fromJson(
            first!!.errorBody()!!.string(),
            com.github.kotlintelegrambot.network.Response::class.java
        ) as com.github.kotlintelegrambot.network.Response<Nothing>)
    }
    return this
}

fun <T, R> Pair<Response<T?>?, Exception?>.bimap(mapResponse: (T?) -> R, mapError: (ResponseError) -> R): R =
    if (first?.isSuccessful == true && first?.body() != null) {
        val response = first!!.body()!!
        mapResponse(response)
    } else {
        mapError(ResponseError(first?.errorBody(), second))
    }
