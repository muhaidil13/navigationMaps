package com.example.tugas_maps.data.model

import com.mapbox.geojson.Point


sealed class Marker(val id: String?, val locationName: String, val location: Point, val type: String){
    class WisataMarker(id: String?, locationName: String, location: Point, type: String):  Marker(id,locationName,location,type)
    class KulinerMarker(id: String?, locationName: String, location: Point, type: String):  Marker(id, locationName,location,type)


}