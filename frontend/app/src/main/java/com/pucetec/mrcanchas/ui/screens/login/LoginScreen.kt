package com.pucetec.mrcanchas.ui.screens.login

import android.util.Base64
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.pucetec.mrcanchas.models.UserProfileRequest
import com.pucetec.mrcanchas.services.CognitoAuthRequest
import com.pucetec.mrcanchas.services.CognitoRetrofitClient
import com.pucetec.mrcanchas.services.RetrofitClient
import com.pucetec.mrcanchas.services.SessionManager
import kotlinx.coroutines.launch
import org.json.JSONObject

// Helper function to safely decode JWT payload to a Map
fun decodeJwtPayload(token: String): Map<String, Any>? {
    val parts = token.split(".")
    if (parts.size < 2) return null
    return try {
        val payloadBytes = Base64.decode(parts[1], Base64.DEFAULT)
        val payloadString = String(payloadBytes, Charsets.UTF_8)
        val jsonObject = JSONObject(payloadString)
        val map = mutableMapOf<String, Any>()
        val keys = jsonObject.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            map[key] = jsonObject.get(key)
        }
        map
    } catch (e: Exception) {
        null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sessionManager = remember { SessionManager(context) }

    val clientId = "5n067t1f01s9pn6f6a0qbpmamf"

    var usernameInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.background
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "MrCanchas",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Ingresa tus credenciales de Cognito",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(28.dp))

                OutlinedTextField(
                    value = usernameInput,
                    onValueChange = { usernameInput = it },
                    label = { Text("Usuario o Email") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Email, contentDescription = "Email Icon")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = { passwordInput = it },
                    label = { Text("Contraseña") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = "Password Icon")
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (isLoading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                } else {
                    Button(
                        onClick = {
                            if (usernameInput.isBlank() || passwordInput.isBlank()) {
                                Toast.makeText(context, "Por favor ingrese usuario y contraseña", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            isLoading = true

                            scope.launch {
                                try {
                                    // 1. Authenticate against AWS Cognito
                                    val cognitoApi = CognitoRetrofitClient.getApiService()
                                    val authResponse = cognitoApi.initiateAuth(
                                        CognitoAuthRequest(
                                            clientId = clientId,
                                            authParameters = mapOf(
                                                "USERNAME" to usernameInput,
                                                "PASSWORD" to passwordInput
                                            )
                                        )
                                    )

                                    val accessToken = authResponse.authenticationResult?.accessToken
                                    val idToken = authResponse.authenticationResult?.idToken
                                    if (accessToken.isNullOrEmpty()) {
                                        throw Exception("No se recibió el token de acceso de Cognito.")
                                    }

                                    // 2. Decode claims to get Cognito user profile & check groups for admin
                                    val claims = decodeJwtPayload(idToken ?: accessToken)
                                    val groupsArray = claims?.get("cognito:groups") as? org.json.JSONArray
                                    var isAdmin = false
                                    if (groupsArray != null) {
                                        for (i in 0 until groupsArray.length()) {
                                            if (groupsArray.optString(i) == "ADMIN") {
                                                isAdmin = true
                                                break
                                            }
                                        }
                                    }

                                    // 3. Save session credentials
                                    sessionManager.clearSession()
                                    sessionManager.saveToken(accessToken)
                                    sessionManager.saveAdminStatus(isAdmin)
                                    sessionManager.saveGuestStatus(false)

                                    val name = claims?.get("name") as? String
                                        ?: claims?.get("cognito:username") as? String
                                        ?: usernameInput
                                    val email = claims?.get("email") as? String
                                        ?: if (usernameInput.contains("@")) usernameInput else ""
                                    val phone = claims?.get("phone_number") as? String
                                        ?: claims?.get("phone") as? String
                                        ?: ""

                                    // 4. Sync profile with backend
                                    val backendApi = RetrofitClient.getApiService(context)
                                    try {
                                        val profile = backendApi.getMyProfile()
                                        sessionManager.saveUserProfile(
                                            profile.name,
                                            profile.email,
                                            profile.phone
                                        )
                                        Toast.makeText(context, "¡Bienvenido, ${profile.name}!", Toast.LENGTH_SHORT).show()
                                        onLoginSuccess()
                                    } catch (eGet: Exception) {
                                        try {
                                            val newProfile = backendApi.createMyProfile(
                                                UserProfileRequest(
                                                    name = name,
                                                    email = email,
                                                    phone = phone
                                                )
                                            )
                                            sessionManager.saveUserProfile(
                                                newProfile.name,
                                                newProfile.email,
                                                newProfile.phone
                                            )
                                            Toast.makeText(context, "Sesión iniciada con éxito!", Toast.LENGTH_SHORT).show()
                                            onLoginSuccess()
                                        } catch (eCreate: Exception) {
                                            sessionManager.saveUserProfile(name, email, phone)
                                            Toast.makeText(context, "Sesión iniciada (Perfil temporal)", Toast.LENGTH_SHORT).show()
                                            onLoginSuccess()
                                        }
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "Iniciar Sesión",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Public/Guest Login Button
                    OutlinedButton(
                        onClick = {
                            sessionManager.clearSession()
                            sessionManager.saveGuestStatus(true)
                            sessionManager.saveUserProfile("Invitado", "Acceso Público", "")
                            Toast.makeText(context, "Ingresaste como Invitado", Toast.LENGTH_SHORT).show()
                            onLoginSuccess()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = "Entrar como Invitado",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
