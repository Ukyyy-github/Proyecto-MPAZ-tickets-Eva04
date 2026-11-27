package cl.mpaz.ticket.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cl.mpaz.ticket.AppContainer
import cl.mpaz.ticket.ui.auth.AuthViewModel
import cl.mpaz.ticket.ui.auth.LoginScreen
import cl.mpaz.ticket.ui.auth.RegisterScreen
import cl.mpaz.ticket.ui.perfil.PerfilScreen
import cl.mpaz.ticket.ui.perfil.PerfilViewModel
import cl.mpaz.ticket.ui.tickets.TicketDetailScreen
import cl.mpaz.ticket.ui.tickets.TicketDetailViewModel
import cl.mpaz.ticket.ui.tickets.TicketFormScreen
import cl.mpaz.ticket.ui.tickets.TicketFormViewModel
import cl.mpaz.ticket.ui.tickets.TicketListScreen
import cl.mpaz.ticket.ui.tickets.TicketListViewModel

object Rutas {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val REGISTRO = "registro"
    const val LISTA = "tickets"
    const val PERFIL = "perfil"
    const val FORMULARIO = "formulario?ticketId={ticketId}"
    const val DETALLE = "detalle/{ticketId}"

    fun formulario(ticketId: Long? = null) =
        if (ticketId == null) "formulario" else "formulario?ticketId=$ticketId"

    fun detalle(ticketId: Long) = "detalle/$ticketId"
}

@Composable
fun AppNav(container: AppContainer) {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel(factory = Fabricas.auth(container))
    val usuario by authViewModel.usuario.collectAsState()
    val sesionLista by authViewModel.sesionLista.collectAsState()

    // Redirige según haya o no sesión iniciada (login, registro exitoso, cierre de sesión).
    LaunchedEffect(sesionLista, usuario) {
        if (!sesionLista) return@LaunchedEffect
        val ruta = navController.currentBackStackEntry?.destination?.route ?: Rutas.SPLASH
        val enAutenticacion = ruta == Rutas.SPLASH || ruta == Rutas.LOGIN || ruta == Rutas.REGISTRO
        if (usuario != null && enAutenticacion) {
            navController.navigate(Rutas.LISTA) {
                popUpTo(navController.graph.id) { inclusive = true }
            }
        } else if (usuario == null && (!enAutenticacion || ruta == Rutas.SPLASH)) {
            navController.navigate(Rutas.LOGIN) {
                popUpTo(navController.graph.id) { inclusive = true }
            }
        }
    }

    NavHost(navController = navController, startDestination = Rutas.SPLASH) {
        composable(Rutas.SPLASH) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        composable(Rutas.LOGIN) {
            LoginScreen(
                viewModel = authViewModel,
                onIrARegistro = { navController.navigate(Rutas.REGISTRO) }
            )
        }
        composable(Rutas.REGISTRO) {
            RegisterScreen(
                viewModel = authViewModel,
                onVolver = { navController.popBackStack() }
            )
        }
        composable(Rutas.LISTA) {
            val vm: TicketListViewModel = viewModel(factory = Fabricas.lista(container))
            TicketListScreen(
                viewModel = vm,
                onAbrirTicket = { navController.navigate(Rutas.detalle(it)) },
                onCrearTicket = { navController.navigate(Rutas.formulario()) },
                onAbrirPerfil = { navController.navigate(Rutas.PERFIL) }
            )
        }
        composable(Rutas.PERFIL) {
            val vm: PerfilViewModel = viewModel(factory = Fabricas.perfil(container))
            PerfilScreen(
                viewModel = vm,
                onVolver = { navController.popBackStack() }
            )
        }
        composable(
            route = Rutas.FORMULARIO,
            arguments = listOf(navArgument("ticketId") {
                type = NavType.LongType
                defaultValue = -1L
            })
        ) { entrada ->
            val ticketId = entrada.arguments?.getLong("ticketId")?.takeIf { it > 0 }
            val vm: TicketFormViewModel = viewModel(factory = Fabricas.formulario(container, ticketId))
            TicketFormScreen(
                viewModel = vm,
                esEdicion = ticketId != null,
                onVolver = { navController.popBackStack() }
            )
        }
        composable(
            route = Rutas.DETALLE,
            arguments = listOf(navArgument("ticketId") { type = NavType.LongType })
        ) { entrada ->
            val ticketId = entrada.arguments?.getLong("ticketId") ?: 0L
            val vm: TicketDetailViewModel = viewModel(factory = Fabricas.detalle(container, ticketId))
            TicketDetailScreen(
                viewModel = vm,
                onVolver = { navController.popBackStack() },
                onEditar = { navController.navigate(Rutas.formulario(it)) }
            )
        }
    }
}
