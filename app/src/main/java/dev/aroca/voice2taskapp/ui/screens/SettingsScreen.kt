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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.aroca.voice2taskapp.R
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
    var groqSaved by remember { mutableStateOf(false) }

    // Acordeón integraciones
    var integracionesExpanded by remember { mutableStateOf(false) }
    var groqExpanded by remember { mutableStateOf(false) }

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
                title = { Text(stringResource(R.string.settings_title), color = TextPrimary, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.settings_back), tint = TextPrimary)
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
            Text(stringResource(R.string.settings_section_cuenta), color = TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp))

            // Info usuario
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

            // Groq Key en Cuenta
            Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Surface)) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { groqExpanded = !groqExpanded; groqSaved = false }.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Key, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
                            Column {
                                Text(stringResource(R.string.settings_groq_key), color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    if (!groqKey.isNullOrEmpty()) stringResource(R.string.settings_configured) else stringResource(R.string.settings_not_configured),
                                    color = if (!groqKey.isNullOrEmpty()) Primary else TextMuted,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        Icon(if (groqExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null, tint = TextSecondary)
                    }
                    AnimatedVisibility(visible = groqExpanded) {
                        Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(stringResource(R.string.settings_groq_description), color = TextSecondary, fontSize = 13.sp)
                            OutlinedTextField(
                                value = groqInput, onValueChange = { groqInput = it; groqSaved = false },
                                label = { Text(stringResource(R.string.settings_api_key_label)) },
                                modifier = Modifier.fillMaxWidth(),
                                visualTransformation = if (groqVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(onClick = { groqVisible = !groqVisible }) {
                                        Icon(if (groqVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = null, tint = TextSecondary)
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = Border, focusedLabelColor = Primary, cursorColor = Primary)
                            )
                            Button(
                                onClick = { scope.launch { tokenRepository.saveGroqKey(groqInput); groqSaved = true; groqExpanded = false } },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Primary)
                            ) { Text(if (groqSaved) stringResource(R.string.settings_saved) else stringResource(R.string.settings_save), color = OnPrimary) }
                        }
                    }
                }
            }

            // Cambiar contraseña
            Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Surface)) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { cambiarPassExpanded = !cambiarPassExpanded; passError = null; passExito = false }.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
                            Text(stringResource(R.string.settings_change_password), color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                        }
                        Icon(if (cambiarPassExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null, tint = TextSecondary)
                    }
                    AnimatedVisibility(visible = cambiarPassExpanded) {
                        Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = passActual, onValueChange = { passActual = it; passError = null; passExito = false },
                                label = { Text(stringResource(R.string.settings_current_password)) },
                                modifier = Modifier.fillMaxWidth(),
                                visualTransformation = if (passActualVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = { IconButton(onClick = { passActualVisible = !passActualVisible }) { Icon(if (passActualVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = null, tint = TextSecondary) } },
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = Border, focusedLabelColor = Primary, cursorColor = Primary)
                            )
                            OutlinedTextField(
                                value = passNueva, onValueChange = { passNueva = it; passError = null; passExito = false },
                                label = { Text(stringResource(R.string.settings_new_password)) },
                                modifier = Modifier.fillMaxWidth(),
                                visualTransformation = if (passNuevaVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = { IconButton(onClick = { passNuevaVisible = !passNuevaVisible }) { Icon(if (passNuevaVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = null, tint = TextSecondary) } },
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = Border, focusedLabelColor = Primary, cursorColor = Primary)
                            )
                            OutlinedTextField(
                                value = passConfirmar, onValueChange = { passConfirmar = it; passError = null; passExito = false },
                                label = { Text(stringResource(R.string.settings_confirm_password)) },
                                modifier = Modifier.fillMaxWidth(),
                                isError = passConfirmar.isNotBlank() && passConfirmar != passNueva,
                                visualTransformation = if (passConfirmarVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = { IconButton(onClick = { passConfirmarVisible = !passConfirmarVisible }) { Icon(if (passConfirmarVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = null, tint = TextSecondary) } },
                                supportingText = {
                                    if (passConfirmar.isNotBlank() && passConfirmar != passNueva) {
                                        Text(stringResource(R.string.settings_passwords_no_match), color = Error, fontSize = 12.sp)
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = Border, focusedLabelColor = Primary, cursorColor = Primary)
                            )
                            if (passError != null) Text(passError!!, color = Error, fontSize = 13.sp)
                            if (passExito) Text(stringResource(R.string.settings_password_changed), color = Primary, fontSize = 13.sp)
                            Button(
                                onClick = {
                                    scope.launch {
                                        when {
                                            passActual.isBlank() || passNueva.isBlank() || passConfirmar.isBlank() -> { passError = context.getString(R.string.settings_fill_all_fields); return@launch }
                                            passNueva != passConfirmar -> { passError = context.getString(R.string.settings_passwords_differ); return@launch }
                                            passNueva == passActual -> { passError = context.getString(R.string.settings_password_same); return@launch }
                                            passNueva.length < 8 -> { passError = context.getString(R.string.settings_password_too_short); return@launch }
                                        }
                                        passCargando = true
                                        try {
                                            ApiClient.authApi.cambiarPassword(mapOf("password_actual" to passActual, "password_nueva" to passNueva))
                                            passExito = true; passActual = ""; passNueva = ""; passConfirmar = ""; cambiarPassExpanded = false
                                        } catch (e: HttpException) {
                                            passError = try { JSONObject(e.response()?.errorBody()?.string() ?: "").getString("detail") } catch (_: Exception) { "Error al cambiar la contraseña" }
                                        } catch (_: Exception) {
                                            passError = "No se pudo conectar con el servidor"
                                        } finally { passCargando = false }
                                    }
                                },
                                enabled = !passCargando,
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Primary)
                            ) {
                                if (passCargando) CircularProgressIndicator(modifier = Modifier.size(18.dp), color = OnPrimary, strokeWidth = 2.dp)
                                else Text(stringResource(R.string.settings_save_password), color = OnPrimary)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── Integraciones — acordeón único ─────────────────────────────────
            Text(stringResource(R.string.settings_section_integraciones), color = TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp))

            Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Surface)) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { integracionesExpanded = !integracionesExpanded }.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(stringResource(R.string.settings_integrations_title), color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                            Text(stringResource(R.string.settings_integrations_subtitle), color = TextMuted, fontSize = 12.sp)
                        }
                        Icon(if (integracionesExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null, tint = TextSecondary)
                    }
                    AnimatedVisibility(visible = integracionesExpanded) {
                        Column(modifier = Modifier.padding(bottom = 8.dp)) {
                            IntegracionItem(nombre = stringResource(R.string.settings_trello), subtitulo = stringResource(R.string.settings_coming_soon), enabled = false)
                            HorizontalDivider(color = SurfaceVariant, modifier = Modifier.padding(horizontal = 16.dp))
                            IntegracionItem(nombre = stringResource(R.string.settings_google_calendar), subtitulo = stringResource(R.string.settings_coming_soon), enabled = false)
                            HorizontalDivider(color = SurfaceVariant, modifier = Modifier.padding(horizontal = 16.dp))
                            IntegracionItem(nombre = stringResource(R.string.settings_notion), subtitulo = stringResource(R.string.settings_coming_soon), enabled = false)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── Guía ──────────────────────────────────────────────────────────
            Text(stringResource(R.string.settings_section_guia), color = TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp))

            Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Surface)) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { comoFuncionaExpanded = !comoFuncionaExpanded }.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(stringResource(R.string.settings_how_it_works), color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                            Text(stringResource(R.string.settings_how_it_works_subtitle), color = TextMuted, fontSize = 12.sp)
                        }
                        Icon(if (comoFuncionaExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null, tint = TextSecondary)
                    }
                    AnimatedVisibility(visible = comoFuncionaExpanded) {
                        Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            PasoGuia(Icons.Default.Lock, stringResource(R.string.settings_guide_prereq_title), stringResource(R.string.settings_guide_prereq_desc))
                            HorizontalDivider(color = SurfaceVariant)
                            PasoGuia(Icons.Default.Mic, stringResource(R.string.settings_guide_speak_title), stringResource(R.string.settings_guide_speak_desc))
                            HorizontalDivider(color = SurfaceVariant)
                            PasoGuia(Icons.Default.Star, stringResource(R.string.settings_guide_review_title), stringResource(R.string.settings_guide_review_desc))
                            HorizontalDivider(color = SurfaceVariant)
                            PasoGuia(Icons.Default.Star, stringResource(R.string.settings_guide_important_title), stringResource(R.string.settings_guide_important_desc))
                            HorizontalDivider(color = SurfaceVariant)
                            PasoGuia(Icons.Default.CalendarMonth, stringResource(R.string.settings_guide_calendar_title), stringResource(R.string.settings_guide_calendar_desc))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            TextButton(onClick = { showLogoutDialog = true }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = Error, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.settings_logout), color = Error, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            containerColor = Surface,
            title = { Text(stringResource(R.string.settings_logout_title), color = TextPrimary, fontWeight = FontWeight.SemiBold) },
            text = { Text(stringResource(R.string.settings_logout_confirm), color = TextSecondary) },
            confirmButton = {
                Button(onClick = { showLogoutDialog = false; onLogout() }, colors = ButtonDefaults.buttonColors(containerColor = Error)) {
                    Text(stringResource(R.string.settings_logout), color = OnPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text(stringResource(R.string.settings_logout_cancel), color = TextSecondary) }
            }
        )
    }
}

@Composable
private fun IntegracionItem(nombre: String, subtitulo: String, enabled: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(nombre, color = if (enabled) TextPrimary else TextMuted, style = MaterialTheme.typography.titleMedium)
            Text(subtitulo, color = TextMuted, fontSize = 12.sp)
        }
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