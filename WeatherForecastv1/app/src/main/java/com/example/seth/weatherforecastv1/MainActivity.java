package com.example.seth.weatherforecastv1;



import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private TextView mTextMessage;


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Spinner city = (Spinner)findViewById(R.id.cities);
            EditText manCity = (EditText)findViewById(R.id.manCity);
            Button setLoc = (Button) findViewById(R.id.setLocation);
            TextView weather = (TextView)findViewById(R.id.weather);
            switch (item.getItemId()) {
                case R.id.current_weather:
                    mTextMessage.setText("Current Weather");
                    city.setVisibility(View.INVISIBLE);
                    String hey = city.getSelectedItem().toString();
                    new GetWeather().execute(hey);

                    setLoc.setVisibility(View.INVISIBLE);
                    manCity.setVisibility(View.INVISIBLE);
                    weather.setVisibility(View.VISIBLE);
                    return true;
                case R.id.weekly_weather:
                    mTextMessage.setText("Weekly Weather for the next 5 days");
                    city.setVisibility(View.INVISIBLE);

                    String heyW = city.getSelectedItem().toString();
                    new GetWeatherWeek().execute(heyW);

                    setLoc.setVisibility(View.INVISIBLE);
                    manCity.setVisibility(View.INVISIBLE);
                    weather.setVisibility(View.VISIBLE);
                    return true;
                case R.id.set_location:
                    mTextMessage.setText("Set Location");
                    city.setVisibility(View.VISIBLE);
                    setLoc.setVisibility(View.VISIBLE);
                    manCity.setVisibility(View.VISIBLE);
                    weather.setVisibility(View.INVISIBLE);
                    return true;
            }
            return false;
        }

    };
    ArrayList<String> localId = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        View weather = findViewById(R.id.weather);
        weather.setVisibility(View.INVISIBLE);
        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

//To fill the spinner
        CityObjects[] cities = null;
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try {
            InputStream is = getResources().openRawResource(R.raw.city_list);


           BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));

            Gson gson = new GsonBuilder().create();
             cities = gson.fromJson(reader, CityObjects[].class );
        } catch(Exception e) {

            e.getStackTrace();
        }
        ArrayAdapter<String> adapter = null;

        String theString = writer.toString();
        try {
            Spinner city = (Spinner)findViewById(R.id.cities);
             adapter = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item);



            int numCities = cities.length;
            int iter = 0;
            while(iter < numCities) {
                adapter.add(cities[iter].getName());
                localId.add(cities[iter].getName());
                iter++;
            }
            city.setAdapter(adapter);


        } catch(Exception e) {
            System.out.println("cities resource error");
        }

        // For setting Location manually
        Button setLocation = (Button) findViewById(R.id.setLocation);
        setLocation.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                Spinner city = (Spinner)findViewById(R.id.cities);

                String spinnerSelected = (String)city.getSelectedItem();
                EditText manCity = (EditText)findViewById(R.id.manCity);

                if(localId.contains(manCity.getText().toString())) {
                    city.setSelection(((ArrayAdapter<String>)city.getAdapter()).getPosition(manCity.getText().toString()));
                } else {
                    Toast.makeText(getApplicationContext(), "Invalid City - Case Sensitive", Toast.LENGTH_LONG).show();
                    manCity.setText("");
                }
            }
        });

    }

 /*
 *For JSON parsing
 */
    class CityObjects {
        String name;

        public void setName(String nams) {
            name = nams;
        }

        public String getName(){
            return name;
        }
    }

    /**
     * Class to get the weather (CURRENT)
     */
    class GetWeather extends AsyncTask<String,Void,String> {
        String weatherRes = "a";

        @Override
        protected String doInBackground(String... params) {
            String result = "";
            HttpURLConnection conn = null;
            try {
                URL url = new URL("http://api.openweathermap.org/data/2.5/forecast?q="+ URLEncoder.encode(params[0], "UTF-8")+"&APPID=29bc7eb242924041fd7274b3ffb07ed3");
                conn = (HttpURLConnection) url.openConnection();
                InputStream in = new BufferedInputStream(conn.getInputStream());
                if (in != null) {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
                    String line = "";

                    while ((line = bufferedReader.readLine()) != null)
                        result += line;
                }
                in.close();
                return result;
            } catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                if(conn!=null)
                    conn.disconnect();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            weatherRes = result;
            TextView weather = (TextView) findViewById(R.id.weather);
            if(result.equals("")) {
                weather.setText("Please set a valid location. Must be a city.");
            } else {
                try {
                    String toPrint = "";

                    JSONObject json = new JSONObject(result);
                    JSONArray jsons = json.getJSONArray("list");

                    int size = jsons.length();
                    JSONObject info = json.getJSONObject("city");
                    JSONObject today = jsons.getJSONObject(0);
                    JSONObject tomorrow = jsons.getJSONObject(6);


                    JSONArray todayWeather = today.getJSONArray("weather");
                    JSONArray tomorrowWeather = tomorrow.getJSONArray("weather");

                    toPrint = toPrint + "City: " + info.getString("name") + "\n";

                    toPrint = toPrint + "Today's Weather: " + todayWeather.getJSONObject(0).getString("description") + "\n";
                    toPrint = toPrint + "Tomorrow's Weather: " + tomorrowWeather.getJSONObject(0).getString("description") + "\n";
                    weather.setText(toPrint);

                } catch(Exception e) {
                    weather.setText("Weather API Error");
                }

            }

        }



    }

    /**
     * Class to get the weather weekly
     */
    class GetWeatherWeek extends AsyncTask<String,Void,String> {
        String weatherRes = "a";

        @Override
        protected String doInBackground(String... params) {
            String result = "";
            HttpURLConnection conn = null;
            try {
                URL url = new URL("http://api.openweathermap.org/data/2.5/forecast?q="+ URLEncoder.encode(params[0], "UTF-8")+"&APPID=29bc7eb242924041fd7274b3ffb07ed3");
                conn = (HttpURLConnection) url.openConnection();
                InputStream in = new BufferedInputStream(conn.getInputStream());
                if (in != null) {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
                    String line = "";

                    while ((line = bufferedReader.readLine()) != null)
                        result += line;
                }
                in.close();
                return result;
            } catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                if(conn!=null)
                    conn.disconnect();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            weatherRes = result;
            TextView weather = (TextView) findViewById(R.id.weather);
            if(result.equals("")) {
                weather.setText("Please set a valid location. Must be a city.");
            } else {
                try {
                    String toPrint = "";

                    JSONObject json = new JSONObject(result);
                    JSONArray jsons = json.getJSONArray("list");

                    int size = jsons.length();
                    int numDays = 5;

                    JSONObject info = json.getJSONObject("city");

                    toPrint = toPrint + "City: " + info.getString("name") + "\n\n";
                    int iterator = 0;

                    for(int i = 0; i < numDays; i++) {
                        JSONObject first = jsons.getJSONObject(0);
                        String[] splitFirst = first.getString("dt_txt").split("\\s+");


                        JSONObject toUse = null;
                        if(i > 0) {
                            toUse = jsons.getJSONObject(iterator);
                            String[] splitToUse = toUse.getString("dt_txt").split("\\s");
                            while(!splitFirst[1].equals(splitToUse[1])) {
                                iterator++;
                                toUse = jsons.getJSONObject(iterator);
                                splitToUse = toUse.getString("dt_txt").split("\\s");
                            }
                            iterator++;

                        } else {
                            toUse = jsons.getJSONObject(0);
                            iterator++;
                        }
                        JSONArray theWeather = toUse.getJSONArray("weather");
                        toPrint = toPrint + toUse.getString("dt_txt") + ": " + theWeather.getJSONObject(0).getString("description") + "\n";

                    }

                    weather.setText(toPrint); //sets weather to the json of the results You needto parse the json next

                } catch(Exception e) {
                    weather.setText("Weather API Error");
                }

            }

        }



    }


}
