package com.farmer.weather.data.remote

sealed class ApiResult<out T> {

    val TAG = javaClass.simpleName

    data class Success<out T>(val value: T) : ApiResult<T>()

    data class Error(val code: String?, val message: String?, val exception: Throwable? = null) :
        ApiResult<Nothing>()

    object NoData : ApiResult<Nothing>()

}