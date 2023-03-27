package com.example.simpletracker

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity

import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.getDefaultNightMode
import androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.example.simpletracker.databinding.ActivityMainBinding
import kotlinx.coroutines.launch
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private val tagDatabase by lazy { TagDatabase.getDatabase(this).tagDao() }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)


        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow, R.xml.root_preferences
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)


        updateSettings()

    }



    //TODO LAST
    //make sharedPref a local variable so it isn't created every time a setting thing is done?


    private fun setNightMode(view: View? = null) {
        var sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        var pref = sharedPref.getString("dark","not retrieved")
        Log.d("**setNightMode**", "getDefaultNightMode is ${getDefaultNightMode()}, stored preference is $pref")
        when(pref) {
            "MODE_NIGHT_YES" -> setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            "MODE_NIGHT_NO" -> setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            else -> setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    private fun setFont(view: View? = null) {
        var sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        var pref = sharedPref.getString("font","not retrieved")
        Log.d("**setFont**", "$pref")
        when(pref) {
            "lexend" -> setTheme(R.style.Theme_SimpleTracker_lexend)
            "noto" -> setTheme(R.style.Theme_SimpleTracker_noto)
            "shantell" -> setTheme(R.style.Theme_SimpleTracker_shantell)
            else -> setTheme(R.style.Theme_SimpleTracker)
        }
        /* TODO: LAST
        figure out font not applying to header
        https://developer.android.com/develop/ui/views/theming/darktheme
        https://developer.android.com/codelabs/basic-android-kotlin-training-change-app-theme#2
        https://developer.android.com/reference/android/content/Context#setTheme%28int%29
        https://developer.android.com/reference/androidx/appcompat/app/AppCompatDelegate#setDefaultNightMode(int)
        */
    }



    private fun setReminders(view: View? = null) { //TODO FIRST figure out why this isn't working
        var sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        var pref = sharedPref.getBoolean("notificationsEnabled", false)


        Log.d("**setReminders**", "notificationsEnabled: $pref")
        if (pref) {
            lifecycleScope.launch {
                tagDatabase.getTagByAlarm(true).collect {
                    val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager


                    for (tag in it) {
                        val alarmIntent = Intent(this@MainActivity, AlarmReceiver::class.java)
                        alarmIntent.putExtra("tagName", tag.tagName)
                        alarmIntent.putExtra("tagNote", tag.tagNote)
                        alarmIntent.putExtra("tagId", tag.tagId)

                        val hour = tag.reminder.get(Calendar.HOUR)
                        val minute = tag.reminder.get(Calendar.MINUTE)
                        tag.reminder.timeInMillis = System.currentTimeMillis()
                        tag.reminder.set(Calendar.HOUR, hour)
                        tag.reminder.set(Calendar.MINUTE, minute)

                        val pendingIntent = PendingIntent.getBroadcast(
                            this@MainActivity,
                            tag.tagId,
                            alarmIntent,
                            PendingIntent.FLAG_IMMUTABLE )

                        Log.d("**setReminders**", "setting reminder for ${tag.tagName} / ${tag.tagId}")

                        alarmManager.setInexactRepeating(
                            AlarmManager.RTC_WAKEUP,
                            tag.reminder.timeInMillis,
                            AlarmManager.INTERVAL_DAY,
                            pendingIntent
                        )
                    }
                }
            }
        } else {
            lifecycleScope.launch {
                tagDatabase.getAlphabetizedTags().collect {
                    val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

                    for (tag in it) {
                        val alarmIntent = Intent(this@MainActivity, AlarmReceiver::class.java)
                        Log.d("**setReminders**", "canceling reminder for ${tag.tagName} / ${tag.tagId}")
                        val pendingIntent = PendingIntent.getBroadcast(
                            this@MainActivity,
                            tag.tagId,
                            alarmIntent,
                            PendingIntent.FLAG_IMMUTABLE )
                        alarmManager.cancel(pendingIntent)
                    }
                }
            }
        }
    }


    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(name, name, importance).apply {
                description = descriptionText
                lightColor = R.color.grey
                enableLights(true)
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (!notificationManager.areNotificationsEnabled()) {
                Log.d("createNotificationChannel","Notifications are not enabled")
                var sharedPref = PreferenceManager.getDefaultSharedPreferences(this) ?: return
                with(sharedPref.edit()) {
                    putBoolean("notificationsEnabled",false)
                    apply()
                }
            }


            notificationManager.createNotificationChannel(channel)
        }
    }


    private fun updateSettings(view:View? = null) {
        setFont()
        setNightMode()
        createNotificationChannel()
        setReminders()
    }



    //unsure about this one, it came as part of the template
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return true
    }

    //unsure about this one, it came as part of the template
    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }


}