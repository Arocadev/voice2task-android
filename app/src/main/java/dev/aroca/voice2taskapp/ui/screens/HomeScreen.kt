package dev.aroca.voice2taskapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.aroca.voice2taskapp.data.model.Lista
import dev.aroca.voice2taskapp.data.model.Tarea
import dev.aroca.voice2taskapp.ui.theme.*
import dev.aroca.voice2taskapp.viewmodel.ListasState
import dev.aroca.voice2taskapp.viewmodel.ListasViewModel
import dev.aroca.voice2taskapp.viewmodel.TareasViewModel

sealed class HomeTab {
    object Inicio : HomeTab()
    object Importante : HomeTab()
    object Calendario : HomeTab()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToLista: (Int, String) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToTareaDetalle: (Tarea) -> Unit = {},
    onLogout: () -> Unit = {},
    listasViewModel: ListasViewModel = viewModel(),
    tareasViewModel: TareasViewModel = viewModel()
) {
    val state by listasViewModel.state.collectAsState()
    var showCrearLista by remember { mutableStateOf(false) }
    var nombreNueva by remember { mutableStateOf("") }
    var tabActual by remember { mutableStateOf<HomeTab>(HomeTab.Inicio) }

    Scaffold(
        containerColor = Background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Voice", color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Text("2", color = Primary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Text("Task", color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Background),
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Ajustes", tint = TextMuted)
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Surface, tonalElevation = 0.dp) {
                NavItem(Icons.Default.Home, "Inicio", tabActual is HomeTab.Inicio) {
                    tabActual = HomeTab.Inicio
                }
                NavItem(Icons.Default.Star, "Importante", tabActual is HomeTab.Importante) {
                    tabActual = HomeTab.Importante
                }
                NavItem(Icons.Default.CalendarMonth, "Calendario", tabActual is HomeTab.Calendario) {
                    tabActual = HomeTab.Calendario
                }
            }
        },
        floatingActionButton = {
            if (tabActual is HomeTab.Inicio) {
                FloatingActionButton(
                    onClick = { showCrearLista = true },
                    containerColor = Primary,
                    contentColor = OnPrimary,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Nueva lista")
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (tabActual) {
                is HomeTab.Inicio -> {
                    when (val s = state) {
                        is ListasState.Loading -> CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = Primary
                        )
                        is ListasState.Error -> Text(
                            s.message,
                            color = Error,
                            modifier = Modifier.align(Alignment.Center)
                        )
                        is ListasState.Success -> {
                            if (s.listas.isEmpty()) {
                                EmptyListasState(modifier = Modifier.align(Alignment.Center))
                            } else {
                                LazyColumn(
                                    contentPadding = PaddingValues(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    item {
                                        Text(
                                            "Mis listas",
                                            color = TextSecondary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.padding(bottom = 4.dp)
                                        )
                                    }
                                    items(s.listas) { lista ->
                                        ListaCard(
                                            lista = lista,
                                            onClick = { onNavigateToLista(lista.id, lista.nombre) },
                                            onEliminar = { listasViewModel.eliminarLista(lista.id) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                is HomeTab.Importante -> {
                    ImportantesScreen(
                        onVerDetalle = { tarea -> onNavigateToTareaDetalle(tarea) },
                        viewModel = tareasViewModel
                    )
                }
                is HomeTab.Calendario -> {
                    CalendarioScreen(
                        onVerDetalle = { tarea -> onNavigateToTareaDetalle(tarea) },
                        viewModel = tareasViewModel
                    )
                }
            }
        }
    }

    if (showCrearLista) {
        AlertDialog(
            onDismissRequest = { showCrearLista = false; nombreNueva = "" },
            containerColor = Surface,
            title = { Text("Nueva lista", color = TextPrimary) },
            text = {
                OutlinedTextField(
                    value = nombreNueva,
                    onValueChange = { nombreNueva = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = Border,
                        focusedLabelColor = Primary,
                        cursorColor = Primary
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nombreNueva.isNotBlank()) {
                            listasViewModel.crearLista(nombreNueva, null)
                            nombreNueva = ""
                            showCrearLista = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) { Text("Crear", color = OnPrimary) }
            },
            dismissButton = {
                TextButton(onClick = { showCrearLista = false; nombreNueva = "" }) {
                    Text("Cancelar", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun RowScope.NavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    NavigationBarItem(
        icon = { Icon(icon, contentDescription = label) },
        label = { Text(label, fontSize = 11.sp) },
        selected = selected,
        onClick = onClick,
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = Primary,
            selectedTextColor = Primary,
            unselectedIconColor = TextMuted,
            unselectedTextColor = TextMuted,
            indicatorColor = Primary.copy(alpha = 0.12f)
        )
    )
}

@Composable
private fun EmptyListasState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("📋", fontSize = 48.sp)
        Text("Sin listas todavía", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
        Text("Pulsa + para crear tu primera lista", style = MaterialTheme.typography.bodyMedium, color = TextMuted, textAlign = TextAlign.Center)
    }
}

@Composable
fun ListaCard(lista: Lista, onClick: () -> Unit, onEliminar: () -> Unit) {
    var showMenu by remember { mutableStateOf(false) }
    val color = try {
        Color(android.graphics.Color.parseColor(lista.color ?: "#14B8A6"))
    } catch (e: Exception) {
        Primary
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(color))
            Text(
                text = lista.nombre,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                modifier = Modifier.weight(1f)
            )
            Box {
                IconButton(onClick = { showMenu = true }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Opciones", tint = TextMuted, modifier = Modifier.size(20.dp))
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }, containerColor = SurfaceVariant) {
                    DropdownMenuItem(
                        text = { Text("Eliminar", color = Error) },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Error, modifier = Modifier.size(18.dp)) },
                        onClick = { showMenu = false; onEliminar() }
                    )
                }
            }
        }
    }
}