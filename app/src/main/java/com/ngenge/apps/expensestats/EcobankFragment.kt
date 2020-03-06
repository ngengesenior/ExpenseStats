package com.ngenge.apps.expensestats

import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.Telephony
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.anychart.AnyChart
import com.anychart.charts.Cartesian
import com.anychart.enums.Anchor
import com.anychart.enums.HoverMode
import com.anychart.enums.Position
import com.anychart.enums.TooltipPositionMode
import kotlinx.android.synthetic.main.fragment_ecobank.*

/**
 * A simple [Fragment] subclass.
 */
class EcobankFragment : Fragment() {

    private lateinit var projection: Array<String>
    private lateinit var smsInboxUri: Uri
    private lateinit var selectionArgs:Array<String>
    private lateinit var cartesian: Cartesian
    private lateinit var selectionClause:String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_ecobank, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        smsInboxUri = Telephony.Sms.Inbox.CONTENT_URI
        projection = arrayOf(
            Telephony.Sms.Inbox._ID,
            Telephony.Sms.Inbox.BODY,
            Telephony.Sms.Inbox.DATE
        )
        cartesian = AnyChart.cartesian()
        selectionArgs = arrayOf("ECOBANK")
        selectionClause = "${Telephony.Sms.Inbox.ADDRESS} LIKE ?"
        any_chart_view.setProgressBar(progress_bar)

        if (Utils.askForPermissions(this.requireActivity())) {
            val cursor = requireContext().contentResolver.query(smsInboxUri,projection,selectionClause,selectionArgs,"date ASC")
            if (cursor != null) {
                plotEcobankStats(cursor)
            }
        }
        super.onViewCreated(view, savedInstanceState)
    }

    fun plotEcobankStats(cursor: Cursor) {
        val dataEntries = Utils.generateEcobankEntries(cursor)
        val cartesian = AnyChart.cartesian()
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray) {
        when (requestCode) {
            Utils.SMS_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    val cursor = requireContext().contentResolver.query(smsInboxUri,projection,selectionClause,selectionArgs,"date ASC")
                    if (cursor != null) {
                        plotEcobankStats(cursor)
                    }
                } else {
                    Utils.askForPermissions(this.requireActivity())
                }
                return
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}
