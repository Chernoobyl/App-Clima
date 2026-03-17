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
import com.bumptech.glide.Glide
import com.example.weatherapp.R
import com.example.weatherapp.config.ApiConfig
import com.example.weatherapp.databinding.ActivityMainBinding
import com.example.weatherapp.model.WeatherResponse
import com.example.weatherapp.network.RetrofitClient
import com.example.weatherapp.network.WeatherService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private fun updateWeatherAnimation(mainCondition: String, icon: String) {
        val normalized = mainCondition.lowercase(Locale.ROOT)

        val animation = when {
            normalized.contains("thunderstorm") -> R.raw.light_rain
            normalized.contains("drizzle") || normalized.contains("rain") -> R.raw.light_rain
            normalized.contains("snow") -> R.raw.cloudy
            normalized.contains("mist") || normalized.contains("fog") || normalized.contains("haze") -> R.raw.windy
            normalized.contains("cloud") -> R.raw.cloudy
            icon.startsWith("01") -> R.raw.sunny
            else -> R.raw.windy
        }

        binding.weatherAnimationView.setAnimation(animation)
        binding.weatherAnimationView.playAnimation()
    }

    private fun updateWeatherIcon(iconCode: String) {
        val iconUrl = "https://openweathermap.org/img/wn/${iconCode}@2x.png"
        Glide.with(this)
            .load(iconUrl)
            .placeholder(R.mipmap.ic_launcher)
            .error(R.mipmap.ic_launcher)
            .into(binding.weatherIcon)
    }

    private fun renderWeather(weather: WeatherResponse) {
        val firstWeather = weather.weather.firstOrNull() ?: return

        val cityAndCountry = listOf(weather.name, weather.sys?.country)
            .filterNot { it.isNullOrBlank() }
            .joinToString(", ")

        binding.cityText.text = cityAndCountry.ifBlank { weather.name }
        binding.tempText.text = String.format(Locale.getDefault(), "%.1f°C", weather.main.temp)
        binding.descText.text = firstWeather.description.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
        }
        binding.extraInfoText.text = getString(
            R.string.weather_extra_info,
            weather.main.humidity,
            weather.wind?.speed ?: 0.0
        )

        updateWeatherAnimation(firstWeather.main, firstWeather.icon)
        updateWeatherIcon(firstWeather.icon)
    }

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
            } ?: Toast.makeText(this, "Não foi possível obter a localização atual", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            fetchLocation()
        } else if (requestCode == 1) {
            Toast.makeText(this, "Permissão negada", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchWeather(lat: Double, lon: Double) {
        val service = RetrofitClient.instance.create(WeatherService::class.java)
        service.getWeather(lat, lon, ApiConfig.OPEN_WEATHER_API_KEY).enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { renderWeather(it) }
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
        service.getWeatherByCity(location, ApiConfig.OPEN_WEATHER_API_KEY).enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { renderWeather(it) }
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
