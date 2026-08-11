package com.pucetec.mrcanchas.ui.screens.courts

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
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
import com.pucetec.mrcanchas.models.CourtRequest
import com.pucetec.mrcanchas.services.RetrofitClient
import com.pucetec.mrcanchas.services.SessionManager
import com.pucetec.mrcanchas.ui.components.CourtCard
import kotlinx.coroutines.launch
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import retrofit2.HttpException

// Deportes válidos para el dropdown
private val SPORTS = listOf("Fútbol", "Básquet", "Tenis", "Vóley", "Ecuavóley")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourtsScreen(
    onNavigateToDetail: (Long) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var courts by remember { mutableStateOf<List<Court>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val sessionManager = remember { SessionManager(context) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var courtToEdit by remember { mutableStateOf<Court?>(null) }   // NUEVO
    var courtToDelete by remember { mutableStateOf<Court?>(null) } // NUEVO
    var isSaving by remember { mutableStateOf(false) }             // crear/editar
    var isDeleting by remember { mutableStateOf(false) }           // NUEVO

    fun loadCourts() {
        isLoading = true
        errorMessage = null
        scope.launch {
            try {
                val api = RetrofitClient.getApiService(context)
                courts = api.getCourts()
            } catch (e: Exception) {
                errorMessage = when (e) {
                    is UnknownHostException, is ConnectException -> {
                        "No se pudo conectar con el servidor de MrCanchas. Asegúrate de que el backend esté iniciado y de que tu dispositivo tenga acceso a la red local."
                    }
                    is SocketTimeoutException -> {
                        "El servidor tardó demasiado en responder (Timeout). Por favor, intenta de nuevo."
                    }
                    else -> {
                        e.localizedMessage ?: "Ocurrió un error inesperado al cargar las canchas."
                    }
                }
            } finally {
                isLoading = false
            }
        }
    }

    // Crear
    fun submitCreateCourt(request: CourtRequest) {
        isSaving = true
        scope.launch {
            try {
                val api = RetrofitClient.getApiService(context)
                api.createCourt(request)
                showCreateDialog = false
                Toast.makeText(context, "Cancha creada correctamente", Toast.LENGTH_SHORT).show()
                loadCourts()
            } catch (e: Exception) {
                Toast.makeText(context, courtWriteErrorMessage(e), Toast.LENGTH_LONG).show()
            } finally {
                isSaving = false
            }
        }
    }

    // NUEVO — Editar
    fun submitUpdateCourt(id: Long, request: CourtRequest) {
        isSaving = true
        scope.launch {
            try {
                val api = RetrofitClient.getApiService(context)
                api.updateCourt(id, request)
                courtToEdit = null
                Toast.makeText(context, "Cancha actualizada", Toast.LENGTH_SHORT).show()
                loadCourts()
            } catch (e: Exception) {
                Toast.makeText(context, courtWriteErrorMessage(e), Toast.LENGTH_LONG).show()
            } finally {
                isSaving = false
            }
        }
    }

    // NUEVO — Eliminar
    fun submitDeleteCourt(court: Court) {
        isDeleting = true
        scope.launch {
            try {
                val api = RetrofitClient.getApiService(context)
                val response = api.deleteCourt(court.id)
                if (response.isSuccessful) {
                    courtToDelete = null
                    Toast.makeText(context, "Cancha eliminada", Toast.LENGTH_SHORT).show()
                    loadCourts()
                } else {
                    val msg = when (response.code()) {
                        401 -> "Tu sesión expiró. Vuelve a iniciar sesión."
                        403 -> "No tienes permiso para eliminar canchas."
                        404 -> "La cancha ya no existe."
                        409 -> "No se puede eliminar: tiene horarios o reservas asociadas."
                        else -> "No se pudo eliminar la cancha (código ${response.code()})."
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


    LaunchedEffect(Unit) {
        loadCourts()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Canchas Deportivas",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Atrás")
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
            if (sessionManager.isAdmin()) {
                FloatingActionButton(onClick = { showCreateDialog = true }) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Crear cancha")
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
            if (isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (errorMessage != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .align(Alignment.Center),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Error de Conexión",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { loadCourts() },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Reintentar")
                        }
                    }
                }
            } else if (courts.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No hay canchas registradas en este momento.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(courts) { court ->
                        CourtCard(
                            court = court,
                            onClick = { onNavigateToDetail(court.id) },
                            isAdmin = sessionManager.isAdmin(),        // NUEVO
                            onEdit = { courtToEdit = court },          // NUEVO
                            onDelete = { courtToDelete = court }       // NUEVO
                        )
                    }
                }
            }
        }
    }

    // Formulario crear / editar (mismo diálogo)
    if (showCreateDialog || courtToEdit != null) {
        CourtFormDialog(
            initial = courtToEdit, // null = crear, no-null = editar
            isSaving = isSaving,
            onDismiss = {
                if (!isSaving) {
                    showCreateDialog = false
                    courtToEdit = null
                }
            },
            onConfirm = { request ->
                val editing = courtToEdit
                if (editing == null) submitCreateCourt(request)
                else submitUpdateCourt(editing.id, request)
            }
        )
    }

    // NUEVO — confirmación de eliminar
    courtToDelete?.let { court ->
        AlertDialog(
            onDismissRequest = { if (!isDeleting) courtToDelete = null },
            title = { Text("Eliminar cancha") },
            text = { Text("¿Seguro que deseas eliminar \"${court.name}\"? Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(
                    enabled = !isDeleting,
                    onClick = { submitDeleteCourt(court) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    if (isDeleting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onError
                        )
                    } else {
                        Text("Eliminar")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { courtToDelete = null }, enabled = !isDeleting) {
                    Text("Cancelar")
                }
            }
        )
    }
}
// función top-level privada en CourtsScreen.kt (fuera del @Composable)
private fun courtWriteErrorMessage(e: Exception): String = when (e) {
    is HttpException -> when (e.code()) {
        401 -> "Tu sesión expiró. Vuelve a iniciar sesión."
        403 -> "No tienes permiso para esta acción."
        404 -> "La cancha ya no existe."
        409 -> "Ya existe una cancha con ese nombre."
        else -> "Error del servidor (código ${e.code()})."
    }
    is java.net.UnknownHostException, is java.net.ConnectException ->
        "No se pudo conectar con el servidor de MrCanchas."
    is java.net.SocketTimeoutException ->
        "El servidor tardó demasiado en responder (Timeout)."
    else -> e.localizedMessage ?: "Ocurrió un error inesperado."
}

// Formulario reutilizable: crear (initial = null) o editar (initial = cancha)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CourtFormDialog(
    initial: Court?,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (CourtRequest) -> Unit
) {
    val isEdit = initial != null
    // Se reinicia si cambia la cancha objetivo (key = id)
    var name by remember(initial?.id) { mutableStateOf(initial?.name ?: "") }
    var sport by remember(initial?.id) { mutableStateOf(initial?.sport?.takeIf { it in SPORTS } ?: "") }
    var location by remember(initial?.id) { mutableStateOf(initial?.location ?: "") }
    var available by remember(initial?.id) { mutableStateOf(initial?.available ?: true) }
    var sportExpanded by remember { mutableStateOf(false) }
    var triedSubmit by remember(initial?.id) { mutableStateOf(false) }

    val nameTrim = name.trim()
    val locationTrim = location.trim()
    val nameError = triedSubmit && nameTrim.length < 3
    val locationError = triedSubmit && locationTrim.length < 3
    val sportError = triedSubmit && sport !in SPORTS
    val isValid = nameTrim.length in 3..100 &&
            locationTrim.length in 3..100 &&
            sport in SPORTS

    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        title = { Text(if (isEdit) "Editar cancha" else "Nueva cancha") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { if (it.length <= 100) name = it },
                    label = { Text("Nombre") },
                    singleLine = true,
                    isError = nameError,
                    supportingText = { if (nameError) Text("El nombre debe tener al menos 3 caracteres") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))

                ExposedDropdownMenuBox(
                    expanded = sportExpanded,
                    onExpandedChange = { sportExpanded = !sportExpanded }
                ) {
                    OutlinedTextField(
                        value = sport,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Deporte") },
                        isError = sportError,
                        supportingText = { if (sportError) Text("Selecciona un deporte") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = sportExpanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = sportExpanded,
                        onDismissRequest = { sportExpanded = false }
                    ) {
                        SPORTS.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    sport = option
                                    sportExpanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = location,
                    onValueChange = { if (it.length <= 100) location = it },
                    label = { Text("Ubicación") },
                    singleLine = true,
                    isError = locationError,
                    supportingText = { if (locationError) Text("La ubicación debe tener al menos 3 caracteres") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Disponible")
                    Spacer(Modifier.weight(1f))
                    Switch(checked = available, onCheckedChange = { available = it })
                }
            }
        },
        confirmButton = {
            Button(
                enabled = !isSaving,
                onClick = {
                    triedSubmit = true
                    if (isValid) {
                        onConfirm(
                            CourtRequest(
                                name = nameTrim,
                                sport = sport,
                                location = locationTrim,
                                available = available
                            )
                        )
                    }
                }
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(if (isEdit) "Guardar" else "Crear")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) {
                Text("Cancelar")
            }
        }
    )
}