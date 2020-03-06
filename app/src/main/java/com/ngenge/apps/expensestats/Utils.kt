package com.ngenge.apps.expensestats

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.provider.Settings
import android.provider.Telephony
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.chart.common.dataentry.ValueDataEntry
import java.text.SimpleDateFormat
import java.util.regex.Matcher
import java.util.regex.Pattern

object Utils {
    val SMS_REQUEST_CODE: Int = 100

    val numberPattern = Pattern.compile("\\d+")
    val mobileMoneypattern1 = "You new balance"
    val mobileMoneypattern2 = "New account balance"
    val mobileMoneypattern3 = "New balance: FCFA"

    /**
     * @author Ngenge Senior
     * @param sms the mobile money sms to parse
     * @return The balance as a Long
     *
     * So far, there are three patterns that have been identified the way Mobile Money SMSs are sent with the balance
     * The idea is to split the message at the point where each pattern is found, get the last string and get the balance from the
     * string. The first number pattern in the string is the balance
     */
    fun getAmountFromMobileMoneyString(sms: String): Long? {
        var amount: Long? = null
        var finalString: String
        var matcher: Matcher
        when {
            sms.contains(mobileMoneypattern1, ignoreCase = true) -> {

                finalString = splitString(sms, mobileMoneypattern1)
                matcher = numberPattern.matcher(finalString)
                if (matcher.find()) {
                    amount = matcher.group().toLong()
                }
            }
            sms.contains(mobileMoneypattern2, ignoreCase = true) -> {

                finalString = splitString(sms, mobileMoneypattern2)
                matcher = numberPattern.matcher(finalString)
                if (matcher.find()) {
                    amount = matcher.group().toLong()
                }
            }
            sms.contains(mobileMoneypattern3, ignoreCase = true) -> {
                finalString = splitString(sms, mobileMoneypattern3)
                matcher = numberPattern.matcher(finalString)
                if (matcher.find()) {
                    amount = matcher.group().toLong()
                }
            }
            else -> { amount = null }
        }
        return amount
    }

    fun splitString(fromString: String, stringFromWhereToSplit: String): String {
        val strings = fromString.split(stringFromWhereToSplit)
        return strings[strings.size - 1]
    }


    fun generateMobileMoneyEntries(cursor: Cursor):MutableList<DataEntry> {
        val entries = mutableListOf<DataEntry>()
        if (cursor.count > 0) {

            while (cursor.moveToNext()) {
                val date = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.Inbox.DATE))
                val body = cursor.getString(cursor.getColumnIndex(Telephony.Sms.Inbox.BODY))
                val amount = getAmountFromMobileMoneyString(body)
                amount?.let {
                    val dateString = dateTimeToReadableFormat(date.toLong())
                    val dataEntry = ValueDataEntry(dateString,it)
                    entries.add(dataEntry)
                }
            }
        }
        return entries
    }


    fun generateEcobankEntries(cursor: Cursor):MutableList<DataEntry> {
        val entries = mutableListOf<DataEntry>()
        if (cursor.count >0 ) {

            while (cursor.moveToNext()) {
                val date = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.Inbox.DATE))
                val body = cursor.getString(cursor.getColumnIndex(Telephony.Sms.Inbox.BODY))
                if(body.contains("Solde",true)){
                    val extractedAmount = getNumberFromEcobankString(body)
                    entries.add(ValueDataEntry(dateTimeToReadableFormat(date.toLong()),extractedAmount))
                }
            }
        }
        return entries
    }

    private fun dateTimeToReadableFormat(dateTime: Long): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
        return dateFormat.format(dateTime)
    }

    private fun getNumberFromEcobankString(body: String):Long {
        val pattern = "Solde XAF"
        val strings = body.split(pattern)
        val stringWithNumber = strings[strings.size - 1].substringBefore(".")
        val stringWithNoComas = stringWithNumber.replace(",","")
        return stringWithNoComas.toLong()
    }

    private fun isPermissionsAllowed(activity: Activity): Boolean {
        return ContextCompat.checkSelfPermission(activity,
            Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED
    }

    fun askForPermissions(activity: Activity): Boolean {
         if (!isPermissionsAllowed(activity)) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_SMS)) {
                showPermissionDeniedDialog(activity)
            } else {
                ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.READ_SMS), SMS_REQUEST_CODE)
            }
            return false
        }
        return true
    }

    private fun showPermissionDeniedDialog(activity: Activity) {
        AlertDialog.Builder(activity)
            .setTitle("SMS Permission Denied")
            .setMessage("Permission is denied, Please allow permissions from App Settings.")
            .setPositiveButton("App Settings"
            ) { dialogInterface, i ->
                // send to app settings if permission is denied permanently
                val intent = Intent()
                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                val uri = Uri.fromParts("package", activity.packageName, null)
                intent.data = uri
                activity.startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

}