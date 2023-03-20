package com.guni.uvpce.moodleapplibrary

import android.app.ProgressDialog
import android.graphics.Bitmap
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.guni.uvpce.moodleapplibrary.model.*
import com.guni.uvpce.moodleapplibrary.repo.*
import com.guni.uvpce.moodleapplibrary.util.*
import kotlinx.coroutines.*
import java.io.ByteArrayOutputStream
import java.util.*


class MainActivity : AppCompatActivity() {
    var modelRepo:ModelRepository? =null
    val TAG = "MainActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val btn1 = findViewById<Button>(R.id.btn1)
        val tv1 = findViewById<TextView>(R.id.tv1)
        //val iv1 = findViewById<ImageView>(R.id.iv1)
        tv1.setTextIsSelectable(true)
        val volleyApi = VolleyApi(this)
        tv1.movementMethod = ScrollingMovementMethod()

        try {
            GlobalScope.launch{
                modelRepo = ModelRepository.getModelRepo(this@MainActivity)
            }
        }catch (e:Exception){
            Log.e("TAG", "onCreate: Error while creating Model Repository error string:$e", e)
        }
        val mProgressDialog = ProgressDialog(this)
        mProgressDialog.setTitle("URL Fetch")
        mProgressDialog.setMessage("Processing")
        btn1.setOnClickListener {
            val i = (Date().time / 1000).toInt()
            var string = ""
            val mode ="TestCourse"

            MainScope().launch {
                if (mode == "TestAPIConfig") {

                    val it2 = ApiConfig(this@MainActivity).getMoodleData()
                    string += it2.joinToString("\n")
                    val it1 = ApiConfig(this@MainActivity).getMapCourseData()
                    string += it1.joinToString("\n")
                    tv1.text = string

                }
                else if (mode == "TestCourse") {
                    val userInfo = modelRepo!!.getUserInfo("admin")
                    Log.i(TAG, "onCreate: admin UserInfo:$userInfo")
                    val courseList = modelRepo!!.getCourseListEnrolledByUser("admin")
                    //string += "\nCourses:"+courseList.joinToString("\n")
                    val selectedCourse = courseList[5]
                    modelRepo!!.getGroupList(selectedCourse)
                    //string += "Groups:"+groupList.joinToString("\n")
                    Log.i("MainActivity", "onCreate: Selected Course:$selectedCourse")
                    string += "\n\nSelected Course = $selectedCourse\n"
                    val selectedGroup = selectedCourse.groupList[5]
                    Log.i("MainActivity", "onCreate: Selected Course:$selectedGroup")
                    string += "\n\nSelected Group = $selectedCourse\n"
                    //Test Login
                    val user = modelRepo!!.getUserInfo("hms")

                    string += user.toString()

                    val attendance = modelRepo!!.getAttendance(selectedCourse)
                    string += "\nAttendance:\n$attendance\n"
                    Log.i("MainActivity", "onCreate: ${attendance.toString()}")
                    /*modelRepo.createSession(selectedGroup,attendance,
                                                                created_by_user_id=user.id,
                                                            sessionStartTimeInSeconds = Utility().getSeconds(11,0)
                                                            , sessionDuration = Utility().getDurationInSeconds(0,50),
                                                                description = "Session taken by HMS",
                                                                onError = {resultError->
                                                                    Log.i("MainActivity", "createSession: Error:$resultError")
                                                                },
                                                                onReceiveData = {
                                                                    session->
                                                                    Log.i("MainActivity", "createSession: Session created Successfully:\n$session")
                                                                    string += "\nSession:\n$session\n"

                                                                }
                                                            )*/
                    val sessionList = modelRepo!!.getSessionList(
                        selectedCourse,
                        attendance,
                        selectedGroup,
                        needUserList = true,
                    )
                    string += "\n" + (sessionList.joinToString("\n") ?: "")
                    val selectedSession = sessionList.get(0)
                    string += "\n Attendance: Session Count:${
                        selectedSession.getAttendanceCount()
                    }"
                    val selectedSessionUser = selectedSession.userList[15]
                    val qrData = QRMessageData(
                        sessionId = selectedSession.sessionId,
                        selectedSession.sessionStartDate/1000,
                        selectedSession.sessionEndDate/1000,
                        selectedSession.course.id,
                        selectedSession.course.Name,
                        selectedSession.group.groupid,
                        selectedSession.group.groupName,
                        userInfo.id,
                        userInfo.id,
                        "abclat",
                        "abclong",
                        Utility().getSeconds(13, 0),
                        Utility().getSeconds(23, 0),
                        Utility().getDurationInSeconds(10, 0),
                        selectedSession.getCompactStatusList()
                    )
                    val qrString = modelRepo!!.getQRDataString(qrData)
                    Log.i(TAG, "onCreate: qrString: $qrString")
                    string += "\n\nQR String:$qrString\n\n"
                    val objj = QRMessageData.getQRMessageObject(qrString!!)
                    Log.i(TAG,"Message Data: ${qrData}")


                    val studentList = modelRepo!!.getStudentList(selectedSession.course,selectedSession.group)


                    modelRepo!!.sendToStopAttToStudents(studentList)
//                    if(modelRepo!!.sendMessageToStudents(objj,studentList))
//                    {
//                        val objMessage = modelRepo!!.getMessage(studentList[1].id)
//                        val jsonResult = modelRepo?.takePresentAttendance(objMessage.fullMessage, studentList[1].id)
//                        Log.i(TAG,"Take Attendance: $jsonResult")
//                    }
                    val objMessage = modelRepo!!.getPlainMessage(studentList[1].id)
                    val jsonResult = modelRepo?.takePresentAttendance(objMessage.fullMessage, studentList[1].id)
                    Log.i(TAG,"Take Attendance: $jsonResult")





//                    for (stu in studentList){
//                        try{
//                            val objMessage = modelRepo!!.getMessage(stu.id)
//                            Log.i(TAG, "onCreate: Get Message of USerName:${stu.username} message:${objMessage}")
//                        }catch (e:Exception){
//                            Log.e(TAG, "onCreate: Error of User Id:${stu.username} Error:$e", e)
//                        }
//
//                    }

                    //modelRepo!!.sendMessageToStudents(objj,studentList)
                    //Log.i(TAG, "onCreate: qrDataObj=$objj")
                    /*val student_id = selectedSessionUser.id
                    val jsonResult =
                            modelRepo?.takePresentAttendance(qrString, student_id,)

                    string += "\nFor User:$selectedSessionUser ${jsonResult}\n"*/
                    //string += "\n After Attendance: Session Count:${selectedSession.getAttendanceCount().toString()}"
                    tv1.text = string
                    /*MoodleAttendanceBulk(modelRepo,courseList).
                                                        createAttendanceInBulk(onSuccess = {attendanceList->
                                                            var log = attendanceList.joinToString("\n")
                                                            Log.i("MainActivity:Sample", "onCreate,Attendance:\n $log")
                                                            string += log
                                                        })*/
                }
                else if(mode == "TestFacultyCohort"){
                    val facultyList = modelRepo!!.getFacultyListByCohort()
                    Log.i(TAG, "onCreate: facultyList:${facultyList.joinToString("\n")}")
                }
                else if(mode=="Image")
                {
                    mProgressDialog.show()
                    val res = ModelRepository.getMoodleUrlList(this@MainActivity)
                    tv1.text= res[0].url
                    ModelRepository.setMoodleUrlSetting(this@MainActivity,res[0])
                    modelRepo= ModelRepository.getModelRepo(this@MainActivity)
                    val res1 = modelRepo!!.isStudentRegisterForFace(this@MainActivity,"20012011047")
//                    tv1.text = modelRepo!!.convertUrlToBase64()
                    tv1.text=res1.hasUserUploadImg.toString()
                    mProgressDialog.dismiss()

                }
                else if(mode == "message_conversation")
                {

                }

            }
//            try {
//                GlobalScope.launch{
//                    modelRepo = ModelRepository.getModelRepo(this@MainActivity)
//                    runOnUiThread {
//                        iv1.setImageBitmap(modelRepo!!.getURLtoBitmap("http://202.131.126.214/pluginfile.php/3366/user/icon/classic/f1?rev=22353"))
//                    }
//
//                }
//            }catch (e:Exception){
//                Log.e("TAG", "onCreate: Error while creating Model Repository error string:$e", e)
//            }

/*            val attRepo = AttendanceRepository(this,
                "http://202.131.126.214",
                "CoreToken",
                "AttendanceToken",
                "FileToken")
            attRepo.sendMessageMoodle(this,"5","null","null","null","null","null","null","null","null",object:ServerCallback{
                override fun onSuccess(result: JSONArray) {
                    tv1.text=result.toString(4)
                }

                override fun onError(result: String) {
                    tv1.text=result
                }

            })
            attRepo.getFacultyInfoMoodle(this,"admin", object:ServerCallback{
                override fun onSuccess(result: JSONArray) {
                    tv1.text=result.toString(4)
                }

                override fun onError(result: String) {
                    tv1.text=result
                }

            })
            attRepo.createAttendanceMoodle(this,"34","dk6514",object : ServerCallback{
                override fun onSuccess(result: JSONArray) {
                    tv1.text=result.toString(4)
                }

                override fun onError(result: String) {
                    tv1.text=result
                }
            })
            attRepo.getUserInfoMoodle(this,"vrp",object:ServerCallback{
                override fun onSuccess(result: JSONArray) {
                    tv1.text=result.toString(4)
                }

                override fun onError(result: String) {
                    tv1.text=result
                }
            })
            attRepo.createSessionMoodle(this,"34","56", i.toString(),"100000","30",object:ServerCallback{
                override fun onSuccess(result: JSONArray) {
                    tv1.text=result.toString(4)
                }

                override fun onError(result: String) {
                    tv1.text=result
                }

            })
            attRepo.getSessionsListMoodle(this,"52", object:ServerCallback{
                override fun onSuccess(result: JSONArray) {
                    tv1.text=result.toString(4)
                }

                override fun onError(result: String) {
                    tv1.text=result
                }

            })
            attRepo.getCourseGroupsMoodle(this,"35",object:ServerCallback{
                override fun onSuccess(result: JSONArray) {
                    tv1.text=result.toString(4)
                }

                override fun onError(result: String) {
                    tv1.text=result
                }

            })
            attRepo.getUserCoursesListMoodle(this,"admin",object :ServerCallback{
                override fun onError(result: String) {
                    tv1.text=result
                }

                override fun onSuccess(result: JSONArray) {
                    tv1.text=result.toString(4)
                }
            })
            val bitmap = BitmapFactory.decodeResource(resources, R.drawable.gnu_logo)
            var file_content = encodeBitmapImage(bitmap)
            attRepo.uploadFileMoodle(this,"user","draft","0","/","moodleProfile.jpg",file_content,"user","2",object:
                ServerCallback {
                override fun onSuccess(result: JSONArray) {
                    tv1.text=result.toString(4)
                }

                override fun onError(result: String) {
                    tv1.text=result
                }
            })

            attRepo.updatePictureMoodle(this,"463630892","2",object :ServerCallback{
                override fun onSuccess(result: JSONArray) {
                    tv1.text=result.toString(4)
                }

                override fun onError(result: String) {
                    tv1.text=result
                }

            })

            attRepo.getMessageMoodle(this,"5","conversations","2",object: ServerCall {
                override fun onSuccess(result: JSONObject) {
                    tv1.text=result.toString(4)
                }

                override fun onError(result: String) {
                    tv1.text=result
                }

            })
            attRepo.getCategoriesMoodle(this,object :ServerCallback{
                override fun onSuccess(result: JSONArray) {
                    tv1.text=result.toString(4)
                }

                override fun onError(result: String) {
                    tv1.text=result
                }
            })
            attRepo.takeAttendanceMoodle(this,"434","2","5","22","21",object:ServerCallback{
                override fun onSuccess(result: JSONArray) {
                    tv1.text=result.toString(4)
                }

                override fun onError(result: String) {
                    tv1.text=result
                }
            })
            attRepo.getCohorts(this,object:ServerCallback{
                override fun onSuccess(result: JSONArray) {
                    tv1.text=result.toString(4)
                }

                override fun onError(result: String) {
                    tv1.text=result
                }
            })
            val arrayList = ArrayList<String>()
            arrayList.add("5")
            arrayList.add("2")
            attRepo.getUserByFieldMoodle(this,"id",arrayList,object :ServerCallback{
                override fun onSuccess(result: JSONArray) {
                    tv1.text=result.toString(4)
                }

                override fun onError(result: String) {
                    tv1.text=result
                }
            })
            attRepo.getCohortMembersMoodle(this,26,object :ServerCallback{
                override fun onSuccess(result: JSONArray) {
                    tv1.text=result.toString(4)
                }

                override fun onError(result: String) {
                    tv1.text=result
                }
            })
            var url="http://202.131.126.214/pluginfile.php/3370/user/icon/classic/f1?rev=21913"
            var token = "FileToken"
            tv1.text = attRepo.resolveImgURLMoodle(url,token)*/

        }
        tv1.setOnClickListener {
            /*val cm: ClipboardManager =this.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cm.setText(tv1.getText())
            Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show()*/
        }

    }
    private fun encodeBitmapImage(bitmap: Bitmap):String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val bytesofimage = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(bytesofimage, Base64.DEFAULT)
    }

}
