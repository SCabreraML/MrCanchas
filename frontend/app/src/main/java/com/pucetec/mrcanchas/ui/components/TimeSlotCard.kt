package com.pucetec.mrcanchas.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pucetec.mrcanchas.models.TimeSlot

@Composable
fun TimeSlotCard(
    timeSlot: TimeSlot,
    isAdmin: Boolean = false,
    canReserve: Boolean = false,        // NUEVO — true para USER
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {},
    onReserve: () -> Unit = {},          // NUEVO
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = timeSlot.date,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                val (label, color) = when (timeSlot.status) {
                    "AVAILABLE" -> "Disponible" to Color(0xFF2E7D32)
                    "RESERVED" -> "Reservado" to Color(0xFFC62828)
                    "BLOCKED" -> "Bloqueado" to Color(0xFF616161)
                    else -> timeSlot.status to MaterialTheme.colorScheme.onSurfaceVariant
                }
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    color = color,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = "Horario",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${timeSlot.startTime.take(5)} - ${timeSlot.endTime.take(5)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Botones de ADMIN
            if (isAdmin) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar", modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Editar")
                    }
                    Spacer(Modifier.width(4.dp))
                    TextButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Eliminar", color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            // NUEVO — Botón de USER: solo si puede reservar y el horario está disponible
            if (canReserve && timeSlot.status == "AVAILABLE") {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onReserve,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Reservar")
                }
            }
        }
    }
}