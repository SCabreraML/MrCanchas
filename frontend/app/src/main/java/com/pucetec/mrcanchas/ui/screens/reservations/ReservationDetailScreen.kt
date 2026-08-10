package com.pucetec.mrcanchas.ui.screens.reservations

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.pucetec.mrcanchas.models.Reservation
import com.pucetec.mrcanchas.services.RetrofitClient
import com.pucetec.mrcanchas.services.SessionManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationDetailScreen(
    reservationId: Long,
    onBack: () -> Unit,
    onNavigateToMatchResult: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sessionManager = remember { SessionManager(context) }
    var reservation by remember { mutableStateOf<Reservation?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    fun loadReservation() {
        isLoading = true
        scope.launch {
            try {
                val api = RetrofitClient.getApiService(context)
                reservation = api.getReservation(reservationId)
            } catch (e: Exception) {
                Toast.makeText(context, "Error al cargar reserva: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(reservationId) {
        loadReservation()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reserva #${reservationId}") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (reservation == null) {
                Text(
                    text = "No se pudo cargar la reserva.",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = "Reserva Detalles",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("ID: ${reservation!!.id}")
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Fecha de creación: ${reservation!!.createdAt}")
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Inicio: ${reservation!!.startDateTime}")
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Fin: ${reservation!!.endDateTime}")
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Estado: ${reservation!!.status}")
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Organizador: ${reservation!!.ownerUser}")
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = { onNavigateToMatchResult(reservationId) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text("Ver / Registrar Resultados")
                    }

                    if (reservation!!.status.uppercase() != "CANCELLED" && reservation!!.status.uppercase() != "CANCELADA") {
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    try {
                                        val api = RetrofitClient.getApiService(context)
                                        api.cancelReservation(reservationId)
                                        Toast.makeText(context, "Reserva cancelada correctamente", Toast.LENGTH_SHORT).show()
                                        onBack()
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Error al cancelar: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Cancelar Reserva")
                        }
                    }
                }
            }
        }
    }
}
