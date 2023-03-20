package com.guni.uvpce.moodleapplibrary.repo

import android.content.Context
import android.util.Log
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class VolleyApi(val context: Context) {
//    fun volleyConnection(url:String, method: Int,params: MutableMap<String, String>,
//                         onSuccess:(String)->Unit,
//                         onError:(String)->Unit){
//        volleyConnection(url,method,params,headerParams(),onSuccess,onError)
//    }

    fun headerParams():MutableMap<String,String>
    {
        val headerParams: MutableMap<String, String> = HashMap()
        headerParams["Content-Type"] = "application/x-www-form-urlencoded"
        return headerParams
    }
    suspend fun volleyConnection(url:String,params: MutableMap<String, String>):String{
        return suspendCoroutine {content->
            VolleyApi(context).volleyConnection(url, Request.Method.POST,params,headerParams(),
                onSuccess={
                    try{
                        content.resume(it)
                    }catch (e:Exception){
                        Log.e("VolleyApi", "volleyConnection: $e", e)
                        content.resumeWithException(e)
//                        throw Exception("volleyConnection: $e")
                    }
                },onError={error->
                    content.resumeWithException(java.lang.Exception(error))
//                    throw Exception("volleyConnection: $error")
                })
        }
    }
    fun volleyConnection(url:String, method: Int, params: MutableMap<String, String>,headerParams: MutableMap<String, String>,
                         onSuccess: (String)->Unit,
                         onError: (String)->Unit){
        val mRequestQueue = Volley.newRequestQueue(context)
        val request = object : StringRequest(
            method, url,
            { response ->
                onSuccess(response)
            },
            { error ->
                onError(error.toString())
            })
        {
            override fun getParams(): Map<String, String> {
                return params
            }
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                return headerParams
            }
        }
        request.retryPolicy = object : RetryPolicy {
            override fun getCurrentTimeout(): Int {
                return 50000
            }

            override fun getCurrentRetryCount(): Int {
                return 50000
            }

            @Throws(VolleyError::class)
            override fun retry(error: VolleyError) {
            }
        }
        //VolleyLog.DEBUG = true
        mRequestQueue.add(request)
    }
}