package com.guni.uvpce.moodleapplibrary.repo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.StrictMode
import android.util.Base64
import android.util.Log
import androidx.preference.PreferenceManager
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.net.URL
import java.nio.file.*
import com.guni.uvpce.moodleapplibrary.model.*
import com.guni.uvpce.moodleapplibrary.util.*

class ModelRepository constructor(
    private var moodleUrl:MoodleBasicUrl, private var appData: AppData) {
    private lateinit var attRepo:AttendanceRepository
    private lateinit var courseAttMap:JSONObject
    init {
        if(hasObjectCreated){
            throw Exception("Can't Create Object directly")
        }
    }
    companion object{
        private var lastTimeAccessed:Long = 0
        private lateinit var obj:ModelRepository
        private var hasObjectCreated = false
        suspend fun getModelRepo(context: Context,refresh:Boolean = false):ModelRepository {
            return if(!this::obj.isInitialized){
                obj = createObject(context)
                lastTimeAccessed = Utility().getCurrenMillis()
                hasObjectCreated = true
                obj
            }else if(refresh ||((Utility().getCurrenMillis() - lastTimeAccessed)> Utility().getDurationMillis(0,40))){
                obj = createObject(context,false)
                lastTimeAccessed = Utility().getCurrenMillis()
                obj
            } else{
                obj
            }
        }
        private suspend fun createObject(context: Context,newObject:Boolean = true):ModelRepository{
            val it = ApiConfig(context).getMoodleData()
            if (it.isNotEmpty()) {
                AttendanceRepository.moodleUrlList.clear()
                AttendanceRepository.moodleUrlList.addAll(it)
                val moodleUrlId = getMoodleUrlId(context)
                val data = ApiConfig(context).getAppData()
                if(newObject) {
                    val obj1 = ModelRepository(MoodleUrl.getBasicMoodleUrl(moodleUrlId, it), data)
                    obj1.attRepo = AttendanceRepository(context, obj1.moodleUrl)
                    obj1.courseAttMap = ApiConfig(context).getMapCourseJsonObj(obj1.moodleUrl.id)
                    return obj1
                }else{
                    val obj1 = obj
                    obj.moodleUrl = MoodleUrl.getBasicMoodleUrl(moodleUrlId, it)
                    obj.appData = data
                    obj1.attRepo = AttendanceRepository(context, obj1.moodleUrl)
                    obj1.courseAttMap = ApiConfig(context).getMapCourseJsonObj(obj1.moodleUrl.id)
                    return obj1
                }
            } else {
                throw Exception("Not Sufficient Data for Moodle Configuration")
            }
        }
        suspend fun getMoodleUrlList(context: Context):List<MoodleBasicUrl> {
            val it = ApiConfig(context).getMoodleData()
            if (it.isNotEmpty()) {
                AttendanceRepository.moodleUrlList.clear()
                AttendanceRepository.moodleUrlList.addAll(it)
                //val moodleUrlId = getMoodleUrlId(context)
                return (MoodleUrl.getBasicMoodleUrlList(it))
            } else {
                throw java.lang.Exception("Not Sufficient Data for Moodle Configuration")
            }
        }
        fun getMoodleUrlObject(context: Context):MoodleBasicUrl{
            val moodleUrlId = getMoodleUrlId(context)
            return MoodleUrl.getBasicMoodleUrl(moodleUrlId,AttendanceRepository.moodleUrlList)
        }
        private fun getMoodleUrlId(context: Context):String{
            val preference = PreferenceManager.getDefaultSharedPreferences(context)
            return preference.getString("MoodleUrlId","")!!
        }
        fun setMoodleUrlSetting(context: Context,modleUrl: MoodleBasicUrl){
            val preference = PreferenceManager.getDefaultSharedPreferences(context)
            preference.edit().putString("MoodleUrlId",modleUrl.id).apply()
        }
    }

    private val TAG = "ModelRepository"
    /*suspend fun isStudentRegisterForFace(enrollmentNo:String): BaseUserInfo {
        val result = getUserInfo(enrollmentNo)
        result.userImage = Utility().convertUrlToBitmap(attRepo.getFileTokenURL(result.imageUrl))
        val profileImage = Utility().convertBitmapToBase64(result.userImage!!)
        val defaultPic = Utility().convertUrlToBase64(attRepo.getDefaultUserPictureUrl())
        result.hasUserUploadImg = profileImage != defaultPic
        return result
    }*/

    suspend fun isStudentRegisterForFace(context: Context, enrollmentNo:String): BaseUserInfo {
        appData.verifyData()
        val result = getUserInfo(enrollmentNo)
        result.userImage = getURLtoBitmap(result.imageUrl)
        val profileImage = Utility().convertBitmapToBase64(result.userImage!!)
        val defaultPic = convertUrlToBase64(attRepo.getDefaultUserPictureUrl())
        val vall = StringSimilarity().similarity(profileImage.trim(), defaultPic.trim())
        Log.i(TAG, "isStudentRegisterForFace:similarity score: $vall")
        result.hasUserUploadImg =  vall > 0.9
        return result
    }
    suspend fun getUserInfo(userName:String):BaseUserInfo{
            appData.verifyData()
            val result = attRepo.getUserInfoMoodle(userName)
            if (result.length() == 0) {
                throw Exception("Username does not exist")
            }
            val item = result.getJSONObject(0)
            val userid = item.get("id").toString()
            val firstname = item.get("firstname").toString()
            val lastname = item.get("lastname").toString()
            val fullname = item.get("fullname").toString()
            val emailaddr = item.get("email").toString()
            val image = item.get("profileimageurl").toString()

            return BaseUserInfo(userid,userName,firstname,lastname,fullname,emailaddr,image)
    }
    suspend fun uploadStudentPicture(context: Context,userid:String,
                                     curImageUri: Uri):JSONArray {
        appData.verifyData()
        //Update the selected photo in moodle
            val bitmap =
                context.let { it1 ->
                    BitmapUtils.getBitmapFromUri(
                        it1.contentResolver,
                        curImageUri
                    )
                }
            val bitmapStr = bitmap.let { it1 -> BitmapUtils.bitmapToString(it1) }

            //Create regex to get the filename from curImageUri
            val path: Path = Paths.get(curImageUri.toString())
            val filename: String = path.fileName.toString()
            //Uploading the correct chosen pic
            val result = attRepo.uploadFileMoodle("user", "draft", "0", "/", filename, "$bitmapStr", "user", "2")

            Log.i(TAG, "Successfully uploaded the file:$result")
            val item = result.getJSONObject(0)
            //val contextid = item.get("contextid").toString()
            val draftitemid = item.get("itemid").toString()
            //Updated the uploaded picture.
            val result1 = attRepo.updatePictureMoodle(draftitemid, userid)
            return (result1)

    }
    suspend fun getCourseListEnrolledByUser(userName:String):List<MoodleCourse> {
        val result = attRepo.getUserCoursesListMoodle(userName)
        val courseList = ArrayList<MoodleCourse>();
        for (i in 0 until result.length()) {
            courseList.add(
                MoodleCourse(
                    result.getJSONObject(i).getString("courseid"),
                    result.getJSONObject(i).getString("coursename"),
                    userName
                )
            )
        }
        return (courseList)
    }
    suspend fun getStudentList(course: MoodleCourse, group: MoodleGroup):List<MoodleUserInfo> {

        val result = attRepo.getEnrolledUserByCourseGroupMoodle(course.id, group.groupid)
        val userList = ArrayList<MoodleUserInfo>();
        for (i in 0 until result.length()) {
            userList.add(
                MoodleUserInfo(
                    course, group,
                    result.getJSONObject(i).getString("id"),
                    result.getJSONObject(i).getString("username"),
                    result.getJSONObject(i).getString("firstname"),
                    result.getJSONObject(i).getString("lastname"),
                    result.getJSONObject(i).getString("fullname"),
                    result.getJSONObject(i).getString("email"),
                    result.getJSONObject(i).getString("profileimageurl")
                )
            )
        }
        return (userList)
    }
    suspend fun getGroupList(course: MoodleCourse):List<MoodleGroup> {
            val result = attRepo.getCourseGroupsMoodle(course.id)
            val groupList = ArrayList<MoodleGroup>();
            for (i in 0 until result.length()) {
                groupList.add(
                    MoodleGroup(
                        course,
                        result.getJSONObject(i).getString("groupid"),
                        result.getJSONObject(i).getString("groupname")
                    )
                )
            }
            course.groupList.addAll(groupList)
            return (groupList)
    }
    suspend fun createSession(group: MoodleGroup,attendance: MoodleAttendance,
                      sessionStartTimeInSeconds:Long,
                      sessionDuration:Long, created_by_user_id:String, description:String):MoodleSession{
            val result = attRepo.createSessionMoodle(
                course_id = group.course.id,
                attendance_id = attendance.attendanceId,
                session_time = sessionStartTimeInSeconds.toString(),
                duration = sessionDuration.toString(),
                description = description,
                group_id = group.groupid,
                created_by_user_id = created_by_user_id)

            return getSessionInfo(session_id = result.getJSONObject(0).getString("id"),
                attendance=attendance,course=group.course,group=group)

    }
    suspend fun getFacultyListByCohort():List<BaseUserInfo>{
        val cohort_id:Int = AttendanceRepository.moodleUrlList.filter { s->s.id == moodleUrl.id}[0].cohort_id.toInt()
        val result = attRepo.getCohortMembersMoodle(cohort_id)
        val list = ArrayList<BaseUserInfo> ()
        for(i in 0 until result.length()){
            val item = result.getJSONObject(i)
            val userid = item.get("id").toString()
            val firstname = item.get("firstname").toString()
            val lastname = item.get("lastname").toString()
            val userName = item.get("username").toString()
            val fullname = item.get("fullname").toString()
            val emailaddr = item.get("email").toString()
            val image = item.get("profileimageurl").toString()
            list.add(BaseUserInfo(userid,userName,firstname,lastname,fullname,emailaddr,image))
        }

        return list
    }
    suspend fun getSessionList(course: MoodleCourse, attendance: MoodleAttendance, group: MoodleGroup,
                       needUserList:Boolean = false):List<MoodleSession> {

            val result = attRepo.getSessionsListMoodle(attendance.attendanceId)
            Log.i("MoodleRepository", "onSuccess: " + result.toString(4))
            val sessionList = ArrayList<MoodleSession>();
            for (i in 0 until result.length()) {
                val groupId = result.getJSONObject(i).getInt("groupid")
                if (groupId == group.groupid.toInt()) {
                    val obj = MoodleSession(
                        attendance, course, group,
                        result.getJSONObject(i).getString("id"),
                        result.getJSONObject(i).getString("description"),
                        result.getJSONObject(i).getString("statusset"),
                        result.getJSONObject(i).getString("sessdate"),
                        result.getJSONObject(i).getString("duration")
                    )
                    val statusList = result.getJSONObject(i).getJSONArray("statuses")
                    for (j in 0 until statusList.length()) {
                        obj.statusList.add(
                            MoodleSessionStatus(
                                session = obj,
                                id = statusList.getJSONObject(j).getString("id"),
                                name = statusList.getJSONObject(j).getString("acronym"),
                                description = statusList.getJSONObject(j)
                                    .getString("description"),
                                grade = statusList.getJSONObject(j).getInt("grade")
                            )
                        )
                    }
                    if (needUserList) {
                        val userList = result.getJSONObject(i).getJSONArray("users")
                        for (j in 0 until userList.length()) {
                            obj.userList.add(
                                MoodleSessionUser(
                                    id = userList.getJSONObject(j).getString("id"),
                                    firstName = userList.getJSONObject(j).getString("firstname"),
                                    lastName = userList.getJSONObject(j).getString("lastname"),
                                )
                            )
                        }
                    }
                    val attendanceLog = result.getJSONObject(i).getJSONArray("attendance_log")
                    for (j in 0 until attendanceLog.length()) {
                        obj.attendanceLog.add(
                            MoodleSessionAttendanceLog(
                                id = attendanceLog.getJSONObject(j).getString("id"),
                                studentId = attendanceLog.getJSONObject(j).getString("studentid"),
                                statusId = attendanceLog.getJSONObject(j).getString("statusid"),
                                remarks = attendanceLog.getJSONObject(j).getString("remarks"),
                            )
                        )
                    }
                    sessionList.add(obj)
                }

            }
            return (sessionList)

    }
    suspend fun getMessage(userid: String):MoodleQrMessage
    {
            appData.verifyData()
            val res = attRepo.getMessageMoodle(userid)

            return MoodleQrMessage(
                msgId = res.getString("id"),
                userIdFrom = res.getString("useridfrom"),
                userIdTo = res.getString("useridto"),
                fullMessage = res.getString("fullmessage"),
                timeCreated = res.getInt("timecreated")
            )
    }
    suspend fun getPlainMessage(userid: String):MoodleMessage
    {
        appData.verifyData()
        val res = attRepo.getMessageMoodle(userid)

        return MoodleMessage(
            msgId = res.getString("id"),
            userIdFrom = res.getString("useridfrom"),
            userIdTo = res.getString("useridto"),
            fullMessage = res.getString("fullmessage"),
            timeCreated = res.getInt("timecreated")
        )
    }
    suspend fun sendMessageToStudents(message:QRMessageData, userList:List<MoodleUserInfo>):Boolean{
        val res = attRepo.sendMessageMoodle(userList.map { it.id },QRMessageData.getQRMessageString(message))
        Log.i(TAG, "sendMessageToStudents: ${res.toString(4)}")
        //val resObject = res.getJSONObject(0)
        //val errorMessage = resObject.getString("errormessage")
        return true
    }
    suspend fun sendToStopAttToStudents( userList:List<MoodleUserInfo>):Boolean{
        val res = attRepo.sendMessageMoodle(userList.map { it.id },QRMessageData.getStopAttString())
        Log.i(TAG, "sendToStopAttToStudents: ${res.toString(4)}")
        //val resObject = res.getJSONObject(0)
        //val errorMessage = resObject.getString("errormessage")
        return true
    }
    suspend fun getSessionInfo(session_id:String,
                       course: MoodleCourse,
                       attendance: MoodleAttendance,
                       group: MoodleGroup):MoodleSession{
            val result = attRepo.getSessionMoodle(session_id)
            val groupId = result.getInt("groupid")
            if (groupId == group.groupid.toInt()) {
                val obj = MoodleSession(
                    attendance, course, group,
                    result.getString("id"),
                    result.getString("description"),
                    result.getString("statusset"),
                    result.getString("sessdate"),
                    result.getString("duration")
                )
                val statusList = result.getJSONArray("statuses")
                for (j in 0 until statusList.length()) {
                    obj.statusList.add(
                        MoodleSessionStatus(
                            session = obj,
                            id = statusList.getJSONObject(j).getString("id"),
                            name = statusList.getJSONObject(j).getString("acronym"),
                            description = statusList.getJSONObject(j)
                                .getString("description"),
                            grade = statusList.getJSONObject(j).getInt("grade")
                        )
                    )
                }

                val userList = result.getJSONArray("users")
                for (j in 0 until userList.length()) {
                    obj.userList.add(
                        MoodleSessionUser(
                            id = userList.getJSONObject(j).getString("id"),
                            firstName = userList.getJSONObject(j).getString("firstname"),
                            lastName = userList.getJSONObject(j)
                                .getString("lastname")
                        )
                    )
                }

                val attendanceLog = result.getJSONArray("attendance_log")
                for (j in 0 until attendanceLog.length()) {
                    obj.attendanceLog.add(
                        MoodleSessionAttendanceLog(
                            id = attendanceLog.getJSONObject(j).getString("id"),
                            studentId = attendanceLog.getJSONObject(j)
                                .getString("studentid"),
                            statusId = attendanceLog.getJSONObject(j).getString("statusid"),
                            remarks = attendanceLog.getJSONObject(j).getString("remarks"),
                        )
                    )
                }
                return (obj)
            }
            else{
                throw Exception("Error: getSessionInfo Group ID not match")
            }
    }

    fun getAttendance(course: MoodleCourse):MoodleAttendance {
        val it = courseAttMap
        val courseAttendance = it.getJSONObject(course.id)
        val attendanceName = courseAttendance.getString("name")
        val attendanceId = courseAttendance.getString("id")
        return MoodleAttendance(course, attendanceId, attendanceName)
    }

    /**
     * This method returned data is used to generate QR Code image for faculty app.
     */
    fun getQRDataString(data:QRMessageData): String {
        return QRMessageData.getQRMessageString(data)
    }
    private fun verifyStudent(data:QRMessageData):Boolean{
        appData.verifyData()
        val current = Utility().getCurrenMillis()/1000
        if(data.sessionStartDate <= current  && data.sessionEndDate >= current){
            if(data.attendanceStartDate <= current && data.attendanceEndDate >= current){
                return true
            }
        }
        return false
    }
    suspend fun takePresentAttendance(scannedQRStringResponse:String, student_user_id: String):Boolean {
        appData.verifyData()
        val message = getPlainMessage(student_user_id)
        if(QRMessageData.isStopMessage(message.fullMessage))
            throw Exception("Session Time has been expired for Attendance.")
        val it = QRMessageData.getQRMessageObject(scannedQRStringResponse)
            ?: throw Exception("Invalid Scanned Data")
        /*if(it.session.isStudentPresentInSession(student_user_id))
            throw Exception("Student has same attendance as requested.")*/

        val status = it.getPresentStatusId()
        val stud = ArrayList<String>()
        stud.add(student_user_id)
        val res = attRepo.getUserByFieldMoodle("id",stud)
        Log.i(TAG,"Session id:${it.sessionId},User id:${student_user_id},FacultyId:${it.attendanceByFacultyId},status_id:${status.id},status_name:${status.name}")
        Log.i(TAG,"User info: $res")
        if (verifyStudent(it)) {
            val result = attRepo.takeAttendanceMoodle(
                session_id = it.sessionId,
                student_id = student_user_id,
                taken_by_id = it.attendanceByFacultyId, status_id = status.id, status_set = status.name)
            return (result)
        } else {

            throw Exception("Session Time has been expired for Attendance.")
        }
    }
    suspend fun takePresentAttendance(session:MoodleSession,
                                      faculty_info:MoodleUserInfo,
                                      student_user_id: String,isStudentPresent: Boolean):Boolean {
        appData.verifyData()
        /*if(session.isStudentPresentInSession(student_user_id) == isStudentPresent)
            throw Exception("Student has same attendance as requested.")*/
        val status = if(isStudentPresent) session.getPresentStatusId() else session.getAbsentStatusId()
            val result = attRepo.takeAttendanceMoodle(
                session_id = session.sessionId,
                student_id = student_user_id,
                taken_by_id = faculty_info.id, status_id = status.id, status_set = status.name)
            return (result)
    }
    suspend fun createAttendance(course:MoodleCourse,attendanceName:String):MoodleAttendance{
            val result = attRepo.createAttendanceMoodle(course.id,attendanceName)
            return (MoodleAttendance(course, result.getJSONObject(0).getString("id"),attendanceName))
    }
    suspend fun login(recievedMoodleUsername:String, recievedmoodlePassword:String):Boolean {
        appData.verifyData()
        val list = getFacultyListByCohort()
        if(list.find { it.username == recievedMoodleUsername } == null)
            return false
        val result = attRepo.login(recievedMoodleUsername, recievedmoodlePassword)
        return (result)
    }
    fun getURLtoBitmap(imageUrl:String): Bitmap?
    {
        return convertUrlToBitmap(attRepo.getFileTokenURL(imageUrl))
    }
    private fun convertUrlToBitmap(url: String): Bitmap? {
        val newurl: URL
        var bitmap: Bitmap? = null
        try {
            val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)
            newurl = URL(url)
            bitmap = BitmapFactory.decodeStream(newurl.openConnection().getInputStream())
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return bitmap
    }
    fun convertUrlToBase64(url: String): String {
        val newurl: URL
        val bitmap: Bitmap
        var base64: String = ""
        try {
            val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)
            newurl = URL(url)
            bitmap = BitmapFactory.decodeStream(newurl.openConnection().getInputStream())
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            base64 = Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return base64
    }
}