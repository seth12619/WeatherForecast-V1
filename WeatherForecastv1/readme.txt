Readme

Developed using Android studio with a Sony E5553 Android device for a job application I did

This Android application makes use of the OpenWeather api, and a bottom navbar for switching views. 

The set location view has a spinner and an EditText field that works as a search bar for the large number of cities that can be selected at the spinner. Upon pressing the Set Location button, the EditText field's text is compared with the spinner's entries. It changes the input inside the spinner if it exists within the spinner itself, and makes a toast that it is an invalid city otherwise. The application fills the spinner through a local json containing the cities supported by openweathermap. This is done by using a gson that is used by an adapter to fill the spinner.

The API requests are done through an Asynctask to make make the switch between views smoother. When a request is made, this asynctask edits a TextView on the app in order to present the requested weather.

Bug in daily weather forecast wherein it may give the same day's forecast instead of the next day's for tomorrow's weather.