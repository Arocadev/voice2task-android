package dev.aroca.voice2taskapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import dev.aroca.voice2taskapp.data.model.Tarea
import dev.aroca.voice2taskapp.ui.theme.*
import dev.aroca.voice2taskapp.viewmodel.TareasState
import dev.aroca.voice2taskapp.viewmodel.TareasViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarioScreen(
    onVerDetalle: (Tarea) -> Unit = {},
    viewModel: TareasViewModel = viewModel()
) {
    // ViewModel dedicado para el calendario — carga TODAS las tareas sin filtro de lista
    val calendarioViewModel: TareasViewModel = viewModel(key = "calendario")
    val tareasState by calendarioViewModel.tareasState.collectAsState()

    var diaSeleccionado by remember { mutableStateOf<LocalDate?>(LocalDate.now()) }
    val scope = rememberCoroutineScope()

    // Cargar todas las tareas al entrar
    LaunchedEffect(Unit) { calendarioViewModel.cargarTareas(listaId = null) }

    val tareasPorFecha: Map<LocalDate, List<Tarea>> = remember(tareasState) {
        val tareas = (tareasState as? TareasState.Success)?.tareas ?: emptyList()
        tareas
            .filter { it.fecha_limite != null }
            .groupBy { tarea ->
                try { LocalDate.parse(tarea.fecha_limite!!.take(10)) } catch (_: Exception) { null }
            }
            .filterKeys { it != null }
            .mapKeys { it.key!! }
    }

    val tareasDelDia = diaSeleccionado?.let { tareasPorFecha[it] } ?: emptyList()

    val currentMonth = remember { YearMonth.now() }
    val startMonth = remember { currentMonth.minusMonths(12) }
    val endMonth = remember { currentMonth.plusMonths(12) }
    val firstDayOfWeek = remember { firstDayOfWeekFromLocale() }

    val calendarState = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = firstDayOfWeek
    )

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { scope.launch { calendarState.animateScrollToMonth(calendarState.firstVisibleMonth.yearMonth.minusMonths(1)) } }) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Mes anterior", tint = TextPrimary)
            }
            Text(
                calendarState.firstVisibleMonth.yearMonth.let {
                    "${it.month.getDisplayName(TextStyle.FULL, Locale("es")).replaceFirstChar { c -> c.uppercase() }} ${it.year}"
                },
                color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 16.sp
            )
            IconButton(onClick = { scope.launch { calendarState.animateScrollToMonth(calendarState.firstVisibleMonth.yearMonth.plusMonths(1)) } }) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Mes siguiente", tint = TextPrimary)
            }
        }

        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
            listOf("L", "M", "X", "J", "V", "S", "D").forEach { dia ->
                Text(dia, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        HorizontalCalendar(
            state = calendarState,
            dayContent = { day ->
                DiaCalendario(
                    day = day,
                    seleccionado = diaSeleccionado == day.date,
                    tieneTareas = tareasPorFecha.containsKey(day.date),
                    onClick = {
                        if (day.position == DayPosition.MonthDate) {
                            diaSeleccionado = if (diaSeleccionado == day.date) null else day.date
                        }
                    }
                )
            }
        )

        HorizontalDivider(color = SurfaceVariant, modifier = Modifier.padding(vertical = 8.dp))

        if (tareasState is TareasState.Loading) {
            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary, modifier = Modifier.size(32.dp))
            }
        } else if (diaSeleccionado != null) {
            val formatter = DateTimeFormatter.ofPattern("d 'de' MMMM", Locale("es"))
            Text(
                diaSeleccionado!!.format(formatter),
                color = Primary, fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
            if (tareasDelDia.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("Sin tareas este día", color = TextMuted, fontSize = 14.sp)
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(tareasDelDia) { tarea ->
                        TareaCard(
                            tarea = tarea,
                            onCompletar = { calendarioViewModel.completarTarea(tarea.id, tarea.lista_id) },
                            onEliminar = { calendarioViewModel.eliminarTarea(tarea.id, tarea.lista_id) },
                            onMarcarImportante = { calendarioViewModel.marcarImportante(tarea.id, !tarea.importante, tarea.lista_id) },
                            onVerDetalle = { onVerDetalle(tarea) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DiaCalendario(day: CalendarDay, seleccionado: Boolean, tieneTareas: Boolean, onClick: () -> Unit) {
    val esHoy = day.date == LocalDate.now()
    val esMesActual = day.position == DayPosition.MonthDate
    Box(
        modifier = Modifier.aspectRatio(1f).padding(3.dp).clip(CircleShape)
            .background(when { seleccionado -> Primary; esHoy -> Primary.copy(alpha = 0.15f); else -> androidx.compose.ui.graphics.Color.Transparent })
            .clickable(enabled = esMesActual, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                day.date.dayOfMonth.toString(),
                color = when { seleccionado -> OnPrimary; esHoy -> Primary; esMesActual -> TextPrimary; else -> TextMuted.copy(alpha = 0.4f) },
                fontSize = 13.sp,
                fontWeight = if (esHoy || seleccionado) FontWeight.Bold else FontWeight.Normal
            )
            if (tieneTareas && esMesActual) {
                Spacer(modifier = Modifier.height(2.dp))
                Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(if (seleccionado) OnPrimary else Primary))
            }
        }
    }
}