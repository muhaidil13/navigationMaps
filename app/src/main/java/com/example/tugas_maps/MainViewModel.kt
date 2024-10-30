package com.example.tugas_maps

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import bitmapFromDrawableRes
import com.example.tugas_maps.data.model.Marker
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
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
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch


class MainViewModel(application: Application): AndroidViewModel(application) {
    val customPoint =  Point.fromLngLat(119.47908175133358, -5.170677805504212)
    val customBearing = 30.0

    val LOG_TAG = "testMapbox"
    private val MARKER_COLLECTION = "marker"
    private val firestore = FirebaseFirestore.getInstance()

    private val _startPosition = MutableStateFlow<Point?>(null)
    val startPosition: StateFlow<Point?> get() = _startPosition

    private val _startBearing = MutableStateFlow<Double?>(null)
    val startBearing: StateFlow<Double?> get() = _startBearing


    private val _markers = MutableLiveData<List<Marker>>()
    val markers: LiveData<List<Marker>> get() = _markers




    private val _destinationPoint = MutableStateFlow<Point?>(null)
    val destinationPoint: StateFlow<Point?> get() = _destinationPoint


    private val _showDialog = MutableStateFlow<Boolean>(false)
    val showDialog: StateFlow<Boolean> get() = _showDialog

    private val _isAdmin = MutableStateFlow<Boolean>(true)
    val isAdmin: StateFlow<Boolean> get() = _isAdmin

    private val _useCustomLocation = MutableStateFlow<Boolean>(false)
    val isCustom: StateFlow<Boolean> get() = _useCustomLocation


    private val _isStart = MutableStateFlow<Boolean>(false)
    val isStart: StateFlow<Boolean> get() = _isStart

    private val _markerIsAdded = MutableStateFlow<Boolean>(false)
    val markerIsAdded: StateFlow<Boolean> get() = _markerIsAdded


    private val _marker = MutableStateFlow<Marker?>(null)
    val marker: StateFlow<Marker?> get() = _marker




    lateinit var navigation: MapboxNavigation




    init {
        getAllMarkersFromDatabase()
    }



    fun getListOfMarkerWisata():List<Marker>?{
        return _markers.value?.filter { it is Marker.WisataMarker }
    }

    fun getListOfMarkerKuliner():List<Marker>?{
        return _markers.value?.filter { it is Marker.KulinerMarker }
    }

    fun updateStatusmarkerIsAdded(status: Boolean){
        _markerIsAdded.value = status
    }
    fun updatestatususeCustomLocation(status: Boolean){
        _useCustomLocation.value = status
    }

    fun updateStartNavigation(status: Boolean){
        _isStart.value = status
    }

    fun updateShowDialog(status: Boolean){
        _showDialog.value = status
    }

    fun updateStartPosition(point: Point) {
        _startPosition.value = point
    }

    fun  updateStartBearing(bearing: Double){
        _startBearing.value = bearing
    }

    fun updateDestinationPosition(point: Point?) {
        _destinationPoint.value = point
    }


    fun setMapboxNavigation(mapboxNav: MapboxNavigation) {
        navigation = mapboxNav
    }


    fun saveMarkerToDatabase(marker: Marker, context: Context){
        val markerData = hashMapOf(
            "locationName" to marker.locationName,
            "latitude" to marker.location.latitude(),
            "longitude" to marker.location.longitude(),
            "type" to marker.type
        )
        firestore.collection(MARKER_COLLECTION).add(markerData).addOnSuccessListener {
            Toast.makeText(context, "Suksess Menambahkan Marker", Toast.LENGTH_SHORT).show()
            updateStatusmarkerIsAdded(true)
        }.addOnFailureListener{
                Toast.makeText(context, "Gagal Menambahkan Marker", Toast.LENGTH_SHORT).show()
        }
    }

    fun getAllMarkersFromDatabase() {
        firestore.collection(MARKER_COLLECTION)
            .addSnapshotListener{snapshot, error ->
                if(error != null){
                    return@addSnapshotListener
                }
               fetchMarkers(snapshot)
            }
    }
    fun getMarkersById(id: String){
        firestore.collection(MARKER_COLLECTION).document(id).get().addOnSuccessListener {document ->
            if(document.exists()){
                val  idMarker = document.id
                val locationName = document.getString("locationName") ?: ""
                val latitude = document.getDouble("latitude") ?: 0.0
                val longitude = document.getDouble("longitude") ?: 0.0
                val type = document.getString("type") ?: ""
                val point = Point.fromLngLat(longitude, latitude)
                val marker =  when (type) {
                    "wisata" -> Marker.WisataMarker(idMarker, locationName, point, type)
                    "kuliner" -> Marker.KulinerMarker(idMarker, locationName, point, type)
                    else -> null
                }
                _marker.value =marker
            }

        }
    }

    @SuppressLint("MissingPermission")
    fun getUserLocation(context: Context){
        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
        viewModelScope.launch {
            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    Log.d(LOG_TAG, "${it.latitude}")
                    updateStartPosition(Point.fromLngLat(it.longitude, it.latitude))
                    updateStartBearing(it.bearing.toDouble())

                }
            }
        }
    }
    fun fetchMarkers(snapshot: QuerySnapshot?) {
        viewModelScope.launch {
            snapshot?.let {
                if (!snapshot.isEmpty) {
                    val datalist = snapshot.documents.mapNotNull { document ->
                        val  id = document.id
                        val locationName = document.getString("locationName") ?: ""
                        val latitude = document.getDouble("latitude") ?: 0.0
                        val longitude = document.getDouble("longitude") ?: 0.0
                        val type = document.getString("type") ?: ""
                        val point = Point.fromLngLat(longitude, latitude)

                        when (type) {
                            "wisata" -> Marker.WisataMarker(id, locationName, point, type)
                            "kuliner" -> Marker.KulinerMarker(id, locationName, point, type)
                            else -> null
                        }
                    }
                    _markers.value = datalist
                }
            }
        }
    }
}
