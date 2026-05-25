package com.rms.app.core.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Onboarding : Screen("onboarding")
    data object Home : Screen("home")
    data object TenantDetail : Screen("tenant_detail/{tenantId}") {
        fun createRoute(tenantId: Long) = "tenant_detail/$tenantId"
    }
    data object AddTenant : Screen("add_tenant?tenantId={tenantId}") {
        fun createRoute(tenantId: Long? = null) =
            if (tenantId != null) "add_tenant?tenantId=$tenantId" else "add_tenant?tenantId=-1"
    }
    data object PaymentHistory : Screen("payment_history")
    data object AddElectricityReading : Screen("add_electricity_reading/{tenantId}") {
        fun createRoute(tenantId: Long) = "add_electricity_reading/$tenantId"
    }
    data object Documents : Screen("documents")
    data object Reminders : Screen("reminders")
    data object Settings : Screen("settings")
}
