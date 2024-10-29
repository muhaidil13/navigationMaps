package com.example.tugas_maps

import android.app.Application
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.tugas_maps.data.model.Marker
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.MapView
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.navigation.base.extensions.applyDefaultNavigationOptions
import com.mapbox.navigation.base.extensions.applyLanguageAndVoiceUnitOptions
import com.mapbox.navigation.base.route.NavigationRoute
import com.mapbox.navigation.base.route.NavigationRouterCallback
import com.mapbox.navigation.base.route.RouterFailure
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow



class MainViewModel(application: Application): AndroidViewModel(application) {
    val customPoint = Point.fromLngLat(119.47908175133358, -5.170677805504212)

    val LOG_TAG = "testMapbox"
    private val MARKER_COLLECTION = "marker"
    private val firestore = FirebaseFirestore.getInstance()

    private val _markers = MutableLiveData<List<Marker>>()
    val markers: LiveData<List<Marker>> get() = _markers

    private val _startPosition = MutableStateFlow<Point?>(null)
    val startPosition: StateFlow<Point?> get() = _startPosition


    private val _showDialog = MutableStateFlow<Boolean>(false)
    val showDialog: StateFlow<Boolean> get() = _showDialog

    private val _isAdmin = MutableStateFlow<Boolean>(true)
    val isAdmin: StateFlow<Boolean> get() = _isAdmin

    private val _useCustomLocation = MutableStateFlow<Boolean>(true)
    val isCustom: StateFlow<Boolean> get() = _useCustomLocation


    lateinit var navigation: MapboxNavigation
    lateinit var  routeLineView: MapboxRouteLineView



    fun updateIsCustomLocation(status: Boolean){
        _useCustomLocation.value = status
    }

    fun updateShowDialog(status: Boolean){
        _showDialog.value = status
    }

    fun updateStartPosition(latitude: Double, longitude: Double) {
        _startPosition.value = Point.fromLngLat(longitude, latitude)
    }


    fun setMapboxNavigation(mapboxNav: MapboxNavigation) {
        navigation = mapboxNav
    }

    fun updateCamera(mapView: MapView, point: Point){
        val mapAnimationOptionsBuilder = MapAnimationOptions.Builder()
        mapView.camera.easeTo(
            CameraOptions.Builder()
                .center(point)
                .pitch(45.0)
                .zoom(16.0)
                .padding(EdgeInsets(0.0, 0.0, 0.0, 0.0))
                .build(),
            mapAnimationOptionsBuilder.build()
        )
    }
    fun setNavigationRoutes(routes: List<NavigationRoute>, mapView: MapView) {
        navigation.setNavigationRoutes(routes)
        mapView.mapboxMap.style?.apply {
            routeLineView.hideAlternativeRoutes(this)
        }
    }



    fun fetchRoute(context: Context, mapView: MapView, start: Point, destination: Point){
        navigation.requestRoutes(RouteOptions.builder()
            .applyDefaultNavigationOptions()
            .applyLanguageAndVoiceUnitOptions(context)
            .coordinatesList(listOf(start,destination))
            .layersList(listOf(navigation.getZLevel(), null))
            .build(),
            object : NavigationRouterCallback{
                override fun onCanceled(routeOptions: RouteOptions, routerOrigin: String) {

                }

                override fun onFailure(reasons: List<RouterFailure>, routeOptions: RouteOptions) {

                }

                override fun onRoutesReady(routes: List<NavigationRoute>, routerOrigin: String) {
                    setNavigationRoutes(routes, mapView)
                }

            }
        )
    }
    fun addMarker(mapView: MapView, point: Point){
        val pointManager = mapView.annotations.createPointAnnotationManager()
        val annotationOptions = PointAnnotationOptions()
            .withPoint(Point.fromLngLat(point.longitude(), point.latitude()))
            .withIconImage("marker-icon-id") // Use the registered icon

        val  marker = pointManager.create(annotationOptions) // Create the marker on the map
        pointManager.addClickListener{clickMarker ->
            if(marker.id == clickMarker.id){
                Toast.makeText(mapView.context, "Marker ${point.longitude()}", Toast.LENGTH_LONG ).show()
            }
            true
        }

        Log.d(LOG_TAG, "Marker added at: ${point.latitude()}, ${point.longitude()}")
    }

    fun saveMarkerToDatabase(marker: Marker, mapView: MapView){
        val markerData = hashMapOf(
            "locationName" to marker.locationName,
            "latitude" to marker.location.latitude(),
            "longitude" to marker.location.longitude(),
            "type" to marker.type
        )

        val point = Point.fromLngLat(marker.location.longitude(), marker.location.latitude())
        firestore.collection(MARKER_COLLECTION).add(markerData).addOnSuccessListener {
            Toast.makeText(mapView.context, "Suksess Menambahkan Marker", Toast.LENGTH_SHORT).show()
            addMarker(mapView, point)
        }.addOnFailureListener{
            Toast.makeText(mapView.context, "Gagal Menambahkan Marker", Toast.LENGTH_SHORT).show()

        }
    }
    fun getAllMarkersFromDatabase(mapView: MapView) {
        firestore.collection(MARKER_COLLECTION)
            .get()
            .addOnSuccessListener { documents ->
                val markerList = mutableListOf<Marker>()
                for (document in documents) {
                    val locationName = document.getString("locationName") ?: continue
                    val latitude = document.getDouble("latitude") ?: continue
                    val longitude = document.getDouble("longitude") ?: continue
                    val type = document.getString("type") ?: continue
                    val point = Point.fromLngLat(longitude, latitude)

                    // Instantiate the correct subclass of Marker
                    val marker = when (type) {
                        "wisata" -> Marker.WisataMarker(locationName, point, type)
                        "kuliner" -> Marker.KulinerMarker(locationName, point, type)
                        else -> continue // Skip if type is unrecognized
                    }

                    markerList.add(marker)
                    addMarker(mapView,point)
                }
                _markers.value = markerList
            }
            .addOnFailureListener { exception ->
                when(exception){
                    is FirebaseFirestoreException -> {
                        Toast.makeText(mapView.context, "Sedang Terjadi Masalah Pada Jaringan Coba Lagi Nanti", Toast.LENGTH_LONG).show()
                    }
                }
            }
    }
}
