package com.pucetec.mrcanchas.ui.screens.match

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
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
                title = {
                    Text(
                        "Resultados de Partido",
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
                )
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
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "¡Marcador Final!",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(24.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = matchResult!!.teamA,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = matchResult!!.scoreA.toString(),
                                        style = MaterialTheme.typography.displayMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Text(
                                    text = "VS",
                                    style = MaterialTheme.typography.headlineLarge,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.Bold
                                )

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = matchResult!!.teamB,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = matchResult!!.scoreB.toString(),
                                        style = MaterialTheme.typography.displayMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(32.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(24.dp))

                            val resultMessage = if (!matchResult!!.winner.isNullOrBlank()) {
                                "Ganador: ${matchResult!!.winner}"
                            } else {
                                "¡Empate!"
                            }

                            Text(
                                text = resultMessage,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Jugado el: ${matchResult!!.playedAt}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
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
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = teamAName,
                        onValueChange = { teamAName = it },
                        label = { Text("Nombre del Equipo A") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = teamAScore,
                        onValueChange = { teamAScore = it },
                        label = { Text("Puntaje Equipo A") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = teamBName,
                        onValueChange = { teamBName = it },
                        label = { Text("Nombre del Equipo B") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = teamBScore,
                        onValueChange = { teamBScore = it },
                        label = { Text("Puntaje Equipo B") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(32.dp))

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
                                        Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                                    } finally {
                                        isSaving = false
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                "Guardar Resultados",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
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
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Sin Resultados Registrados",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Los resultados de este partido aún no han sido registrados por el Administrador.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}
