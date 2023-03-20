package com.guni.uvpce.moodleapplibrary.model

import android.util.Log
import org.json.JSONArray
import org.json.JSONObject

class MoodleCourse(val id:String,val Name:String,val userName:String): ModelBase {
    val groupList:ArrayList<MoodleGroup> = ArrayList();
    val sessionList:ArrayList<MoodleSession> = ArrayList();
    companion object{
        fun fromJsonObject(jsonString: String): MoodleCourse {
            //Log.i(this::class.java.name, "fromJsonObject: String Input:$jsonString")
            val jsonObject = JSONObject(jsonString)
            val jsonArray = jsonObject.getJSONArray("groupList")
            val obj = MoodleCourse(
                jsonObject.getString("courseId"),
                jsonObject.getString("courseName"),
                jsonObject.getString("userName")
            )
            for(i in 0 until jsonArray.length()){
                obj.groupList.add(MoodleGroup.fromJsonObject(obj,jsonArray[i].toString()))
            }
            return obj
        }
    }

    override fun toJsonObject(): JSONObject {
        val json = JSONObject()
        val jsonArray = JSONArray()
        for(i in 0 until groupList.size){
            jsonArray.put(groupList[i].toJsonObject())
        }
        json.put("groupList",jsonArray)
        json.put("courseId",id)
        json.put("courseName",Name)
        json.put("userName",userName)
        return json
    }

    override fun toString(): String {
        return toJsonObject().toString(4)
    }
}