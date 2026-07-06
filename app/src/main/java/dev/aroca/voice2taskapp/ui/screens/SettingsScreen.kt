package dev.aroca.voice2taskapp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.aroca.voice2taskapp.data.api.ApiClient
import dev.aroca.voice2taskapp.data.repository.TokenRepository
import dev.aroca.voice2taskapp.ui.theme.*
import dev.aroca.voice2taskapp.viewmodel.AuthViewModel
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.HttpException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit = {},
    authViewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val tokenRepository = remember { TokenRepository(context) }
    val scope = rememberCoroutineScope()

    val usuario by authViewModel.usuario.collectAsState()
    val groqKey by tokenRepository.groqKey.collectAsState(initial = "")
    var groqInput by remember { mutableStateOf("") }
    var groqVisible by remember { mutableStateOf(false) }
    var groqExpanded by remember { mutableStateOf(false) }
    var groqSaved by remember { mutableStateOf(false) }

    var comoFuncionaExpanded by remember { mutableStateOf(false) }
    var cambiarPassExpanded by remember { mutableStateOf(false) }
    var passActual by remember { mutableStateOf("") }
    var passNueva by remember { mutableStateOf("") }
    var passConfirmar by remember { mutableStateOf("") }
    var passActualVisible by remember { mutableStateOf(false) }
    var passNuevaVisible by remember { mutableStateOf(false) }
    var passConfirmarVisible by remember { mutableStateOf(false) }
    var passError by remember { mutableStateOf<String?>(null) }
    var passExito by remember { mutableStateOf(false) }
    var passCargando by remember { mutableStateOf(false) }

    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(groqKey) {
        if (!groqKey.isNullOrEmpty()) groqInput = groqKey!!
    }

    Scaffold(
        containerColor = Background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Ajustes", color = TextPrimary, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            // ── Cuenta ─────────────────────────────────────────────────────────
            Text("Cuenta", color = TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp))

            Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Surface)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Primary, modifier = Modifier.size(36.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(usuario?.username ?: "—", color = TextPrimary, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
                        Text(usuario?.email ?: "—", color = TextMuted, fontSize = 13.sp)
                    }
                }
            }

            Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Surface)) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                cambiarPassExpanded = !cambiarPassExpanded
                                passError = null
                                passExito = false
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
                            Text("Cambiar contraseña", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                        }
                        Icon(if (cambiarPassExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null, tint = TextSecondary)
                    }
                    AnimatedVisibility(visible = cambiarPassExpanded) {
                        Column(
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Contraseña actual
                            OutlinedTextField(
                                value = passActual,
                                onValueChange = { passActual = it; passError = null; passExito = false },
                                label = { Text("Contraseña actual") },
                                modifier = Modifier.fillMaxWidth(),
                                visualTransformation = if (passActualVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(onClick = { passActualVisible = !passActualVisible }) {
                                        Icon(if (passActualVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = null, tint = TextSecondary)
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = Border, focusedLabelColor = Primary, cursorColor = Primary)
                            )

                            // Nueva contraseña
                            OutlinedTextField(
                                value = passNueva,
                                onValueChange = { passNueva = it; passError = null; passExito = false },
                                label = { Text("Nueva contraseña") },
                                modifier = Modifier.fillMaxWidth(),
                                visualTransformation = if (passNuevaVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(onClick = { passNuevaVisible = !passNuevaVisible }) {
                                        Icon(if (passNuevaVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = null, tint = TextSecondary)
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = Border, focusedLabelColor = Primary, cursorColor = Primary)
                            )

                            // Confirmar nueva contraseña
                            OutlinedTextField(
                                value = passConfirmar,
                                onValueChange = { passConfirmar = it; passError = null; passExito = false },
                                label = { Text("Repite la nueva contraseña") },
                                modifier = Modifier.fillMaxWidth(),
                                isError = passConfirmar.isNotBlank() && passConfirmar != passNueva,
                                visualTransformation = if (passConfirmarVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(onClick = { passConfirmarVisible = !passConfirmarVisible }) {
                                        Icon(if (passConfirmarVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = null, tint = TextSecondary)
                                    }
                                },
                                supportingText = {
                                    if (passConfirmar.isNotBlank() && passConfirmar != passNueva) {
                                        Text("Las contraseñas no coinciden", color = Error, fontSize = 12.sp)
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = Border, focusedLabelColor = Primary, cursorColor = Primary)
                            )

                            if (passError != null) Text(passError!!, color = Error, fontSize = 13.sp)
                            if (passExito) Text("¡Contraseña cambiada correctamente!", color = Primary, fontSize = 13.sp)

                            Button(
                                onClick = {
                                    scope.launch {
                                        // Validaciones en cliente
                                        when {
                                            passActual.isBlank() || passNueva.isBlank() || passConfirmar.isBlank() -> {
                                                passError = "Rellena todos los campos"
                                                return@launch
                                            }
                                            passNueva != passConfirmar -> {
                                                passError = "Las contraseñas nuevas no coinciden"
                                                return@launch
                                            }
                                            passNueva == passActual -> {
                                                passError = "La nueva contraseña debe ser diferente a la actual"
                                                return@launch
                                            }
                                            passNueva.length < 8 -> {
                                                passError = "La contraseña debe tener al menos 8 caracteres"
                                                return@launch
                                            }
                                        }
                                        passCargando = true
                                        try {
                                            ApiClient.authApi.cambiarPassword(
                                                mapOf("password_actual" to passActual, "password_nueva" to passNueva)
                                            )
                                            passExito = true
                                            passActual = ""
                                            passNueva = ""
                                            passConfirmar = ""
                                            cambiarPassExpanded = false
                                        } catch (e: HttpException) {
                                            passError = try {
                                                JSONObject(e.response()?.errorBody()?.string() ?: "").getString("detail")
                                            } catch (_: Exception) { "Error al cambiar la contraseña" }
                                        } catch (_: Exception) {
                                            passError = "No se pudo conectar con el servidor"
                                        } finally {
                                            passCargando = false
                                        }
                                    }
                                },
                                enabled = !passCargando,
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Primary)
                            ) {
                                if (passCargando) CircularProgressIndicator(modifier = Modifier.size(18.dp), color = OnPrimary, strokeWidth = 2.dp)
                                else Text("Guardar contraseña", color = OnPrimary)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── Integraciones ──────────────────────────────────────────────────
            Text("Integraciones", color = TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp))

            SettingsIntegrationCard(
                title = "Groq API Key",
                subtitle = if (!groqKey.isNullOrEmpty()) "Configurada" else "No configurada",
                expanded = groqExpanded,
                onToggle = { groqExpanded = !groqExpanded }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Necesaria para procesar audios. Consíguela gratis en console.groq.com", color = TextSecondary, fontSize = 13.sp)
                    OutlinedTextField(
                        value = groqInput, onValueChange = { groqInput = it; groqSaved = false },
                        label = { Text("API Key") }, modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (groqVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = { IconButton(onClick = { groqVisible = !groqVisible }) { Icon(if (groqVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = null, tint = TextSecondary) } },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = Border, focusedLabelColor = Primary, cursorColor = Primary)
                    )
                    Button(
                        onClick = { scope.launch { tokenRepository.saveGroqKey(groqInput); groqSaved = true; groqExpanded = false } },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) { Text(if (groqSaved) "¡Guardado!" else "Guardar", color = OnPrimary) }
                }
            }

            SettingsIntegrationCard(title = "Trello", subtitle = "Próximamente", expanded = false, onToggle = {}, enabled = false) {}
            SettingsIntegrationCard(title = "Google Calendar", subtitle = "Próximamente", expanded = false, onToggle = {}, enabled = false) {}
            SettingsIntegrationCard(title = "Notion", subtitle = "Próximamente", expanded = false, onToggle = {}, enabled = false) {}

            Spacer(modifier = Modifier.height(8.dp))

            // ── Guía ──────────────────────────────────────────────────────────
            Text("Guía", color = TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp))

            Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Surface)) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { comoFuncionaExpanded = !comoFuncionaExpanded }.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Cómo funciona", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                            Text("Aprende a usar Voice2Task", color = TextMuted, fontSize = 12.sp)
                        }
                        Icon(if (comoFuncionaExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null, tint = TextSecondary)
                    }
                    AnimatedVisibility(visible = comoFuncionaExpanded) {
                        Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            PasoGuia(Icons.Default.Lock, "Requisito previo", "Para usar Voice2Task necesitas una API Key de Groq (gratuita). Consíguela en console.groq.com y añádela en Integraciones.")
                            HorizontalDivider(color = SurfaceVariant)
                            PasoGuia(Icons.Default.Mic, "Habla", "Abre una lista, pulsa el micrófono y describe tu tarea en voz alta. Puedes incluir fechas, prioridad o cualquier detalle.")
                            HorizontalDivider(color = SurfaceVariant)
                            PasoGuia(Icons.Default.Star, "Revisa y confirma", "La IA convierte tu nota en una tarea estructurada con título, descripción y fecha. Puedes editarla antes de guardar.")
                            HorizontalDivider(color = SurfaceVariant)
                            PasoGuia(Icons.Default.Star, "Marca como importante", "Pulsa ⋮ en cualquier tarea para marcarla como importante. Aparecerá en la tab Importante para acceso rápido.")
                            HorizontalDivider(color = SurfaceVariant)
                            PasoGuia(Icons.Default.CalendarMonth, "Calendario", "Las tareas con fecha límite aparecen en el calendario. Pulsa cualquier día para ver las tareas de ese día.")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            TextButton(
                onClick = { showLogoutDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = Error, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cerrar sesión", color = Error, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            containerColor = Surface,
            title = { Text("Cerrar sesión", color = TextPrimary, fontWeight = FontWeight.SemiBold) },
            text = { Text("¿Seguro que quieres cerrar sesión?", color = TextSecondary) },
            confirmButton = {
                Button(onClick = { showLogoutDialog = false; onLogout() }, colors = ButtonDefaults.buttonColors(containerColor = Error)) {
                    Text("Cerrar sesión", color = OnPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Cancelar", color = TextSecondary) }
            }
        )
    }
}

@Composable
private fun PasoGuia(icono: ImageVector, titulo: String, descripcion: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
        Icon(icono, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp).padding(top = 2.dp))
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(titulo, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(descripcion, color = TextSecondary, fontSize = 13.sp)
        }
    }
}

@Composable
private fun SettingsIntegrationCard(
    title: String,
    subtitle: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    enabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Surface)) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().then(if (enabled) Modifier.clickable { onToggle() } else Modifier).padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(title, color = if (enabled) TextPrimary else TextMuted, style = MaterialTheme.typography.titleMedium)
                    Text(subtitle, color = if (subtitle == "Configurada") Primary else TextMuted, fontSize = 12.sp)
                }
                if (enabled) Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null, tint = TextSecondary)
            }
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp), content = content)
            }
        }
    }
}