package com.guni.uvpce.moodleapplibrary.model

import org.json.JSONObject

interface ModelBase {
    fun toJsonObject(): JSONObject
}