package com.guni.uvpce.moodleapplibrary.model

import android.graphics.Bitmap
import android.util.Log
import com.guni.uvpce.moodleapplibrary.repo.AttendanceRepository
import org.json.JSONObject

open class BaseUserInfo(var id: String,
                        var username: String,
                        var firstname: String,
                        var lastname: String,
                        var fullname:String,
                        var emailAddress:String,
                        var imageUrl:String): ModelBase
{
    var userImage:Bitmap? = null
    var hasUserUploadImg:Boolean = false
    //var image:Bitmap = Utility().convertBase64StringToImage(Utility().convertUrlToBase64(imageUrl))
    companion object{
        fun fromJsonObject(jsonString: String): BaseUserInfo {
            val jsonObject = JSONObject(jsonString)

            return BaseUserInfo(
                jsonObject.getString("userId"),
                jsonObject.getString("userName"),
                jsonObject.getString("firstname"),
                jsonObject.getString("lastname"),
                jsonObject.getString("fullname"),
                jsonObject.getString("emailAddress"),
                jsonObject.getString("imageUrl")
            )
        }
    }
    override fun toJsonObject(): JSONObject {
        val json = JSONObject()
        json.put("userId",id)
        json.put("userName",username)
        json.put("firstname",firstname)
        json.put("lastname",lastname)
        json.put("fullname",fullname)
        json.put("emailAddress",emailAddress)
        json.put("imageUrl",imageUrl)
        return json
    }

    override fun toString(): String {
        return toJsonObject().toString(4)
    }

}


class MoodleUserInfo(var course: MoodleCourse, var group: MoodleGroup,
                     id: String, username: String,
                     firstname: String, lastname: String,
                     fullName:String, emailAddress:String, imageUrl:String):
    BaseUserInfo(id, username,firstname, lastname, fullName,emailAddress, imageUrl){


    override fun toString(): String {
        return super.toString() +"\n"+course.Name+"\n"+group.groupName
    }
}
class UserStatusBulk(val session_id:String, val taken_by_id: String, session:JSONObject, val attRepository: AttendanceRepository){
    val statuses = session.getJSONArray("statuses").getJSONObject(1)
    val status_id = statuses.getString("id")
    val status_set = session.getString("statusset")
    val userList = session.getJSONArray("users")
    suspend fun startExecution():Boolean{
        //Log.i("AttendaceRepository", "startExecution: started")
        return setUserStatus(0)
    }
    private suspend fun setUserStatus(currentIndex:Int):Boolean {
        if (currentIndex >= userList.length()) {
            return true
        }
        //Log.i("AttendaceRepository", "setUserStatus: currentIndex=$currentIndex and userList.Length=${userList.length()}")
        val student_id = userList.getJSONObject(currentIndex).getString("id")
        val response = attRepository.takeAttendanceMoodle(
            session_id = session_id,
            taken_by_id = taken_by_id,
            student_id = student_id,
            status_id = status_id, status_set = status_set
        )
        return setUserStatus(currentIndex + 1)
    }
}