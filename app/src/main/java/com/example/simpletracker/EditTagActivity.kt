package com.example.simpletracker

import android.app.Activity
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.ToggleButton
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.util.*

class EditTagActivity : AppCompatActivity() {

    private val tagDatabase by lazy { TagDatabase.getDatabase(this).tagDao() }
    private lateinit var adapter: PointsRVAdapter
    private var localTag: Tag = Tag(
        0,
        "",
        "",
        Date(0),
        Calendar.getInstance(),
        false)
    private var pointsToDelete = mutableListOf<Tag.Point>()
    private var pointsToInsert = mutableListOf<Tag.Point>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_tag)

        localTag.tagId = intent.getIntExtra("tag_id", 0)

        //retrieve the tag from database
        getTagFromDatabase(localTag.tagId)

        setRecyclerView()
        observePoints()



        val reminderStuff = findViewById<LinearLayout>(R.id.reminder_linear_layout)
        val reminderHeader = findViewById<TextView>(R.id.reminder_header)
        val toggle = findViewById<ToggleButton>(R.id.reminder_toggle)
        val dataHeader = findViewById<TextView>(R.id.noteInfo)
        val timeSelected = findViewById<TextView>(R.id.time_selected)
        val timePicker = findViewById<TextView>(R.id.time_picker)
        val backDate = findViewById<Button>(R.id.backDate)

        //get from preferences
        val notificationsEnabled = PreferenceManager.getDefaultSharedPreferences(this)
            .getBoolean("notificationsEnabled", false)

        if(!notificationsEnabled){
            reminderStuff.visibility = View.GONE
            reminderHeader.visibility = View.GONE

            //should be redundant but won't hurt?
            alarmOff()

        } else {
            if (localTag.tagId == 0) {
                reminderStuff.visibility = View.GONE
                dataHeader.visibility = View.GONE
                backDate.visibility = View.GONE
                reminderHeader.text = getText(R.string.reminderStuff)
            } else {


                //check if alarm is set and put toggle to checked if so
                if (!localTag.reminderOn) {
                    //timeSelected.visibility = View.GONE
                    //timePicker.visibility = View.GONE
                    //TODO FIRST make alarm on/off more visible
                } else {
                    //timeSelected.visibility = View.VISIBLE
                    //timePicker.visibility = View.VISIBLE
                    toggle.isChecked = true
                }

                toggle.setOnClickListener {
                    localTag.reminderOn = toggle.isChecked
                    if (!localTag.reminderOn) {
                        timeSelected.visibility = View.GONE
                        timePicker.visibility = View.GONE
                    } else {
                        timeSelected.visibility = View.VISIBLE
                        timePicker.visibility = View.VISIBLE
                    }
                }
            }
        }

        val deleteButton = findViewById<Button>(R.id.deleteTag)
        if(localTag.tagId == 0){
            deleteButton.visibility = View.GONE
        } else {
            deleteButton.setOnClickListener {

                val builder = AlertDialog.Builder(this)
                builder.setMessage("Are you sure you want to delete this tag?")
                    .setCancelable(false)
                    .setNegativeButton("No") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .setPositiveButton("Yes") { _, _ ->
                        lifecycleScope.launch {
                            getTagFromDatabase(localTag.tagId)
                            tagDatabase.deletePointsByTag(localTag.tagId)
                            tagDatabase.deleteTag(localTag)
                            alarmOff()
                            //close the activity
                            setResult(Activity.RESULT_OK, intent)
                            finish()
                        }
                    }
                val alert = builder.create()
                alert.show()
            }
        }

        val saveButton = findViewById<Button>(R.id.saveTag)
        saveButton.setOnClickListener {

            localTag.tagName = findViewById<TextView>(R.id.editTagName).text.toString()
            localTag.tagNote = findViewById<TextView>(R.id.editTagNote).text.toString()

            if(localTag.tagName == "") {
                Snackbar.make(it, getString(R.string.not_empty), Snackbar.LENGTH_LONG).show()
            } else {
                lifecycleScope.launch {
                    if (localTag.tagId == 0) {
                        tagDatabase.insert(localTag)
                    } else {
                        tagDatabase.updateTag(localTag)
                        if (localTag.reminderOn) alarmOn()
                        else alarmOff()
                        for (point in pointsToDelete) {tagDatabase.deletePoint(point)}
                        for (point in pointsToInsert) {tagDatabase.insert(point)}
                    }
                }
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        }

        val timePickerButton = findViewById<Button>(R.id.time_picker)
        timePickerButton.setOnClickListener {pickTime()}




        findViewById<Button>(R.id.backDate).setOnClickListener() {
            val backDateIntent = Intent(this, Backdate::class.java)
            backDateIntent.putExtra("tag_id", localTag.tagId)
            backdateResultLauncher.launch(backDateIntent)
        }

    }

    private val backdateResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {

                /*val newPoint = Tag.Point(0,
                    result.data?.getIntExtra("tagId",0) ?: 0,
                    Date(result.data?.getLongExtra("time",0) ?: 0),
                    result.data?.getIntExtra("severity",0) ?:0 )

                if (newPoint.tagIdForeignKey == 0 || newPoint.time.equals(0) || newPoint.severity == 0) {
                } else {
                  pointsToInsert.add(newPoint)
                    //add point to recyclerview list?
                } // */
            }
        }

    fun cancel(view: View? = null) { finish() }

    private fun getTagFromDatabase(tagId: Int, view: View? = null) {
        lifecycleScope.launch {
            tagDatabase.getTagById(tagId).collect {
                if (it != null) {
                    localTag.tagName = it.tagName
                    localTag.tagNote = it.tagNote
                    localTag.dateAdded = it.dateAdded
                    localTag.reminder.set(Calendar.HOUR, it.reminder.get(Calendar.HOUR))
                    localTag.reminder.set(Calendar.MINUTE, it.reminder.get(Calendar.MINUTE))
                    localTag.reminderOn = it.reminderOn
                }

                findViewById<TextView>(R.id.editTagName).text = localTag.tagName
                findViewById<TextView>(R.id.editTagNote).text = localTag.tagNote
                findViewById<TextView>(R.id.time_selected).text = timeString()
                findViewById<ToggleButton>(R.id.reminder_toggle).isChecked = localTag.reminderOn

                logLocalTag()
            }
        }
    }

    private fun timeString():String {
        var timeString = ""
        timeString += if (localTag.reminder.get(Calendar.HOUR) != 0)
            "${localTag.reminder.get(Calendar.HOUR)}:"
        else
            "12:"
        if (localTag.reminder.get(Calendar.MINUTE) < 10 )
            timeString += "0"
        timeString += "${localTag.reminder.get(Calendar.MINUTE)} "
        timeString += if (localTag.reminder.get(Calendar.AM_PM) != 1)
            "AM"
        else
            "PM"
        return timeString
    }

    private fun logLocalTag(){
        Log.d("tagId","${localTag.tagId}")
        Log.d("tagName","${localTag.tagName}")
        Log.d("tagNote","${localTag.tagNote}")
        Log.d("dateAdded","${localTag.dateAdded}")
        Log.d("reminder","${localTag.reminder.timeInMillis} / "+timeString())
    }

    private fun setRecyclerView() {
        val pointsRecyclerview = findViewById<RecyclerView>(R.id.point_view)
        pointsRecyclerview?.layoutManager = LinearLayoutManager(this)
        pointsRecyclerview?.setHasFixedSize(true)
        adapter = PointsRVAdapter()
        adapter.setItemListener(object : RecyclerClickListener {


            // Tap the tag
            override fun onItemClick(position: Int) {
                //leave this empty for now?
            }

            //TODO: LAST
            //fix this so that its not using the same onSavePointClick as the tag recyclerview
            override fun onSavePointClick(position: Int, uninportant: Int) {
                val pointList = adapter.currentList.toMutableList()
                val pointId = pointList[position].pointId
                val tagIdForeignKey = pointList[position].tagIdForeignKey
                val severity = pointList[position].severity
                val time = pointList[position].time
                val removePoint = Tag.Point(pointId, tagIdForeignKey, time, severity)
                pointList.removeAt(position)
                adapter.submitList(pointList)
                pointsToDelete.add(removePoint)
            }

        })
        pointsRecyclerview?.adapter = adapter
    }

    private fun observePoints(view: View? = null) {
        lifecycleScope.launch {
            tagDatabase.getPointsByTag(intent.getIntExtra("tag_id", 0)).collect { pointsList ->
                if (pointsList.isNotEmpty()) {
                    Log.d("**observePoints**","${pointsList.size}")
                    adapter.submitList(pointsList)
                }
            }
        }
    }

    private fun pickTime(view: View? = null) {

        val txtTime = findViewById<TextView>(R.id.time_selected)

        // Launch Time Picker Dialog
        val timePickerDialog = TimePickerDialog(this,
            TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                localTag.reminder.set(Calendar.HOUR, hourOfDay)
                localTag.reminder.set(Calendar.MINUTE, minute)
                txtTime?.text = timeString()
            },
            localTag.reminder[Calendar.HOUR_OF_DAY],
            localTag.reminder[Calendar.MINUTE],
            false
        )
        timePickerDialog.show()
    }


    private fun alarmOff(view: View? = null) {


        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(this, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, localTag.tagId, alarmIntent, PendingIntent.FLAG_IMMUTABLE)
        alarmManager.cancel(pendingIntent)


        //Snackbar.make(findViewById(R.id.reminder_header), "Alarm canceled", Snackbar.LENGTH_LONG).show()
    }

    private fun alarmOn(view: View? = null) {

        if (localTag.tagId == 0) return

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(this, AlarmReceiver::class.java)
        alarmIntent.putExtra("tagName", localTag.tagName)
        alarmIntent.putExtra("tagNote", localTag.tagNote)
        alarmIntent.putExtra("tagId", localTag.tagId)
        val pendingIntent = PendingIntent.getBroadcast(this, localTag.tagId, alarmIntent, PendingIntent.FLAG_IMMUTABLE)

        val hour = localTag.reminder.get(Calendar.HOUR)
        val minute = localTag.reminder.get(Calendar.MINUTE)
        localTag.reminder.timeInMillis = System.currentTimeMillis()
        localTag.reminder.set(Calendar.HOUR, hour)
        localTag.reminder.set(Calendar.MINUTE, minute)
        if (localTag.reminder.timeInMillis < System.currentTimeMillis())
            localTag.reminder.add(Calendar.DAY_OF_YEAR,1)

        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            localTag.reminder.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        ) // */



        /*/just for debugging
        localTag.reminder.timeInMillis = System.currentTimeMillis()
        localTag.reminder.add(Calendar.SECOND, 30)
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, localTag.reminder.timeInMillis, pendingIntent)
        //alarmManager.set(AlarmManager.RTC_WAKEUP, localTag.reminder.timeInMillis, pendingIntent)
        // */



    }



}
