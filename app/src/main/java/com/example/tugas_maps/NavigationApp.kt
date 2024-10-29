package com.example.tugas_maps

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import bitmapFromDrawableRes
import com.example.tugas_maps.ui.components.BottomMenu
import com.example.tugas_maps.ui.screen.AboutScreen
import com.example.tugas_maps.ui.screen.HomeScreen
import com.example.tugas_maps.ui.screen.KulinerScreen
import com.example.tugas_maps.ui.screen.MapScreen
import com.example.tugas_maps.ui.screen.WisataScreen
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.mapbox.api.directions.v5.models.RouteLeg
import com.mapbox.common.location.Location
import com.mapbox.geojson.Point
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.gestures.addOnMapLongClickListener
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.navigation.base.route.NavigationRoute
import com.mapbox.navigation.base.trip.model.RouteLegProgress
import com.mapbox.navigation.base.trip.model.RouteProgress
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.directions.session.RoutesObserver
import com.mapbox.navigation.core.replay.route.ReplayProgressObserver
import com.mapbox.navigation.core.trip.session.LocationMatcherResult
import com.mapbox.navigation.core.trip.session.LocationObserver
import com.mapbox.navigation.core.trip.session.RouteProgressObserver
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
fun  NavigationApp(mapboxNavigation: MapboxNavigation) {
    val scrollState = rememberScrollState()
    val navController = rememberNavController()
    val context = LocalContext.current

    val mainViewModel: MainViewModel = viewModel()
    val routeLineViewOption: MapboxRouteLineViewOptions = MapboxRouteLineViewOptions.Builder(context).build()
    mainViewModel.routeLineView =  MapboxRouteLineView(routeLineViewOption)
    mainViewModel.setMapboxNavigation(mapboxNavigation)


    val mapView = MapView(context)

    val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)



    MainScreen(
        navController = navController,
        mapView = mapView,
        scrollState = scrollState,
        fusedLocationProviderClient = fusedLocationProviderClient,
        mainViewModel=mainViewModel
    )
}

@Composable
fun MainScreen(navController: NavHostController,
               scrollState: ScrollState,
               mapView: MapView,
               fusedLocationProviderClient: FusedLocationProviderClient,
               mainViewModel: MainViewModel
){
    Scaffold(
        bottomBar ={
            BottomMenu(navController = navController)
        }
    ){ values ->
        Navigation(navController = navController,
            mainViewModel=mainViewModel,
            scrollState = scrollState,
            paddingValues = values,
            mapView =mapView,
            fusedLocationProviderClient = fusedLocationProviderClient
        )
    }
}

@Composable
fun Navigation(
    navController: NavHostController,
    mainViewModel: MainViewModel,
    scrollState: ScrollState,
    paddingValues: PaddingValues,
    mapView: MapView,
    fusedLocationProviderClient: FusedLocationProviderClient
){
    NavHost(navController = navController, startDestination = BottomMenuScreen.HomePage.route, modifier = Modifier.padding(paddingValues)){
        BottomNavigation(navController= navController,  mapView = mapView, fusedLocationProviderClient =  fusedLocationProviderClient, mainViewModel =  mainViewModel,)
    }
}

fun NavGraphBuilder.BottomNavigation(
    mapView: MapView,
    navController: NavController,
    fusedLocationProviderClient: FusedLocationProviderClient,
    mainViewModel: MainViewModel
){

    composable(BottomMenuScreen.HomePage.route){


        LaunchedEffect(Unit) {
            mainViewModel.getAllMarkersFromDatabase(mapView)
        }
        SetUp(
            mapView = mapView,
            mainViewModel = mainViewModel,
            routeLineView = mainViewModel.routeLineView
        )
        HomeScreen(mainViewModel)
    }

    composable(BottomMenuScreen.WisataPage.route){

        LaunchedEffect(Unit) {
            mainViewModel.getAllMarkersFromDatabase(mapView)
        }

        WisataScreen(navController,mainViewModel )
    }
    composable(BottomMenuScreen.KulinerPage.route){

        KulinerScreen(navController,mainViewModel)
    }
    composable(BottomMenuScreen.AboutPage.route){
        AboutScreen()
    }
    composable("map/{locationName}/{lat}/{lng}") { backStackEntry ->


        SetUp(
            mapView = mapView,
            mainViewModel = mainViewModel,
            routeLineView = mainViewModel.routeLineView
        )
        val locationName = backStackEntry.arguments?.getString("locationName") ?: ""
        val lat = backStackEntry.arguments?.getString("lat")?.toDoubleOrNull() ?: 0.0
        val lng = backStackEntry.arguments?.getString("lng")?.toDoubleOrNull() ?: 0.0

        val pointDestintion = Point.fromLngLat(lng, lat)

        MapScreen(
            mapView = mapView,
            mainViewModel = mainViewModel,
            fusedLocationProviderClient = fusedLocationProviderClient,
            destinationPosition = pointDestintion
        )
    }

}

@Composable
fun SetUp(routeLineView:MapboxRouteLineView,  mapView: MapView, mainViewModel: MainViewModel){
    val isAdmin by mainViewModel.isAdmin.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) { grand ->
        if(!grand){
            Toast.makeText(mapView.context, "Require Notification", Toast.LENGTH_LONG).show()
        }
    }
    val navigationLocationProvider = NavigationLocationProvider()
    val routeLineApiOptions: MapboxRouteLineApiOptions = MapboxRouteLineApiOptions
        .Builder()
        .vanishingRouteLineEnabled(true)
        .build()


    val routeArrowApi = MapboxRouteArrowApi()
    val routeArrowApiOptions: RouteArrowOptions = RouteArrowOptions.Builder(mapView.context).withSlotName(
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
            mainViewModel.navigation.stopTripSession()
            mainViewModel.navigation.setNavigationRoutes(listOf())
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

    val locationObserver = object: LocationObserver{
        override fun onNewLocationMatcherResult(locationMatcherResult: LocationMatcherResult) {
            val enhancedLocation = locationMatcherResult.enhancedLocation
            navigationLocationProvider.changePosition(enhancedLocation,locationMatcherResult.keyPoints)
            mainViewModel.updateCamera(mapView, Point.fromLngLat(enhancedLocation.longitude, enhancedLocation.latitude))
        }

        override fun onNewRawLocation(rawLocation: Location) {


        }

    }


    val lifecycleOwner = LocalLifecycleOwner.current

    lifecycleOwner.lifecycle.addObserver(LifecycleEventObserver{_, event ->
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
                mainViewModel.navigation.unregisterRoutesObserver(routesObserver)
                mainViewModel.navigation.unregisterLocationObserver(locationObserver)
                mainViewModel.navigation.unregisterRouteProgressObserver(routeProgressObserver)
                mainViewModel.navigation.stopTripSession()
            }
            Lifecycle.Event.ON_STOP -> {
                mainViewModel.navigation.unregisterRoutesObserver(routesObserver)
                mainViewModel.navigation.unregisterLocationObserver(locationObserver)
                mainViewModel.navigation.unregisterRouteProgressObserver(routeProgressObserver)
                mainViewModel.navigation.stopTripSession()
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


    mapView.mapboxMap.loadStyle(Style.MAPBOX_STREETS){
        routeLineView.initializeLayers(it)
        bitmapFromDrawableRes(mapView.context, R.drawable.red_mark)?.let{bitmap ->
            it.addImage("marker-icon-id", bitmap)
        }

        if(isAdmin){
            mapView.mapboxMap.addOnMapLongClickListener{point ->
                Log.d(mainViewModel.LOG_TAG, "Marker Custom: latitude is ${point.latitude()} longitude ${point.longitude()} ")
                mainViewModel.updateShowDialog(true)
                true
            }
        }


    }
    mapView.location.apply {
        setLocationProvider(navigationLocationProvider)
        addOnIndicatorPositionChangedListener(onPositionChange)
        enabled = true
    }



}
