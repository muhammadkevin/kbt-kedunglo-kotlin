package com.example.kbtkedunglo.utilsclass

import android.location.Location

data class maxDistanceScreenChange(val maxDistance:Float)
data class LocationWithSpeedAverageChange(val speed:String, val location:Location)
data class TimeChangeEvent(val currentTime: Long)
data class StatusTimeChangeEvent(val status:String)
data class ScreenStatusEvent(val status:Boolean)
data class LocationChangeEvent(val location:Location)
data class DismissAlarmAlert(val location:Location, val road:Location)
class FileCreatedEvent(val status: Boolean)