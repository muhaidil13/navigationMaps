package com.example.tugas_maps

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Place
import androidx.compose.ui.graphics.vector.ImageVector


sealed class BottomMenuScreen(
    val route:String,
    val icon: ImageVector,
    val title: String
){
    object HomePage:BottomMenuScreen(
        route = "Home Screen",
        icon = Icons.Outlined.Home,
        title ="Home"
    )
    object WisataPage:BottomMenuScreen(
        route = "Wisata Screen",
        icon = Icons.Outlined.LocationOn,
        title ="Wisata"
    )
    object KulinerPage:BottomMenuScreen(
        route = "Kuliner Screen",
        icon = Icons.Outlined.Place,
        title ="Kuliner"
    )
    object AboutPage:BottomMenuScreen(
        route = "About Screen",
        icon = Icons.Outlined.AccountCircle,
        title ="Tentang"
    )
}