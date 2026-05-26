package com.raulcn.freeed.app.navigation

object FreeEdGraphs {
    const val AUTH = "auth_graph"
    const val ONBOARDING = "onboarding_graph"
    const val MAIN = "main_graph"
}

sealed class FreeEdDestination(
    val route: String
) {
    data object Splash : FreeEdDestination("splash")
    data object Login : FreeEdDestination("login")
    data object Register : FreeEdDestination("register")
    data object RoleSelection : FreeEdDestination("role_selection")
    data object StudentProfileSetup : FreeEdDestination("student_profile_setup")
    data object CompanyProfileSetup : FreeEdDestination("company_profile_setup")
    data object Home : FreeEdDestination("home")
    data object Explore : FreeEdDestination("explore")
    data object CreateService : FreeEdDestination("create_service")
    data object Requests : FreeEdDestination("requests")
    data object MyProfile : FreeEdDestination("my_profile")
    data object Favorites : FreeEdDestination("favorites")
    data object MyServices : FreeEdDestination("my_services")
    data object SentRequests : FreeEdDestination("sent_requests")
    data object ReceivedRequests : FreeEdDestination("received_requests")
    data object Settings : FreeEdDestination("settings")
    data object StudentProfile : FreeEdDestination("student_profile/{profileId}") {
        fun createRoute(profileId: String): String = "student_profile/$profileId"
    }

    data object ServiceDetail : FreeEdDestination("service_detail/{serviceId}") {
        fun createRoute(serviceId: String): String = "service_detail/$serviceId"
    }

    data object ServiceEditor : FreeEdDestination("service_editor/{serviceId}") {
        fun createRoute(serviceId: String): String = "service_editor/$serviceId"
    }

    data object RequestDetail : FreeEdDestination("request_detail/{requestId}") {
        fun createRoute(requestId: String): String = "request_detail/$requestId"
    }

    data object PortfolioItemDetail : FreeEdDestination("portfolio_item/{portfolioItemId}") {
        fun createRoute(portfolioItemId: String): String = "portfolio_item/$portfolioItemId"
    }
}

enum class MainTabDestination(
    val route: String,
    val label: String
) {
    HOME(route = FreeEdDestination.Home.route, label = "Inicio"),
    EXPLORE(route = FreeEdDestination.Explore.route, label = "Explorar"),
    CREATE(route = FreeEdDestination.CreateService.route, label = "Publicar"),
    REQUESTS(route = FreeEdDestination.Requests.route, label = "Solicitudes"),
    PROFILE(route = FreeEdDestination.MyProfile.route, label = "Perfil")
}

