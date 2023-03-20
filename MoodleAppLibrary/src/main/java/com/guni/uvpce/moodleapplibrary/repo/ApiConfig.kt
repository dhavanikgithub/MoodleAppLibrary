package com.guni.uvpce.moodleapplibrary.repo

import android.content.Context
import android.util.Log
import com.android.volley.*
import com.guni.uvpce.moodleapplibrary.model.AppData
import com.guni.uvpce.moodleapplibrary.model.MoodleMapCourseAttendance
import com.guni.uvpce.moodleapplibrary.model.MoodleUrl
import org.json.JSONArray
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class ApiConfig(val context: Context) {
    private val TAG ="ApiConfig"
    private fun getUrl(name:String):String{return "https://script.google.com/macros/s/AKfycbwP3XOlI33GcQzZ1m7DWzt-CuwRy3YB8BBwGU_0lFf7KD56kUY/exec?spreadsheet=a&action=get&id=1VTFOXOo96qUF3XAw10oCXa0_-0yP1T1ypVVl0hMVZyw&sheet=$name&sheetuser=all&sheetuserIndex=0"}
    private suspend fun getData(name: String):JSONArray{
        return suspendCoroutine {content->
            VolleyApi(context).volleyConnection(getUrl(name),Request.Method.GET,HashMap(),HashMap(),
                onSuccess={
                    try{
                        //Log.i(TAG, "getData: Request: $name: Response $it")
                        val onSuccess = JSONObject(it).getJSONArray("records")
                        content.resume(onSuccess)
                    }catch (e:Exception){
                        Log.e(TAG, "getData: For $name : Error: $e", e)
                        //onError = ("Error:getMoodleData:$e")
                        content.resumeWithException(e)
                    }
                },onError={error-> content.resumeWithException(java.lang.Exception(error)) })
        }
    }
    suspend fun getAppData(): AppData {
        val it = getData(name = "AppSetting")
        return AppData(
            it.getJSONObject(0).getInt("Close") == 11,
            it.getJSONObject(0).getInt("verify_time") == 11,
            it.getJSONObject(0).getString("start_time"),
            it.getJSONObject(0).getString("end_time")
        )
    }
    suspend fun getMoodleData():List<MoodleUrl> {
        val arrayList = ArrayList<MoodleUrl>()
        val it = getData(name = "Moodle")
        for (i in 0 until it.length()) {
            arrayList.add(
                MoodleUrl(
                    it.getJSONObject(i).getString("id"),
                    it.getJSONObject(i).getString("Url"),
                    it.getJSONObject(i).getString("Attedance_token"),
                    it.getJSONObject(i).getString("core_token"),
                    it.getJSONObject(i).getString("file_token"),
                    it.getJSONObject(i).getString("cohort_id"),
                    it.getJSONObject(i).getString("admin_id")
                )
            )
        }

        return arrayList
    }
    suspend fun getMapCourseData():List<MoodleMapCourseAttendance> {
        val arrayList = ArrayList<MoodleMapCourseAttendance>()
        val it = getData(name = "Course")
        for (i in 0 until it.length()) {
            arrayList.add(
                MoodleMapCourseAttendance(
                    it.getJSONObject(i).getString("moodle_id"),
                    it.getJSONObject(i).getString("course_id"),
                    it.getJSONObject(i).getString("attendance_id"),
                    it.getJSONObject(i).getString("attendance_name")
                )
            )
        }
        return arrayList
    }
    suspend fun getMapCourseJsonObj(moodleId:String):JSONObject{
        return MoodleMapCourseAttendance.toJsonObject(getMapCourseData().filter { it.moodle_id == moodleId })
    }
}