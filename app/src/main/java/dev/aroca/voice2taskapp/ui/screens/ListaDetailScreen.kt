package dev.aroca.voice2taskapp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.aroca.voice2taskapp.data.model.Tarea
import dev.aroca.voice2taskapp.ui.theme.*
import dev.aroca.voice2taskapp.viewmodel.FiltroTareas
import dev.aroca.voice2taskapp.viewmodel.TareasState
import dev.aroca.voice2taskapp.viewmodel.TareasViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaDetailScreen(
    listaId: Int,
    listaNombre: String,
    onBack: () -> Unit,
    onGrabar: () -> Unit = {},
    onVerDetalle: (Tarea) -> Unit = {},
    viewModel: TareasViewModel = viewModel()
) {
    val tareasState by viewModel.tareasState.collectAsState()
    val filtroActivo by viewModel.filtroActivo.collectAsState()
    val busquedaState by viewModel.busquedaState.collectAsState()

    var busquedaActiva by remember { mutableStateOf(false) }
    var textoBusqueda by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(listaId) { viewModel.cargarTareas(listaId) }

    LaunchedEffect(textoBusqueda) {
        if (busquedaActiva) viewModel.buscarTareas(textoBusqueda)
    }

    Scaffold(
        containerColor = Background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    if (busquedaActiva) {
                        TextField(
                            value = textoBusqueda,
                            onValueChange = { textoBusqueda = it },
                            placeholder = { Text("Buscar tareas...", color = TextMuted) },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = SurfaceVariant,
                                unfocusedContainerColor = SurfaceVariant,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                cursorColor = Primary,
                                focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                                unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = { viewModel.buscarTareas(textoBusqueda) })
                        )
                        LaunchedEffect(Unit) { focusRequester.requestFocus() }
                    } else {
                        Text(listaNombre, color = Primary, fontWeight = FontWeight.SemiBold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (busquedaActiva) {
                            busquedaActiva = false
                            textoBusqueda = ""
                        } else {
                            onBack()
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        busquedaActiva = !busquedaActiva
                        if (!busquedaActiva) textoBusqueda = ""
                    }) {
                        Icon(
                            if (busquedaActiva) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = "Buscar",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Background)
            )
        },
        floatingActionButton = {
            if (!busquedaActiva) {
                FloatingActionButton(
                    onClick = onGrabar,
                    containerColor = Primary,
                    contentColor = OnPrimary,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Mic, contentDescription = "Grabar audio")
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            // ── Chips de filtro (solo cuando no hay búsqueda activa) ───────────
            AnimatedVisibility(visible = !busquedaActiva) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(FiltroTareas.entries.toList()) { filtro ->
                        val seleccionado = filtroActivo == filtro
                        FilterChip(
                            selected = seleccionado,
                            onClick = { viewModel.cargarTareasConFiltro(listaId, filtro) },
                            label = {
                                Text(
                                    when (filtro) {
                                        FiltroTareas.TODAS -> "Todas"
                                        FiltroTareas.PENDIENTES -> "Pendientes"
                                        FiltroTareas.COMPLETADAS -> "Completadas"
                                        FiltroTareas.IMPORTANTES -> "Importantes"
                                    },
                                    style = MaterialTheme.typography.bodySmall
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Primary,
                                selectedLabelColor = OnPrimary,
                                containerColor = SurfaceVariant,
                                labelColor = TextSecondary
                            )
                        )
                    }
                }
            }

            // ── Contenido ─────────────────────────────────────────────────────
            val estadoActivo = if (busquedaActiva) busquedaState else tareasState

            Box(modifier = Modifier.fillMaxSize()) {
                when (val s = estadoActivo) {
                    is TareasState.Loading -> CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Primary
                    )
                    is TareasState.Error -> Text(
                        s.message,
                        color = Error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                    is TareasState.Success -> {
                        if (s.tareas.isEmpty()) {
                            Column(
                                modifier = Modifier.align(Alignment.Center),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    if (busquedaActiva) "🔍" else "🎙️",
                                    style = MaterialTheme.typography.headlineLarge
                                )
                                Text(
                                    if (busquedaActiva) "Sin resultados" else "Sin tareas todavía",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary
                                )
                                Text(
                                    if (busquedaActiva) "Prueba con otro texto" else "Pulsa el micrófono para crear una",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextMuted
                                )
                            }
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(s.tareas) { tarea ->
                                    TareaCard(
                                        tarea = tarea,
                                        onCompletar = { viewModel.completarTarea(tarea.id, listaId) },
                                        onEliminar = { viewModel.eliminarTarea(tarea.id, listaId) },
                                        onMarcarImportante = {
                                            viewModel.marcarImportante(tarea.id, !tarea.importante, listaId)
                                        },
                                        onVerDetalle = { onVerDetalle(tarea) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TareaCard(
    tarea: Tarea,
    onCompletar: () -> Unit,
    onEliminar: () -> Unit,
    onMarcarImportante: () -> Unit = {},
    onVerDetalle: () -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }

    val prioridadColor = when (tarea.prioridad) {
        "ALTA" -> PriorityAlta
        "MEDIA" -> PriorityMedia
        else -> PriorityBaja
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onVerDetalle() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (tarea.completada) Surface.copy(alpha = 0.6f) else Surface
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(36.dp)
                    .background(
                        if (tarea.completada) prioridadColor.copy(alpha = 0.35f) else prioridadColor,
                        RoundedCornerShape(2.dp)
                    )
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        tarea.titulo,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (tarea.completada) TextMuted else TextPrimary,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (tarea.importante) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = "Importante",
                            tint = androidx.compose.ui.graphics.Color(0xFFF59E0B),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                tarea.descripcion?.let {
                    if (it.isNotBlank()) Text(it, style = MaterialTheme.typography.bodyMedium, maxLines = 1, color = TextMuted)
                }
                tarea.fecha_limite?.let {
                    Text(it.take(10), style = MaterialTheme.typography.bodySmall, color = PrimaryLight)
                }
            }

            if (tarea.completada) {
                Icon(Icons.Default.Check, contentDescription = "Completada", tint = Primary, modifier = Modifier.size(18.dp))
            }

            Box {
                IconButton(onClick = { showMenu = true }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Opciones", tint = TextMuted, modifier = Modifier.size(20.dp))
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }, containerColor = SurfaceVariant) {
                    if (!tarea.completada) {
                        DropdownMenuItem(
                            text = { Text("Marcar como completada", color = TextPrimary) },
                            leadingIcon = { Icon(Icons.Default.Check, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp)) },
                            onClick = { showMenu = false; onCompletar() }
                        )
                    }
                    DropdownMenuItem(
                        text = {
                            Text(
                                if (tarea.importante) "Quitar importante" else "Marcar como importante",
                                color = TextPrimary
                            )
                        },
                        leadingIcon = {
                            Icon(
                                if (tarea.importante) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = null,
                                tint = androidx.compose.ui.graphics.Color(0xFFF59E0B),
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        onClick = { showMenu = false; onMarcarImportante() }
                    )
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