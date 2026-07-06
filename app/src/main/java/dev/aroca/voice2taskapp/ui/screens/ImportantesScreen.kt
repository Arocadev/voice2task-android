package dev.aroca.voice2taskapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.aroca.voice2taskapp.data.model.Tarea
import dev.aroca.voice2taskapp.ui.theme.*
import dev.aroca.voice2taskapp.viewmodel.TareasState
import dev.aroca.voice2taskapp.viewmodel.TareasViewModel

@Composable
fun ImportantesScreen(
    onVerDetalle: (Tarea) -> Unit = {},
    viewModel: TareasViewModel = viewModel()
) {
    val importantesState by viewModel.importantesState.collectAsState()

    LaunchedEffect(Unit) { viewModel.cargarImportantes() }

    Box(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        when (val s = importantesState) {
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
                        Text("⭐", style = MaterialTheme.typography.headlineLarge)
                        Text(
                            "Sin tareas importantes",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "Abre una lista y pulsa ⋮ en cualquier tarea",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Text(
                                "${s.tareas.size} tarea${if (s.tareas.size != 1) "s" else ""} importante${if (s.tareas.size != 1) "s" else ""}",
                                color = TextMuted,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                        items(s.tareas) { tarea ->
                            TareaCard(
                                tarea = tarea,
                                onCompletar = { viewModel.completarTarea(tarea.id, tarea.lista_id) },
                                onEliminar = { viewModel.eliminarTarea(tarea.id, tarea.lista_id) },
                                onMarcarImportante = {
                                    viewModel.marcarImportante(tarea.id, false, tarea.lista_id)
                                    viewModel.cargarImportantes()
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