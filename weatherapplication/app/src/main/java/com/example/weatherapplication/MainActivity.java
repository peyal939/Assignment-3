package com.example.weatherapplication;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private EditText cityInput;
    private LinearLayout forecastContainer; // Container for forecast blocks
    private TextView resultView; // Declare resultView

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cityInput = findViewById(R.id.cityInput);
        Button fetchButton = findViewById(R.id.fetchButton);
        forecastContainer = findViewById(R.id.forecastContainer); // Initialize the forecast container
        resultView = findViewById(R.id.resultView); // Initialize resultView

        fetchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String city = cityInput.getText().toString().trim();
                // Clear previous results and error messages before fetching new data
                forecastContainer.removeAllViews();
                resultView.setText(""); // Clear any previous error messages
                resultView.setVisibility(View.GONE); // Hide the result view initially

                if (!city.isEmpty()) {
                    new FetchWeatherTask().execute(city);
                } else {
                    resultView.setText("Please enter a city name.");
                    resultView.setVisibility(View.VISIBLE); // Show message if no input
                }
            }
        });
    }

    private class FetchWeatherTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String cityName = params[0];
            return HttpRequestHandler.fetchWeatherData(cityName);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            // Clear previous forecasts
            forecastContainer.removeAllViews();

            try {
                JSONObject jsonObject = new JSONObject(result);

                // Check if there is an error in the response
                if (jsonObject.has("cod") && jsonObject.getString("cod").equals("404")) {
                    resultView.setText("City not found. Please try again.");
                    resultView.setVisibility(View.VISIBLE); // Show error message for city not found
                    return;
                }

                JSONArray list = jsonObject.getJSONArray("list");

                for (int i = 0; i < list.length(); i += 8) { // Every 8th item corresponds to daily forecast
                    JSONObject dayForecast = list.getJSONObject(i);
                    JSONObject main = dayForecast.getJSONObject("main");
                    JSONArray weatherArray = dayForecast.getJSONArray("weather");
                    JSONObject weather = weatherArray.getJSONObject(0);

                    String date = dayForecast.getString("dt_txt").split(" ")[0]; // Get only the date part
                    String temp = main.getString("temp") + "°C";
                    String feelsLike = main.getString("feels_like") + "°C"; // Feels like temperature
                    String humidity = main.getString("humidity") + "%"; // Humidity percentage
                    String description = weather.getString("description");

                    // Inflate the forecast item layout
                    View forecastItem = getLayoutInflater().inflate(R.layout.forecast_item, null);

                    // Set data to views in this item
                    TextView dateText = forecastItem.findViewById(R.id.dateText);
                    TextView tempText = forecastItem.findViewById(R.id.tempText);
                    TextView descriptionText = forecastItem.findViewById(R.id.descriptionText);
                    TextView feelsLikeText = forecastItem.findViewById(R.id.feelsLikeText);
                    TextView humidityText = forecastItem.findViewById(R.id.humidityText);

                    dateText.setText(date);
                    tempText.setText(temp);
                    descriptionText.setText(description);
                    feelsLikeText.setText("Feels Like: " + feelsLike); // Set feels like text
                    humidityText.setText("Humidity: " + humidity); // Set humidity text

                    // Add the inflated view to the container
                    forecastContainer.addView(forecastItem); // Add each item to the LinearLayout
                }

            } catch (Exception e) {
                resultView.setText("Error: Unable to parse data.");
                resultView.setVisibility(View.VISIBLE); // Show error message for parsing issues
                e.printStackTrace();
            }
        }
    }
}