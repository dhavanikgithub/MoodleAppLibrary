package com.guni.uvpce.moodleapplibrary.model

import com.guni.uvpce.moodleapplibrary.util.Utility
import org.json.JSONObject

class MoodleBasicUrl(val id:String,val url:String){
    constructor(moodleUrl: MoodleUrl) : this(moodleUrl.id,moodleUrl.url)
}
class AppData(val close:Boolean,val verifyTime:Boolean,val startTime:String, val endTime:String){
    fun verifyData(){
        if(close)
            throw Exception("Connection is closed by Admin. Contact Admin")

        if(verifyTime){
            if(!Utility().isCurrentTimeBetween(startTime,endTime))
                throw Exception("Attendance time is expired. Contact Admin.")
        }
    }
}
class MoodleMapCourseAttendance(val moodle_id:String ,val course_id:String,val attendance_id:String,val attendance_name:String):ModelBase {
    companion object{
        fun fromJsonObject(jsonString:String): MoodleMapCourseAttendance{
            val jsonObject = JSONObject(jsonString)
            return MoodleMapCourseAttendance(
                jsonObject.getString("moodle_id"),
                jsonObject.getString("course_id"),
                jsonObject.getString("attendance_id"),
                jsonObject.getString("attendance_name"))
        }

        fun toJsonObject(list:List<MoodleMapCourseAttendance>):JSONObject{
            val json = JSONObject()
            for (element in list){
                json.put(element.course_id,getJsonObject(element.attendance_id,element.attendance_name))
            }
            return json
        }
        fun getJsonObject(id:String,name:String):JSONObject{
            val jsonObject = JSONObject()
            jsonObject.put("id",id)
            jsonObject.put("name",name)
            return jsonObject
        }
    }
    override fun toJsonObject(): JSONObject {
        val jsonObject = JSONObject()
        jsonObject.put("moodle_id",moodle_id)
        jsonObject.put("course_id",course_id)
        jsonObject.put("attendance_id",attendance_id)
        jsonObject.put("attendance_name",attendance_name)
        return jsonObject
    }

    override fun toString(): String {
        return toJsonObject().toString(4)
    }
}
class MoodleUrl(val id:String,val url:String,val att_token:String,val core_token:String,val file_token:String,val cohort_id:String,val admin_id:String):ModelBase {
    companion object{
        fun fromJsonObject(jsonString:String): MoodleUrl{
            val jsonObject = JSONObject(jsonString)
            return MoodleUrl(jsonObject.getString("id"),
                jsonObject.getString("url"),
                jsonObject.getString("att_token"),
                jsonObject.getString("core_token"),
                jsonObject.getString("file_token"),
                jsonObject.getString("cohort_id"),
                jsonObject.getString("admin_id"))
        }
        fun getMoodleUrl(urlId:String,list:List<MoodleUrl>):MoodleUrl{
            for(element in list){
                if (urlId == element.id)
                    return element
            }
            return list[0]
        }
        fun getBasicMoodleUrl(urlId:String,list:List<MoodleUrl>):MoodleBasicUrl{
            for(element in list){
                if (urlId == element.id)
                    return MoodleBasicUrl(element)
            }
            return MoodleBasicUrl(list[0])
        }
        fun getBasicMoodleUrlList(list:List<MoodleUrl>):List<MoodleBasicUrl>{
            val returnList = ArrayList<MoodleBasicUrl>()
            for(element in list){
                returnList.add(MoodleBasicUrl(element))
            }
            return returnList
        }
    }

    override fun toJsonObject(): JSONObject {
        val jsonObject = JSONObject()
        jsonObject.put("id",id)
        jsonObject.put("url",url)
        jsonObject.put("att_token",att_token)
        jsonObject.put("core_token",core_token)
        jsonObject.put("file_token",file_token)
        jsonObject.put("cohort_id",cohort_id)
        jsonObject.put("admin_id",admin_id)
        return jsonObject
    }

    override fun toString(): String {
        return toJsonObject().toString(4)
    }
}