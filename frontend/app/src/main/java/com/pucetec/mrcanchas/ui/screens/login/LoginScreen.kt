package com.pucetec.mrcanchas.ui.screens.login

import android.util.Base64
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
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

    // Constants for our new pool
    val clientId = "5n067t1f01s9pn6f6a0qbpmamf"

    var usernameInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var isAdminChecked by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "MrCanchas",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Inicia sesión con tu cuenta de Cognito",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = usernameInput,
            onValueChange = { usernameInput = it },
            label = { Text("Usuario o Correo Electrónico") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 1
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = passwordInput,
            onValueChange = { passwordInput = it },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            maxLines = 1
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isAdminChecked,
                onCheckedChange = { isAdminChecked = it }
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Simular Rol de ADMIN")
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (isLoading) {
            CircularProgressIndicator()
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
                            // 1. Authenticate with AWS Cognito using USER_PASSWORD_AUTH
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

                            // 2. Save session credentials
                            sessionManager.saveToken(accessToken)
                            sessionManager.saveAdminStatus(isAdminChecked)

                            // 3. Extract user details from Cognito tokens
                            val claims = decodeJwtPayload(idToken ?: accessToken)
                            val name = claims?.get("name") as? String
                                ?: claims?.get("cognito:username") as? String
                                ?: usernameInput
                            val email = claims?.get("email") as? String
                                ?: if (usernameInput.contains("@")) usernameInput else ""
                            val phone = claims?.get("phone_number") as? String
                                ?: claims?.get("phone") as? String
                                ?: ""

                            // 4. Check or register user profile with our backend automatically
                            val backendApi = RetrofitClient.getApiService(context)
                            try {
                                // Try fetching existing profile
                                val profile = backendApi.getMyProfile()
                                sessionManager.saveUserProfile(
                                    profile.name,
                                    profile.email,
                                    profile.phone
                                )
                                Toast.makeText(context, "¡Bienvenido de vuelta, ${profile.name}!", Toast.LENGTH_SHORT).show()
                                onLoginSuccess()
                            } catch (eGet: Exception) {
                                // If profile not found (or another networking issue), try creating it automatically
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
                                    Toast.makeText(context, "¡Sesión iniciada y perfil registrado!", Toast.LENGTH_SHORT).show()
                                    onLoginSuccess()
                                } catch (eCreate: Exception) {
                                    // If profile creation failed, fallback to login anyway but notify
                                    sessionManager.saveUserProfile(name, email, phone)
                                    Toast.makeText(context, "Sesión iniciada (Perfil offline)", Toast.LENGTH_SHORT).show()
                                    onLoginSuccess()
                                }
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error de Autenticación: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Iniciar Sesión")
            }
        }
    }
}
