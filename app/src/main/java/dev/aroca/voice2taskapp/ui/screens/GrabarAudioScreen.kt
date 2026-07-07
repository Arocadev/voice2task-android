package dev.aroca.voice2taskapp.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import dev.aroca.voice2taskapp.R
import dev.aroca.voice2taskapp.data.api.ExternalApiClient
import dev.aroca.voice2taskapp.data.model.AudioProcesamientoResponse
import dev.aroca.voice2taskapp.data.repository.TokenRepository
import dev.aroca.voice2taskapp.ui.theme.*
import dev.aroca.voice2taskapp.viewmodel.GrabarAudioViewModel
import dev.aroca.voice2taskapp.viewmodel.GrabarState
import dev.aroca.voice2taskapp.viewmodel.PasosProcesando
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrabarAudioScreen(
    listaId: Int,
    listaNombre: String,
    onDismiss: () -> Unit,
    onTareaCreada: () -> Unit,
    onIrAjustes: () -> Unit = {},
    viewModel: GrabarAudioViewModel = viewModel()
) {
    val context = LocalContext.current
    val tokenRepository = remember { TokenRepository(context) }
    val groqKey by tokenRepository.groqKey.collectAsState(initial = "")

    val state by viewModel.state.collectAsState()
    val segundos by viewModel.segundos.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) viewModel.iniciarGrabacion(context)
    }

    LaunchedEffect(state) {
        if (state is GrabarState.Confirmada) {
            onTareaCreada()
            viewModel.resetear()
        }
    }

    if (groqKey.isNullOrBlank()) {
        PantallaSinGroqKey(onCancelar = onDismiss, onIrAjustes = onIrAjustes)
        return
    }

    when (val s = state) {
        is GrabarState.Idle, is GrabarState.Grabando -> {
            PantallaGrabacion(
                grabando = s is GrabarState.Grabando,
                segundos = segundos,
                onIniciar = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                onFinalizar = { viewModel.detenerYProcesar() },
                onCancelar = { viewModel.cancelar(); onDismiss() }
            )
        }
        is GrabarState.Procesando -> PantallaProcessando(paso = s.paso)
        is GrabarState.Propuesta -> {
            PantallaConfirmacion(
                propuesta = s.respuesta,
                listaNombre = listaNombre,
                tokenRepository = tokenRepository,
                onConfirmar = { viewModel.confirmarTarea(s.respuesta, listaId) },
                onCancelar = { viewModel.resetear(); onDismiss() }
            )
        }
        is GrabarState.Error -> {
            PantallaError(
                mensaje = s.message,
                onReintentar = { viewModel.resetear() },
                onCancelar = { viewModel.resetear(); onDismiss() }
            )
        }
        is GrabarState.Confirmada -> { /* handled by LaunchedEffect */ }
    }
}

@Composable
private fun PantallaSinGroqKey(onCancelar: () -> Unit, onIrAjustes: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(Background), contentAlignment = Alignment.Center) {
        IconButton(onClick = onCancelar, modifier = Modifier.align(Alignment.TopStart).padding(16.dp)) {
            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.grabar_cancel), tint = TextSecondary)
        }
        Column(modifier = Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Icon(Icons.Default.Key, contentDescription = null, tint = Primary, modifier = Modifier.size(56.dp))
            Text(stringResource(R.string.groq_key_required_title), color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
            Text(stringResource(R.string.groq_key_required_desc), color = TextSecondary, fontSize = 14.sp, textAlign = TextAlign.Center)
            Button(onClick = onIrAjustes, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Primary)) {
                Text(stringResource(R.string.groq_key_go_settings), color = OnPrimary, fontWeight = FontWeight.SemiBold)
            }
            TextButton(onClick = onCancelar) { Text(stringResource(R.string.grabar_cancel), color = TextSecondary) }
        }
    }
}

@Composable
private fun PantallaGrabacion(
    grabando: Boolean,
    segundos: Int,
    onIniciar: () -> Unit,
    onFinalizar: () -> Unit,
    onCancelar: () -> Unit
) {
    val pulseAnim = rememberInfiniteTransition(label = "pulse")
    val scale by pulseAnim.animateFloat(
        initialValue = 1f,
        targetValue = if (grabando) 1.12f else 1f,
        animationSpec = infiniteRepeatable(animation = tween(700, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse),
        label = "scale"
    )
    Box(modifier = Modifier.fillMaxSize().background(Background), contentAlignment = Alignment.Center) {
        IconButton(onClick = onCancelar, modifier = Modifier.align(Alignment.TopStart).padding(16.dp)) {
            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.grabar_cancel), tint = TextSecondary)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(32.dp)) {
            Box(contentAlignment = Alignment.Center) {
                if (grabando) {
                    Box(modifier = Modifier.size(160.dp).scale(scale * 1.25f).clip(CircleShape).background(Primary.copy(alpha = 0.08f)))
                    Box(modifier = Modifier.size(160.dp).scale(scale * 1.12f).clip(CircleShape).background(Primary.copy(alpha = 0.12f)))
                }
                Box(
                    modifier = Modifier.size(140.dp).clip(CircleShape).background(Primary)
                        .then(if (!grabando) Modifier.clickable { onIniciar() } else Modifier),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Mic, contentDescription = stringResource(R.string.grabar_mic_desc), tint = OnPrimary, modifier = Modifier.size(60.dp))
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (grabando) stringResource(R.string.grabar_listening) else stringResource(R.string.grabar_tap_to_record),
                    color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.SemiBold
                )
                if (grabando) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        String.format(Locale.getDefault(), "%02d:%02d", segundos / 60, segundos % 60),
                        color = Primary, fontSize = 32.sp, fontWeight = FontWeight.Bold
                    )
                }
            }
            if (grabando) WaveformBars()
            if (grabando) {
                Button(onClick = onFinalizar, modifier = Modifier.fillMaxWidth(0.7f).height(52.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Primary)) {
                    Text(stringResource(R.string.grabar_finish), color = OnPrimary, fontWeight = FontWeight.SemiBold)
                }
                TextButton(onClick = onCancelar) { Text(stringResource(R.string.grabar_cancel), color = TextSecondary) }
            }
        }
    }
}

@Composable
private fun WaveformBars() {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val heights = listOf(16, 32, 48, 24, 40, 16, 28, 44, 20, 36)
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically, modifier = Modifier.height(56.dp)) {
        heights.forEachIndexed { index, baseH ->
            val scale by infiniteTransition.animateFloat(
                initialValue = 0.3f, targetValue = 1f,
                animationSpec = infiniteRepeatable(animation = tween(500 + index * 60, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse),
                label = "bar$index"
            )
            Box(modifier = Modifier.width(3.dp).height((baseH * scale).dp).background(PrimaryLight, RoundedCornerShape(2.dp)))
        }
    }
}

@Composable
private fun PantallaProcessando(paso: PasosProcesando) {
    Box(modifier = Modifier.fillMaxSize().background(Background), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(32.dp)) {
            Box(
                modifier = Modifier.size(120.dp).clip(CircleShape).background(Surface).border(1.dp, Primary.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Primary, modifier = Modifier.size(48.dp), strokeWidth = 3.dp)
            }
            Text(stringResource(R.string.procesando_title), color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            Column(verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.Start, modifier = Modifier.padding(horizontal = 32.dp)) {
                PasoProcessado(stringResource(R.string.procesando_transcribing), done = paso != PasosProcesando.TRANSCRIBIENDO, activo = paso == PasosProcesando.TRANSCRIBIENDO)
                PasoProcessado(stringResource(R.string.procesando_understanding), done = paso == PasosProcesando.CREANDO, activo = paso == PasosProcesando.ENTENDIENDO)
                PasoProcessado(stringResource(R.string.procesando_creating), done = false, activo = paso == PasosProcesando.CREANDO)
            }
        }
    }
}

@Composable
private fun PasoProcessado(texto: String, done: Boolean, activo: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        when {
            done -> Icon(Icons.Default.Check, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp))
            activo -> CircularProgressIndicator(color = Primary, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
            else -> Icon(Icons.Default.RadioButtonUnchecked, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp))
        }
        Text(
            texto,
            color = when { done -> TextPrimary; activo -> TextPrimary; else -> TextMuted },
            fontSize = 14.sp,
            fontWeight = if (activo) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PantallaConfirmacion(
    propuesta: AudioProcesamientoResponse,
    listaNombre: String,
    tokenRepository: TokenRepository,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var titulo by remember { mutableStateOf(propuesta.titulo) }
    var descripcion by remember { mutableStateOf(propuesta.descripcion) }
    var prioridad by remember { mutableStateOf(propuesta.prioridad) }

    val trelloListId by tokenRepository.trelloListId.collectAsState(initial = "")
    val trelloApiKey by tokenRepository.trelloApiKey.collectAsState(initial = "")
    val trelloToken by tokenRepository.trelloToken.collectAsState(initial = "")
    val notionDatabaseId by tokenRepository.notionDatabaseId.collectAsState(initial = "")
    val notionToken by tokenRepository.notionToken.collectAsState(initial = "")

    val hayTrello = !trelloListId.isNullOrEmpty()
    val hayNotion = !notionDatabaseId.isNullOrEmpty()

    var enviarVoice2Task by remember { mutableStateOf(true) }
    var enviarTrello by remember { mutableStateOf(false) }
    var enviarNotion by remember { mutableStateOf(false) }

    var guardando by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val prioridadColor = when (prioridad) { "ALTA" -> PriorityAlta; "MEDIA" -> PriorityMedia; else -> PriorityBaja }

    val fechaMostrar = propuesta.fecha_limite?.take(10)?.let { iso ->
        try {
            val p = iso.split("-")
            if (p.size == 3) "${p[2]}/${p[1]}/${p[0]}" else iso
        } catch (_: Exception) { iso }
    }

    Scaffold(
        containerColor = Background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(stringResource(R.string.confirmacion_title), color = Primary, fontWeight = FontWeight.SemiBold)
                        Text(stringResource(R.string.confirmacion_subtitle), color = TextSecondary, fontSize = 12.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onCancelar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.lista_back), tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Background)
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Background)
                    .navigationBarsPadding() // <-- Safe Area para la barra inferior
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (errorMsg != null) {
                    Text(
                        errorMsg!!,
                        color = Error,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = {
                        scope.launch {
                            guardando = true
                            errorMsg = null
                            try {
                                if (enviarVoice2Task) onConfirmar()

                                if (enviarTrello && hayTrello) {
                                    try {
                                        ExternalApiClient.trelloApi.crearTarjeta(
                                            idList = trelloListId!!,
                                            name = titulo,
                                            desc = descripcion,
                                            due = propuesta.fecha_limite,
                                            key = trelloApiKey!!,
                                            token = trelloToken!!
                                        )
                                    } catch (_: Exception) {
                                        errorMsg = "Error al crear tarjeta en Trello"
                                    }
                                }

                                if (enviarNotion && hayNotion) {
                                    try {
                                        val titleArray = JsonArray().apply {
                                            add(
                                                JsonObject().apply {
                                                    add(
                                                        "text",
                                                        JsonObject().apply {
                                                            addProperty("content", titulo)
                                                        }
                                                    )
                                                }
                                            )
                                        }

                                        val properties = JsonObject().apply {
                                            add(
                                                "Name",
                                                JsonObject().apply {
                                                    add("title", titleArray)
                                                }
                                            )
                                        }

                                        val body = JsonObject().apply {
                                            add(
                                                "parent",
                                                JsonObject().apply {
                                                    addProperty("database_id", notionDatabaseId!!)
                                                }
                                            )
                                            add("properties", properties)
                                        }

                                        ExternalApiClient.notionApi.crearPagina(
                                            token = "Bearer ${notionToken!!}",
                                            body = body
                                        )
                                    } catch (_: Exception) {
                                        errorMsg = "Error al crear página en Notion"
                                    }
                                }
                            } finally {
                                guardando = false
                            }
                        }
                    },
                    enabled = !guardando && (enviarVoice2Task || enviarTrello || enviarNotion),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    if (guardando) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = OnPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            stringResource(R.string.confirmacion_save),
                            color = OnPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CampoConfirmacion(label = stringResource(R.string.confirmacion_label_titulo)) {
                OutlinedTextField(value = titulo, onValueChange = { titulo = it }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = Border, focusedLabelColor = Primary, cursorColor = Primary))
            }
            CampoConfirmacion(label = stringResource(R.string.confirmacion_label_descripcion)) {
                OutlinedTextField(value = descripcion, onValueChange = { descripcion = it }, modifier = Modifier.fillMaxWidth(), minLines = 2, shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = Border, focusedLabelColor = Primary, cursorColor = Primary))
            }
            CampoConfirmacion(label = stringResource(R.string.confirmacion_label_lista)) {
                OutlinedTextField(value = listaNombre, onValueChange = {}, enabled = false, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(disabledBorderColor = Border, disabledTextColor = TextSecondary))
            }
            if (fechaMostrar != null) {
                CampoConfirmacion(label = stringResource(R.string.confirmacion_label_fecha)) {
                    OutlinedTextField(
                        value = fechaMostrar, onValueChange = {}, enabled = false,
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                        trailingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = TextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(disabledBorderColor = Border, disabledTextColor = TextSecondary)
                    )
                }
            }
            CampoConfirmacion(label = stringResource(R.string.confirmacion_label_prioridad)) {
                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).border(1.dp, Border, RoundedCornerShape(12.dp)).padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(prioridadColor))
                    Text(prioridad.lowercase().replaceFirstChar { it.uppercase() }, color = prioridadColor, fontWeight = FontWeight.SemiBold)
                }
            }
            CampoConfirmacion(label = stringResource(R.string.confirmacion_label_transcripcion)) {
                Text(text = propuesta.audio_transcripcion, color = TextMuted, fontSize = 13.sp, modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Surface).padding(12.dp))
            }

            // ── Selector de destinos ───────────────────────────────────────────
            if (hayTrello || hayNotion) {
                Spacer(modifier = Modifier.height(4.dp))
                Text("¿Dónde quieres guardarlo?", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Text("Puedes seleccionar una o más opciones", color = TextMuted, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(4.dp))
                DestinoChip(label = "Voice2Task", seleccionado = enviarVoice2Task, onClick = { enviarVoice2Task = !enviarVoice2Task })
                if (hayTrello) DestinoChip(label = "Trello", seleccionado = enviarTrello, onClick = { enviarTrello = !enviarTrello })
                if (hayNotion) DestinoChip(label = "Notion", seleccionado = enviarNotion, onClick = { enviarNotion = !enviarNotion })
            }
        }
    }
}

@Composable
private fun DestinoChip(label: String, seleccionado: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = if (seleccionado) Primary.copy(alpha = 0.15f) else Surface,
        border = androidx.compose.foundation.BorderStroke(if (seleccionado) 1.5.dp else 1.dp, if (seleccionado) Primary else Border)
    ) {
        Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = if (seleccionado) Primary else TextPrimary, fontWeight = if (seleccionado) FontWeight.SemiBold else FontWeight.Normal, fontSize = 14.sp)
            if (seleccionado) Icon(Icons.Default.Check, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun CampoConfirmacion(label: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        content()
    }
}

@Composable
private fun PantallaError(mensaje: String, onReintentar: () -> Unit, onCancelar: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(Background), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(32.dp)) {
            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Error, modifier = Modifier.size(56.dp))
            Text(stringResource(R.string.error_title), color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            Text(mensaje, color = TextSecondary, fontSize = 14.sp, textAlign = TextAlign.Center)
            Button(onClick = onReintentar, shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Primary)) {
                Text(stringResource(R.string.error_retry), color = OnPrimary)
            }
            TextButton(onClick = onCancelar) { Text(stringResource(R.string.error_cancel), color = TextSecondary) }
        }
    }
}