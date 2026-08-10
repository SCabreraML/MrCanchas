package com.pucetec.mrcanchas.services

import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

// Cognito Authentication Requests Models
data class CognitoAuthRequest(
    @SerializedName("AuthFlow") val authFlow: String = "USER_PASSWORD_AUTH",
    @SerializedName("ClientId") val clientId: String,
    @SerializedName("AuthParameters") val authParameters: Map<String, String>
)

data class CognitoAuthResult(
    @SerializedName("AccessToken") val accessToken: String,
    @SerializedName("ExpiresIn") val expiresIn: Int,
    @SerializedName("IdToken") val idToken: String?,
    @SerializedName("RefreshToken") val refreshToken: String?,
    @SerializedName("TokenType") val tokenType: String?
)

data class CognitoAuthResponse(
    @SerializedName("AuthenticationResult") val authenticationResult: CognitoAuthResult?,
    @SerializedName("ChallengeName") val challengeName: String?,
    @SerializedName("Session") val session: String?
)

// Cognito API Service
interface CognitoApiService {
    @POST("/")
    @Headers(
        "Content-Type: application/x-amz-json-1.1",
        "X-Amz-Target: AWSCognitoIdentityProviderService.InitiateAuth"
    )
    suspend fun initiateAuth(
        @Body request: CognitoAuthRequest
    ): CognitoAuthResponse
}

// Cognito Retrofit Client
object CognitoRetrofitClient {
    private const val COGNITO_BASE_URL = "https://cognito-idp.us-east-1.amazonaws.com/"

    private var cognitoService: CognitoApiService? = null

    fun getApiService(): CognitoApiService {
        if (cognitoService == null) {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(COGNITO_BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            cognitoService = retrofit.create(CognitoApiService::class.java)
        }
        return cognitoService!!
    }
}
