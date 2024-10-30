package com.example.tugas_maps.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.tugas_maps.BottomMenuScreen
import com.example.tugas_maps.R

@Composable
fun BottomMenu(navController: NavController){
    val menuItems = listOf(
        BottomMenuScreen.HomePage,
        BottomMenuScreen.WisataPage,
        BottomMenuScreen.KulinerPage,
        BottomMenuScreen.AboutPage,
    )
    NavigationBar(
        containerColor = colorResource(id = R.color.white)
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        menuItems.forEach {
            NavigationBarItem(
                label = { Text(text = it.title)},
                selected = currentRoute == it.route,
                onClick = {
                    navController.navigate(it.route){
                        navController.graph.startDestinationRoute?.let {route ->
                            popUpTo(route){
                                saveState = true
//                                inclusive = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = { Icon(imageVector =it.icon, contentDescription = "") },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.Blue,
                    unselectedIconColor = Color.DarkGray
                ),


                )
        }
    }
}