package com.guni.uvpce.moodleapplibrary.model

import android.util.Log
import org.json.JSONObject

class MoodleGroup(val course: MoodleCourse, val groupid:String, val groupName:String): ModelBase {
    companion object{
        fun fromJsonObject(course: MoodleCourse, jsonString: String): MoodleGroup {
            //Log.i(this::class.java.name, "fromJsonObject: String Input:$jsonString")
            val jsonObject = JSONObject(jsonString)
            return MoodleGroup(
                course,
                jsonObject.getString("groupid"),
                jsonObject.getString("groupName")
            )
        }
    }
    override fun toJsonObject(): JSONObject {
        val json = JSONObject()
        json.put("groupid",groupid)
        json.put("groupName",groupName)
        return json
    }

    override fun toString(): String {
        return toJsonObject().toString(4)
    }
}