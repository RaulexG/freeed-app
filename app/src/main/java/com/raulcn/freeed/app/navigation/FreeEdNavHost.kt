package com.raulcn.freeed.app.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.BusinessCenter
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import com.raulcn.freeed.app.session.AppSessionUiState
import com.raulcn.freeed.app.session.AppSessionViewModel
import com.raulcn.freeed.app.session.SessionRouteTarget
import com.raulcn.freeed.feature.auth.LoginRoute
import com.raulcn.freeed.feature.auth.RegisterRoute
import com.raulcn.freeed.feature.home.CreateServiceRoute
import com.raulcn.freeed.feature.home.ExploreRoute
import com.raulcn.freeed.feature.home.HomeRoute
import com.raulcn.freeed.feature.onboarding.CompanyProfileSetupRoute
import com.raulcn.freeed.feature.onboarding.RoleSelectionRoute
import com.raulcn.freeed.feature.onboarding.StudentProfileSetupRoute
import com.raulcn.freeed.feature.portfolio.PortfolioItemDetailRoute
import com.raulcn.freeed.feature.profile.MyProfileRoute
import com.raulcn.freeed.feature.profile.StudentProfileDetailRoute
import com.raulcn.freeed.feature.requests.RequestDetailRoute
import com.raulcn.freeed.feature.requests.RequestsRoute
import com.raulcn.freeed.feature.services.ServiceDetailRoute
import com.raulcn.freeed.feature.services.ServiceEditorRoute
import com.raulcn.freeed.feature.splash.SplashRoute
import com.raulcn.freeed.feature.system.FavoritesRoute
import com.raulcn.freeed.feature.system.MyServicesRoute
import com.raulcn.freeed.feature.system.ReceivedRequestsRoute
import com.raulcn.freeed.feature.system.SentRequestsRoute
import com.raulcn.freeed.feature.system.SettingsRoute

@Composable
fun FreeEdNavHost(
    sessionViewModel: AppSessionViewModel
) {
    val appState = rememberFreeEdAppState()
    val sessionUiState by sessionViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(
        sessionUiState.hasActiveSession,
        sessionUiState.profile?.id,
        sessionUiState.isLoading
    ) {
        if (sessionUiState.hasActiveSession && sessionUiState.profile == null && !sessionUiState.isLoading) {
            sessionViewModel.refreshUserContext()
        }
    }

    LaunchedEffect(sessionUiState.routeTarget) {
        when (sessionUiState.routeTarget) {
            SessionRouteTarget.Login -> appState.navController.navigate(FreeEdDestination.Login.route) {
                popUpTo(FreeEdDestination.Splash.route) { inclusive = true }
                launchSingleTop = true
            }

            SessionRouteTarget.Home -> appState.navController.navigate(FreeEdDestination.Home.route) {
                popUpTo(FreeEdDestination.Splash.route) { inclusive = true }
                launchSingleTop = true
            }

            SessionRouteTarget.StudentSetup -> appState.navController.navigate(
                FreeEdDestination.StudentProfileSetup.route
            ) {
                popUpTo(FreeEdDestination.Splash.route) { inclusive = true }
                launchSingleTop = true
            }

            SessionRouteTarget.CompanySetup -> appState.navController.navigate(
                FreeEdDestination.CompanyProfileSetup.route
            ) {
                popUpTo(FreeEdDestination.Splash.route) { inclusive = true }
                launchSingleTop = true
            }

            null -> Unit
        }
    }

    Scaffold(
        bottomBar = {
            if (appState.shouldShowBottomBar()) {
                FreeEdBottomBar(
                    currentRoute = appState.currentDestination()?.route,
                    onNavigate = { destination ->
                        val protected = destination == MainTabDestination.CREATE ||
                            destination == MainTabDestination.REQUESTS ||
                            destination == MainTabDestination.PROFILE

                        if (protected && !sessionUiState.hasActiveSession) {
                            appState.navController.navigate(FreeEdDestination.Login.route)
                        } else {
                            appState.navigateToTopLevelDestination(destination)
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = appState.navController,
            startDestination = FreeEdGraphs.AUTH,
            modifier = Modifier.padding(innerPadding)
        ) {
            authGraph(appState, sessionViewModel)
            onboardingGraph(appState, sessionViewModel)
            mainGraph(appState, sessionUiState, sessionViewModel)
            detailGraph(appState, sessionUiState)
        }
    }
}

private fun androidx.navigation.NavGraphBuilder.authGraph(
    appState: FreeEdAppState,
    sessionViewModel: AppSessionViewModel
) {
    navigation(
        route = FreeEdGraphs.AUTH,
        startDestination = FreeEdDestination.Splash.route
    ) {
        composable(FreeEdDestination.Splash.route) {
            SplashRoute(
                sessionUiState = sessionViewModel.uiState.value
            )
        }
        composable(FreeEdDestination.Login.route) {
            LoginRoute(
                onLoginSuccess = {
                    appState.navController.navigate(FreeEdDestination.Splash.route) {
                        popUpTo(FreeEdDestination.Login.route) { inclusive = true }
                    }
                },
                onRegisterClick = { appState.navController.navigate(FreeEdDestination.Register.route) },
                onBackClick = {
                    if (!appState.navController.popBackStack()) {
                        appState.navController.navigate(FreeEdDestination.Home.route) {
                            popUpTo(FreeEdDestination.Login.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
            )
        }
        composable(FreeEdDestination.Register.route) {
            RegisterRoute(
                onRegistrationFinished = {
                    appState.navController.navigate(FreeEdDestination.Splash.route) {
                        popUpTo(FreeEdDestination.Register.route) { inclusive = true }
                    }
                },
                onBackClick = { appState.navController.popBackStack() }
            )
        }
    }
}

private fun androidx.navigation.NavGraphBuilder.onboardingGraph(
    appState: FreeEdAppState,
    sessionViewModel: AppSessionViewModel
) {
    navigation(
        route = FreeEdGraphs.ONBOARDING,
        startDestination = FreeEdDestination.RoleSelection.route
    ) {
        composable(FreeEdDestination.RoleSelection.route) {
            RoleSelectionRoute(
                onStudentSelected = { appState.navController.navigate(FreeEdDestination.StudentProfileSetup.route) },
                onCompanySelected = { appState.navController.navigate(FreeEdDestination.CompanyProfileSetup.route) },
                onCancel = { appState.navController.popBackStack() }
            )
        }
        composable(FreeEdDestination.StudentProfileSetup.route) {
            StudentProfileSetupRoute(
                sessionProfile = sessionViewModel.uiState.value.profile,
                onComplete = {
                    sessionViewModel.refreshUserContext()
                    appState.navController.navigate(FreeEdDestination.Splash.route) {
                        popUpTo(FreeEdDestination.StudentProfileSetup.route) { inclusive = true }
                    }
                },
                onBackClick = { appState.navController.popBackStack() }
            )
        }
        composable(FreeEdDestination.CompanyProfileSetup.route) {
            CompanyProfileSetupRoute(
                sessionProfile = sessionViewModel.uiState.value.profile,
                onComplete = {
                    sessionViewModel.refreshUserContext()
                    appState.navController.navigate(FreeEdDestination.Splash.route) {
                        popUpTo(FreeEdDestination.CompanyProfileSetup.route) { inclusive = true }
                    }
                },
                onBackClick = { appState.navController.popBackStack() }
            )
        }
    }
}

private fun androidx.navigation.NavGraphBuilder.mainGraph(
    appState: FreeEdAppState,
    sessionUiState: AppSessionUiState,
    sessionViewModel: AppSessionViewModel
) {
    navigation(
        route = FreeEdGraphs.MAIN,
        startDestination = FreeEdDestination.Home.route
    ) {
        composable(FreeEdDestination.Home.route) {
            HomeRoute(
                sessionProfile = sessionUiState.profile,
                onExploreMore = { appState.navController.navigate(FreeEdDestination.Explore.route) },
                onOpenOwnProfile = {
                    appState.navController.navigate(FreeEdDestination.MyProfile.route)
                },
                onOpenService = { serviceId ->
                    appState.navController.navigate(FreeEdDestination.ServiceDetail.createRoute(serviceId))
                },
                onRequireAuth = { appState.navController.navigate(FreeEdDestination.Login.route) }
            )
        }
        composable(FreeEdDestination.Explore.route) {
            ExploreRoute(
                sessionProfile = sessionUiState.profile,
                onOpenOwnProfile = { appState.navController.navigate(FreeEdDestination.MyProfile.route) },
                onOpenService = { serviceId ->
                    appState.navController.navigate(FreeEdDestination.ServiceDetail.createRoute(serviceId))
                },
                onRequireAuth = { appState.navController.navigate(FreeEdDestination.Login.route) }
            )
        }
        composable(FreeEdDestination.CreateService.route) {
            CreateServiceRoute(
                sessionProfile = sessionUiState.profile,
                hasActiveSession = sessionUiState.hasActiveSession,
                onOpenEditor = {
                    appState.navController.navigate(FreeEdDestination.ServiceEditor.createRoute("new"))
                },
                onRequireAuth = { appState.navController.navigate(FreeEdDestination.Login.route) }
            )
        }
        composable(FreeEdDestination.Requests.route) {
            RequestsRoute(
                sessionProfile = sessionUiState.profile,
                onOpenRequest = {
                    appState.navController.navigate(FreeEdDestination.RequestDetail.createRoute("demo-request"))
                },
                onOpenSentRequests = { appState.navController.navigate(FreeEdDestination.SentRequests.route) },
                onOpenReceivedRequests = {
                    appState.navController.navigate(FreeEdDestination.ReceivedRequests.route)
                }
            )
        }
        composable(FreeEdDestination.MyProfile.route) {
            MyProfileRoute(
                sessionProfile = sessionUiState.profile,
                onOpenPortfolioItem = {
                    appState.navController.navigate(
                        FreeEdDestination.PortfolioItemDetail.createRoute("demo-portfolio")
                    )
                },
                onOpenService = {
                    appState.navController.navigate(FreeEdDestination.ServiceDetail.createRoute("demo-service"))
                },
                onOpenSettings = { appState.navController.navigate(FreeEdDestination.Settings.route) },
                onOpenFavorites = { appState.navController.navigate(FreeEdDestination.Favorites.route) },
                onOpenMyServices = { appState.navController.navigate(FreeEdDestination.MyServices.route) },
                onSignOut = {
                    sessionViewModel.signOut()
                    appState.navController.navigate(FreeEdDestination.Login.route) {
                        popUpTo(FreeEdDestination.Home.route) { inclusive = true }
                    }
                }
            )
        }
    }
}

private fun androidx.navigation.NavGraphBuilder.detailGraph(
    appState: FreeEdAppState,
    sessionUiState: AppSessionUiState
) {
    composable(
        route = FreeEdDestination.StudentProfile.route,
        arguments = listOf(navArgument("profileId") { type = NavType.StringType })
    ) { backStackEntry ->
        StudentProfileDetailRoute(
            profileId = backStackEntry.arguments?.getString("profileId").orEmpty(),
            onBackClick = { appState.navController.popBackStack() }
        )
    }

    composable(
        route = FreeEdDestination.ServiceDetail.route,
        arguments = listOf(navArgument("serviceId") { type = NavType.StringType })
    ) { backStackEntry ->
        ServiceDetailRoute(
            serviceId = backStackEntry.arguments?.getString("serviceId").orEmpty(),
            sessionProfile = sessionUiState.profile,
            onBackClick = { appState.navController.popBackStack() },
            onEditClick = { id ->
                appState.navController.navigate(FreeEdDestination.ServiceEditor.createRoute(id))
            },
            onRequireAuth = { appState.navController.navigate(FreeEdDestination.Login.route) }
        )
    }

    composable(
        route = FreeEdDestination.ServiceEditor.route,
        arguments = listOf(navArgument("serviceId") { type = NavType.StringType })
    ) { backStackEntry ->
        ServiceEditorRoute(
            serviceId = backStackEntry.arguments?.getString("serviceId").orEmpty(),
            onBackClick = { appState.navController.popBackStack() }
        )
    }

    composable(
        route = FreeEdDestination.RequestDetail.route,
        arguments = listOf(navArgument("requestId") { type = NavType.StringType })
    ) { backStackEntry ->
        RequestDetailRoute(
            requestId = backStackEntry.arguments?.getString("requestId").orEmpty(),
            onBackClick = { appState.navController.popBackStack() }
        )
    }

    composable(
        route = FreeEdDestination.PortfolioItemDetail.route,
        arguments = listOf(navArgument("portfolioItemId") { type = NavType.StringType })
    ) { backStackEntry ->
        PortfolioItemDetailRoute(
            portfolioItemId = backStackEntry.arguments?.getString("portfolioItemId").orEmpty(),
            onBackClick = { appState.navController.popBackStack() }
        )
    }

    composable(FreeEdDestination.Favorites.route) {
        FavoritesRoute(onBackClick = { appState.navController.popBackStack() })
    }
    composable(FreeEdDestination.MyServices.route) {
        MyServicesRoute(onBackClick = { appState.navController.popBackStack() })
    }
    composable(FreeEdDestination.SentRequests.route) {
        SentRequestsRoute(onBackClick = { appState.navController.popBackStack() })
    }
    composable(FreeEdDestination.ReceivedRequests.route) {
        ReceivedRequestsRoute(onBackClick = { appState.navController.popBackStack() })
    }
    composable(FreeEdDestination.Settings.route) {
        SettingsRoute(onBackClick = { appState.navController.popBackStack() })
    }
}

@Composable
private fun FreeEdBottomBar(
    currentRoute: String?,
    onNavigate: (MainTabDestination) -> Unit
) {
    NavigationBar {
        MainTabDestination.entries.forEach { destination ->
            NavigationBarItem(
                selected = currentRoute == destination.route,
                onClick = { onNavigate(destination) },
                icon = {
                    val icon = when (destination) {
                        MainTabDestination.HOME -> Icons.Outlined.Home
                        MainTabDestination.EXPLORE -> Icons.Outlined.Search
                        MainTabDestination.CREATE -> Icons.Outlined.AddCircle
                        MainTabDestination.REQUESTS -> Icons.Outlined.BusinessCenter
                        MainTabDestination.PROFILE -> Icons.Outlined.Person
                    }
                    Icon(imageVector = icon, contentDescription = destination.label)
                },
                label = { Text(destination.label) }
            )
        }
    }
}
