package com.guni.uvpce.moodleapplibrary.model

import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
class MoodleSessionUser(val id:String,val firstName:String, val lastName:String):ModelBase{
    companion object{
        fun fromJsonObject(jsonString: String): MoodleSessionUser{
            val jsonObject = JSONObject(jsonString)
            return MoodleSessionUser(jsonObject.getString("id"),
                jsonObject.getString("firstName"),
                jsonObject.getString("lastName"))
        }
    }
    override fun toJsonObject(): JSONObject {
        val json = JSONObject()
        json.put("id",id)
        json.put("firstName",firstName)
        json.put("lastName",lastName)
        return json
    }

    override fun toString(): String {
        return toJsonObject().toString(4)
    }
}
class MoodleSessionAttendanceLog(val id:String,val studentId:String,val statusId:String,val remarks:String): ModelBase{
    companion object{
        fun fromJsonObject(jsonString: String): MoodleSessionAttendanceLog{
            val jsonObject = JSONObject(jsonString)
            return MoodleSessionAttendanceLog(
                jsonObject.getString("id"),
                jsonObject.getString("studentId"),
                jsonObject.getString("statusId"),
                jsonObject.getString("remarks")
            )
        }
    }
    override fun toJsonObject(): JSONObject {
        val json = JSONObject()
        json.put("id",id)
        json.put("studentId",studentId)
        json.put("statusId",statusId)
        json.put("remarks",remarks)
        return json
    }
    override fun toString(): String {
        return toJsonObject().toString(4)
    }
}
class MoodleSessionAttendanceCount{
    var presentCount = 0
    var absentCount = 0
    var notMarked = 0
    override fun toString(): String {
        return "Present:$presentCount\nAbsent:$absentCount\nNotMarked:$notMarked"
    }
}
class MoodleSession(val attendance: MoodleAttendance,
                    val course: MoodleCourse,
                    val group: MoodleGroup,
                    val sessionId:String,
                    val description:String,
                    val status_set:String,
                    sessionStartDateString:String,
                    val duration:String): ModelBase
{
    val attendanceLog = ArrayList<MoodleSessionAttendanceLog>()
    val userList = ArrayList<MoodleSessionUser>()
    val statusList = ArrayList<MoodleSessionStatus>()

    val durationMinutes= duration.toLong() / 60;
    val sessionStartDate:Long = sessionStartDateString.trim().toLong()*1000
    val sessionEndDate:Long = (sessionStartDateString.toLong() + duration.toLong())*1000
    fun getPresentStatusId():MoodleSessionStatus{
        val returnId = statusList[0]
        for(i in 0 until statusList.size){
            if(statusList[i].name.uppercase(Locale.ROOT) == "P")
                return statusList[i]
        }
        return returnId
    }
    /*fun isStudentPresentInSession(studentId: String):Boolean{
        if(userList.isEmpty())
            return false
        val list = userList.filter { s->s.id ==  studentId}
        if(list.isEmpty())
            throw Exception("Given Student is not available.")
        return list.get(0).statusId == getPresentStatusId().id
    }*/
    fun getCompactStatusList():ArrayList<MoodleCompactSessionStatus>{
        val compactList = ArrayList<MoodleCompactSessionStatus>()
        for(i in 0 until statusList.size){
            compactList.add(MoodleCompactSessionStatus(statusList[i].id,statusList[i].name,statusList[i].description,statusList[i].grade))
        }
        return compactList
    }
    fun getAbsentStatusId():MoodleSessionStatus{
        val returnId = statusList[0]
        for(i in 0 until statusList.size){
            if(statusList[i].name.uppercase(Locale.ROOT) == "A")
                return statusList[i]
        }
        return returnId
    }
    companion object{
        fun fromJsonObject(jsonString: String): MoodleSession {
            //Log.i(this::class.java.name, "fromJsonObject: String Input:$jsonString")
            val jsonObject = JSONObject(jsonString)
            val jsonArray = jsonObject.getJSONArray("statusList")
            val course = MoodleCourse.fromJsonObject(jsonObject.getString("course"))
            val attendance = MoodleAttendance.fromJsonObject(jsonObject.getString("attendance"))
            val group = MoodleGroup.fromJsonObject(course,jsonObject.getString("group"))
            val obj = MoodleSession(
                attendance,
                course,
                group,
                jsonObject.getString("sessionId"),
                jsonObject.getString("description"),
                jsonObject.getString("status_set"),
                jsonObject.getString("sessionStartDateString"),
                jsonObject.getString("duration")
            )
            for(i in 0 until jsonArray.length()){
                obj.statusList.add(MoodleSessionStatus.fromJsonObject(obj,jsonArray[i].toString()))
            }
            return obj
        }
    }
    fun getAttendanceCount():MoodleSessionAttendanceCount{
        val objCount = MoodleSessionAttendanceCount()
        val presentStatus = getPresentStatusId()
        val absentStatus = getAbsentStatusId()
        for(element in attendanceLog){
            if(element.statusId == presentStatus.id){
                objCount.presentCount += 1
            }else if(element.statusId == absentStatus.id){
                objCount.absentCount += 1
            }
            else{
                objCount.notMarked += 1
            }
        }
        return objCount
    }
    override fun toJsonObject(): JSONObject {
        val json = JSONObject()
        val jsonArray = JSONArray()
        for(i in 0 until statusList.size){
            jsonArray.put(statusList[i].toJsonObject())
        }
        json.put("statusList",jsonArray)
        json.put("attendance",attendance.toJsonObject())
        json.put("course",course.toJsonObject())
        json.put("group",group.toJsonObject())
        json.put("sessionId",sessionId)
        json.put("description",description)
        json.put("status_set",status_set)
        json.put("sessionStartDateString",sessionStartDate)
        json.put("duration",duration)
        return json
    }

    override fun toString(): String {
        val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy hh:mm:ss a")
        return "${toJsonObject().toString(4)}\nid=$sessionId \nattendanceid=${attendance.attendanceId} \ngroupid=${group.groupid} \nsessionStartDate=${simpleDateFormat.format(sessionStartDate)} \nsessionEndDate=${simpleDateFormat.format(sessionEndDate)} \nduration=$durationMinutes mins\n"
    }
}