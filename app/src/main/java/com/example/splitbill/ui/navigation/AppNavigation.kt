package com.example.splitbill.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.splitbill.ui.auth.LoginScreen
import com.example.splitbill.ui.group.CreateGroupScreen
import com.example.splitbill.ui.group.JoinGroupScreen
import com.example.splitbill.ui.home.HomeScreen
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val auth = FirebaseAuth.getInstance()

    // val startDestination = if (auth.currentUser != null) "home" else "login"
    // TEMPORARY: Bypass auth for development
    val startDestination = "home"

    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        composable("home") {
            HomeScreen(
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                onCreateGroup = {
                    navController.navigate("create_group")
                },
                onJoinGroup = {
                    navController.navigate("join_group")
                }
            )
        }
        composable("create_group") {
            CreateGroupScreen(
                onGroupCreated = { groupId ->
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable("join_group") {
            JoinGroupScreen(
                onGroupJoined = { groupId ->
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }
    }
}

