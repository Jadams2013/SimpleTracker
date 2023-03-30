package com.example.simpletracker

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.util.*

class Backdate : AppCompatActivity() {

    private val tagDatabase by lazy { TagDatabase.getDatabase(this).tagDao() }
    private var tagId = 0
    private var current: Calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_backdate)

        tagId = intent.getIntExtra("tag_id", 0)
        if (tagId == 0) {
            //throw exception?
            finish()
        }



        findViewById<Button>(R.id.saveBackdate).setOnClickListener() {
            var severity = findViewById<SeekBar>(R.id.seekBar).progress
            severity++

            /*val data = Intent()
            data.putExtra("tagID",tagId)
            data.putExtra("time", current.timeInMillis)
            data.putExtra("severity", severity)

            setResult(Activity.RESULT_OK, data)
            finish() // */

            lifecycleScope.launch {
                tagDatabase.insert(Tag.Point(0, tagId, Date(current.timeInMillis),severity))
                finish()
            }
        }

        findViewById<Button>(R.id.cancelBackdate).setOnClickListener() { finish() }

        findViewById<Button>(R.id.timeButton).setOnClickListener() { pickTime() }

        findViewById<Button>(R.id.dateButton).setOnClickListener() { pickDate() }



    }



    private fun pickDate(view: View? = null) {
        val txtTime = findViewById<TextView>(R.id.backdate_text)

        // Launch Time Picker Dialog
        val datePickerDialog = DatePickerDialog(this,
            DatePickerDialog.OnDateSetListener() { _, year, month, day ->
                current.set(Calendar.DAY_OF_MONTH, day)
                current.set(Calendar.YEAR, year)
                current.set(Calendar.MONTH, month)
                txtTime?.text = dateString()
            },
            current.get(Calendar.YEAR),
            current.get(Calendar.MONTH),
            current.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }



    private fun pickTime(view: View? = null) {

        val txtTime = findViewById<TextView>(R.id.backdate_text)

        // Launch Time Picker Dialog
        val timePickerDialog = TimePickerDialog(this,
            TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                current.set(Calendar.HOUR, hourOfDay)
                current.set(Calendar.MINUTE, minute)
                txtTime?.text = dateString()
            },
            current.get(Calendar.HOUR_OF_DAY),
            current.get(Calendar.MINUTE),
            false
        )
        timePickerDialog.show()
    }


    private fun dateString():String {
        var dateString = timeString()

        dateString += "\n ${current.get(Calendar.DAY_OF_MONTH)} " +
                "${current.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())} " +
                "${current.get(Calendar.YEAR)}"
        return dateString
    }

    private fun timeString():String {
        var timeString = ""
        timeString += if (current.get(Calendar.HOUR) != 0)
            "${current.get(Calendar.HOUR)}:"
        else
            "12:"
        if (current.get(Calendar.MINUTE) < 10 )
            timeString += "0"
        timeString += "${current.get(Calendar.MINUTE)} "
        timeString += if (current.get(Calendar.AM_PM) != 1)
            "AM"
        else
            "PM"
        return timeString
    }


}