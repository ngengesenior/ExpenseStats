package com.ngenge.apps.expensestats

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.Telephony
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.cursoradapter.widget.SimpleCursorAdapter
import com.anychart.AnyChart
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.chart.common.dataentry.ValueDataEntry
import com.anychart.charts.Cartesian
import com.anychart.enums.Anchor
import com.anychart.enums.HoverMode
import com.anychart.enums.Position
import com.anychart.enums.TooltipPositionMode
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat


class MainActivity : AppCompatActivity() {
    private val SMS_REQUEST_CODE: Int = 100
    private lateinit var projection:Array<String>
    private var selectionClause:String? = null
    private lateinit var selectionArgs:Array<String>
    private lateinit var smsInboxUri:Uri
    private lateinit var smsListItems:IntArray
    private lateinit var cartesian: Cartesian
    private val mobileMoneyPatterns = arrayListOf<String>("You new balance: ","New account balance:","New balance: FCFA" )

    private lateinit var adapter: SimpleCursorAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        smsInboxUri = Telephony.Sms.Inbox.CONTENT_URI
        projection = arrayOf(
            Telephony.Sms.Inbox._ID,
            Telephony.Sms.Inbox.BODY,
            Telephony.Sms.Inbox.DATE

        )
        any_chart_view.setProgressBar(progress_bar)

        cartesian = AnyChart.cartesian()

        val selectionClause = "${Telephony.Sms.Inbox.ADDRESS} LIKE ?"
        selectionArgs = arrayOf("ECOBANK")

        smsListItems = intArrayOf(R.id.smsBodyView,R.id.textDate)

        //val cursor = contentResolver.query(smsInboxUri,projection,selectionClause,selectionArgs,null)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_DENIED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_SMS)) {

                Toast.makeText(this,"Permissions are required to read SMS for Ecobank", Toast.LENGTH_LONG).show()
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_SMS),SMS_REQUEST_CODE)
            }
        } else {

            val cursor = contentResolver.query(smsInboxUri,projection,selectionClause,selectionArgs,"date ASC")

            cursor?.let {
                generateData(it)
            }





            adapter = SimpleCursorAdapter(
                applicationContext,
                R.layout.sms_layout,
                cursor,
                projection,
                smsListItems,
                0
            )


        }


    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        when(requestCode) {
            SMS_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {


                    val cursor = contentResolver.query(smsInboxUri,projection,selectionClause,selectionArgs,"date ASC")

                    cursor?.let {
                        generateData(it)
                    }
                    adapter = SimpleCursorAdapter(
                        applicationContext,
                        R.layout.sms_layout,
                        cursor,
                        projection,
                        smsListItems,
                        0
                    )


                } else {

                    Toast.makeText(this,"Permission to read SMS denied. Closing app",Toast.LENGTH_LONG).show()
                    finish()

                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun generateData(cursor:Cursor) {

        val dataEntries = mutableListOf<DataEntry>()
        if (cursor.count >0 ) {

            while (cursor.moveToNext()) {


                val date = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.Inbox.DATE))
                val body = cursor.getString(cursor.getColumnIndex(Telephony.Sms.Inbox.BODY))
                if(body.contains("Solde",true)){
                    val extractedAmount = getNumberFromEcobankString(body)
                    dataEntries.add(ValueDataEntry(dateTimeToReadableFormat(date.toLong()),extractedAmount))

                }


            }

        } else{
            Toast.makeText(this,"No messages from Ecobank",Toast.LENGTH_LONG).show()
            return
        }



        val column = cartesian.column(dataEntries)
        column.tooltip()
            .titleFormat("{%X}")
            .position(Position.CENTER_BOTTOM)
            .anchor(Anchor.CENTER_BOTTOM)
            .offsetX(0.0)
            .offsetY(5.0)
            .format("FCFA{%Value}{groupsSeparator: }")

        cartesian.animation(true)
        cartesian.title("Ecobank Balance against Date Time")
        cartesian.yScale().minimum(0.0)

        cartesian.yAxis(0).labels().format("FCFA{%Value}{groupsSeparator: }")

        cartesian.tooltip().positionMode(TooltipPositionMode.POINT)
        cartesian.interactivity().hoverMode(HoverMode.BY_X)

        cartesian.xAxis(0).title("Date Time")
        cartesian.yAxis(0).title("Balance")
        any_chart_view.setChart(cartesian)

    }

    /**
     * @param body The body of the text message received from Ecobank
     * Ecobank messages come with a particular pattern and there is a part that starts with 'Solde XAF'
     * and after this, comes the account balance. The duty of this method is to extract the amount from this string
     */
    private fun getNumberFromEcobankString(body: String):Long {
        val pattern = "Solde XAF"
        val strings = body.split(pattern)
        val stringWithNumber = strings[strings.size - 1].substringBefore(".")
        val stringWithNoComas = stringWithNumber.replace(",","")
        return stringWithNoComas.toLong()
    }

    private fun getAmountFromMobileMoneyString(body: String) {
        val pattern1 = "You new balance: "
        val pattern2 = "New account balance:"
        val pattern3 = "New balance: FCFA"
        var amount:Long
        if (body.contains(mobileMoneyPatterns[0],ignoreCase = true)) {
            val strings = body.split(mobileMoneyPatterns[0])
            amount = strings[strings.size - 1].substringBefore(" FCFA").toLong()
        } else if (body.contains(mobileMoneyPatterns[1])) {

        }


    }

    private fun dateTimeToReadableFormat(dateTime:Long):String {

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
        return dateFormat.format(dateTime)
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == R.id.privacy_policy) {
            startActivity(Intent(this,PrivacyPolicyActivity::class.java))
        }
        return true
    }
}
