package cl.mpaz.ticket

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import cl.mpaz.ticket.ui.navigation.AppNav
import cl.mpaz.ticket.ui.theme.MPAZTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as MpazApp).container
        setContent {
            MPAZTheme {
                AppNav(container)
            }
        }
    }
}
