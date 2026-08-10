package com.pucetec.mrcanchas.ui.screens.courts

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pucetec.mrcanchas.models.Court
import com.pucetec.mrcanchas.models.ReservationRequest
import com.pucetec.mrcanchas.models.TimeSlot
import com.pucetec.mrcanchas.services.RetrofitClient
import com.pucetec.mrcanchas.services.SessionManager
import com.pucetec.mrcanchas.ui.components.TimeSlotCard
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourtDetailScreen(
    courtId: Long,
    onBack: () -> Unit,
    onNavigateToMyReservations: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sessionManager = remember { SessionManager(context) }
    val isAdmin = sessionManager.isAdmin()

    var court by remember { mutableStateOf<Court?>(null) }
    var timeSlots by remember { mutableStateOf<List<TimeSlot>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    fun loadData() {
        isLoading = true
        scope.launch {
            try {
                val api = RetrofitClient.getApiService(context)
                court = api.getCourt(courtId)
                timeSlots = api.getTimeSlotsByCourt(courtId)
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(courtId) {
        loadData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        court?.name ?: "Detalles de Cancha",
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
            } else if (court == null) {
                Text(
                    text = "No se pudo cargar la cancha.",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Header / Court Info card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = court!!.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Deporte: ${court!!.sport}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Ubicación: ${court!!.location}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Horarios Disponibles",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    if (timeSlots.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No hay horarios configurados para esta cancha.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f)
                        ) {
                            items(timeSlots) { slot ->
                                TimeSlotCard(
                                    timeSlot = slot,
                                    showReserveButton = !isAdmin, // Only show reserve button to non-admins (users)
                                    onReserveClick = {
                                        scope.launch {
                                            try {
                                                val api = RetrofitClient.getApiService(context)
                                                api.createReservation(ReservationRequest(timeSlotId = slot.id))
                                                Toast.makeText(context, "¡Reserva realizada con éxito!", Toast.LENGTH_SHORT).show()
                                                onNavigateToMyReservations()
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Error al reservar: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
