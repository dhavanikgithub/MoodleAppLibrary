package com.guni.uvpce.moodleapplibrary.repo

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

interface iAttendanceRepository {

    /**
     * getUserInfoMoodle() use for get user info by user name.
     * @param UserName String
     * @param onSuccess JSONArray
     * @param onError String
     *
     * @return UserInfo - JSONArray
     * @see <a href="https://github.com/dhavanikgithub/attendance_moodle_api_lib/blob/master/functions%20response/getFacultyInfoMoodle.txt">Sample Response</a>
     */
    suspend fun getUserInfoMoodle(username:String):JSONArray

    /**
     * getFacultyInfoMoodle() use for get faculty info by user name.
     * @param FacultyName String
     * @param onSuccess JSONArray
     * @param onError String
     *
     * @return FacultyInfo - JSONArray
     * @see <a href="https://github.com/dhavanikgithub/attendance_moodle_api_lib/blob/master/functions%20response/getFacultyInfoMoodle.txt">Sample Response</a>
     */
    suspend fun getFacultyInfoMoodle(faculty_name: String):JSONArray

    /**
     * getUserCoursesListMoodle() use for get courses list by user name.
     * @param UserName String
     * @param onSuccess JSONArray
     * @param onError String
     *
     * @return List of Courses - JSONArray
     * @see <a href="https://github.com/dhavanikgithub/attendance_moodle_api_lib/blob/master/functions%20response/getUserCoursesListMoodle.txt">Sample Response</a>
     */
    suspend fun getUserCoursesListMoodle(username:String):JSONArray

    /**
     * createAttendanceMoodle() use for create an attendance.
     * @param CourseID String
     * @param AttendanceName String
     * @param onSuccess JSONArray
     * @param onError String
     *
     * @return attendance id - JSONArray
     * @see <a href="https://github.com/dhavanikgithub/attendance_moodle_api_lib/blob/master/functions%20response/createAttendanceMoodle.txt">Sample Response</a>
     */
    suspend fun createAttendanceMoodle(course_id:String, attendance_name:String):JSONArray

    /**
     * createSessionMoodle() use for create session inside attendance.
     * @param CourseID String
     * @param AttendanceID String
     * @param SessionTime String
     * @param Created_By_User_Id String
     * @param Duration String
     * @param GroupID  String
     * @param onSuccess JSONArray
     * @param onError String
     *
     * @return session id - JSONArray
     * @see <a href="https://github.com/dhavanikgithub/attendance_moodle_api_lib/blob/master/functions%20response/createSessionMoodle.txt">Sample Response</a>
     */
    suspend fun createSessionMoodle(course_id:String,attendance_id:String,
                            session_time:String,
                            created_by_user_id:String,
                            duration:String,description:String,
                            group_id:String):JSONArray

    /**
     * getCourseGroupsMoodle() use for list Groups of specific Course.
     * @param CourseID String
     * @param onSuccess JSONArray
     * @param onError String
     *
     * @return GroupsInfo - JSONArray
     * @see <a href="https://github.com/dhavanikgithub/attendance_moodle_api_lib/blob/master/functions%20response/getCourseGroupsMoodle.txt">Sample Response</a>
     */
    suspend fun getCourseGroupsMoodle(course_id: String):JSONArray

    /**
     * sendMessageMoodle() use for sending a message on Moodle.
     *
     *
     * @return MessageInfo - JSONArray
     * @see <a href="https://github.com/dhavanikgithub/attendance_moodle_api_lib/blob/master/functions%20response/sendMessangeMoodle.txt">Sample Response</a>
     */
    suspend fun sendMessageMoodle( userList:List<String>, qrMessage:String):JSONArray

    /**
     * takeAttendanceMoodle() use for mark attendance on moodle.
     * @param SessionID String
     * @param StudentID String
     * @param TakenByID String
     * @param StatusID String
     * @param SetStatusID String
     * @param onSuccess Boolean
     * @param onError String
     *
     * @return Boolean
     */
    suspend fun takeAttendanceMoodle(session_id:String,student_id:String,taken_by_id:String,status_id:String, status_set:String):Boolean

    /**
     * getSessionMoodle() use to retrieve session data.
     * @param session_id String
     * @param onSuccess JSONObject
     * @param onError String
     *
     * @return SessionData - JSONObject
     */
    suspend fun getSessionMoodle(session_id: String):JSONObject
    /**
     * getSessionsListMoodle() use for list out all sessions of an attendance.
     * @param AttendanceID String
     * @param onSuccess JSONArray
     * @param onError String
     *
     * @return SessionsList - JSONArray
     * @see <a href="https://github.com/dhavanikgithub/attendance_moodle_api_lib/blob/master/functions%20response/getSessionsListMoodle.txt">Sample Response</a>
     */
    suspend fun getSessionsListMoodle(attendance_id:String):JSONArray

    /**
     * uploadFileMoodle() use for upload file on moodle.
     * @param Component String
     * @param FileArea String
     * @param ItemID String
     * @param FilePath String
     * @param FileName String
     * @param FileContent String
     * @param ContextLevel String
     * @param InstanceID String
     * @param onSuccess JSONArray
     * @param onError String
     *
     * @return UploadFileInfo - JSONArray
     * @see <a href="https://github.com/dhavanikgithub/attendance_moodle_api_lib/blob/master/functions%20response/uploadFileMoodle.txt">Sample Response</a>
     */
    suspend fun uploadFileMoodle(component:String,file_area:String,item_id:String,file_path:String,
                                 file_name:String,file_content:String,context_level:String,
                                 instanceid:String):JSONArray

    /**
     * updatePictureMoodle() use for use for update profile picture in moodle.
     * @param DraftItemID String
     * @param UserID String
     * @param onSuccess JSONArray
     * @param onError String
     *
     * @return UpdatedPictureInfo - JSONArray
     * @see <a href="https://github.com/dhavanikgithub/attendance_moodle_api_lib/blob/master/functions%20response/updatePictureMoodle.txt">Sample Response</a>
     */
    suspend fun updatePictureMoodle(draft_item_id:String,user_id:String):JSONArray

    /**
     * getMessageMoodle() use for get message from moodle.
     * @param UserID String
     * @param Type String
     * @param Read String
     * @param onSuccess JSONObject
     * @param onError String
     *
     * @return Message - JSONObject
     * @see <a href="https://github.com/dhavanikgithub/attendance_moodle_api_lib/blob/master/functions%20response/getMessageMoodle.txt">Sample Response</a>
     */
    suspend fun getMessageMoodle(user_id:String):JSONObject

    /**
     * getCategoriesMoodle() use for get categories from moodle.
     * @param onSuccess JSONArray
     * @param onError String
     *
     * @return CategoriesInfo - JSONArray
     * @see <a href="https://github.com/dhavanikgithub/attendance_moodle_api_lib/blob/master/functions%20response/getCategoriesMoodle.txt">Sample Response</a>
     */
    suspend fun getCategoriesMoodle():JSONArray

    /**
     * getCohortsMoodle() use to get cohorts from moodle.
     * @param onSuccess JSONArray
     * @param onError String
     */
    suspend fun getCohortsMoodle():JSONArray

    /**
     * getCohortMembersMoodle() use to get members in particular cohort from moodle.
     * @param cohort_id Int
     * @param onSuccess JSONArray
     * @param onError String
     * @return ChortMembersInfo - JSONArray
     */
    suspend fun getCohortMembersMoodle(cohort_id:Int):JSONArray

    /**
     * getUserByFieldMoodle() use to get user's fileds info from moodle.
     * @param field String
     * @param value ArrayList<String>
     * @param onSuccess JSONArray
     * @param onError String
     * @return UserFieldsInfo(i.e., profileimageurl, fullname, email, and so on.) - JSONArray
     */
    suspend fun getUserByFieldMoodle(field:String,value:ArrayList<String>):JSONArray

    /**
     * resolveImgURLMoodle() use to resolve the moodle URL.
     * @param url String
     * @param token String
     * @param onSuccess String
     * @param onError String
     *
     * @return Resolved URL - String*/
    suspend fun resolveImgURLMoodle(
        url: String,
        token: String
    ):String

    /**
     * getEnrolledUserByCourseGroupMoodle() use to get info of enrolled users in course of group of courses.
     * @param courseid String
     * @param groupid String
     * @param onSuccess JSONArray
     * @param onError String
     *
     * @return EnrolledUsersInCourse - JSONArray*/
    suspend fun getEnrolledUserByCourseGroupMoodle( courseid: String,groupid:String ):JSONArray

    /**
     * getTeacherUserByCourseGroupMoodle() use to get info of teachers in course of group of courses.
     * @param courseid String
     * @param groupid String
     * @param roleid String
     * @param onSuccess JSONArray
     * @param onError String
     *
     * @return Teachers of Course - JSONArray
     */
    suspend fun getTeacherUserByCourseGroupMoodle(courseid: String, groupid:String, roleid:String):JSONArray

    /**
     * login() use
     * @param recievedMoodleUsername String
     * @param recievedMoodlePassword String
     *
     * @return Successfully logged in or not (true or false) - Boolean
     */
    suspend fun login(recievedMoodleUsername:String, recievedMoodlePassword:String):Boolean
}