package com.example.tugas_maps.ui.screen

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
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
import com.google.android.gms.location.LocationServices
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.MapInitOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.PuckBearing
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.gestures.addOnMapLongClickListener
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location

@Composable
fun ScreenForMaps(mainViewModel: MainViewModel){

    val userLocation by mainViewModel.startPosition.collectAsState()
    val userBearing by mainViewModel.startBearing.collectAsState()
    val kulinerMarkers = mainViewModel.getListOfMarkerKuliner()
    val wisataMarkers = mainViewModel.getListOfMarkerWisata()
    val destinationPoint by mainViewModel.destinationPoint.collectAsState()
    val isAdmin by mainViewModel.isAdmin.collectAsState()
    val markerisAdded by mainViewModel.markerIsAdded.collectAsState()
    val context = LocalContext.current


    val initialCamera: CameraOptions = CameraOptions
        .Builder()
        .center(Point.fromLngLat(userLocation!!.longitude(), userLocation!!.latitude()))
        .zoom(17.0)
        .pitch(20.0)
        .bearing(30.0)
        .build()


    val mapInitOption = MapInitOptions(context, cameraOptions = initialCamera,  styleUri = Style.MAPBOX_STREETS)
    val mapView = MapView(context, mapInitOption)

    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) {grand ->
        if(!grand){
            Toast.makeText(context, "Required Permission Location", Toast.LENGTH_SHORT).show()
        }
    }
    mapView.mapboxMap.loadStyle(Style.MAPBOX_STREETS){ style ->
        mapView.location.apply {
            puckBearing  = PuckBearing.COURSE
            locationPuck = createDefault2DPuck(withBearing = true)
            puckBearingEnabled = true
            enabled = true
        }
        bitmapFromDrawableRes(mapView.context, R.drawable.red_mark)?.let{ bitmap ->
            style.addImage("marker-icon-id", bitmap)
        }
        bitmapFromDrawableRes(mapView.context, R.drawable.ic_launcher_foreground)?.let{ bitmap ->
            style.addImage("marker-icon-id-2", bitmap)
        }
    }
    kulinerMarkers?.forEach {
        val point = Point.fromLngLat(it.location.longitude(), it.location.latitude())
        addMarker(mapView,point, mainViewModel)
    }
    wisataMarkers?.forEach {
        val point = Point.fromLngLat(it.location.longitude(), it.location.latitude())
        addMarker(mapView,point, mainViewModel,"marker-icon-id-2")
    }
    mapView.mapboxMap.addOnMapLongClickListener{point ->
        if(isAdmin){
            Log.d(mainViewModel.LOG_TAG, "Marker Custom: latitude is ${point.latitude()} longitude ${point.longitude()} ")
            mainViewModel.updateDestinationPosition(point)
            mainViewModel.updateShowDialog(true)
            if(markerisAdded){
                addMarker(mapView,point, mainViewModel)
            }
        }
        true
    }



    LaunchedEffect(Unit) {
        if(ContextCompat.checkSelfPermission(mapView.context,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)

        }
    }
    val showDialog by mainViewModel.showDialog.collectAsState()
    val destinationPosition by mainViewModel.destinationPoint.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {

                updateCamera(mapView =  mapView, point = userLocation!!, mainViewModel.LOG_TAG, bearing = 40.0, zoomLevel = 18.0 )

            }) {
                Icon(imageVector = Icons.Outlined.LocationOn, contentDescription = "")
            }
        },

    ){innerPadding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)){
            AndroidView(factory = {mapView})
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart) // Menempatkan tombol di kiri atas
                    .padding(8.dp)
            ) {
                Button(
                    onClick = { /* Aksi tombol */ },
                    content = { Text("Button") }
                )
            }
        }
        if(showDialog && destinationPosition != null){
            MarkerDialog(location = destinationPosition!! , onDismis = {
                mainViewModel.updateShowDialog(false)
                mainViewModel.updateDestinationPosition(null)
            }) { marker ->

                mainViewModel.saveMarkerToDatabase(marker, context)
                mainViewModel.updateShowDialog(false)
                mainViewModel.updateStatusmarkerIsAdded(false)

            }
        }
    }
}
private fun addMarker(mapView: MapView, point: Point, mainViewModel: MainViewModel, iconLabel:String = "marker-icon-id"){
    val pointManager = mapView.annotations.createPointAnnotationManager()
    val annotationOptions = PointAnnotationOptions()
        .withPoint(Point.fromLngLat(point.longitude(), point.latitude()))
        .withIconImage(iconLabel)

    val  mark = pointManager.create(annotationOptions)
    pointManager.addClickListener{clickMarker ->
        if(mark.id == clickMarker.id){
            Toast.makeText(mapView.context, "Marker ${point.longitude()}", Toast.LENGTH_LONG ).show()
            updateCamera(mapView = mapView, point = point, mainViewModel.LOG_TAG)
        }
        Toast.makeText(mapView.context, "Sukses Menambah Marker", Toast.LENGTH_SHORT).show()

        true
    }
}
private fun updateCamera(mapView: MapView, point: Point, LOG_TAG: String = "testMapbox", bearing: Double? = 90.0, zoomLevel:Double? = 18.9, startDelay:Long? = 500L){
    val mapAnimationOptionsBuilder = MapAnimationOptions.Builder().startDelay( startDelay ?: 800L)
    val isvalid =  mapView.mapboxMap.isValid()
    if(isvalid){
        Log.d(LOG_TAG, "${isvalid}")
        mapView.apply {
            mapView.camera.easeTo(
                CameraOptions.Builder()
                    .center(point)
                    .pitch(45.0)
                    .bearing(bearing)
                    .zoom(zoomLevel)
                    .padding(EdgeInsets(0.0, 0.0, 0.0, 0.0))
                    .build(),
                mapAnimationOptionsBuilder.build()
            )
        }
    }else{
        Log.d(LOG_TAG, "${isvalid}")
    }
}

