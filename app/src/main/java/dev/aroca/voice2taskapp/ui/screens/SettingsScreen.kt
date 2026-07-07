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
import dev.aroca.voice2taskapp.data.api.ExternalApiClient
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

    // Groq
    val groqKey by tokenRepository.groqKey.collectAsState(initial = "")
    var groqInput by remember { mutableStateOf("") }
    var groqVisible by remember { mutableStateOf(false) }
    var groqExpanded by remember { mutableStateOf(false) }
    var groqSaved by remember { mutableStateOf(false) }

    // Trello
    val trelloApiKey by tokenRepository.trelloApiKey.collectAsState(initial = "")
    val trelloToken by tokenRepository.trelloToken.collectAsState(initial = "")
    val trelloListId by tokenRepository.trelloListId.collectAsState(initial = "")
    val trelloListName by tokenRepository.trelloListName.collectAsState(initial = "")
    var trelloApiKeyInput by remember { mutableStateOf("") }
    var trelloTokenInput by remember { mutableStateOf("") }
    var trelloApiKeyVisible by remember { mutableStateOf(false) }
    var trelloTokenVisible by remember { mutableStateOf(false) }
    var trelloExpanded by remember { mutableStateOf(false) }
    var trelloBoards by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var trelloLists by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var trelloSelectedBoardId by remember { mutableStateOf("") }
    var trelloCargandoBoards by remember { mutableStateOf(false) }
    var trelloCargandoLists by remember { mutableStateOf(false) }
    var trelloError by remember { mutableStateOf<String?>(null) }
    var trelloSaved by remember { mutableStateOf(false) }
    var trelloInfoDialog by remember { mutableStateOf(false) }
    var groqInfoDialog by remember { mutableStateOf(false) }

    // Notion
    val notionToken by tokenRepository.notionToken.collectAsState(initial = "")
    val notionDatabaseId by tokenRepository.notionDatabaseId.collectAsState(initial = "")
    val notionDatabaseName by tokenRepository.notionDatabaseName.collectAsState(initial = "")
    var notionTokenInput by remember { mutableStateOf("") }
    var notionTokenVisible by remember { mutableStateOf(false) }
    var notionExpanded by remember { mutableStateOf(false) }
    var notionDatabases by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var notionCargando by remember { mutableStateOf(false) }
    var notionError by remember { mutableStateOf<String?>(null) }
    var notionSaved by remember { mutableStateOf(false) }
    var notionInfoDialog by remember { mutableStateOf(false) }

    var integracionesExpanded by remember { mutableStateOf(false) }

    // Cambiar contraseña
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

    val msgFill = stringResource(R.string.settings_fill_all_fields)
    val msgDiffer = stringResource(R.string.settings_passwords_differ)
    val msgSame = stringResource(R.string.settings_password_same)
    val msgShort = stringResource(R.string.settings_password_too_short)

    var comoFuncionaExpanded by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showFaqDialog by remember { mutableStateOf(false) }
    var showAvisoLegalDialog by remember { mutableStateOf(false) }

    LaunchedEffect(groqKey) { if (!groqKey.isNullOrEmpty()) groqInput = groqKey!! }
    LaunchedEffect(trelloApiKey) { if (!trelloApiKey.isNullOrEmpty()) trelloApiKeyInput = trelloApiKey!! }
    LaunchedEffect(trelloToken) { if (!trelloToken.isNullOrEmpty()) trelloTokenInput = trelloToken!! }
    LaunchedEffect(notionToken) { if (!notionToken.isNullOrEmpty()) notionTokenInput = notionToken!! }

    Scaffold(
        containerColor = Background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.settings_title), color = Primary, fontWeight = FontWeight.SemiBold) },
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
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            // ── Cuenta ────────────────────────────────────────────────────────
            Text(stringResource(R.string.settings_section_cuenta), color = TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp))

            Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Surface)) {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Primary, modifier = Modifier.size(36.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(usuario?.username ?: "—", color = TextPrimary, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
                        Text(usuario?.email ?: "—", color = TextMuted, fontSize = 13.sp)
                    }
                }
            }

            // Cambiar contraseña
            Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Surface)) {
                Column {
                    Row(modifier = Modifier.fillMaxWidth().clickable { cambiarPassExpanded = !cambiarPassExpanded; passError = null; passExito = false }.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
                            Text(stringResource(R.string.settings_change_password), color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                        }
                        Icon(if (cambiarPassExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null, tint = TextSecondary)
                    }
                    AnimatedVisibility(visible = cambiarPassExpanded) {
                        Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(value = passActual, onValueChange = { passActual = it; passError = null; passExito = false }, label = { Text(stringResource(R.string.settings_current_password)) }, modifier = Modifier.fillMaxWidth(), visualTransformation = if (passActualVisible) VisualTransformation.None else PasswordVisualTransformation(), trailingIcon = { IconButton(onClick = { passActualVisible = !passActualVisible }) { Icon(if (passActualVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = null, tint = TextSecondary) } }, shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = Border, focusedLabelColor = Primary, cursorColor = Primary))
                            OutlinedTextField(value = passNueva, onValueChange = { passNueva = it; passError = null; passExito = false }, label = { Text(stringResource(R.string.settings_new_password)) }, modifier = Modifier.fillMaxWidth(), visualTransformation = if (passNuevaVisible) VisualTransformation.None else PasswordVisualTransformation(), trailingIcon = { IconButton(onClick = { passNuevaVisible = !passNuevaVisible }) { Icon(if (passNuevaVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = null, tint = TextSecondary) } }, shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = Border, focusedLabelColor = Primary, cursorColor = Primary))
                            OutlinedTextField(value = passConfirmar, onValueChange = { passConfirmar = it; passError = null; passExito = false }, label = { Text(stringResource(R.string.settings_confirm_password)) }, modifier = Modifier.fillMaxWidth(), isError = passConfirmar.isNotBlank() && passConfirmar != passNueva, visualTransformation = if (passConfirmarVisible) VisualTransformation.None else PasswordVisualTransformation(), trailingIcon = { IconButton(onClick = { passConfirmarVisible = !passConfirmarVisible }) { Icon(if (passConfirmarVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = null, tint = TextSecondary) } }, supportingText = { if (passConfirmar.isNotBlank() && passConfirmar != passNueva) Text(stringResource(R.string.settings_passwords_no_match), color = Error, fontSize = 12.sp) }, shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = Border, focusedLabelColor = Primary, cursorColor = Primary))
                            if (passError != null) Text(passError!!, color = Error, fontSize = 13.sp)
                            if (passExito) Text(stringResource(R.string.settings_password_changed), color = Primary, fontSize = 13.sp)
                            Button(
                                onClick = {
                                    scope.launch {
                                        when {
                                            passActual.isBlank() || passNueva.isBlank() || passConfirmar.isBlank() -> { passError = msgFill; return@launch }
                                            passNueva != passConfirmar -> { passError = msgDiffer; return@launch }
                                            passNueva == passActual -> { passError = msgSame; return@launch }
                                            passNueva.length < 8 -> { passError = msgShort; return@launch }
                                        }
                                        passCargando = true
                                        try {
                                            ApiClient.authApi.cambiarPassword(mapOf("password_actual" to passActual, "password_nueva" to passNueva))
                                            passExito = true; passActual = ""; passNueva = ""; passConfirmar = ""; cambiarPassExpanded = false
                                        } catch (e: HttpException) {
                                            passError = try { JSONObject(e.response()?.errorBody()?.string() ?: "").getString("detail") } catch (_: Exception) { "Error al cambiar la contraseña" }
                                        } catch (_: Exception) { passError = "No se pudo conectar con el servidor" }
                                        finally { passCargando = false }
                                    }
                                },
                                enabled = !passCargando, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Primary)
                            ) {
                                if (passCargando) CircularProgressIndicator(modifier = Modifier.size(18.dp), color = OnPrimary, strokeWidth = 2.dp)
                                else Text(stringResource(R.string.settings_save_password), color = OnPrimary)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── Integraciones ─────────────────────────────────────────────────
            Text(stringResource(R.string.settings_section_integraciones), color = TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp))

            Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Surface)) {
                Column {
                    Row(modifier = Modifier.fillMaxWidth().clickable { integracionesExpanded = !integracionesExpanded }.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(stringResource(R.string.settings_integrations_title), color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                            Text(stringResource(R.string.settings_integrations_subtitle), color = TextMuted, fontSize = 12.sp)
                        }
                        Icon(if (integracionesExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null, tint = TextSecondary)
                    }
                    AnimatedVisibility(visible = integracionesExpanded) {
                        Column(modifier = Modifier.padding(bottom = 8.dp)) {

                            // ── Groq ─────────────────────────────────────────
                            HorizontalDivider(color = SurfaceVariant, modifier = Modifier.padding(horizontal = 16.dp))
                            Row(modifier = Modifier.fillMaxWidth().clickable { groqExpanded = !groqExpanded; groqSaved = false }.padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text(stringResource(R.string.settings_groq_key), color = TextPrimary, style = MaterialTheme.typography.titleSmall)
                                    Text(if (!groqKey.isNullOrEmpty()) stringResource(R.string.settings_configured) else stringResource(R.string.settings_not_configured), color = if (!groqKey.isNullOrEmpty()) Primary else TextMuted, fontSize = 12.sp)
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { groqInfoDialog = true }, modifier = Modifier.size(28.dp)) {
                                        Icon(Icons.Default.Info, contentDescription = "Info", tint = TextMuted, modifier = Modifier.size(18.dp))
                                    }
                                    Icon(if (groqExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null, tint = TextSecondary)
                                }
                            }
                            AnimatedVisibility(visible = groqExpanded) {
                                Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text(stringResource(R.string.settings_groq_description), color = TextSecondary, fontSize = 13.sp)
                                    OutlinedTextField(value = groqInput, onValueChange = { groqInput = it; groqSaved = false }, label = { Text(stringResource(R.string.settings_api_key_label)) }, modifier = Modifier.fillMaxWidth(), visualTransformation = if (groqVisible) VisualTransformation.None else PasswordVisualTransformation(), trailingIcon = { IconButton(onClick = { groqVisible = !groqVisible }) { Icon(if (groqVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = null, tint = TextSecondary) } }, shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = Border, focusedLabelColor = Primary, cursorColor = Primary))
                                    Button(onClick = { scope.launch { tokenRepository.saveGroqKey(groqInput); groqSaved = true; groqExpanded = false } }, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Primary)) {
                                        Text(if (groqSaved) stringResource(R.string.settings_saved) else stringResource(R.string.settings_save), color = OnPrimary)
                                    }
                                }
                            }

                            // ── Trello ────────────────────────────────────────
                            HorizontalDivider(color = SurfaceVariant, modifier = Modifier.padding(horizontal = 16.dp))
                            Row(modifier = Modifier.fillMaxWidth().clickable { trelloExpanded = !trelloExpanded }.padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text(stringResource(R.string.settings_trello), color = TextPrimary, style = MaterialTheme.typography.titleSmall)
                                    Text(if (!trelloListId.isNullOrEmpty()) "Lista: ${trelloListName ?: ""}" else stringResource(R.string.settings_not_configured), color = if (!trelloListId.isNullOrEmpty()) Primary else TextMuted, fontSize = 12.sp)
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { trelloInfoDialog = true }, modifier = Modifier.size(28.dp)) {
                                        Icon(Icons.Default.Info, contentDescription = "Info", tint = TextMuted, modifier = Modifier.size(18.dp))
                                    }
                                    Icon(if (trelloExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null, tint = TextSecondary)
                                }
                            }
                            AnimatedVisibility(visible = trelloExpanded) {
                                Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text("Paso 1 — Credenciales", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    OutlinedTextField(value = trelloApiKeyInput, onValueChange = { trelloApiKeyInput = it; trelloBoards = emptyList(); trelloLists = emptyList(); trelloSaved = false }, label = { Text("API Key") }, modifier = Modifier.fillMaxWidth(), visualTransformation = if (trelloApiKeyVisible) VisualTransformation.None else PasswordVisualTransformation(), trailingIcon = { IconButton(onClick = { trelloApiKeyVisible = !trelloApiKeyVisible }) { Icon(if (trelloApiKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = null, tint = TextSecondary) } }, shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = Border, focusedLabelColor = Primary, cursorColor = Primary))
                                    OutlinedTextField(value = trelloTokenInput, onValueChange = { trelloTokenInput = it; trelloBoards = emptyList(); trelloLists = emptyList(); trelloSaved = false }, label = { Text("Token") }, modifier = Modifier.fillMaxWidth(), visualTransformation = if (trelloTokenVisible) VisualTransformation.None else PasswordVisualTransformation(), trailingIcon = { IconButton(onClick = { trelloTokenVisible = !trelloTokenVisible }) { Icon(if (trelloTokenVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = null, tint = TextSecondary) } }, shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = Border, focusedLabelColor = Primary, cursorColor = Primary))
                                    if (trelloError != null) Text(trelloError!!, color = Error, fontSize = 12.sp)
                                    Text("Paso 2 — Selecciona un tablero", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    Button(onClick = { scope.launch { trelloCargandoBoards = true; trelloError = null; trelloLists = emptyList(); try { val boards = ExternalApiClient.trelloApi.getBoards(trelloApiKeyInput, trelloTokenInput); trelloBoards = boards.map { it.id to it.name }; tokenRepository.saveTrello(trelloApiKeyInput, trelloTokenInput) } catch (_: Exception) { trelloError = "Error al conectar con Trello. Verifica tus credenciales." } finally { trelloCargandoBoards = false } } }, enabled = trelloApiKeyInput.isNotBlank() && trelloTokenInput.isNotBlank() && !trelloCargandoBoards, modifier = Modifier.fillMaxWidth().height(44.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = SurfaceVariant)) {
                                        if (trelloCargandoBoards) CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Primary, strokeWidth = 2.dp)
                                        else Text("Cargar tableros", color = TextPrimary, fontSize = 13.sp)
                                    }
                                    if (trelloBoards.isNotEmpty()) {
                                        trelloBoards.forEach { (id, name) ->
                                            val sel = trelloSelectedBoardId == id
                                            Surface(modifier = Modifier.fillMaxWidth().clickable { trelloSelectedBoardId = id; trelloLists = emptyList(); trelloSaved = false; scope.launch { trelloCargandoLists = true; try { val lists = ExternalApiClient.trelloApi.getLists(id, trelloApiKeyInput, trelloTokenInput); trelloLists = lists.map { it.id to it.name } } catch (_: Exception) { trelloError = "Error al cargar las listas del tablero." } finally { trelloCargandoLists = false } } }, shape = RoundedCornerShape(10.dp), color = if (sel) Primary.copy(alpha = 0.15f) else SurfaceVariant, border = androidx.compose.foundation.BorderStroke(if (sel) 1.5.dp else 1.dp, if (sel) Primary else Border)) {
                                                Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                                    Text(name, color = if (sel) Primary else TextPrimary, fontSize = 14.sp, fontWeight = if (sel) FontWeight.SemiBold else FontWeight.Normal)
                                                    if (sel && trelloCargandoLists) CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Primary, strokeWidth = 2.dp)
                                                    else if (sel) Icon(Icons.Default.ExpandMore, contentDescription = null, tint = Primary, modifier = Modifier.size(16.dp))
                                                }
                                            }
                                        }
                                    }
                                    if (trelloLists.isNotEmpty()) {
                                        Text("Paso 3 — Selecciona la lista donde irán las tarjetas", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                        trelloLists.forEach { (id, name) ->
                                            val sel = trelloListId == id
                                            Surface(modifier = Modifier.fillMaxWidth().clickable { scope.launch { tokenRepository.saveTrelloList(id, name); trelloSaved = true } }, shape = RoundedCornerShape(10.dp), color = if (sel) Primary.copy(alpha = 0.15f) else SurfaceVariant, border = androidx.compose.foundation.BorderStroke(if (sel) 1.5.dp else 1.dp, if (sel) Primary else Border)) {
                                                Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                                    Text(name, color = if (sel) Primary else TextPrimary, fontSize = 14.sp, fontWeight = if (sel) FontWeight.SemiBold else FontWeight.Normal)
                                                    if (sel) Icon(Icons.Default.Check, contentDescription = null, tint = Primary, modifier = Modifier.size(16.dp))
                                                }
                                            }
                                        }
                                        if (trelloSaved) Text("✓ Lista guardada correctamente", color = Primary, fontSize = 12.sp)
                                    }
                                }
                            }

                            // ── Notion ────────────────────────────────────────
                            HorizontalDivider(color = SurfaceVariant, modifier = Modifier.padding(horizontal = 16.dp))
                            Row(modifier = Modifier.fillMaxWidth().clickable { notionExpanded = !notionExpanded }.padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text(stringResource(R.string.settings_notion), color = TextPrimary, style = MaterialTheme.typography.titleSmall)
                                    Text(if (!notionDatabaseId.isNullOrEmpty()) "BD: ${notionDatabaseName ?: ""}" else stringResource(R.string.settings_not_configured), color = if (!notionDatabaseId.isNullOrEmpty()) Primary else TextMuted, fontSize = 12.sp)
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { notionInfoDialog = true }, modifier = Modifier.size(28.dp)) {
                                        Icon(Icons.Default.Info, contentDescription = "Info", tint = TextMuted, modifier = Modifier.size(18.dp))
                                    }
                                    Icon(if (notionExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null, tint = TextSecondary)
                                }
                            }
                            AnimatedVisibility(visible = notionExpanded) {
                                Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text("Paso 1 — Integration Token", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    OutlinedTextField(value = notionTokenInput, onValueChange = { notionTokenInput = it; notionDatabases = emptyList(); notionSaved = false }, label = { Text("Integration Token") }, modifier = Modifier.fillMaxWidth(), visualTransformation = if (notionTokenVisible) VisualTransformation.None else PasswordVisualTransformation(), trailingIcon = { IconButton(onClick = { notionTokenVisible = !notionTokenVisible }) { Icon(if (notionTokenVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = null, tint = TextSecondary) } }, shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = Border, focusedLabelColor = Primary, cursorColor = Primary))
                                    if (notionError != null) Text(notionError!!, color = Error, fontSize = 12.sp)
                                    Text("Paso 2 — Selecciona una base de datos", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    Button(onClick = { scope.launch { notionCargando = true; notionError = null; try { val resp = ExternalApiClient.notionApi.searchDatabases("Bearer $notionTokenInput"); notionDatabases = resp.results.map { it.id to it.name }; tokenRepository.saveNotion(notionTokenInput) } catch (_: Exception) { notionError = "Error al conectar con Notion. Verifica tu token y que hayas compartido la base de datos con tu integración." } finally { notionCargando = false } } }, enabled = notionTokenInput.isNotBlank() && !notionCargando, modifier = Modifier.fillMaxWidth().height(44.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = SurfaceVariant)) {
                                        if (notionCargando) CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Primary, strokeWidth = 2.dp)
                                        else Text("Cargar bases de datos", color = TextPrimary, fontSize = 13.sp)
                                    }
                                    if (notionDatabases.isNotEmpty()) {
                                        notionDatabases.forEach { (id, name) ->
                                            val sel = notionDatabaseId == id
                                            Surface(modifier = Modifier.fillMaxWidth().clickable { scope.launch { tokenRepository.saveNotionDatabase(id, name); notionSaved = true } }, shape = RoundedCornerShape(10.dp), color = if (sel) Primary.copy(alpha = 0.15f) else SurfaceVariant, border = androidx.compose.foundation.BorderStroke(if (sel) 1.5.dp else 1.dp, if (sel) Primary else Border)) {
                                                Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                                    Text(name, color = if (sel) Primary else TextPrimary, fontSize = 14.sp, fontWeight = if (sel) FontWeight.SemiBold else FontWeight.Normal)
                                                    if (sel) Icon(Icons.Default.Check, contentDescription = null, tint = Primary, modifier = Modifier.size(16.dp))
                                                }
                                            }
                                        }
                                        if (notionSaved) Text("✓ Base de datos guardada correctamente", color = Primary, fontSize = 12.sp)
                                    }
                                }
                            }

                            // ── Google Calendar ───────────────────────────────
                            HorizontalDivider(color = SurfaceVariant, modifier = Modifier.padding(horizontal = 16.dp))
                            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text(stringResource(R.string.settings_google_calendar), color = TextMuted, style = MaterialTheme.typography.titleSmall)
                                    Text(stringResource(R.string.settings_coming_soon), color = TextMuted, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── Guía ──────────────────────────────────────────────────────────
            Text(stringResource(R.string.settings_section_guia), color = TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp))

            Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Surface)) {
                Column {
                    Row(modifier = Modifier.fillMaxWidth().clickable { comoFuncionaExpanded = !comoFuncionaExpanded }.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
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

            Spacer(modifier = Modifier.height(16.dp))

            // ── FAQ y Aviso Legal ─────────────────────────────────────────────
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { showFaqDialog = true },
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Border)
                ) {
                    Icon(Icons.Default.HelpOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("FAQ", fontSize = 13.sp)
                }
                OutlinedButton(
                    onClick = { showAvisoLegalDialog = true },
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Border)
                ) {
                    Icon(Icons.Default.Gavel, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Aviso legal", fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── Cerrar sesión ─────────────────────────────────────────────────
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Button(
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier.height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceVariant),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = Error, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.settings_logout), color = Error, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // ── Dialogs ───────────────────────────────────────────────────────────────

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false }, containerColor = Surface,
            title = { Text(stringResource(R.string.settings_logout_title), color = TextPrimary, fontWeight = FontWeight.SemiBold) },
            text = { Text(stringResource(R.string.settings_logout_confirm), color = TextSecondary) },
            confirmButton = { Button(onClick = { showLogoutDialog = false; onLogout() }, colors = ButtonDefaults.buttonColors(containerColor = Error)) { Text(stringResource(R.string.settings_logout), color = OnPrimary) } },
            dismissButton = { TextButton(onClick = { showLogoutDialog = false }) { Text(stringResource(R.string.settings_logout_cancel), color = TextSecondary) } }
        )
    }

    if (showFaqDialog) {
        AlertDialog(
            onDismissRequest = { showFaqDialog = false }, containerColor = Surface,
            title = { Text("Preguntas frecuentes", color = TextPrimary, fontWeight = FontWeight.SemiBold) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    FaqItem("¿Es gratuita la app?", "Voice2Task es gratuita. Solo necesitas una API Key de Groq para procesar los audios, que también es gratuita en console.groq.com.")
                    HorizontalDivider(color = SurfaceVariant)
                    FaqItem("¿Dónde se guardan mis datos?", "Tus tareas y listas se guardan en un servidor seguro asociado a tu cuenta. Las credenciales de Trello y Notion se guardan únicamente en tu dispositivo.")
                    HorizontalDivider(color = SurfaceVariant)
                    FaqItem("¿Qué pasa si no tengo Groq Key?", "Sin Groq Key no podrás crear tareas por voz, pero puedes ver y gestionar tareas existentes. Ve a Ajustes → Integraciones → Groq API Key para configurarla.")
                    HorizontalDivider(color = SurfaceVariant)
                    FaqItem("¿Por qué no aparece mi tarea en el calendario?", "Solo aparecen las tareas con fecha límite asignada. Al grabar el audio, menciona la fecha (ej: 'para el viernes') y la IA la detectará automáticamente.")
                    HorizontalDivider(color = SurfaceVariant)
                    FaqItem("¿Puedo conectar Trello y Notion a la vez?", "Sí. Cuando creas una tarea por voz, puedes elegir en qué destinos guardarla: Voice2Task, Trello, Notion, o todos a la vez.")
                    HorizontalDivider(color = SurfaceVariant)
                    FaqItem("¿Cómo cambio la contraseña?", "Ve a Ajustes → Cuenta → Cambiar contraseña. Necesitarás introducir tu contraseña actual y la nueva dos veces.")
                }
            },
            confirmButton = { TextButton(onClick = { showFaqDialog = false }) { Text("Cerrar", color = Primary) } }
        )
    }

    if (showAvisoLegalDialog) {
        AlertDialog(
            onDismissRequest = { showAvisoLegalDialog = false }, containerColor = Surface,
            title = { Text("Aviso legal y privacidad", color = TextPrimary, fontWeight = FontWeight.SemiBold) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Responsable", color = Primary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Text("Voice2Task es una aplicación desarrollada por ArocaDev con fines personales y de portfolio. No es una empresa comercial registrada.", color = TextSecondary, fontSize = 13.sp)
                    HorizontalDivider(color = SurfaceVariant)
                    Text("Datos que recogemos", color = Primary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Text("• Correo electrónico y nombre de usuario para crear tu cuenta.\n• El audio que grabas se envía a la API de Groq para su transcripción y no se almacena en nuestros servidores.\n• Las tareas y listas que creas se guardan en nuestra base de datos asociadas a tu cuenta.", color = TextSecondary, fontSize = 13.sp)
                    HorizontalDivider(color = SurfaceVariant)
                    Text("Datos de terceros", color = Primary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Text("Las credenciales de Trello y Notion (API Keys y tokens) se almacenan únicamente en tu dispositivo mediante almacenamiento seguro de Android. En ningún caso se envían a nuestros servidores.", color = TextSecondary, fontSize = 13.sp)
                    HorizontalDivider(color = SurfaceVariant)
                    Text("Uso de los datos", color = Primary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Text("Tus datos se utilizan exclusivamente para el funcionamiento de la aplicación. No se venden ni se comparten con terceros salvo las integraciones que tú mismo configuras (Groq, Trello, Notion).", color = TextSecondary, fontSize = 13.sp)
                    HorizontalDivider(color = SurfaceVariant)
                    Text("Tus derechos", color = Primary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Text("Puedes solicitar la eliminación de tu cuenta y todos tus datos en cualquier momento contactando al desarrollador. Al eliminar la cuenta, se borran todos tus datos de nuestros servidores.", color = TextSecondary, fontSize = 13.sp)
                    HorizontalDivider(color = SurfaceVariant)
                    Text("Limitación de responsabilidad", color = Primary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Text("Esta aplicación se proporciona tal cual, sin garantías de disponibilidad continua. El desarrollador no se hace responsable de pérdidas de datos derivadas del uso de la aplicación.", color = TextSecondary, fontSize = 13.sp)
                }
            },
            confirmButton = { TextButton(onClick = { showAvisoLegalDialog = false }) { Text("Cerrar", color = Primary) } }
        )
    }

    if (groqInfoDialog) {
        AlertDialog(
            onDismissRequest = { groqInfoDialog = false }, containerColor = Surface,
            title = { Text("¿Cómo obtener la Groq API Key?", color = TextPrimary, fontWeight = FontWeight.SemiBold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("La API Key de Groq es gratuita y permite procesar tus audios con Whisper.", color = TextSecondary, fontSize = 14.sp)
                    Text("1. Ve a console.groq.com en tu navegador", color = TextSecondary, fontSize = 14.sp)
                    Text("2. Crea una cuenta o inicia sesión", color = TextSecondary, fontSize = 14.sp)
                    Text("3. Ve a API Keys → Create API Key", color = TextSecondary, fontSize = 14.sp)
                    Text("4. Copia la clave y pégala aquí", color = TextSecondary, fontSize = 14.sp)
                }
            },
            confirmButton = { TextButton(onClick = { groqInfoDialog = false }) { Text("Entendido", color = Primary) } }
        )
    }

    if (trelloInfoDialog) {
        AlertDialog(
            onDismissRequest = { trelloInfoDialog = false }, containerColor = Surface,
            title = { Text("¿Cómo conectar Trello?", color = TextPrimary, fontWeight = FontWeight.SemiBold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Necesitas una API Key y un Token de Trello. Ambos son gratuitos.", color = TextSecondary, fontSize = 14.sp)
                    Text("1. Ve a trello.com/app-key en tu navegador", color = TextSecondary, fontSize = 14.sp)
                    Text("2. Copia tu API Key y pégala en el primer campo", color = TextSecondary, fontSize = 14.sp)
                    Text("3. En esa misma página pulsa el enlace 'Token' — se abrirá una página donde Trello te pedirá permiso. Acéptalo y copia el token que aparece.", color = TextSecondary, fontSize = 14.sp)
                    Text("4. Pega el Token en el segundo campo", color = TextSecondary, fontSize = 14.sp)
                    Text("5. Pulsa 'Cargar tableros', selecciona el tablero y luego la lista donde quieres que se creen las tarjetas", color = TextSecondary, fontSize = 14.sp)
                }
            },
            confirmButton = { TextButton(onClick = { trelloInfoDialog = false }) { Text("Entendido", color = Primary) } }
        )
    }

    if (notionInfoDialog) {
        AlertDialog(
            onDismissRequest = { notionInfoDialog = false }, containerColor = Surface,
            title = { Text("¿Cómo conectar Notion?", color = TextPrimary, fontWeight = FontWeight.SemiBold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Necesitas crear una integración en Notion para obtener un token.", color = TextSecondary, fontSize = 14.sp)
                    Text("1. Ve a notion.so/my-integrations en tu navegador", color = TextSecondary, fontSize = 14.sp)
                    Text("2. Pulsa 'New integration', ponle nombre y guárdala", color = TextSecondary, fontSize = 14.sp)
                    Text("3. Copia el 'Internal Integration Token' que aparece", color = TextSecondary, fontSize = 14.sp)
                    Text("4. Importante: abre tu base de datos en Notion, pulsa los tres puntos (⋯) → Connections → y añade tu integración. Sin este paso no funcionará.", color = TextSecondary, fontSize = 14.sp)
                    Text("5. Pega el token aquí, pulsa 'Cargar bases de datos' y selecciona la que quieras usar", color = TextSecondary, fontSize = 14.sp)
                }
            },
            confirmButton = { TextButton(onClick = { notionInfoDialog = false }) { Text("Entendido", color = Primary) } }
        )
    }
}

@Composable
private fun FaqItem(pregunta: String, respuesta: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(pregunta, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        Text(respuesta, color = TextSecondary, fontSize = 13.sp)
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