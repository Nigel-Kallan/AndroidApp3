package com.trios.androidapp3

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class MainActivity : AppCompatActivity() {

    // Provides access to the device's location
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Keeps track of the participant's current Treasure Hunt location
    private var currentLocationIndex = 0

    // Stores the participant's Treasure Hunt progress
     private val preferencesName = "TreasureHuntProgress"
     private val progressKey = "currentLocationIndex"


    // Handles the result after the user is asked for location permission
    private val locationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { permissionGranted ->

            if (permissionGranted) {
                // Permission granted, so get the device location
                getCurrentLocation()
            } else {
                Toast.makeText(
                    this,
                    "Location permission is needed for the treasure hunt",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Display the main Treasure Hunt screen
        setContentView(R.layout.activity_main)

         // Load the participant's previously saved progress
         val sharedPreferences =
             getSharedPreferences(preferencesName, MODE_PRIVATE)

         currentLocationIndex =
             sharedPreferences.getInt(progressKey, 0)

         // Check whether the participant already completed all 20 locations
         if (currentLocationIndex >= TreasureHuntData.locations.size) {
           
             val progressTextView =
                 findViewById<android.widget.TextView>(R.id.progressTextView)
         
             val locationTextView =
                 findViewById<android.widget.TextView>(R.id.locationTextView)
         
             val distanceTextView =
                 findViewById<android.widget.TextView>(R.id.distanceTextView)
         
             progressTextView.text = "Progress: 20 / 20"
             locationTextView.text = "Treasure Hunt Complete!"
         
             distanceTextView.text =
                 "Congratulations!\n" +
                 "You visited all 20 locations.\n" +
                 "You are eligible for the draw for a FREE VACATION!"
         
             // Keep the index valid so the app cannot access outside the list
             currentLocationIndex = TreasureHuntData.locations.size - 1
         }

                // Display normal progress only if the Treasure Hunt is not complete
                if (sharedPreferences.getInt(progressKey, 0) < TreasureHuntData.locations.size) {

                    val progressTextView =
                        findViewById<android.widget.TextView>(R.id.progressTextView)

                    progressTextView.text =
                        "Progress: $currentLocationIndex / 20"

                    // Display the correct current Treasure Hunt location
                    val locationTextView =
                        findViewById<android.widget.TextView>(R.id.locationTextView)

                    locationTextView.text =
                        TreasureHuntData.locations[currentLocationIndex].name
                }
          
        // Connect to Google's location service
        fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(this)

        // Connect the Check My Location button
        val checkLocationButton =
            findViewById<Button>(R.id.checkLocationButton)

        // Check or request location permission when the button is pressed
        checkLocationButton.setOnClickListener {

            if (
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {

                // Permission already granted, so get the location
                getCurrentLocation()

            } else {

                // Ask the user for location permission
                locationPermissionLauncher.launch(
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            }
        }
    }

    // Gets the most recently known location of the device
    private fun getCurrentLocation() {

        // Check permission again before accessing location
        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        fusedLocationClient.getCurrentLocation(
            com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
            null
        ).addOnSuccessListener { location ->

                if (location != null) {

                    // Get the participant's current Treasure Hunt location
                    val treasureLocation =
                        TreasureHuntData.locations[currentLocationIndex]

                    // Create a Location object for the current destination
                    val destination = Location("TreasureLocation").apply {
                        latitude = treasureLocation.latitude
                        longitude = treasureLocation.longitude
                    }

                    // Calculate the distance from the participant to the current destination
                    val distanceInMeters = location.distanceTo(destination)

                    // Display the distance clearly on the main screen
                    val distanceTextView =
                        findViewById<android.widget.TextView>(R.id.distanceTextView)

                    // Check whether the participant is within 100 metres
                    if (distanceInMeters <= 100) {

                        distanceTextView.text =
                            "You found ${treasureLocation.name}!\n" +
                                    "Distance: ${distanceInMeters.toInt()} metres"

                        // Update the progress shown on the screen
                           val progressTextView =
                               findViewById<android.widget.TextView>(R.id.progressTextView)

                           progressTextView.text =
                               "Progress: ${currentLocationIndex + 1} / 20"

                             // Save the participant's progress
                             val sharedPreferences =
                                 getSharedPreferences(preferencesName, MODE_PRIVATE)

                             sharedPreferences.edit()
                                 .putInt(progressKey, currentLocationIndex + 1)
                                 .apply()

                           // Move to the next Treasure Hunt location
                           if (currentLocationIndex < TreasureHuntData.locations.size - 1) {

                               currentLocationIndex++

                               val nextLocation = TreasureHuntData.locations[currentLocationIndex]

                               val locationTextView =
                                   findViewById<android.widget.TextView>(R.id.locationTextView)

                               locationTextView.text = nextLocation.name

                           } else {

                               // All 20 Treasure Hunt locations have been completed
                               val locationTextView =
                                   findViewById<android.widget.TextView>(R.id.locationTextView)

                               locationTextView.text = "Treasure Hunt Complete!"

                               distanceTextView.text =
                                   "Congratulations!\n" +
                                   "You visited all 20 locations.\n" +
                                   "You are eligible for the draw for a FREE VACATION!"
                           }

                    } else {

                        distanceTextView.text =
                            "Distance to ${treasureLocation.name}:\n" +
                                    "${distanceInMeters.toInt()} metres"
                    }

                } else {

                    Toast.makeText(
                        this,
                        "Location not available yet",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }
}