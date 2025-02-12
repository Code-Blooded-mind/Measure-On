package com.example.measureon.utils

import android.content.Context
import android.content.Intent
import android.net.Uri

object SmsHelper {
    fun sendSms(context: Context, phoneNumber: String, message: String) {
        val smsIntent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:$phoneNumber"))
        smsIntent.putExtra("sms_body", message)
        context.startActivity(smsIntent)
    }
}
