package com.example.tugas_maps.data.model

import com.mapbox.geojson.Point


sealed class Marker(val locationName: String, val location: Point, val type: String){
    class WisataMarker(locationName: String, location: Point, type: String):  Marker(locationName,location,type)
    class KulinerMarker(locationName: String, location: Point, type: String):  Marker(locationName,location,type)


}