package com.guni.uvpce.moodleapplibrary.model

import com.guni.uvpce.moodleapplibrary.util.compressString
import com.guni.uvpce.moodleapplibrary.util.decompressString
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList

class QRMessageData(
    val sessionId: String,
    val sessionStartDate:Long,
    val sessionEndDate:Long,
    val courseId:String,
    val courseName:String,
    val groupId:String,
    val groupName:String,
    val loggedInFacultyUserId:String,
    val attendanceByFacultyId:String,
    val facultyLocationLat:String,
    val facultyLocationLong:String,
    val attendanceStartDate:Long,
    val attendanceEndDate:Long,
    val attendanceDuration:Long,val statusList:ArrayList<MoodleCompactSessionStatus>): ModelBase {
    fun getPresentStatusId():MoodleCompactSessionStatus{
        val returnId = statusList[0]
        for(i in 0 until statusList.size){
            if(statusList[i].name.uppercase(Locale.ROOT) == "P")
                return statusList[i]
        }
        return returnId
    }
    fun getAbsentStatusId():MoodleCompactSessionStatus{
        val returnId = statusList[0]
        for(i in 0 until statusList.size){
            if(statusList[i].name.uppercase(Locale.ROOT) == "A")
                return statusList[i]
        }
        return returnId
    }
    override fun toJsonObject(): JSONObject {
        val json = JSONObject()
        //json.put("session",session.toJsonObject())
        json.put("a",sessionId)
        json.put("b",sessionStartDate)
        json.put("c",sessionEndDate)
        json.put("d",courseId)
        json.put("e",courseName)
        json.put("f",groupId)
        json.put("g",groupName)
        json.put("h",loggedInFacultyUserId)
        json.put("i",attendanceByFacultyId)
        json.put("j",facultyLocationLat)
        json.put("k",facultyLocationLong)
        json.put("l",attendanceStartDate)
        json.put("m",attendanceEndDate)
        json.put("n",attendanceDuration)
        val jsonArray = JSONArray()
        for(i in 0 until statusList.size){
            jsonArray.put(statusList[i].toJsonObject())
        }
        json.put("o",jsonArray)
        return json
    }

    override fun toString(): String {
        val jsonObject = toJsonObject()
        return jsonObject.toString()
    }
    companion object{
        fun fromJsonObject(jsonString: String):QRMessageData {
            val jsonObject = JSONObject(jsonString)
            val jsonArray = jsonObject.getJSONArray("o")
            val statusList = ArrayList<MoodleCompactSessionStatus>()
            for(i in 0 until jsonArray.length()){
                statusList.add(MoodleCompactSessionStatus.fromJsonObject(jsonArray[i].toString()))
            }
            val obj = QRMessageData(
                jsonObject.getString("a"),
                jsonObject.getLong("b"),
                jsonObject.getLong("c"),
                jsonObject.getString("d"),
                jsonObject.getString("e"),
                jsonObject.getString("f"),
                jsonObject.getString("g"),
                jsonObject.getString("h"),
                jsonObject.getString("i"),
                jsonObject.getString("j"),
                jsonObject.getString("k"),
                jsonObject.getLong("l"),
                jsonObject.getLong("m"),
                jsonObject.getLong("n"),
                statusList
            )

            return obj
        }
        fun getQRMessageObject(QrCodeMessage:String): QRMessageData {
            //val jsonString = Base64.decode(QrCodeMessage, Base64.DEFAULT).toString()
            //Log.i(this::class.java.name, "getQRMessageObject: String Input:$jsonString")
            //return fromJsonObject(jsonString)
            //val charset = Charsets.UTF_8
            //return QrCodeMessage.toByteArray(charset).uncompress().let { fromJsonObject(it) }
            return fromJsonObject(decompressString(QrCodeMessage))
        }
        fun getQRMessageString(QrCodeMessageObject:QRMessageData): String {
            //val qrStringData = QrCodeMessageObject.toString()
            //return (Base64.encodeToString(qrStringData.toByteArray(),Base64.DEFAULT))
            //val charset = Charsets.UTF_8
            //return String(QrCodeMessageObject.toString().compress(),charset)
            return compressString(QrCodeMessageObject.toString())
        }
        fun getCompressedString(message:String):String{
            return compressString(message)
        }
        fun getDeCompressedString(message:String):String{
            return decompressString(message)
        }
        fun getStopAttString():String{
            return getCompressedString(stopMessage)
        }
        fun isStopMessage(message:String):Boolean{
            return getDeCompressedString(message) == stopMessage
        }
        private const val stopMessage = "Stop Attendance from CE/IT Dept UVPCE Ganpat University College"
    }
}