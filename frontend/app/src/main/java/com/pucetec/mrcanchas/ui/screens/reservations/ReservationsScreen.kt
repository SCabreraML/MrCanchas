package com.pucetec.mrcanchas.ui.screens.reservations

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.pucetec.mrcanchas.models.Reservation
import com.pucetec.mrcanchas.services.RetrofitClient
import com.pucetec.mrcanchas.ui.components.ReservationCard
import kotlinx.coroutines.launch
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationsScreen(
    onNavigateToDetail: (Long) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var reservations by remember { mutableStateOf<List<Reservation>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var reservationToCancel by remember { mutableStateOf<Reservation?>(null) }
    var isCancelling by remember { mutableStateOf(false) }

    fun loadReservations() {
        isLoading = true
        errorMessage = null
        scope.launch {
            try {
                val api = RetrofitClient.getApiService(context)
                reservations = api.getMyReservations()
            } catch (e: Exception) {
                errorMessage = when (e) {
                    is UnknownHostException, is ConnectException -> {
                        "No se pudo conectar con el servidor de MrCanchas. Asegúrate de que el backend esté iniciado."
                    }
                    is SocketTimeoutException -> {
                        "El servidor tardó demasiado en responder (Timeout). Por favor, intenta de nuevo."
                    }
                    else -> {
                        e.localizedMessage ?: "Ocurrió un error inesperado al cargar tus reservas."
                    }
                }
            } finally {
                isLoading = false
            }
        }
    }

    fun submitCancel(reservation: Reservation) {
        isCancelling = true
        scope.launch {
            try {
                val response = RetrofitClient.getApiService(context).cancelReservation(reservation.id)
                if (response.isSuccessful) {
                    reservationToCancel = null
                    Toast.makeText(context, "Reserva cancelada", Toast.LENGTH_SHORT).show()
                    loadReservations()
                } else {
                    val msg = when (response.code()) {
                        401 -> "Tu sesión expiró. Vuelve a iniciar sesión."
                        403 -> "No tienes permiso para cancelar esta reserva."
                        404 -> "La reserva ya no existe."
                        409 -> "Esta reserva no se puede cancelar."
                        else -> "No se pudo cancelar la reserva (código ${response.code()})."
                    }
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, e.localizedMessage ?: "Error de red al cancelar.", Toast.LENGTH_LONG).show()
            } finally {
                isCancelling = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadReservations()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Mis Reservas",
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
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Error de Servidor",
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
                            onClick = { loadReservations() },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Reintentar")
                        }
                    }
                }
            } else if (reservations.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No tienes reservas registradas.",
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
                    items(reservations) { reservation ->
                        ReservationCard(
                            reservation = reservation,
                            onClick = { onNavigateToDetail(reservation.id) },
                            onCancel = { reservationToCancel = reservation }
                        )
                    }
                }
            }
        }
    }

    reservationToCancel?.let { reservation ->
        AlertDialog(
            onDismissRequest = { if (!isCancelling) reservationToCancel = null },
            title = { Text("Cancelar reserva") },
            text = { Text("¿Seguro que deseas cancelar la reserva #${reservation.id}?") },
            confirmButton = {
                Button(
                    enabled = !isCancelling,
                    onClick = { submitCancel(reservation) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    if (isCancelling) {
                        CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onError)
                    } else {
                        Text("Sí, cancelar")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { reservationToCancel = null }, enabled = !isCancelling) {
                    Text("No")
                }
            }
        )
    }
}