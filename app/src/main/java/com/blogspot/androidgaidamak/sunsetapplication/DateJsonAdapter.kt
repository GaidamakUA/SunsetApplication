package com.blogspot.androidgaidamak.sunsetapplication

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import java.text.SimpleDateFormat
import java.util.*

class DateJsonAdapter : JsonAdapter<Date>() {
    private val HACK_FORMAT = SimpleDateFormat("dd/MM/yyyy", Locale.UK)
    private val DATE_FORMAT = SimpleDateFormat("dd/MM/yyyy hh:mm:ss aa", Locale.UK)

    init {
        DATE_FORMAT.timeZone = TimeZone.getTimeZone("UTC")
        HACK_FORMAT.timeZone = TimeZone.getTimeZone("UTC")
    }

    override fun fromJson(reader: JsonReader?): Date? {
        // To prevent parsing for 1970
        val string = HACK_FORMAT.format(Date()) + " " + reader?.nextString()
        return DATE_FORMAT.parse(string)
    }

    override fun toJson(writer: JsonWriter?, value: Date?) {
        throw NotImplementedError("Doesn't meant to send data. Implement if necessary");
    }
}