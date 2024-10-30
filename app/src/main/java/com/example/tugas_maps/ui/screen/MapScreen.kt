package com.example.tugas_maps.ui.screen

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import bitmapFromDrawableRes
import com.example.tugas_maps.MainViewModel
import com.example.tugas_maps.R
import com.example.tugas_maps.ui.components.MarkerDialog
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.bindgen.Expected
import com.mapbox.common.location.Location
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.ImageHolder
import com.mapbox.maps.MapInitOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.PuckBearing
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.navigation.base.extensions.applyDefaultNavigationOptions
import com.mapbox.navigation.base.extensions.applyLanguageAndVoiceUnitOptions
import com.mapbox.navigation.base.route.NavigationRoute
import com.mapbox.navigation.base.route.NavigationRouterCallback
import com.mapbox.navigation.base.route.RouterFailure
import com.mapbox.navigation.base.trip.model.RouteProgress
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.directions.session.RoutesObserver
import com.mapbox.navigation.core.trip.session.LocationMatcherResult
import com.mapbox.navigation.core.trip.session.LocationObserver
import com.mapbox.navigation.core.trip.session.RouteProgressObserver
import com.mapbox.navigation.ui.components.maps.camera.view.MapboxRecenterButton
import com.mapbox.navigation.ui.maps.internal.route.line.RouteLineApiClearRouteLineValue
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider
import com.mapbox.navigation.ui.maps.route.RouteLayerConstants.TOP_LEVEL_ROUTE_LINE_LAYER_ID
import com.mapbox.navigation.ui.maps.route.arrow.api.MapboxRouteArrowApi
import com.mapbox.navigation.ui.maps.route.arrow.api.MapboxRouteArrowView
import com.mapbox.navigation.ui.maps.route.arrow.model.RouteArrowOptions
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineApiOptions
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineViewOptions



@Composable
fun MapScreen( mainViewModel: MainViewModel, destinationPosition: Point, navController:  NavController) {
    val mapboxNavigation = mainViewModel.navigation
    val isStart by mainViewModel.isStart.collectAsState()
    val startPositionState = mainViewModel.startPosition.value
    val startBearing by mainViewModel.startBearing.collectAsState()
    val isCustomPosition by mainViewModel.isCustom.collectAsState()

    val startPosition = if(isCustomPosition)  mainViewModel.customPoint else startPositionState
    val bearingPosition =if(isCustomPosition)  mainViewModel.customBearing else  startBearing
    val context = LocalContext.current

    val initialCamera: CameraOptions = CameraOptions
        .Builder()
        .center(Point.fromLngLat(startPosition!!.longitude(), startPosition.latitude()))
        .zoom(20.0)
        .pitch(20.0)
        .bearing(bearingPosition)
        .build()


    val mapInitOption = MapInitOptions(context, cameraOptions = initialCamera,  styleUri = Style.MAPBOX_STREETS)
    val mapView = MapView(context, mapInitOption)

    val permissionLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) { grand ->
        if(!grand){
            Toast.makeText(mapView.context, "Requred Location Permission", Toast.LENGTH_SHORT).show()
        }
    }
    val routeLineViewOption: MapboxRouteLineViewOptions = MapboxRouteLineViewOptions.Builder(context).build()
    val routeLineView =  MapboxRouteLineView(routeLineViewOption)

    val navigationLocationProvider = NavigationLocationProvider()
    val routeLineApiOptions: MapboxRouteLineApiOptions = MapboxRouteLineApiOptions
        .Builder()
        .vanishingRouteLineEnabled(true)
        .build()


    val routeArrowApi = MapboxRouteArrowApi()
    val routeArrowApiOptions: RouteArrowOptions = RouteArrowOptions.Builder(context).withSlotName(
        TOP_LEVEL_ROUTE_LINE_LAYER_ID
    ).build()

    val routeArrowView = MapboxRouteArrowView(routeArrowApiOptions)
    val routeLineApi= MapboxRouteLineApi(routeLineApiOptions)


    val routesObserver = RoutesObserver{routeUpdateResult ->
        routeLineApi.setNavigationRoutes(
            routeUpdateResult.navigationRoutes
        ){values ->
            mapView.mapboxMap.style?.apply {
                routeLineView.renderRouteDrawData(this, values)
            }
        }
    }



    val onPositionChange = OnIndicatorPositionChangedListener{point->
        val result = routeLineApi.updateTraveledRouteLine(point)
        mapView.mapboxMap.style?.apply {
            routeLineView.renderRouteLineUpdate(this, result)
        }
    }

    val routeProgressObserver  = RouteProgressObserver{routeProgress: RouteProgress ->

        if (routeProgress.remainingWaypoints == 0) {
            mapboxNavigation.stopTripSession()
            mapboxNavigation.setNavigationRoutes(listOf())
        }
        routeLineApi.updateWithRouteProgress(routeProgress){result ->
            mapView.mapboxMap.style?.apply {
                routeLineView.renderRouteLineUpdate(this, result)
            }
        }

        val arrowUpdate = routeArrowApi.addUpcomingManeuverArrow(routeProgress)
        mapView.mapboxMap.style?.apply {
            routeArrowView.renderManeuverUpdate(this, arrowUpdate)
        }
    }

    val locationObserver = object: LocationObserver {
        override fun onNewLocationMatcherResult(locationMatcherResult: LocationMatcherResult) {
            val enhancedLocation = locationMatcherResult.enhancedLocation
            navigationLocationProvider.changePosition(enhancedLocation,locationMatcherResult.keyPoints)
            if(isStart){
                updateCamera(mapView =  mapView, point= Point.fromLngLat(enhancedLocation.longitude, enhancedLocation.latitude), bearing = enhancedLocation.bearing)
            }else{
                updateCamera(mapView =  mapView, point= Point.fromLngLat(enhancedLocation.longitude, enhancedLocation.latitude), bearing = enhancedLocation.bearing, zoomLevel = 18.0, startDelay = 700L)

            }
        }

        override fun onNewRawLocation(rawLocation: Location) {

        }

    }
    fun stopNavigate(){
        mapboxNavigation.stopTripSession()
        mapboxNavigation.setNavigationRoutes(emptyList())
        navController.popBackStack()

    }
    fun fetchRoute(){
        mapboxNavigation.requestRoutes(
            RouteOptions.builder()
                .applyDefaultNavigationOptions()
                .applyLanguageAndVoiceUnitOptions(context)
                .coordinatesList(listOf(startPosition,destinationPosition))
                .layersList(listOf(mapboxNavigation.getZLevel(), null))
                .build(),
            object : NavigationRouterCallback {
                override fun onCanceled(routeOptions: RouteOptions, routerOrigin: String) {

                }

                override fun onFailure(reasons: List<RouterFailure>, routeOptions: RouteOptions) {

                }

                override fun onRoutesReady(routes: List<NavigationRoute>, routerOrigin: String) {
                    mapboxNavigation.setNavigationRoutes(routes)
                    mapView.mapboxMap.style?.apply {
                        routeLineView.hideAlternativeRoutes(this)
                    }
                }

            }
        )
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    lifecycleOwner.lifecycle.addObserver(LifecycleEventObserver{ _, event ->
        when(event){
            Lifecycle.Event.ON_CREATE -> {}
            Lifecycle.Event.ON_START -> {}
            Lifecycle.Event.ON_RESUME -> {
                mainViewModel.navigation.registerRoutesObserver(routesObserver)
                mainViewModel.navigation.registerLocationObserver(locationObserver)
                mainViewModel.navigation.registerRouteProgressObserver(routeProgressObserver)
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                    if(ActivityCompat.checkSelfPermission(mapView.context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED){
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
                mainViewModel.navigation.startTripSession()
            }
            Lifecycle.Event.ON_PAUSE -> {

            }
            Lifecycle.Event.ON_STOP -> {

            }
            Lifecycle.Event.ON_DESTROY -> {
                mainViewModel.navigation.unregisterRoutesObserver(routesObserver)
                mainViewModel.navigation.unregisterLocationObserver(locationObserver)
                mainViewModel.navigation.unregisterRouteProgressObserver(routeProgressObserver)
                mainViewModel.navigation.stopTripSession()

            }
            Lifecycle.Event.ON_ANY -> {

            }
        }
    })
    mapView.mapboxMap.loadStyle(Style.MAPBOX_STREETS){ style ->
        routeLineView.initializeLayers(style)
        bitmapFromDrawableRes(mapView.context, R.drawable.red_mark)?.let{ bitmap ->
            style.addImage("marker-icon-id", bitmap)
        }
        mapView.location.apply {
            setLocationProvider(navigationLocationProvider)
            addOnIndicatorPositionChangedListener(onPositionChange)
            puckBearing  = PuckBearing.COURSE
            locationPuck = createDefault2DPuck(withBearing = true)
            puckBearingEnabled = true
            enabled = true
        }
    }
    addMarker(mapView, destinationPosition, mainViewModel)




    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(
                mapView.context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

    }


    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                updateCamera(mapView =  mapView, point= startPosition, bearing = bearingPosition,  startDelay = 700L)
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
                text = startPosition.let {
                    "Start Position: ${it.latitude()}, ${it.longitude()}"
                }
            )
            Text(
                text = destinationPosition.let {
                    "destination Position: ${it.latitude()}, ${it.longitude()}"
                } ?: "destination Position: Not set"
            )


            Button(
                onClick = {
                    mainViewModel.updateStartNavigation(true)
                    fetchRoute()

                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(text = "Start Navigation")
            }
            Button(
                onClick = {
                    mainViewModel.updateStartNavigation(false)
                    stopNavigate()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(text = "End Navigation")
            }
        }
    }
}
private fun addMarker(mapView: MapView, point: Point, mainViewModel: MainViewModel){
    val pointManager = mapView.annotations.createPointAnnotationManager()
    val annotationOptions = PointAnnotationOptions()
        .withPoint(Point.fromLngLat(point.longitude(), point.latitude()))
        .withIconImage("marker-icon-id")

    val  mark = pointManager.create(annotationOptions)
    pointManager.addClickListener{clickMarker ->
        if(mark.id == clickMarker.id){
            Toast.makeText(mapView.context, "Marker ${point.longitude()}", Toast.LENGTH_LONG ).show()
            updateCamera(mapView = mapView, point = point, mainViewModel.LOG_TAG)
        }
        Toast.makeText(mapView.context, "Sukses Menambah Marker", Toast.LENGTH_SHORT).show()

        true
    }
    mainViewModel.updateStatusmarkerIsAdded(false)
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






