package com.example.weatherapp.ui

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import com.example.weatherapp.R
import com.example.weatherapp.databinding.ActivityMainBinding
import com.example.weatherapp.model.WeatherResponse
import com.example.weatherapp.network.RetrofitClient
import com.example.weatherapp.network.WeatherService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val API_KEY = "aee9c236caab1be6a995f2d1ed348515"

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_WeatherApp_Light)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fetchLocation()

        binding.toggleThemeButton.setOnClickListener {
            val current = AppCompatDelegate.getDefaultNightMode()
            AppCompatDelegate.setDefaultNightMode(
                if (current == AppCompatDelegate.MODE_NIGHT_YES)
                    AppCompatDelegate.MODE_NIGHT_NO
                else AppCompatDelegate.MODE_NIGHT_YES
            )
        }

        binding.changeLocationButton.setOnClickListener {
            val editText = EditText(this)
            editText.hint = "Cidade, Estado"
            AlertDialog.Builder(this)
                .setTitle("Digite a cidade e estado")
                .setView(editText)
                .setPositiveButton("Buscar") { _, _ ->
                    val input = editText.text.toString().trim()
                    if (input.isNotEmpty()) {
                        fetchWeatherByCity(input)
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    private fun fetchLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                fetchWeather(it.latitude, it.longitude)
            }
        }
    }

    private fun fetchWeather(lat: Double, lon: Double) {
        val service = RetrofitClient.instance.create(WeatherService::class.java)
        service.getWeather(lat, lon, API_KEY).enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                if (response.isSuccessful) {
                    val weather = response.body()
                    weather?.let {
                        binding.cityText.text = it.name
                        binding.tempText.text = "${it.main.temp}°C"
                        binding.descText.text = it.weather.first().description
                    }
                } else {
                    Toast.makeText(this@MainActivity, "Erro ao obter clima", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Falha na conexão", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchWeatherByCity(location: String) {
        val service = RetrofitClient.instance.create(WeatherService::class.java)
        service.getWeatherByCity(location, API_KEY).enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                if (response.isSuccessful) {
                    val weather = response.body()
                    weather?.let {
                        binding.cityText.text = it.name
                        binding.tempText.text = "${it.main.temp}°C"
                        binding.descText.text = it.weather.first().description
                    }
                } else {
                    Toast.makeText(this@MainActivity, "Cidade não encontrada", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Falha na conexão", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
