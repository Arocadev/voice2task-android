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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.aroca.voice2taskapp.data.model.AudioProcesamientoResponse
import dev.aroca.voice2taskapp.data.repository.TokenRepository
import dev.aroca.voice2taskapp.ui.theme.*
import dev.aroca.voice2taskapp.viewmodel.GrabarAudioViewModel
import dev.aroca.voice2taskapp.viewmodel.GrabarState

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

    // Mientras groqKey es null aún está cargando — no mostrar nada todavía

    // Sin Groq Key configurada — mostrar aviso
    if (groqKey.isNullOrBlank()) {
        PantallaSinGroqKey(
            onCancelar = onDismiss,
            onIrAjustes = onIrAjustes
        )
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
        is GrabarState.Procesando -> PantallaProcessando()
        is GrabarState.Propuesta -> {
            PantallaConfirmacion(
                propuesta = s.respuesta,
                listaId = listaId,
                listaNombre = listaNombre,
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
private fun PantallaSinGroqKey(
    onCancelar: () -> Unit,
    onIrAjustes: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize().background(Background),
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = onCancelar,
            modifier = Modifier.align(Alignment.TopStart).padding(16.dp)
        ) {
            Icon(Icons.Default.Close, contentDescription = "Cancelar", tint = TextSecondary)
        }

        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(Icons.Default.Key, contentDescription = null, tint = Primary, modifier = Modifier.size(56.dp))

            Text(
                "API Key de Groq necesaria",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )

            Text(
                "Para procesar audios necesitas una API Key de Groq. Es gratuita y puedes obtenerla en console.groq.com",
                color = TextSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            Button(
                onClick = onIrAjustes,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text("Ir a Ajustes", color = OnPrimary, fontWeight = FontWeight.SemiBold)
            }

            TextButton(onClick = onCancelar) {
                Text("Cancelar", color = TextSecondary)
            }
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
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier.fillMaxSize().background(Background),
        contentAlignment = Alignment.Center
    ) {
        IconButton(onClick = onCancelar, modifier = Modifier.align(Alignment.TopStart).padding(16.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Cancelar", tint = TextSecondary)
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
                    Icon(Icons.Default.Mic, contentDescription = "Micrófono", tint = OnPrimary, modifier = Modifier.size(60.dp))
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (grabando) "Escuchando..." else "Toca para grabar",
                    color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.SemiBold
                )
                if (grabando) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(String.format("%02d:%02d", segundos / 60, segundos % 60), color = Primary, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                }
            }

            if (grabando) WaveformBars()

            if (grabando) {
                Button(
                    onClick = onFinalizar,
                    modifier = Modifier.fillMaxWidth(0.7f).height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) { Text("Finalizar", color = OnPrimary, fontWeight = FontWeight.SemiBold) }

                TextButton(onClick = onCancelar) { Text("Cancelar", color = TextSecondary) }
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
private fun PantallaProcessando() {
    Box(modifier = Modifier.fillMaxSize().background(Background), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(32.dp)) {
            Box(
                modifier = Modifier.size(120.dp).clip(CircleShape).background(Surface).border(1.dp, Primary.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = Primary, modifier = Modifier.size(48.dp), strokeWidth = 3.dp) }

            Text("Analizando tu voz...", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)

            Column(verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.Start, modifier = Modifier.padding(horizontal = 32.dp)) {
                PasoProcessado("Transcribiendo", done = true)
                PasoProcessado("Entendiendo", done = true)
                PasoProcessado("Creando tarea", done = false)
            }
        }
    }
}

@Composable
private fun PasoProcessado(texto: String, done: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        if (done) Icon(Icons.Default.Check, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp))
        else CircularProgressIndicator(color = Primary, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
        Text(texto, color = if (done) TextPrimary else TextSecondary, fontSize = 14.sp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PantallaConfirmacion(
    propuesta: AudioProcesamientoResponse,
    listaId: Int,
    listaNombre: String,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit
) {
    var titulo by remember { mutableStateOf(propuesta.titulo) }
    var descripcion by remember { mutableStateOf(propuesta.descripcion) }
    var prioridad by remember { mutableStateOf(propuesta.prioridad) }

    val prioridadColor = when (prioridad) {
        "ALTA" -> PriorityAlta
        "MEDIA" -> PriorityMedia
        else -> PriorityBaja
    }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Revisar tarea", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        Text("La IA ha creado esta tarea", color = TextSecondary, fontSize = 12.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onCancelar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier.fillMaxWidth().background(Background).padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = onConfirmar, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Primary)) {
                    Text("Guardar tarea", color = OnPrimary, fontWeight = FontWeight.SemiBold)
                }
                TextButton(onClick = onCancelar, modifier = Modifier.fillMaxWidth()) {
                    Text("Editar transcripción", color = TextSecondary)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CampoConfirmacion(label = "Título") {
                OutlinedTextField(value = titulo, onValueChange = { titulo = it }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = Border, focusedLabelColor = Primary, cursorColor = Primary))
            }
            CampoConfirmacion(label = "Descripción") {
                OutlinedTextField(value = descripcion, onValueChange = { descripcion = it }, modifier = Modifier.fillMaxWidth(), minLines = 2, shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = Border, focusedLabelColor = Primary, cursorColor = Primary))
            }
            CampoConfirmacion(label = "Lista") {
                OutlinedTextField(value = listaNombre, onValueChange = {}, enabled = false, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(disabledBorderColor = Border, disabledTextColor = TextSecondary))
            }
            propuesta.fecha_limite?.let { fecha ->
                CampoConfirmacion(label = "Fecha") {
                    OutlinedTextField(value = fecha.take(10), onValueChange = {}, enabled = false, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                        trailingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = TextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(disabledBorderColor = Border, disabledTextColor = TextSecondary))
                }
            }
            CampoConfirmacion(label = "Prioridad") {
                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).border(1.dp, Border, RoundedCornerShape(12.dp)).padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(prioridadColor))
                    Text(prioridad.lowercase().replaceFirstChar { it.uppercase() }, color = prioridadColor, fontWeight = FontWeight.SemiBold)
                }
            }
            CampoConfirmacion(label = "Transcripción original") {
                Text(text = propuesta.audio_transcripcion, color = TextMuted, fontSize = 13.sp,
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Surface).padding(12.dp))
            }
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
            Text("Algo salió mal", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            Text(mensaje, color = TextSecondary, fontSize = 14.sp, textAlign = TextAlign.Center)
            Button(onClick = onReintentar, shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Primary)) {
                Text("Reintentar", color = OnPrimary)
            }
            TextButton(onClick = onCancelar) { Text("Cancelar", color = TextSecondary) }
        }
    }
}