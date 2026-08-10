package com.pucetec.mrcanchas.ui.screens.login

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
import com.pucetec.mrcanchas.models.UserProfileRequest
import com.pucetec.mrcanchas.services.CognitoAuthRequest
import com.pucetec.mrcanchas.services.CognitoRetrofitClient
import com.pucetec.mrcanchas.services.RetrofitClient
import com.pucetec.mrcanchas.services.SessionManager
import kotlinx.coroutines.launch

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
    var nameInput by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf("") }
    var phoneInput by remember { mutableStateOf("") }
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
            text = "Bienvenido a MrCanchas",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Login con Cognito (USER_PASSWORD_AUTH)",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = usernameInput,
            onValueChange = { usernameInput = it },
            label = { Text("Usuario o Email de Cognito") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 1
        )

        Spacer(modifier = Modifier.height(8.dp))

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

        Text(
            text = "Datos de Perfil (Para sincronizar con Backend)",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = nameInput,
            onValueChange = { nameInput = it },
            label = { Text("Nombre Completo") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = emailInput,
            onValueChange = { emailInput = it },
            label = { Text("Correo Electrónico") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = phoneInput,
            onValueChange = { phoneInput = it },
            label = { Text("Teléfono") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
        )

        Spacer(modifier = Modifier.height(8.dp))

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

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = {
                    if (usernameInput.isBlank() || passwordInput.isBlank() || nameInput.isBlank() || emailInput.isBlank() || phoneInput.isBlank()) {
                        Toast.makeText(context, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show()
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

                            val token = authResponse.authenticationResult?.accessToken
                            if (token.isNullOrEmpty()) {
                                throw Exception("No se recibió el token de acceso de Cognito.")
                            }

                            // 2. Save session credentials
                            sessionManager.saveToken(token)
                            sessionManager.saveAdminStatus(isAdminChecked)

                            // 3. Register or Sync user profile with our backend
                            val backendApi = RetrofitClient.getApiService(context)
                            try {
                                val profileResponse = backendApi.createMyProfile(
                                    UserProfileRequest(
                                        name = nameInput,
                                        email = emailInput,
                                        phone = phoneInput
                                    )
                                )
                                sessionManager.saveUserProfile(
                                    profileResponse.name,
                                    profileResponse.email,
                                    profileResponse.phone
                                )
                                Toast.makeText(context, "¡Sesión iniciada y perfil registrado!", Toast.LENGTH_SHORT).show()
                                onLoginSuccess()
                            } catch (eProfile: Exception) {
                                // If profile already existed on backend, try to fetch it
                                try {
                                    val existingProfile = backendApi.getMyProfile()
                                    sessionManager.saveUserProfile(
                                        existingProfile.name,
                                        existingProfile.email,
                                        existingProfile.phone
                                    )
                                    Toast.makeText(context, "Bienvenido de vuelta, ${existingProfile.name}!", Toast.LENGTH_SHORT).show()
                                    onLoginSuccess()
                                } catch (eGet: Exception) {
                                    Toast.makeText(context, "Error al sincronizar perfil: ${eGet.localizedMessage}", Toast.LENGTH_LONG).show()
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
