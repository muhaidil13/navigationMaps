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
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.core.content.ContextCompat
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
import com.example.tugas_maps.ui.screen.DetailScreen
import com.example.tugas_maps.ui.screen.HomeScreen
import com.example.tugas_maps.ui.screen.KulinerScreen
import com.example.tugas_maps.ui.screen.MapScreen
import com.example.tugas_maps.ui.screen.ScreenForMaps
import com.example.tugas_maps.ui.screen.WisataScreen
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.mapbox.api.directions.v5.models.RouteLeg
import com.mapbox.common.location.Location
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
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
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


@ExperimentalMaterial3Api
@Composable
fun  NavigationApp(mapboxNavigation: MapboxNavigation) {
    val scrollState = rememberScrollState()
    val navController = rememberNavController()


    val mainViewModel: MainViewModel = viewModel()
    val context = LocalContext.current

    val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    val laucher = rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) { grand->
        if(!grand){
            Toast.makeText(context,"Please Accept Permission", Toast.LENGTH_LONG).show()
        }
    }
    LaunchedEffect(Unit) {
        if(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            fusedLocationProviderClient.lastLocation.addOnSuccessListener { loc ->
                loc?.let { location ->
                    val point = Point.fromLngLat(location.longitude, location.latitude)
                    mainViewModel.updateStartPosition(point)
                    mainViewModel.updateStartBearing(location.bearing.toDouble())
                }
            }
        }else{
            laucher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }



    mainViewModel.setMapboxNavigation(mapboxNavigation)




    MainScreen(
        navController = navController,
        scrollState = scrollState,
        mainViewModel=mainViewModel
    )
}

@ExperimentalMaterial3Api
@Composable
fun MainScreen(navController: NavHostController,
               scrollState: ScrollState,
               mainViewModel: MainViewModel
){

    val context = LocalContext.current
    mainViewModel.getUserLocation(context)

    Scaffold(

    ){ values ->
        Navigation(navController = navController,
            mainViewModel=mainViewModel,
            scrollState = scrollState,
            paddingValues = values,
        )
    }
}
@ExperimentalMaterial3Api
@Composable
fun Navigation(
    navController: NavHostController,
    mainViewModel: MainViewModel,
    scrollState: ScrollState,
    paddingValues: PaddingValues,

){
    NavHost(navController = navController, startDestination = BottomMenuScreen.HomePage.route, modifier = Modifier.padding(paddingValues)){

        BottomNavigation(navController= navController,  mainViewModel =  mainViewModel,)
    }
}
@ExperimentalMaterial3Api
fun NavGraphBuilder.BottomNavigation(
    navController: NavController,
    mainViewModel: MainViewModel
){

    composable(BottomMenuScreen.HomePage.route){



        HomeScreen(mainViewModel, navController)
    }

    composable(BottomMenuScreen.WisataPage.route){


        WisataScreen(navController,mainViewModel)
    }
    composable(BottomMenuScreen.KulinerPage.route){


        KulinerScreen(navController,mainViewModel)
    }
    composable(BottomMenuScreen.AboutPage.route){
        AboutScreen(navController)
    }
    composable("screenFormap"){


        ScreenForMaps(mainViewModel)
    }

    composable("detail/{idLocation}"){ backStackEntry ->
        val idLocation = backStackEntry.arguments?.getString("idLocation") ?: ""
        DetailScreen(idLocation = idLocation, mainViewModel, navController)
    }

    composable("map/{idLocation}/{lat}/{lng}") { backStackEntry ->

        val idLocation = backStackEntry.arguments?.getString("idLocation") ?: ""
        val lat = backStackEntry.arguments?.getString("lat")?.toDoubleOrNull() ?: 0.0
        val lng = backStackEntry.arguments?.getString("lng")?.toDoubleOrNull() ?: 0.0

        val pointDestintion = Point.fromLngLat(lng, lat)

        MapScreen(
            mainViewModel = mainViewModel,
            navController = navController,
            destinationPosition = pointDestintion,
        )
    }

}

