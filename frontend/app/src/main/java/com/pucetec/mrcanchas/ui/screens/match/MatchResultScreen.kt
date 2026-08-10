package com.pucetec.mrcanchas.ui.screens.match

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.pucetec.mrcanchas.models.MatchResult
import com.pucetec.mrcanchas.models.MatchResultRequest
import com.pucetec.mrcanchas.models.TeamScoreRequest
import com.pucetec.mrcanchas.services.RetrofitClient
import com.pucetec.mrcanchas.services.SessionManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchResultScreen(
    reservationId: Long,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sessionManager = remember { SessionManager(context) }
    val isAdmin = sessionManager.isAdmin()

    var matchResult by remember { mutableStateOf<MatchResult?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // Inputs for ADMIN to create match result
    var teamAName by remember { mutableStateOf("") }
    var teamBName by remember { mutableStateOf("") }
    var teamAScore by remember { mutableStateOf("") }
    var teamBScore by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    fun loadMatchResult() {
        isLoading = true
        scope.launch {
            try {
                val api = RetrofitClient.getApiService(context)
                matchResult = api.getMatchResult(reservationId)
            } catch (e: Exception) {
                // If not found, it means result hasn't been posted yet
                matchResult = null
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(reservationId) {
        loadMatchResult()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Resultados del Partido") },
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
            } else if (matchResult != null) {
                // Display existing results
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "¡Marcador Final!",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = matchResult!!.teamA, style = MaterialTheme.typography.titleLarge)
                                    Text(text = matchResult!!.scoreA.toString(), style = MaterialTheme.typography.displayMedium)
                                }

                                Text(text = "VS", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary)

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = matchResult!!.teamB, style = MaterialTheme.typography.titleLarge)
                                    Text(text = matchResult!!.scoreB.toString(), style = MaterialTheme.typography.displayMedium)
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            if (!matchResult!!.winner.isNullOrBlank()) {
                                Text(
                                    text = "Ganador: ${matchResult!!.winner}",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Text(
                                    text = "¡Empate!",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Jugado en: ${matchResult!!.playedAt}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else if (isAdmin) {
                // Form for admin to create match result
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Top
                ) {
                    Text(
                        text = "Registrar Resultados (ADMIN)",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = teamAName,
                            onValueChange = { teamAName = it },
                            label = { Text("Nombre Equipo A") },
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = teamAScore,
                            onValueChange = { teamAScore = it },
                            label = { Text("Puntaje") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(0.5f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = teamBName,
                            onValueChange = { teamBName = it },
                            label = { Text("Nombre Equipo B") },
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = teamBScore,
                            onValueChange = { teamBScore = it },
                            label = { Text("Puntaje") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(0.5f)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    if (isSaving) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    } else {
                        Button(
                            onClick = {
                                val scoreA = teamAScore.toIntOrNull()
                                val scoreB = teamBScore.toIntOrNull()
                                if (teamAName.isBlank() || teamBName.isBlank() || scoreA == null || scoreB == null) {
                                    Toast.makeText(context, "Ingrese nombres y puntajes válidos", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }

                                val winner = when {
                                    scoreA > scoreB -> teamAName
                                    scoreB > scoreA -> teamBName
                                    else -> null
                                }

                                isSaving = true
                                scope.launch {
                                    try {
                                        val api = RetrofitClient.getApiService(context)
                                        val isoDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).apply {
                                            timeZone = TimeZone.getTimeZone("UTC")
                                        }.format(Date())

                                        api.createMatchResult(
                                            reservationId = reservationId,
                                            request = MatchResultRequest(
                                                teams = listOf(
                                                    TeamScoreRequest(teamAName, scoreA),
                                                    TeamScoreRequest(teamBName, scoreB)
                                                ),
                                                winner = winner,
                                                status = "FINISHED",
                                                playedAt = isoDate
                                            )
                                        )
                                        Toast.makeText(context, "Resultados registrados con éxito", Toast.LENGTH_SHORT).show()
                                        loadMatchResult()
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Error al guardar resultados: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                                    } finally {
                                        isSaving = false
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Guardar Resultados")
                        }
                    }
                }
            } else {
                // Not admin, and match has no results yet
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Los resultados de este partido aún no han sido registrados.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Por favor, espera a que el Administrador registre el marcador.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}
