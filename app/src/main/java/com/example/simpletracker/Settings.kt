package com.example.simpletracker

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*


class Settings : PreferenceFragmentCompat() {

    override fun onCreatePreferences(
        savedInstanceState: Bundle?,
        rootKey: String?
    ) {

        setPreferencesFromResource(R.xml.root_preferences, rootKey)
        Log.d("Message", "onCreatePreferences")

    }

    //https://developer.android.com/reference/android/content/SharedPreferences
    //https://developer.android.com/develop/ui/views/components/settings/use-saved-values#kotlin
    //https://developer.android.com/reference/androidx/preference/PreferenceFragmentCompat#onPreferenceTreeClick(androidx.preference.Preference)
    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        when (preference.key) {
            "signInButton" -> signIn()
            "syncNowButton" -> sync()
            //"dark" -> dealWithTheme()
            //"font" -> dealWithFont()
            "clearMemoryButton" -> clearMemory()
            "notificationsEnabled" -> dealWithNotifs()
            else -> Log.d("Message", "unknown preference click")
        }
        return true
    }

    private fun dealWithNotifs() {
        (activity as MainActivity?)?.setReminders()
    }
    private fun clearMemory() {
        val tagDatabase by lazy { TagDatabase.getDatabase(requireContext()).tagDao() }
        GlobalScope.launch {
            tagDatabase.clearAllTags()
            tagDatabase.clearAllPoints()
        }
    }

    private fun signIn() {
        Log.d("Message", "signInButton")
        startActivity(Intent(android.provider.Settings.ACTION_SETTINGS))
    }

    private fun sync() {
        Log.d("Message", "syncNowButton")
        //var mBuilder:NotificationCompat.Builder =  NotificationCompat.Builder(this)

    }


}

