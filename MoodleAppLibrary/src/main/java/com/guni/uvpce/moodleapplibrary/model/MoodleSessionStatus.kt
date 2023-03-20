package com.guni.uvpce.moodleapplibrary.model

import org.json.JSONObject

class MoodleSessionStatus(val session: MoodleSession, val id:String, val name:String, val description:String, val grade:Int): ModelBase {
    companion object{
        fun fromJsonObject(session: MoodleSession, jsonString: String):MoodleSessionStatus {
            val jsonObject = JSONObject(jsonString)
            return MoodleSessionStatus(
                        session,
                        jsonObject.getString("statusId"),
                        jsonObject.getString("statusName"),
                        jsonObject.getString("description"),
                        jsonObject.getInt("grade")
            )
        }
    }
    override fun toJsonObject(): JSONObject {
        val json = JSONObject()
        json.put("statusId",id)
        json.put("statusName",name)
        json.put("description",description)
        json.put("grade",grade)
        return json
    }

    override fun toString(): String {
        return "$session\nid=$id, name:$name, description:$description, grade=$grade"
    }
}
class MoodleCompactSessionStatus(val id:String, val name:String, val description:String, val grade:Int): ModelBase {
    companion object{
        fun fromJsonObject(jsonString: String):MoodleCompactSessionStatus {
            val jsonObject = JSONObject(jsonString)
            return MoodleCompactSessionStatus(

                jsonObject.getString("a"),
                jsonObject.getString("b"),
                jsonObject.getString("c"),
                jsonObject.getInt("d")
            )
        }
    }
    override fun toJsonObject(): JSONObject {
        val json = JSONObject()
        json.put("a",id)
        json.put("b",name)
        json.put("c",description)
        json.put("d",grade)
        return json
    }

    override fun toString(): String {
        return "name:$name, description:$description, grade=$grade"
    }
}