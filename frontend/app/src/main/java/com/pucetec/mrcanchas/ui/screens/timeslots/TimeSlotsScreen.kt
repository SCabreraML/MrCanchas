package com.pucetec.mrcanchas.ui.screens.timeslots

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pucetec.mrcanchas.models.Court
import com.pucetec.mrcanchas.models.ReservationRequest
import com.pucetec.mrcanchas.models.TimeSlot
import com.pucetec.mrcanchas.models.TimeSlotRequest
import com.pucetec.mrcanchas.services.RetrofitClient
import com.pucetec.mrcanchas.services.SessionManager
import com.pucetec.mrcanchas.ui.components.CourtCard
import com.pucetec.mrcanchas.ui.components.TimeSlotCard
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeSlotsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sessionManager = remember { SessionManager(context) }

    var selectedCourt by remember { mutableStateOf<Court?>(null) }

    // Canchas
    var courts by remember { mutableStateOf<List<Court>>(emptyList()) }
    var courtsLoading by remember { mutableStateOf(true) }
    var courtsError by remember { mutableStateOf<String?>(null) }

    // Horarios de la cancha elegida
    var slots by remember { mutableStateOf<List<TimeSlot>>(emptyList()) }
    var slotsLoading by remember { mutableStateOf(false) }
    var slotsError by remember { mutableStateOf<String?>(null) }

    // Diálogos y estados de acción
    var showCreate by remember { mutableStateOf(false) }
    var slotToEdit by remember { mutableStateOf<TimeSlot?>(null) }
    var slotToDelete by remember { mutableStateOf<TimeSlot?>(null) }
    var isSaving by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }
    var isReserving by remember { mutableStateOf(false) }

    fun loadCourts() {
        courtsLoading = true
        courtsError = null
        scope.launch {
            try {
                courts = RetrofitClient.getApiService(context).getCourts()
            } catch (e: Exception) {
                courtsError = loadErrorMessage(e)
            } finally {
                courtsLoading = false
            }
        }
    }

    fun loadSlots(courtId: Long) {
        slotsLoading = true
        slotsError = null
        scope.launch {
            try {
                slots = RetrofitClient.getApiService(context).getTimeSlotsByCourt(courtId)
            } catch (e: Exception) {
                slotsError = loadErrorMessage(e)
            } finally {
                slotsLoading = false
            }
        }
    }

    fun submitCreate(request: TimeSlotRequest) {
        isSaving = true
        scope.launch {
            try {
                RetrofitClient.getApiService(context).createTimeSlot(request)
                showCreate = false
                Toast.makeText(context, "Horario creado", Toast.LENGTH_SHORT).show()
                selectedCourt?.let { loadSlots(it.id) }
            } catch (e: Exception) {
                Toast.makeText(context, timeSlotWriteErrorMessage(e), Toast.LENGTH_LONG).show()
            } finally {
                isSaving = false
            }
        }
    }

    fun submitUpdate(id: Long, request: TimeSlotRequest) {
        isSaving = true
        scope.launch {
            try {
                RetrofitClient.getApiService(context).updateTimeSlot(id, request)
                slotToEdit = null
                Toast.makeText(context, "Horario actualizado", Toast.LENGTH_SHORT).show()
                selectedCourt?.let { loadSlots(it.id) }
            } catch (e: Exception) {
                Toast.makeText(context, timeSlotWriteErrorMessage(e), Toast.LENGTH_LONG).show()
            } finally {
                isSaving = false
            }
        }
    }

    fun submitDelete(slot: TimeSlot) {
        isDeleting = true
        scope.launch {
            try {
                val response = RetrofitClient.getApiService(context).deleteTimeSlot(slot.id)
                if (response.isSuccessful) {
                    slotToDelete = null
                    Toast.makeText(context, "Horario eliminado", Toast.LENGTH_SHORT).show()
                    selectedCourt?.let { loadSlots(it.id) }
                } else {
                    val msg = when (response.code()) {
                        401 -> "Tu sesión expiró. Vuelve a iniciar sesión."
                        403 -> "No tienes permiso para eliminar horarios."
                        404 -> "El horario ya no existe."
                        409 -> "No se puede eliminar: el horario tiene una reserva asociada."
                        else -> "No se pudo eliminar el horario (código ${response.code()})."
                    }
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, e.localizedMessage ?: "Error de red al eliminar.", Toast.LENGTH_LONG).show()
            } finally {
                isDeleting = false
            }
        }
    }

    fun submitReserve(slot: TimeSlot) {
        isReserving = true
        scope.launch {
            try {
                RetrofitClient.getApiService(context).createReservation(
                    ReservationRequest(timeSlotId = slot.id)
                )
                Toast.makeText(context, "Reserva creada", Toast.LENGTH_SHORT).show()
                selectedCourt?.let { loadSlots(it.id) }  // el horario pasa a RESERVED
            } catch (e: Exception) {
                Toast.makeText(context, reserveErrorMessage(e), Toast.LENGTH_LONG).show()
            } finally {
                isReserving = false
            }
        }
    }

    LaunchedEffect(Unit) { loadCourts() }
    LaunchedEffect(selectedCourt?.id) {
        selectedCourt?.let { loadSlots(it.id) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        selectedCourt?.name ?: "Horarios",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (selectedCourt != null) selectedCourt = null else onBack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                windowInsets = WindowInsets(0.dp)
            )
        },
        floatingActionButton = {
            if (selectedCourt != null && sessionManager.isAdmin()) {
                FloatingActionButton(onClick = { showCreate = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Crear horario")
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            if (selectedCourt == null) {
                // ----- Paso 1: elegir cancha -----
                when {
                    courtsLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                    courtsError != null -> CenterMessage(courtsError!!) { loadCourts() }
                    courts.isEmpty() -> CenterMessage("No hay canchas registradas.")
                    else -> LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        item {
                            Text(
                                "Elige una cancha para ver sus horarios",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        items(courts) { court ->
                            CourtCard(
                                court = court,
                                onClick = { selectedCourt = court }
                            )
                        }
                    }
                }
            } else {
                // ----- Paso 2: horarios de la cancha -----
                when {
                    slotsLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                    slotsError != null -> CenterMessage(slotsError!!) {
                        selectedCourt?.let { loadSlots(it.id) }
                    }
                    slots.isEmpty() -> CenterMessage("Esta cancha aún no tiene horarios.")
                    else -> LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        items(slots) { slot ->
                            TimeSlotCard(
                                timeSlot = slot,
                                isAdmin = sessionManager.isAdmin(),
                                canReserve = !sessionManager.isAdmin() && !sessionManager.isGuest(),
                                onEdit = { slotToEdit = slot },
                                onDelete = { slotToDelete = slot },
                                onReserve = { submitReserve(slot) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Crear / editar horario
    if (showCreate || slotToEdit != null) {
        val court = selectedCourt
        if (court != null) {
            TimeSlotFormDialog(
                initial = slotToEdit,
                isSaving = isSaving,
                onDismiss = {
                    if (!isSaving) {
                        showCreate = false
                        slotToEdit = null
                    }
                },
                onConfirm = { date, start, end ->
                    val request = TimeSlotRequest(
                        courtId = court.id,
                        date = date,
                        startTime = start,
                        endTime = end
                    )
                    val editing = slotToEdit
                    if (editing == null) submitCreate(request) else submitUpdate(editing.id, request)
                }
            )
        }
    }

    // Confirmar eliminación
    slotToDelete?.let { slot ->
        AlertDialog(
            onDismissRequest = { if (!isDeleting) slotToDelete = null },
            title = { Text("Eliminar horario") },
            text = { Text("¿Seguro que deseas eliminar el horario del ${slot.date} (${slot.startTime.take(5)} - ${slot.endTime.take(5)})?") },
            confirmButton = {
                Button(
                    enabled = !isDeleting,
                    onClick = { submitDelete(slot) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    if (isDeleting) {
                        CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onError)
                    } else {
                        Text("Eliminar")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { slotToDelete = null }, enabled = !isDeleting) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun BoxScope.CenterMessage(text: String, onRetry: (() -> Unit)? = null) {
    Column(
        modifier = Modifier.align(Alignment.Center).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
        if (onRetry != null) {
            Spacer(Modifier.height(16.dp))
            Button(onClick = onRetry) { Text("Reintentar") }
        }
    }
}

// Formulario reutilizable: crear (initial = null) o editar (initial = horario)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimeSlotFormDialog(
    initial: TimeSlot?,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (date: String, startTime: String, endTime: String) -> Unit
) {
    val context = LocalContext.current
    val isEdit = initial != null

    var date by remember(initial?.id) { mutableStateOf(initial?.date ?: "") }
    var startTime by remember(initial?.id) { mutableStateOf(initial?.startTime?.take(5) ?: "") }
    var endTime by remember(initial?.id) { mutableStateOf(initial?.endTime?.take(5) ?: "") }
    var triedSubmit by remember(initial?.id) { mutableStateOf(false) }

    val dateError = triedSubmit && date.isBlank()
    val startError = triedSubmit && startTime.isBlank()
    val endBlankError = triedSubmit && endTime.isBlank()
    val orderError = triedSubmit && startTime.isNotBlank() && endTime.isNotBlank() && !isBefore(startTime, endTime)
    val isValid = date.isNotBlank() && startTime.isNotBlank() && endTime.isNotBlank() && isBefore(startTime, endTime)

    fun openDatePicker() {
        val cal = Calendar.getInstance()
        parseDate(date)?.let { (y, m, d) -> cal.set(y, m - 1, d) }
        DatePickerDialog(
            context,
            { _, y, m, d -> date = "%04d-%02d-%02d".format(y, m + 1, d) },
            cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.minDate = System.currentTimeMillis() - 60_000
        }.show()
    }

    fun openTimePicker(current: String, onPicked: (String) -> Unit) {
        val (h, min) = parseTime(current) ?: (8 to 0)
        TimePickerDialog(
            context,
            { _, hh, mm -> onPicked("%02d:%02d".format(hh, mm)) },
            h, min, true
        ).show()
    }

    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        title = { Text(if (isEdit) "Editar horario" else "Nuevo horario") },
        text = {
            Column {
                PickerField("Fecha", date, "Elegir fecha", dateError, "Elige una fecha") { openDatePicker() }
                Spacer(Modifier.height(8.dp))
                PickerField("Hora inicio", startTime, "Elegir hora", startError, "Elige la hora de inicio") {
                    openTimePicker(startTime) { startTime = it }
                }
                Spacer(Modifier.height(8.dp))
                PickerField(
                    "Hora fin", endTime, "Elegir hora",
                    endBlankError || orderError,
                    if (orderError) "La hora de fin debe ser mayor que la de inicio" else "Elige la hora de fin"
                ) {
                    openTimePicker(endTime) { endTime = it }
                }
            }
        },
        confirmButton = {
            Button(
                enabled = !isSaving,
                onClick = {
                    triedSubmit = true
                    if (isValid) onConfirm(date, startTime, endTime)
                }
            ) {
                if (isSaving) {
                    CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text(if (isEdit) "Guardar" else "Crear")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) { Text("Cancelar") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PickerField(
    label: String,
    value: String,
    placeholder: String,
    isError: Boolean,
    errorText: String,
    onClick: () -> Unit
) {
    Box {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            isError = isError,
            supportingText = { if (isError) Text(errorText) },
            modifier = Modifier.fillMaxWidth()
        )
        Box(Modifier.matchParentSize().clickable(onClick = onClick))
    }
}

// ---------- helpers ----------
private fun parseDate(s: String): Triple<Int, Int, Int>? {
    val p = s.split("-")
    if (p.size < 3) return null
    val y = p[0].toIntOrNull() ?: return null
    val m = p[1].toIntOrNull() ?: return null
    val d = p[2].toIntOrNull() ?: return null
    return Triple(y, m, d)
}

private fun parseTime(s: String): Pair<Int, Int>? {
    val p = s.split(":")
    if (p.size < 2) return null
    val h = p[0].toIntOrNull() ?: return null
    val m = p[1].toIntOrNull() ?: return null
    return h to m
}

private fun isBefore(a: String, b: String): Boolean {
    val ta = parseTime(a) ?: return false
    val tb = parseTime(b) ?: return false
    return ta.first * 60 + ta.second < tb.first * 60 + tb.second
}

private fun loadErrorMessage(e: Exception): String = when (e) {
    is java.net.UnknownHostException, is java.net.ConnectException ->
        "No se pudo conectar con el servidor de MrCanchas."
    is java.net.SocketTimeoutException ->
        "El servidor tardó demasiado en responder (Timeout)."
    else -> e.localizedMessage ?: "Ocurrió un error inesperado."
}

private fun timeSlotWriteErrorMessage(e: Exception): String = when (e) {
    is HttpException -> when (e.code()) {
        401 -> "Tu sesión expiró. Vuelve a iniciar sesión."
        403 -> "No tienes permiso para esta acción."
        404 -> "El horario o la cancha ya no existe."
        400 -> "Datos del horario inválidos."
        409 -> "El horario entra en conflicto con otro existente."
        else -> "Error del servidor (código ${e.code()})."
    }
    is java.net.UnknownHostException, is java.net.ConnectException ->
        "No se pudo conectar con el servidor de MrCanchas."
    is java.net.SocketTimeoutException ->
        "El servidor tardó demasiado en responder (Timeout)."
    else -> e.localizedMessage ?: "Ocurrió un error inesperado."
}

private fun reserveErrorMessage(e: Exception): String = when (e) {
    is HttpException -> when (e.code()) {
        401 -> "Tu sesión expiró. Vuelve a iniciar sesión."
        403 -> "No tienes permiso para reservar."
        404 -> "El horario ya no existe."
        409 -> "Ese horario ya fue reservado por alguien más."
        else -> "No se pudo crear la reserva (código ${e.code()})."
    }
    is java.net.UnknownHostException, is java.net.ConnectException ->
        "No se pudo conectar con el servidor de MrCanchas."
    is java.net.SocketTimeoutException ->
        "El servidor tardó demasiado en responder (Timeout)."
    else -> e.localizedMessage ?: "Ocurrió un error inesperado."
}