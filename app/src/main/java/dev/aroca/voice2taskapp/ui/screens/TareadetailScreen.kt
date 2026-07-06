package dev.aroca.voice2taskapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.aroca.voice2taskapp.data.model.Tarea
import dev.aroca.voice2taskapp.ui.theme.*
import dev.aroca.voice2taskapp.viewmodel.EditarState
import dev.aroca.voice2taskapp.viewmodel.TareasViewModel

// Convierte "YYYY-MM-DD..." → "DD/MM/YYYY" para mostrar al usuario
private fun fechaParaMostrar(fechaIso: String?): String {
    if (fechaIso == null) return ""
    val partes = fechaIso.take(10).split("-")
    if (partes.size != 3) return fechaIso.take(10)
    return "${partes[2]}/${partes[1]}/${partes[0]}"
}

// Convierte "DD/MM/YYYY" → "YYYY-MM-DDT00:00:00" para el backend
private fun fechaParaBackend(fechaInput: String): String? {
    val limpio = fechaInput.trim()
    if (limpio.isBlank()) return null
    return try {
        val partes = limpio.split("/")
        if (partes.size == 3 && partes[0].length == 2 && partes[1].length == 2 && partes[2].length == 4) {
            "${partes[2]}-${partes[1]}-${partes[0]}T00:00:00"
        } else null
    } catch (_: Exception) {
        null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TareaDetailScreen(
    tarea: Tarea,
    onBack: () -> Unit,
    onCompletar: () -> Unit,
    onEliminar: () -> Unit,
    viewModel: TareasViewModel = viewModel()
) {
    val editarState by viewModel.editarState.collectAsState()
    var mostrarEditar by remember { mutableStateOf(false) }

    val prioridadColor = when (tarea.prioridad) {
        "ALTA" -> PriorityAlta
        "MEDIA" -> PriorityMedia
        else -> PriorityBaja
    }

    LaunchedEffect(editarState) {
        if (editarState is EditarState.Success) {
            mostrarEditar = false
            viewModel.resetEditarState()
            onBack()
        }
    }

    Scaffold(
        containerColor = Background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Detalle de tarea", color = Primary, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { mostrarEditar = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Primary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Background)
            )
        },
        bottomBar = {
            if (!tarea.completada) {
                Surface(color = Surface, tonalElevation = 0.dp) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Button(
                            onClick = onCompletar,
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary)
                        ) {
                            Text("Marcar como completada", color = OnPrimary)
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Título", color = TextSecondary, fontSize = 12.sp)
                Text(tarea.titulo, style = MaterialTheme.typography.headlineSmall, color = TextPrimary, fontWeight = FontWeight.SemiBold)
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Prioridad", color = TextSecondary, fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(prioridadColor))
                    Text(
                        tarea.prioridad.lowercase().replaceFirstChar { it.uppercase() },
                        color = prioridadColor,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                    if (tarea.completada) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(shape = RoundedCornerShape(20.dp), color = Primary.copy(alpha = 0.15f)) {
                            Text("Completada", color = Primary, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                        }
                    }
                }
            }

            tarea.descripcion?.let { desc ->
                if (desc.isNotBlank()) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Descripción", color = TextSecondary, fontSize = 12.sp)
                        Text(desc, color = TextPrimary, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }

            tarea.fecha_limite?.let { fecha ->
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Fecha límite", color = TextSecondary, fontSize = 12.sp)
                    Text(fechaParaMostrar(fecha), color = PrimaryLight, style = MaterialTheme.typography.bodyLarge)
                }
            }

            tarea.audio_transcripcion?.let { transcripcion ->
                if (transcripcion.isNotBlank()) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Nota de voz original", color = TextSecondary, fontSize = 12.sp)
                        Surface(shape = RoundedCornerShape(12.dp), color = Surface) {
                            Text(transcripcion, color = TextMuted, fontSize = 13.sp, modifier = Modifier.padding(14.dp))
                        }
                    }
                }
            }

            Text("Creada el ${fechaParaMostrar(tarea.created_at)}", color = TextMuted, fontSize = 12.sp)
        }
    }

    if (mostrarEditar) {
        EditarTareaSheet(
            tarea = tarea,
            editarState = editarState,
            onDismiss = {
                mostrarEditar = false
                viewModel.resetEditarState()
            },
            onGuardar = { titulo, descripcion, fechaInput, prioridad ->
                val fechaBackend = fechaParaBackend(fechaInput ?: "")
                viewModel.editarTarea(tarea.id, titulo, descripcion, fechaBackend, prioridad, tarea.lista_id)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarTareaSheet(
    tarea: Tarea,
    editarState: EditarState,
    onDismiss: () -> Unit,
    onGuardar: (titulo: String, descripcion: String?, fechaLimite: String?, prioridad: String) -> Unit
) {
    var titulo by remember { mutableStateOf(tarea.titulo) }
    var descripcion by remember { mutableStateOf(tarea.descripcion ?: "") }
    var prioridad by remember { mutableStateOf(tarea.prioridad) }
    // Mostrar la fecha existente en formato DD/MM/YYYY
    var fechaLimite by remember { mutableStateOf(fechaParaMostrar(tarea.fecha_limite)) }
    var fechaError by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceVariant
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Editar tarea", style = MaterialTheme.typography.titleLarge, color = TextPrimary, fontWeight = FontWeight.SemiBold)

            OutlinedTextField(
                value = titulo,
                onValueChange = { titulo = it },
                label = { Text("Título", color = TextSecondary) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary, unfocusedBorderColor = TextMuted,
                    focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, cursorColor = Primary
                )
            )

            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción", color = TextSecondary) },
                modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp),
                maxLines = 4,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary, unfocusedBorderColor = TextMuted,
                    focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, cursorColor = Primary
                )
            )

            OutlinedTextField(
                value = fechaLimite,
                onValueChange = {
                    fechaLimite = it
                    fechaError = false
                },
                label = { Text("Fecha límite", color = TextSecondary) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("DD/MM/YYYY", color = TextMuted) },
                isError = fechaError,
                supportingText = {
                    if (fechaError) Text("Formato incorrecto. Usa DD/MM/YYYY", color = Error, fontSize = 12.sp)
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary, unfocusedBorderColor = TextMuted,
                    focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, cursorColor = Primary
                )
            )

            Text("Prioridad", color = TextSecondary, fontSize = 13.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("BAJA", "MEDIA", "ALTA").forEach { p ->
                    val color = when (p) {
                        "ALTA" -> PriorityAlta
                        "MEDIA" -> PriorityMedia
                        else -> PriorityBaja
                    }
                    val seleccionado = prioridad == p
                    Surface(
                        modifier = Modifier.clickable { prioridad = p },
                        shape = RoundedCornerShape(20.dp),
                        color = if (seleccionado) color.copy(alpha = 0.2f) else SurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (seleccionado) 1.5.dp else 1.dp,
                            color = if (seleccionado) color else TextMuted.copy(alpha = 0.4f)
                        )
                    ) {
                        Text(
                            p.lowercase().replaceFirstChar { it.uppercase() },
                            color = if (seleccionado) color else TextMuted,
                            fontSize = 13.sp,
                            fontWeight = if (seleccionado) FontWeight.SemiBold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            if (editarState is EditarState.Error) {
                Text(editarState.message, color = Error, fontSize = 13.sp)
            }

            Button(
                onClick = {
                    if (titulo.isNotBlank()) {
                        // Validar fecha si el usuario escribió algo
                        if (fechaLimite.isNotBlank() && fechaParaBackend(fechaLimite) == null) {
                            fechaError = true
                            return@Button
                        }
                        onGuardar(
                            titulo.trim(),
                            descripcion.trim().ifBlank { null },
                            fechaLimite.trim().ifBlank { null },
                            prioridad
                        )
                    }
                },
                enabled = titulo.isNotBlank() && editarState !is EditarState.Loading,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                if (editarState is EditarState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = OnPrimary, strokeWidth = 2.dp)
                } else {
                    Text("Guardar cambios", color = OnPrimary, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}