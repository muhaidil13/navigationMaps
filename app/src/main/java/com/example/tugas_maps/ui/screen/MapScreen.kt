package com.example.tugas_maps.ui.screen

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import bitmapFromDrawableRes
import com.example.tugas_maps.MainViewModel
import com.example.tugas_maps.R
import com.example.tugas_maps.ui.components.MarkerDialog
import com.google.android.gms.location.FusedLocationProviderClient
import com.mapbox.bindgen.Expected
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.ImageHolder
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.PuckBearing
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.navigation.ui.components.maps.camera.view.MapboxRecenterButton
import com.mapbox.navigation.ui.maps.internal.route.line.RouteLineApiClearRouteLineValue
import com.mapbox.navigation.ui.maps.route.RouteLayerConstants.TOP_LEVEL_ROUTE_LINE_LAYER_ID
import com.mapbox.navigation.ui.maps.route.line.model.RouteLineClearValue
import com.mapbox.navigation.ui.maps.route.line.model.RouteLineError

@Composable
fun MapScreen( mainViewModel: MainViewModel, fusedLocationProviderClient: FusedLocationProviderClient, mapView: MapView, destinationPosition: Point) {
    val useCustomLocation by mainViewModel.isCustom.collectAsState()


    val startPosition by mainViewModel.startPosition.collectAsState()

    val showDialog by mainViewModel.showDialog.collectAsState()

    val locationStart = if(useCustomLocation) mainViewModel.customPoint else startPosition



    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) { grand ->
        if(!grand){
            Toast.makeText(mapView.context, "Requred Location Permission", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(
                mapView.context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        else{
            findUserLocation(mapView, fusedLocationProviderClient, mainViewModel)
        }
    }


    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                findUserLocation(mapView, fusedLocationProviderClient, mainViewModel)
            }) {
                Icon(imageVector = Icons.Outlined.LocationOn, contentDescription = "")
            }
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(modifier = Modifier.weight(1f)) {
                AndroidView(factory = { mapView
                })
            }

            Text(
                text = startPosition?.let {
                    "Start Position: ${it.latitude()}, ${it.longitude()}"
                } ?: "Start Position: Not set"
            )
            Text(
                text = destinationPosition.let {
                    "destination Position: ${it.latitude()}, ${it.longitude()}"
                } ?: "destination Position: Not set"
            )
            if(showDialog){
                MarkerDialog(location = destinationPosition , onDismis = { mainViewModel.updateShowDialog(false) }) { marker ->
                    mainViewModel.saveMarkerToDatabase(marker, mapView)
                    mainViewModel.updateShowDialog(false)
                }
            }

            Button(
                onClick = {
                    mainViewModel.fetchRoute(context = mapView.context ,mapView, locationStart!!, destinationPosition)

                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp) // Add padding for nicer spacing
            ) {
                Text(text = "Start Navigation")
            }
            Button(
                onClick = {
//                    mainViewModel.fetchRoute(context = mapView.context ,mapView, locationStart!!, mainViewModel.destinationPosition.value!!)
                    mainViewModel.navigation.stopTripSession()
                    mainViewModel.navigation.setNavigationRoutes(listOf())


                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp) // Add padding for nicer spacing
            ) {
                Text(text = "End Navigation")
            }
        }
    }
}

fun findUserLocation(mapView: MapView, fusedLocationProviderClient: FusedLocationProviderClient, mainViewModel: MainViewModel){

          if(ContextCompat.checkSelfPermission(mapView.context,Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
              fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
                  location?.let {
                      mapView.mapboxMap.setCamera(
                          CameraOptions.Builder()
                              .center(Point.fromLngLat(it.latitude, it.longitude))
                              .zoom(10.0)
                              .pitch(10.00)
                              .build())

                      mainViewModel.updateStartPosition(it.latitude, it.longitude)
                      Log.d(mainViewModel.LOG_TAG, "Location Start ${it.latitude} dan ${it.longitude}")

                  }
              }

        }


}