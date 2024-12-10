package com.example.weather;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.weather.api.ApiClient;
import com.example.weather.api.ApiInterface;
import com.example.weather.model.WeatherResponse;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private EditText etCity;
    private Button btnGetWeather;
    private TextView tvWeatherInfo;
    private ImageView weatherIcon;
    private final String apiKey = "5d75645657067d5e01d327c2e4ed3d01";
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toggle;

    private static final int VOICE_RECOGNITION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set up the Drawer Layout
        drawerLayout = findViewById(R.id.drawerLayout);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Set up the NavigationView
        navigationView = findViewById(R.id.navigationView);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_home) {
                    // Handle home action
                } else if (id == R.id.nav_settings) {
                    // Handle settings action
                } else if (id == R.id.nav_about) {
                    // Handle about action
                    Intent aboutIntent = new Intent(MainActivity.this, AboutActivity.class);
                    startActivity(aboutIntent);
                }
                // Close the drawer after an item is selected
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });

        etCity = findViewById(R.id.etCity);
        btnGetWeather = findViewById(R.id.btnGetWeather);
        tvWeatherInfo = findViewById(R.id.tvWeatherInfo);
        weatherIcon = findViewById(R.id.weatherIcon);

        // Set OnClickListener for the Get Weather button
        btnGetWeather.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String city = etCity.getText().toString().trim();
                if (!city.isEmpty()) {
                    getWeatherData(city);
                } else {
                    Toast.makeText(MainActivity.this, "Please enter a city name", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Add OnTouchListener to the EditText for voice input
        etCity.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_RIGHT = 2;
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (etCity.getRight() - etCity.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        // Start voice recognition
                        startVoiceRecognition();
                        return true;
                    }
                }
                return false;
            }
        });
    }

    // Start voice recognition for city name input
    private void startVoiceRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak the city name...");
        startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
    }

    // Handle the result of the voice recognition
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            etCity.setText(result.get(0));  // Set the recognized text to the EditText
        }
    }

    // Handle the back button for closing the drawer
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    // Fetch weather data from the API
    private void getWeatherData(String city) {
        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<WeatherResponse> call = apiInterface.getWeather(city, apiKey, "metric");

        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    WeatherResponse weatherResponse = response.body();

                    // Format the weather information
                    String weatherInfo = String.format(
                            "City: %s\nTemperature: %.2f°C\nHumidity: %d%%\nCondition: %s",
                            weatherResponse.getName(),
                            weatherResponse.getMain().getTemp(),
                            weatherResponse.getMain().getHumidity(),
                            weatherResponse.getWeather().get(0).getDescription());

                    // Set the formatted weather information to the TextView
                    tvWeatherInfo.setText(weatherInfo);

                    // Update the weather icon based on the weather condition
                    String weatherCondition = weatherResponse.getWeather().get(0).getDescription();
                    if (weatherCondition.contains("clear")) {
                        weatherIcon.setImageResource(R.drawable.sunny);  // Set sunny weather icon
                    } else if (weatherCondition.contains("rain")) {
                        weatherIcon.setImageResource(R.drawable.rainy);  // Set rainy weather icon
                    } else if (weatherCondition.contains("cloud")) {
                        weatherIcon.setImageResource(R.drawable.cloudy); // Set cloudy weather icon
                    } else {
                        weatherIcon.setImageResource(R.drawable.cloudy_icon);  // Default icon
                    }

                } else {
                    tvWeatherInfo.setText("City not found!");
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                Log.e("MainActivity", "Error: " + t.getMessage());
                tvWeatherInfo.setText("Failed to get data.");
            }
        });
    }
}
