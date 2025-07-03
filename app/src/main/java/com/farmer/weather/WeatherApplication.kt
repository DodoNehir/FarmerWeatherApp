package com.farmer.weather

import android.app.Application
import com.farmer.weather.data.AppContainer
import com.farmer.weather.data.WeatherContainer

class WeatherApplication: Application() {
    lateinit var container : AppContainer

    override fun onCreate() {
        super.onCreate()
        container = WeatherContainer(this)
    }
}