package com.dzian1s.autopartsapp.ui

import android.content.Context
import com.dzian1s.autopartsapp.R
import retrofit2.HttpException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

fun Context.toUiError(e: Throwable): String {
    return when (e) {
        is UnknownHostException -> getString(R.string.err_no_internet)
        is SocketTimeoutException -> getString(R.string.err_timeout)
        is HttpException -> getString(R.string.err_server, e.code())
        else -> e.message ?: getString(R.string.something_went_wrong)
    }
}