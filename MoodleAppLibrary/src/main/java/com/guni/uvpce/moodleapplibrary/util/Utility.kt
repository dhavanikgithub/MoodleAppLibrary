package com.guni.uvpce.moodleapplibrary.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.StrictMode
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class Utility {
    fun getCurrenMillis():Long{
        val cal = Calendar.getInstance()
        return cal.timeInMillis
    }
    fun getCurrenDateTime():String{
        val cal = Calendar.getInstance()
        val df = SimpleDateFormat("dd-MM-yyyy hh:mm a")
        return df.format(cal.time)
    }
    fun isCurrentTimeBetween(startTime:String,endTime:String):Boolean{
        val currentMilli = getCurrenMillis()
        return currentMilli > getMillis(startTime) && currentMilli < getMillis(endTime)
    }
    fun getDurationMillis(hour:Int, min:Int):Long{
        return (hour * 60 * 60 + min *60) * (1000.toLong())
    }
    fun getMillis(hourMin:String):Long{
        if(!hourMin.contains(":"))
            throw Exception("Time Format is not Proper.")
        val strArr = hourMin.split(":")
        if(strArr.size < 2)
            throw Exception("Time Format is not Proper.")
        try{
            return getMillis(strArr[0].toInt(),strArr[1].toInt())
        }catch (e:Exception){
            throw Exception("Time Format is not Proper")
        }
    }
    fun getMillis(hour:Int, min:Int):Long{
        val setCalc = Calendar.getInstance()
        setCalc[Calendar.HOUR_OF_DAY] = hour
        setCalc[Calendar.MINUTE] = min
        setCalc[Calendar.SECOND] = 0
        return setCalc.timeInMillis
    }
    fun getDurationInSeconds(hour:Int, min:Int):Long{
        return hour * 60 * 60 + min * 60.toLong()
    }
    fun getSeconds(hour:Int, min:Int):Long{
        return getMillis(hour,min)/1000
    }
    fun getMillis(date:Int, month:Int, year:Int,hour:Int, min:Int):Long{
        val setCalc = Calendar.getInstance()
        setCalc[Calendar.DATE] = date
        setCalc[Calendar.MONTH] = month
        setCalc[Calendar.YEAR] = year
        setCalc[Calendar.HOUR_OF_DAY] = hour
        setCalc[Calendar.MINUTE] = min
        setCalc[Calendar.SECOND] = 0
        return setCalc.timeInMillis
    }
    fun getSeconds(date:Int, month:Int, year:Int,hour:Int, min:Int):Long{
        return getMillis(date,month,year,hour,min)/1000
    }

    fun convertBitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
    }

    fun convertBase64StringToImage(base64encodedData: String): Bitmap {
        val decodedByte = Base64.decode(base64encodedData, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.size)
    }

    fun stringEqualDistance(firstString:String,secondString:String):Double {
        try {
            if (firstString == null && secondString == null) {
                return 1.0
            }
            if (firstString == null || secondString == null) {
                return 0.0
            }
            return StringSimilarity().similarity(firstString,secondString)
        }
        catch (ex:Exception)
        {
            throw Exception("Error: String equal distance check [$ex]")
        }
    }
}