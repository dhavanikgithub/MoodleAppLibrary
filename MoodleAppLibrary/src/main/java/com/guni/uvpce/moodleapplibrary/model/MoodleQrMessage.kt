package com.guni.uvpce.moodleapplibrary.model

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class MoodleQrMessage(val msgId:String, val userIdFrom:String, val userIdTo:String, val fullMessage:String,
                      val timeCreated:Int):ModelBase{
    var message:QRMessageData
    init {
        message = QRMessageData.getQRMessageObject(fullMessage)
    }

    override fun toJsonObject(): JSONObject {
        val json = JSONObject()
        json.put("msgId",msgId)
        json.put("userIdFrom",userIdFrom)
        json.put("userIdTo",userIdTo)
        json.put("fullMessage",fullMessage)
        json.put("timeCreated",timeCreated)
        json.put("message",message.toJsonObject())
        return json
    }

    override fun toString(): String {
        return toJsonObject().toString(4)
    }
}
class MoodleMessage(val msgId:String, val userIdFrom:String, val userIdTo:String, val fullMessage:String,
                      val timeCreated:Int):ModelBase{

    override fun toJsonObject(): JSONObject {
        val json = JSONObject()
        json.put("msgId",msgId)
        json.put("userIdFrom",userIdFrom)
        json.put("userIdTo",userIdTo)
        json.put("fullMessage",fullMessage)
        json.put("timeCreated",timeCreated)
        return json
    }

    override fun toString(): String {
        return toJsonObject().toString(4)
    }
}