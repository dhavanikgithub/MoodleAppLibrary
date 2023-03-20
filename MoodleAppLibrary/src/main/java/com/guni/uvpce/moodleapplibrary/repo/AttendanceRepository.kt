package com.guni.uvpce.moodleapplibrary.repo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.StrictMode
import android.util.Base64
import android.util.Log
import com.guni.uvpce.moodleapplibrary.model.MoodleBasicUrl
import com.guni.uvpce.moodleapplibrary.model.MoodleUrl
import com.guni.uvpce.moodleapplibrary.model.UserStatusBulk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.net.URL

class AttendanceRepository(
    context: Context,
    moodleBasicConfig: MoodleBasicUrl
): iAttendanceRepository {
    companion object{
        val moodleUrlList = ArrayList<MoodleUrl>()
    }

    private val volleyApi = VolleyApi(context)
    private val moodleConfig:MoodleUrl = MoodleUrl.getMoodleUrl(moodleBasicConfig.id, moodleUrlList)
    private val MoodleURL = moodleConfig.url
    private val CORE_TOKEN = moodleConfig.core_token
    private val ATTENDANCE_TOKEN = moodleConfig.att_token
    private val UPLOAD_FILE_TOKEN = moodleConfig.file_token
    private val TAG = "AttendanceRepository"
    fun getMoodleServerUrl():String {return "$MoodleURL/webservice/rest/server.php" }
    private fun getTokenUrl():String
    {
        return "$MoodleURL/login/token.php"
    }
    /*fun getFileTokenURL(url: String): String {
        var finalurl = "$MoodleURL/webservice/"
        finalurl += url.substring(url.indexOf("pluginfile.php")) //For not Default Pic
        finalurl = finalurl.split("?")[0]
        finalurl += "?token=${UPLOAD_FILE_TOKEN}"
        return finalurl
    }*/

    private val userDefaultPicURL =
        "/webservice/pluginfile.php/89/user/icon/f1?token=${CORE_TOKEN}"

    fun getDefaultUserPictureUrl():String{
        return "$MoodleURL$userDefaultPicURL"
    }
    private fun getUrlPluginPhp(url:String):String{
        return if(url.indexOf("pluginfile.php") != -1)
            "$MoodleURL/webservice/"+url.substring(url.indexOf("pluginfile.php"))
        else if(url.indexOf("image.php") != -1)
            "$MoodleURL/theme/"+url.substring(url.indexOf("image.php"))
        else
            throw java.lang.Exception("Moodle URL: $url has no either pluginfile or image php file.")
    }
    fun getFileTokenURL(url: String): String {
        var finalurl = getUrlPluginPhp(url)
        val subString = finalurl.split("?")
        if(subString.isEmpty())
            throw Exception("Image URL is not valid")
        finalurl = subString[0]
        finalurl += "?token=${UPLOAD_FILE_TOKEN}"
        return finalurl
    }
    override suspend fun getUserInfoMoodle(username: String):JSONArray {
        val params: MutableMap<String, String> = HashMap()
        params["wstoken"] = CORE_TOKEN
        params["wsfunction"] = "core_user_get_users_by_field"
        params["moodlewsrestformat"] = "json"
        params["field"] = "username"
        params["values[]"] = username
        try{
            return (JSONArray(volleyApi.volleyConnection(getMoodleServerUrl(), params)))
        }
        catch (ex:Exception)
        {
            throw Exception(ex.message)
        }

    }

    override suspend fun getUserByFieldMoodle(field: String, value: ArrayList<String>):JSONArray {
        val params: MutableMap<String, String> = HashMap()
        params["wstoken"] = CORE_TOKEN
        params["wsfunction"] = "core_user_get_users_by_field"
        params["moodlewsrestformat"] = "json"
        params["field"] = field
        for (i in 0 until value.size){
            params["values[$i]"] = value[i]
        }
        try{
            return JSONArray(volleyApi.volleyConnection(getMoodleServerUrl(),params))
        }
        catch (ex:Exception)
        {
            throw Exception(ex.message)
        }

    }

    override suspend fun getFacultyInfoMoodle(faculty_name: String) :JSONArray{
        val params: MutableMap<String, String> = HashMap()
        params["wstoken"] = CORE_TOKEN
        params["wsfunction"] = "core_user_get_users_by_field"
        params["moodlewsrestformat"] = "json"
        params["field"] = "username"
        params["values[]"] = faculty_name

        try{
            return JSONArray(volleyApi.volleyConnection(getMoodleServerUrl(),params))
        }
        catch (ex:Exception)
        {
            throw Exception(ex.message)
        }

    }

    override suspend fun getUserCoursesListMoodle(username: String):JSONArray {
        try{
            val result = getUserInfoMoodle(username)
            if (result.length() == 0) {
                return JSONArray()
            }
            val userid = result.getJSONObject(0).getString("id")
            val params: MutableMap<String, String> = HashMap()
            params["wstoken"] = CORE_TOKEN
            params["wsfunction"] = "core_enrol_get_users_courses"
            params["moodlewsrestformat"] = "json"
            params["userid"] = userid
            val response = volleyApi.volleyConnection(getMoodleServerUrl(), params)
            val newjsonArray = JSONArray()
            val jsonarray = JSONArray(response)
            for (i in 0 until jsonarray.length()) {
                val jsonobject = jsonarray.getJSONObject(i)
                val newjsonObject = JSONObject()
                newjsonObject.put("courseid", jsonobject.getString("id"))
                newjsonObject.put("coursename", jsonobject.getString("fullname"))
                newjsonArray.put(newjsonObject)
            }
            return (newjsonArray)
        }
        catch (ex:java.lang.Exception)
        {
            throw Exception(ex.message)
        }



    }

    override suspend fun createAttendanceMoodle(course_id: String, attendance_name:String):JSONArray {
        try {
            val params: MutableMap<String, String> = HashMap()
            params["wstoken"] = ATTENDANCE_TOKEN
            params["wsfunction"] = "mod_attendance_add_attendance"
            params["moodlewsrestformat"] = "json"
            params["courseid"] = course_id
            params["name"] = attendance_name
            val response = volleyApi.volleyConnection(getMoodleServerUrl(), params)
            val attendanceIdPattern = Regex("\\d+")
            val attendanceId = attendanceIdPattern.find(response, 0)
            if (attendanceId != null) {
                val arrayJSON = JSONArray()
                val objectJSON = JSONObject()
                objectJSON.put("id", attendanceId.value)
                arrayJSON.put(objectJSON)
                return (arrayJSON)
            } else {
                throw java.lang.Exception("Error:can't found Attendance")
            }
        }
        catch (ex:java.lang.Exception)
        {
            throw Exception(ex.message)
        }
    }

    private suspend fun setDefaultAttendance(session_id:String,taken_by_id: String):JSONObject {
        try {
            val session = getSessionMoodle(session_id)
            val result = UserStatusBulk(session_id, taken_by_id, session, this).startExecution()

            if (result)
                return (session)
            else
                throw Exception("Due to Slow Connectivity, Can't set Default (Absent) Attendance Status")
        }
        catch (ex:java.lang.Exception)
        {
            throw Exception(ex.message)
        }
    }

    override suspend fun createSessionMoodle(
        course_id:String, attendance_id:String, session_time:String, created_by_user_id:String, duration:String, description:String, group_id:String):JSONArray {
        val params: MutableMap<String, String> = HashMap()
        params["wstoken"] = ATTENDANCE_TOKEN
        params["wsfunction"] = "mod_attendance_add_session"
        params["moodlewsrestformat"] = "json"
        params["attendanceid"] = attendance_id
        params["sessiontime"] = session_time
        params["description"] = description
        params["duration"] = duration
        params["groupid"] = group_id
        try {
            val response = volleyApi.volleyConnection(getMoodleServerUrl(), params)

            val sessionIdPattern = Regex("\\d+")
            val sessionId = sessionIdPattern.find(response, 0)
            if (sessionId != null) {
                val arrayJSON = JSONArray()
                val objectJSON = JSONObject()
                objectJSON.put("id",sessionId.value)
                arrayJSON.put(objectJSON)
                setDefaultAttendance(session_id = sessionId.value, taken_by_id = created_by_user_id)
                return arrayJSON
            }
            else{
                throw Exception("Can't Create Session because sessionId = null found")
            }
        }
        catch (ex:java.lang.Exception)
        {
            throw Exception(ex.message)
        }

    }

    override suspend fun getCourseGroupsMoodle(course_id: String):JSONArray {
        val params: MutableMap<String, String> = HashMap()
        params["wstoken"] = CORE_TOKEN
        params["wsfunction"] = "core_group_get_course_groups"
        params["moodlewsrestformat"] = "json"
        params["courseid"] = course_id

        try {
            val response = volleyApi.volleyConnection(getMoodleServerUrl(), params)
            val arrayJSON = JSONArray(response)
            val newArryJSON = JSONArray()

            for (i in 0 until arrayJSON.length()) {
                val jsonobject = arrayJSON.getJSONObject(i)
                val objectJSON = JSONObject()
                objectJSON.put("groupid", jsonobject.getString("id"))
                objectJSON.put("groupname", jsonobject.getString("name"))
                newArryJSON.put(objectJSON)
            }
            return newArryJSON
        }
        catch (ex:java.lang.Exception)
        {
            throw Exception(ex.message)
        }
    }

    override suspend fun sendMessageMoodle( userList:List<String>, qrMessage:String):JSONArray {
        val params: MutableMap<String, String> = HashMap()
        params["wstoken"] = CORE_TOKEN
        params["wsfunction"] = "core_message_send_instant_messages"
        params["moodlewsrestformat"] = "json"
        for(i in userList.indices){
            params["messages[$i][touserid]"]=userList[i]
            params["messages[$i][text]"]=qrMessage
        }

        try {
            val response = volleyApi.volleyConnection(getMoodleServerUrl(),params)
            return JSONArray(response)
        }
        catch (ex:java.lang.Exception)
        {
            throw Exception(ex.message)
        }

    }

    override suspend fun takeAttendanceMoodle(
        session_id: String,
        student_id: String,
        taken_by_id: String,
        status_id: String,
        status_set: String
    ):Boolean {

        val params: MutableMap<String, String> = HashMap()
        params["wstoken"] = ATTENDANCE_TOKEN
        params["wsfunction"] = "mod_attendance_update_user_status"
        params["moodlewsrestformat"] = "json"
        params["sessionid"] = session_id
        params["studentid"] = student_id
        params["takenbyid"] = taken_by_id
        params["statusid"] = status_id
        params["statusset"] = status_set

        try {
            val response = volleyApi.volleyConnection(getMoodleServerUrl(),params)
            return response == "null"
        }
        catch (ex:java.lang.Exception)
        {
            throw Exception(ex.message)
        }

    }

    override suspend fun getSessionMoodle(
        session_id: String
    ):JSONObject {

        val params: MutableMap<String, String> = HashMap()
        params["wstoken"] = ATTENDANCE_TOKEN
        params["wsfunction"] = "mod_attendance_get_session"
        params["moodlewsrestformat"] = "json"
        params["sessionid"] = session_id

        try {
            val response =volleyApi.volleyConnection(getMoodleServerUrl(),params)
            return JSONObject(response)
        }
        catch (ex:java.lang.Exception)
        {
            throw Exception(ex.message)
        }

    }

    override suspend fun getSessionsListMoodle(
        attendance_id: String
    ):JSONArray {
        val params: MutableMap<String, String> = HashMap()
        params["wstoken"] = ATTENDANCE_TOKEN
        params["wsfunction"] = "mod_attendance_get_sessions"
        params["moodlewsrestformat"] = "json"
        params["attendanceid"] = attendance_id

        try {
            val response = volleyApi.volleyConnection(getMoodleServerUrl(),params)
            return JSONArray(response)
        }
        catch (ex:java.lang.Exception)
        {
            throw Exception(ex.message)
        }

    }

    override suspend fun uploadFileMoodle(
        component: String,
        file_area: String,
        item_id: String,
        file_path: String,
        file_name: String,
        file_content: String,
        context_level: String,
        instanceid: String
    ):JSONArray {
        val params: MutableMap<String, String> = HashMap()
        params["wstoken"] = UPLOAD_FILE_TOKEN
        params["wsfunction"] = "core_files_upload"
        params["moodlewsrestformat"] = "json"
        params["component"] = component
        params["filearea"] = file_area
        params["itemid"] = item_id
        params["filepath"] = file_path
        params["filename"] = file_name
        params["filecontent"] = file_content
        params["contextlevel"] = context_level
        params["instanceid"] = instanceid

        try {
            val response = volleyApi.volleyConnection(getMoodleServerUrl(),params)
            val jsonObject = JSONObject(response)
            val jsonArray = JSONArray()
            jsonArray.put(jsonObject)
            return  jsonArray
        }
        catch (ex:java.lang.Exception)
        {
            throw Exception(ex.message)
        }

    }

    override suspend fun updatePictureMoodle(
        draft_item_id: String,
        user_id: String
    ):JSONArray {
        val params: MutableMap<String, String> = HashMap()
        params["wstoken"] = CORE_TOKEN
        params["wsfunction"] = "core_user_update_picture"
        params["moodlewsrestformat"] = "json"
        params["draftitemid"] = draft_item_id
        params["userid"] = user_id

        try {
            val response = volleyApi.volleyConnection(getMoodleServerUrl(),params)
            val jsonObject = JSONObject(response)
            val jsonArray = JSONArray()
            jsonArray.put(jsonObject)
            return jsonArray
        }
        catch (ex:java.lang.Exception)
        {
            throw Exception(ex.message)
        }


    }

    override suspend fun getMessageMoodle(
        user_id: String
    ):JSONObject {
        val params: MutableMap<String, String> = HashMap()
        params["wstoken"] = CORE_TOKEN
        params["wsfunction"] = "core_message_get_messages"
        params["moodlewsrestformat"] = "json"
        params["useridto"] = user_id
        params["useridfrom"]=moodleConfig.admin_id
        params["type"] = "conversations"
        params["read"] = "2"
        params["limitnum"] = "1"

        try {
            val response = volleyApi.volleyConnection(getMoodleServerUrl(), params)
            return JSONObject(response).getJSONArray("messages").getJSONObject(0)
        }
        catch (ex:java.lang.Exception)
        {
            throw Exception(ex.message)
        }

    }

    override suspend fun getCategoriesMoodle():JSONArray
    {
        val params: MutableMap<String, String> = HashMap()
        params["wstoken"] = CORE_TOKEN
        params["wsfunction"] = "core_course_get_categories"
        params["moodlewsrestformat"] = "json"

        try {
            return JSONArray(volleyApi.volleyConnection(getMoodleServerUrl(), params))
        }
        catch (ex:java.lang.Exception)
        {
            throw Exception(ex.message)
        }

    }

    override suspend fun getCohortsMoodle():JSONArray {
        val params: MutableMap<String, String> = HashMap()
        params["wstoken"] = UPLOAD_FILE_TOKEN
        params["wsfunction"] = "core_cohort_get_cohorts"
        params["moodlewsrestformat"] = "json"

        try {
            return JSONArray(volleyApi.volleyConnection(getMoodleServerUrl(), params))
        }
        catch (ex:java.lang.Exception)
        {
            throw Exception(ex.message)
        }

    }

    override suspend fun getCohortMembersMoodle(cohort_id: Int):JSONArray {
        val params: MutableMap<String, String> = HashMap()
        params["wstoken"] = UPLOAD_FILE_TOKEN
        params["wsfunction"] = "core_cohort_get_cohort_members"
        params["moodlewsrestformat"] = "json"
        params["cohortids[]"] = cohort_id.toString()
        try {
            val response = volleyApi.volleyConnection(getMoodleServerUrl(), params)
            val jsonArrayRes = JSONArray(response)
            val jsonObjectRes = jsonArrayRes.getJSONObject(0)
            val userIds = jsonObjectRes.getString("userids")
            val pattern = Regex("\\d+")
            val res1: Sequence<MatchResult> = pattern.findAll(userIds, 0)
            val userList = ArrayList<String>()
            // Prints all the matches using forEach loop
            res1.forEach { matchResult -> userList.add(matchResult.value) }
            return getUserByFieldMoodle("id", userList)
        } catch (ex: java.lang.Exception) {
            throw Exception(ex.message)
        }

    }

    override suspend fun resolveImgURLMoodle(
        url: String,
        token: String
    ):String {
            var finalurl : String
            val uri = Uri.parse(url)
            val bitmap: Bitmap
            val base64: String
            finalurl = "http://" + uri.host + "/webservice/"
            finalurl += url.substring(url.indexOf("pluginfile.php")) //For not Default Pic
            finalurl = finalurl.split("?")[0]
            finalurl += "?token=${token}"

            val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)
            val newUrl = URL(finalurl)
            bitmap = BitmapFactory.decodeStream(withContext(Dispatchers.IO) {
                withContext(Dispatchers.IO) {
                    newUrl.openConnection()
                }.getInputStream()
            })
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            base64 = Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)

            return base64

    }

    override suspend fun getEnrolledUserByCourseGroupMoodle(
        courseid: String,
        groupid: String
    ):JSONArray {
        val params: MutableMap<String, String> = HashMap()
        params["wstoken"] = CORE_TOKEN
        params["wsfunction"] = "core_enrol_get_enrolled_users"
        params["moodlewsrestformat"] = "json"
        params["courseid"] = courseid
        params["options[0][name]"] = "groupid"
        params["options[0][value]"] = groupid
        params["options[1][name]"] = "groupid"
        params["options[1][value]"] = "'username, profileimageurl, fullname'"

        try {
            return JSONArray(volleyApi.volleyConnection(getMoodleServerUrl(),params))
        }
        catch (ex:java.lang.Exception)
        {
            throw Exception(ex.message)
        }

    }

    override suspend fun getTeacherUserByCourseGroupMoodle(
        courseid: String,
        groupid:String,
        roleid:String):JSONArray {

        val params: MutableMap<String, String> = HashMap()
        params["wstoken"] = CORE_TOKEN
        params["wsfunction"] = "core_enrol_get_enrolled_users"
        params["moodlewsrestformat"] = "json"
        params["courseid"] = courseid
        params["options[0][name]"] = "roleid"
        params["options[0][value]"] = roleid

        try {
            return JSONArray(volleyApi.volleyConnection(getMoodleServerUrl(), params))
        }
        catch (ex:java.lang.Exception)
        {
            throw Exception(ex.message)
        }

    }

    override suspend fun login(recievedMoodleUsername:String,
                       recievedMoodlePassword:String):Boolean{

        val params: MutableMap<String, String> = HashMap()
        params["username"] = recievedMoodleUsername
        params["password"] = recievedMoodlePassword
        params["service"] = "moodle_mobile_app"

        try {
            val response = volleyApi.volleyConnection(getTokenUrl(),params)
            Log.i(TAG, "login: $response")
            return response.indexOf("token") != -1
        }
        catch (ex:java.lang.Exception)
        {
            throw Exception(ex.message)
        }

    }
}