package com.raulcn.freeed.app.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.BusinessCenter
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
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
import com.raulcn.freeed.feature.portfolio.MyPortfolioRoute
import com.raulcn.freeed.feature.portfolio.PortfolioEditorRoute
import com.raulcn.freeed.feature.portfolio.PortfolioEditorViewModel
import com.raulcn.freeed.feature.portfolio.PortfolioItemDetailRoute
import com.raulcn.freeed.feature.profile.MyProfileRoute
import com.raulcn.freeed.feature.profile.CompanyProfileDetailRoute
import com.raulcn.freeed.feature.profile.StudentProfileDetailRoute
import com.raulcn.freeed.feature.requests.ReceivedRequestsRoute
import com.raulcn.freeed.feature.requests.RequestDetailRoute
import com.raulcn.freeed.feature.requests.RequestsRoute
import com.raulcn.freeed.feature.requests.SendRequestRoute
import com.raulcn.freeed.feature.requests.SentRequestsRoute
import com.raulcn.freeed.feature.services.MyServicesRoute
import com.raulcn.freeed.feature.services.ServiceDetailRoute
import com.raulcn.freeed.feature.services.ServiceEditorRoute
import com.raulcn.freeed.feature.services.ServiceEditorViewModel
import com.raulcn.freeed.feature.splash.SplashRoute
import com.raulcn.freeed.feature.favorites.FavoritesRoute
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
        val rootId = appState.navController.graph.findStartDestination().id
        when (sessionUiState.routeTarget) {
            SessionRouteTarget.Login -> appState.navController.navigate(FreeEdDestination.Login.route) {
                popUpTo(FreeEdDestination.Splash.route) { inclusive = true }
                launchSingleTop = true
            }

            SessionRouteTarget.Home -> appState.navController.navigate(FreeEdDestination.Home.route) {
                popUpTo(rootId) { inclusive = true }
                launchSingleTop = true
            }

            SessionRouteTarget.StudentSetup -> appState.navController.navigate(
                FreeEdDestination.StudentProfileSetup.route
            ) {
                popUpTo(rootId) { inclusive = true }
                launchSingleTop = true
            }

            SessionRouteTarget.CompanySetup -> appState.navController.navigate(
                FreeEdDestination.CompanyProfileSetup.route
            ) {
                popUpTo(rootId) { inclusive = true }
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
                    hasActiveSession = sessionUiState.hasActiveSession,
                    role = sessionUiState.profile?.role,
                    onNavigate = { destination ->
                        if (sessionUiState.hasActiveSession &&
                            sessionUiState.profile?.role == com.raulcn.freeed.core.model.UserRole.COMPANY &&
                            destination == MainTabDestination.CREATE
                        ) {
                            appState.navController.navigate(FreeEdDestination.Favorites.route)
                            return@FreeEdBottomBar
                        }

                        if (!sessionUiState.hasActiveSession && destination == MainTabDestination.REQUESTS) {
                            appState.navController.navigate(FreeEdDestination.Login.route)
                            return@FreeEdBottomBar
                        }

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
            mainGraph(appState, sessionViewModel)
            detailGraph(appState, sessionViewModel)
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
        composable(FreeEdDestination.StudentProfileEdit.route) {
            StudentProfileSetupRoute(
                sessionProfile = sessionViewModel.uiState.value.profile,
                onComplete = {
                    sessionViewModel.refreshUserContext()
                    appState.navController.popBackStack()
                },
                onBackClick = { appState.navController.popBackStack() }
            )
        }
        composable(FreeEdDestination.CompanyProfileEdit.route) {
            CompanyProfileSetupRoute(
                sessionProfile = sessionViewModel.uiState.value.profile,
                onComplete = {
                    sessionViewModel.refreshUserContext()
                    appState.navController.popBackStack()
                },
                onBackClick = { appState.navController.popBackStack() }
            )
        }
    }
}

private fun androidx.navigation.NavGraphBuilder.mainGraph(
    appState: FreeEdAppState,
    sessionViewModel: AppSessionViewModel
) {
    navigation(
        route = FreeEdGraphs.MAIN,
        startDestination = FreeEdDestination.Home.route
    ) {
        composable(FreeEdDestination.Home.route) {
            val sessionUiState by sessionViewModel.uiState.collectAsStateWithLifecycle()
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
            val sessionUiState by sessionViewModel.uiState.collectAsStateWithLifecycle()
            ExploreRoute(
                sessionProfile = sessionUiState.profile,
                onOpenOwnProfile = { appState.navController.navigate(FreeEdDestination.MyProfile.route) },
                onOpenService = { serviceId ->
                    appState.navController.navigate(FreeEdDestination.ServiceDetail.createRoute(serviceId))
                },
                onOpenStudent = { profileId ->
                    appState.navController.navigate(FreeEdDestination.StudentProfile.createRoute(profileId))
                },
                onRequireAuth = { appState.navController.navigate(FreeEdDestination.Login.route) }
            )
        }
        composable(FreeEdDestination.CreateService.route) {
            val sessionUiState by sessionViewModel.uiState.collectAsStateWithLifecycle()
            CreateServiceRoute(
                sessionProfile = sessionUiState.profile,
                hasActiveSession = sessionUiState.hasActiveSession,
                onOpenEditor = {
                    appState.navController.navigate(
                        FreeEdDestination.ServiceEditor.createRoute(ServiceEditorViewModel.NEW_SERVICE_ARG)
                    )
                },
                onRequireAuth = { appState.navController.navigate(FreeEdDestination.Login.route) }
            )
        }
        composable(FreeEdDestination.Requests.route) {
            val sessionUiState by sessionViewModel.uiState.collectAsStateWithLifecycle()
            RequestsRoute(
                sessionProfile = sessionUiState.profile,
                onOpenRequest = { requestId ->
                    appState.navController.navigate(FreeEdDestination.RequestDetail.createRoute(requestId))
                },
                onOpenCompanyProfile = { companyId ->
                    appState.navController.navigate(FreeEdDestination.CompanyProfile.createRoute(companyId))
                },
            )
        }
        composable(FreeEdDestination.MyProfile.route) {
            val sessionUiState by sessionViewModel.uiState.collectAsStateWithLifecycle()
            MyProfileRoute(
                sessionProfile = sessionUiState.profile,
                hasActiveSession = sessionUiState.hasActiveSession,
                onOpenPortfolioItem = {
                    appState.navController.navigate(FreeEdDestination.MyPortfolio.route)
                },
                onOpenService = {
                    appState.navController.navigate(FreeEdDestination.MyServices.route)
                },
                onOpenServiceDetail = { id ->
                    appState.navController.navigate(FreeEdDestination.ServiceDetail.createRoute(id))
                },
                onOpenPortfolioItemById = { id ->
                    appState.navController.navigate(FreeEdDestination.PortfolioItemDetail.createRoute(id))
                },
                onOpenSettings = {
                    if (sessionUiState.hasActiveSession) {
                        appState.navController.navigate(FreeEdDestination.Settings.route)
                    } else {
                        appState.navController.navigate(FreeEdDestination.Login.route)
                    }
                },
                onOpenFavorites = { appState.navController.navigate(FreeEdDestination.Favorites.route) },
                onOpenMyServices = { appState.navController.navigate(FreeEdDestination.MyServices.route) },
                onEditProfile = {
                    if (!sessionUiState.hasActiveSession) {
                        appState.navController.navigate(FreeEdDestination.Register.route)
                    } else if (sessionUiState.profile?.role == com.raulcn.freeed.core.model.UserRole.STUDENT) {
                        appState.navController.navigate(FreeEdDestination.StudentProfileEdit.route)
                    } else {
                        appState.navController.navigate(FreeEdDestination.CompanyProfileEdit.route)
                    }
                },
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
    sessionViewModel: AppSessionViewModel
) {
    composable(
        route = FreeEdDestination.StudentProfile.route,
        arguments = listOf(navArgument("profileId") { type = NavType.StringType })
    ) { backStackEntry ->
        StudentProfileDetailRoute(
            profileId = backStackEntry.arguments?.getString("profileId").orEmpty(),
            onBackClick = { appState.navController.popBackStack() },
            onOpenService = { id ->
                appState.navController.navigate(FreeEdDestination.ServiceDetail.createRoute(id))
            },
            onOpenPortfolioItem = { id ->
                appState.navController.navigate(FreeEdDestination.PortfolioItemDetail.createRoute(id))
            }
        )
    }

    composable(
        route = FreeEdDestination.CompanyProfile.route,
        arguments = listOf(navArgument("profileId") { type = NavType.StringType })
    ) { backStackEntry ->
        CompanyProfileDetailRoute(
            profileId = backStackEntry.arguments?.getString("profileId").orEmpty(),
            onBackClick = { appState.navController.popBackStack() }
        )
    }

    composable(
        route = FreeEdDestination.ServiceDetail.route,
        arguments = listOf(navArgument("serviceId") { type = NavType.StringType })
    ) { backStackEntry ->
        val sessionUiState by sessionViewModel.uiState.collectAsStateWithLifecycle()
        ServiceDetailRoute(
            serviceId = backStackEntry.arguments?.getString("serviceId").orEmpty(),
            sessionProfile = sessionUiState.profile,
            onBackClick = { appState.navController.popBackStack() },
            onEditClick = { id ->
                appState.navController.navigate(FreeEdDestination.ServiceEditor.createRoute(id))
            },
            onRequestClick = { id ->
                appState.navController.navigate(FreeEdDestination.SendRequest.createRoute(id))
            },
            onRequireAuth = { appState.navController.navigate(FreeEdDestination.Login.route) }
        )
    }

    composable(
        route = FreeEdDestination.SendRequest.route,
        arguments = listOf(navArgument("serviceId") { type = NavType.StringType })
    ) { backStackEntry ->
        SendRequestRoute(
            serviceId = backStackEntry.arguments?.getString("serviceId").orEmpty(),
            onBackClick = { appState.navController.popBackStack() }
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
        val sessionUiState by sessionViewModel.uiState.collectAsStateWithLifecycle()
        RequestDetailRoute(
            requestId = backStackEntry.arguments?.getString("requestId").orEmpty(),
            sessionProfile = sessionUiState.profile,
            onBackClick = { appState.navController.popBackStack() }
        )
    }

    composable(
        route = FreeEdDestination.PortfolioItemDetail.route,
        arguments = listOf(navArgument("portfolioItemId") { type = NavType.StringType })
    ) { backStackEntry ->
        PortfolioItemDetailRoute(
            portfolioItemId = backStackEntry.arguments?.getString("portfolioItemId").orEmpty(),
            onBackClick = { appState.navController.popBackStack() },
            onEditClick = { id ->
                appState.navController.navigate(FreeEdDestination.PortfolioEditor.createRoute(id))
            }
        )
    }

    composable(
        route = FreeEdDestination.PortfolioEditor.route,
        arguments = listOf(navArgument("portfolioItemId") { type = NavType.StringType })
    ) { backStackEntry ->
        PortfolioEditorRoute(
            portfolioItemId = backStackEntry.arguments?.getString("portfolioItemId").orEmpty(),
            onBackClick = { appState.navController.popBackStack() }
        )
    }

    composable(FreeEdDestination.MyPortfolio.route) {
        MyPortfolioRoute(
            onBackClick = { appState.navController.popBackStack() },
            onCreateNew = {
                appState.navController.navigate(
                    FreeEdDestination.PortfolioEditor.createRoute(PortfolioEditorViewModel.NEW_PORTFOLIO_ARG)
                )
            },
            onOpenItem = { id ->
                appState.navController.navigate(FreeEdDestination.PortfolioItemDetail.createRoute(id))
            },
            onEditItem = { id ->
                appState.navController.navigate(FreeEdDestination.PortfolioEditor.createRoute(id))
            }
        )
    }

    composable(FreeEdDestination.Favorites.route) {
        FavoritesRoute(
            onBackClick = { appState.navController.popBackStack() },
            onOpenService = { id ->
                appState.navController.navigate(FreeEdDestination.ServiceDetail.createRoute(id))
            }
        )
    }
    composable(FreeEdDestination.MyServices.route) {
        MyServicesRoute(
            onBackClick = { appState.navController.popBackStack() },
            onCreateNew = {
                appState.navController.navigate(
                    FreeEdDestination.ServiceEditor.createRoute(ServiceEditorViewModel.NEW_SERVICE_ARG)
                )
            },
            onEditService = { id ->
                appState.navController.navigate(FreeEdDestination.ServiceEditor.createRoute(id))
            }
        )
    }
    composable(FreeEdDestination.SentRequests.route) {
        SentRequestsRoute(
            onBackClick = { appState.navController.popBackStack() },
            onOpenRequest = { id ->
                appState.navController.navigate(FreeEdDestination.RequestDetail.createRoute(id))
            }
        )
    }
    composable(FreeEdDestination.ReceivedRequests.route) {
        ReceivedRequestsRoute(
            onBackClick = { appState.navController.popBackStack() },
            onOpenRequest = { id ->
                appState.navController.navigate(FreeEdDestination.RequestDetail.createRoute(id))
            },
            onOpenCompanyProfile = { id ->
                appState.navController.navigate(FreeEdDestination.CompanyProfile.createRoute(id))
            },
        )
    }
    composable(FreeEdDestination.Settings.route) {
        SettingsRoute(onBackClick = { appState.navController.popBackStack() })
    }
}

@Composable
private fun FreeEdBottomBar(
    currentRoute: String?,
    hasActiveSession: Boolean,
    role: com.raulcn.freeed.core.model.UserRole?,
    onNavigate: (MainTabDestination) -> Unit
) {
    data class VisualTab(
        val destination: MainTabDestination,
        val label: String,
        val icon: androidx.compose.ui.graphics.vector.ImageVector,
        val selectedRoute: String = destination.route
    )

    val tabs = when {
        !hasActiveSession -> listOf(
            VisualTab(MainTabDestination.HOME, "Inicio", Icons.Outlined.Home),
            VisualTab(MainTabDestination.EXPLORE, "Explorar", Icons.Outlined.Search),
            VisualTab(MainTabDestination.CREATE, "Crear", Icons.Outlined.AddCircle),
            VisualTab(MainTabDestination.REQUESTS, "Entrar", Icons.Outlined.Person, FreeEdDestination.Login.route),
            VisualTab(MainTabDestination.PROFILE, "Perfil", Icons.Outlined.Person)
        )
        role == com.raulcn.freeed.core.model.UserRole.COMPANY -> listOf(
            VisualTab(MainTabDestination.HOME, "Inicio", Icons.Outlined.Home),
            VisualTab(MainTabDestination.EXPLORE, "Explorar", Icons.Outlined.Search),
            VisualTab(MainTabDestination.CREATE, "Guardados", Icons.Outlined.FavoriteBorder, FreeEdDestination.Favorites.route),
            VisualTab(MainTabDestination.REQUESTS, "Solicitudes", Icons.Outlined.BusinessCenter),
            VisualTab(MainTabDestination.PROFILE, "Perfil", Icons.Outlined.Person)
        )
        else -> listOf(
            VisualTab(MainTabDestination.HOME, "Inicio", Icons.Outlined.Home),
            VisualTab(MainTabDestination.EXPLORE, "Explorar", Icons.Outlined.Search),
            VisualTab(MainTabDestination.CREATE, "Publicar", Icons.Outlined.AddCircle),
            VisualTab(MainTabDestination.REQUESTS, "Solicitudes", Icons.Outlined.BusinessCenter),
            VisualTab(MainTabDestination.PROFILE, "Perfil", Icons.Outlined.Person)
        )
    }

    NavigationBar(
        tonalElevation = 0.dp
    ) {
        tabs.forEach { tab ->
            val selected = currentRoute == tab.selectedRoute
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(tab.destination) },
                icon = {
                    if (tab.destination == MainTabDestination.CREATE) {
                        Surface(
                            shape = CircleShape,
                            color = if (selected) {
                                androidx.compose.material3.MaterialTheme.colorScheme.primary
                            } else {
                                androidx.compose.material3.MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                            }
                        ) {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.label,
                                tint = if (selected) {
                                    androidx.compose.material3.MaterialTheme.colorScheme.onPrimary
                                } else {
                                    androidx.compose.material3.MaterialTheme.colorScheme.primary
                                },
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    } else {
                        Icon(imageVector = tab.icon, contentDescription = tab.label)
                    }
                },
                label = { Text(tab.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                    selectedTextColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                    indicatorColor = androidx.compose.material3.MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                    unselectedIconColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
