package com.example.splitbill.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.splitbill.ui.auth.LoginScreen
import com.example.splitbill.ui.group.CreateGroupScreen
import com.example.splitbill.ui.group.GroupDashboardScreen
import com.example.splitbill.ui.group.JoinGroupScreen
import com.example.splitbill.ui.group.PayScreen
import com.example.splitbill.ui.group.SettleUpScreen
import com.example.splitbill.ui.group.GroupDashboardViewModel
import com.example.splitbill.ui.home.HomeScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val auth = FirebaseAuth.getInstance()
    val uid = com.example.splitbill.util.CurrentUser.uid()
    val groupDashboardViewModel: GroupDashboardViewModel = viewModel {
        GroupDashboardViewModel(currentUserId = uid ?: "")
    }

    val startDestination = if (auth.currentUser != null) "home" else "login"

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
                    navController.navigate("dashboard/$groupId") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable("join_group") {
            JoinGroupScreen(
                onGroupJoined = { groupId ->
                    navController.navigate("dashboard/$groupId") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable("dashboard/{groupId}") { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId") ?: ""
            GroupDashboardScreen(
                groupId = groupId,
                viewModel = groupDashboardViewModel,
                onPay = { navController.navigate("pay") },
                onSettleUp = { navController.navigate("settle") }
            )
        }
        composable("pay") {
            PayScreen(
                state = groupDashboardViewModel,
                onDone = { navController.popBackStack() }
            )
        }
        composable("settle") {
            SettleUpScreen(
                state = groupDashboardViewModel,
                onDone = { navController.popBackStack() }
            )
        }
    }
}

