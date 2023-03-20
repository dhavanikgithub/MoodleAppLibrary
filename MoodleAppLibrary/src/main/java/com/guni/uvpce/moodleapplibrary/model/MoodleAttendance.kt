package com.guni.uvpce.moodleapplibrary.model

import com.guni.uvpce.moodleapplibrary.repo.ModelRepository
import org.json.JSONObject

class MoodleAttendance(val course: MoodleCourse, val attendanceId:String, val attendanceName:String):ModelBase {
    companion object{
        fun fromJsonObject(jsonString: String):MoodleAttendance {
            //Log.i(this::class.java.name, "fromJsonObject: String Input:$jsonString")
            val jsonObject = JSONObject(jsonString)
            return MoodleAttendance(
                MoodleCourse.fromJsonObject(jsonObject.getString("course")),
                jsonObject.getString("attendanceId"),
                jsonObject.getString("attendanceName")
            )
        }
    }

    override fun toJsonObject():JSONObject{
        val json = JSONObject()
        json.put("course",course.toJsonObject())
        json.put("attendanceId",attendanceId)
        json.put("attendanceName",attendanceName)
        return json
    }
    override fun toString(): String {
        return toJsonObject().toString(4)
    }
}
class MoodleAttendanceBulk(val modelRepo: ModelRepository, val courseList: List<MoodleCourse>){
    var moodleAttendanceList:ArrayList<MoodleAttendance> = java.util.ArrayList()
    suspend fun createAttendanceInBulk(onSuccess:(ArrayList<MoodleAttendance>)-> Unit){
        onAttendanceCreate(0,onSuccess)
    }
    suspend fun onAttendanceCreate(currentIndex:Int,onSuccess:(ArrayList<MoodleAttendance>)-> Unit){
        if(currentIndex >= courseList.size) {
            onSuccess(moodleAttendanceList)
            return
        }
        val attendance = modelRepo.createAttendance(courseList[currentIndex],"Attendance")
        moodleAttendanceList.add(attendance)
        suspend { onAttendanceCreate(currentIndex+1,onSuccess) }
    }

}