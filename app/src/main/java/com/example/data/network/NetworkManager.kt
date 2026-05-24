package com.example.data.network

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.data.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.concurrent.TimeUnit

// Retrofit API Service Interface
interface TeenlancerApiService {
    @GET("api/profiles")
    suspend fun getProfiles(): List<UserProfile>

    @PUT("api/profiles/{id}")
    suspend fun updateProfile(@Path("id") id: Int, @Body profile: UserProfile): UserProfile

    @GET("api/gigs")
    suspend fun getGigs(): List<Gig>

    @POST("api/gigs")
    suspend fun createGig(@Body gig: Gig): Gig

    @PUT("api/gigs/{id}")
    suspend fun updateGig(@Path("id") id: Int, @Body gig: Gig): Gig

    @Multipart
    @POST("api/gigs/{id}/upload-proof")
    suspend fun uploadPaymentProof(
        @Path("id") gigId: Int,
        @Part screenshot: MultipartBody.Part
    ): UploadProofResponse

    @GET("api/matchmaking")
    suspend fun getMatchmakingPosts(): List<MatchmakingPost>

    @POST("api/matchmaking")
    suspend fun createMatchmakingPost(@Body post: MatchmakingPost): MatchmakingPost

    @GET("api/matchmaking/{postId}/applications")
    suspend fun getApplications(@Path("postId") postId: Int): List<Application>

    @POST("api/matchmaking/{postId}/applications")
    suspend fun applyToPost(@Path("postId") postId: Int, @Body application: Application): Application

    @PUT("api/applications/{id}")
    suspend fun updateApplicationStatus(@Path("id") id: Int, @Body body: StatusUpdateBody): Application

    @GET("api/forum")
    suspend fun getThreads(): List<TechThread>

    @POST("api/forum")
    suspend fun createThread(@Body thread: TechThread): TechThread

    @GET("api/forum/{threadId}/replies")
    suspend fun getReplies(@Path("threadId") threadId: Int): List<ThreadReply>

    @POST("api/forum/{threadId}/replies")
    suspend fun createReply(@Path("threadId") threadId: Int, @Body reply: ThreadReply): ThreadReply

    @GET("api/reviews/{userId}")
    suspend fun getReviews(@Path("userId") userId: Int): List<Review>

    @POST("api/reviews")
    suspend fun createReview(@Body review: Review): Review

    @GET("api/notifications/{userId}")
    suspend fun getNotifications(@Path("userId") userId: Int): List<Notification>

    @POST("api/notifications")
    suspend fun createNotification(@Body notification: Notification): Notification

    @PUT("api/notifications/{userId}/read-all")
    suspend fun markAllNotificationsRead(@Path("userId") userId: Int)

    @PUT("api/notifications/{id}/read")
    suspend fun markNotificationRead(@Path("id") id: Int)
}

// Network Request / Response helper classes
data class UploadProofResponse(val proofUri: String)
data class StatusUpdateBody(val status: String)

object NetworkManager {
    private const val TAG = "NetworkManager"
    private var baseHttpUrl = "http://10.0.2.2:3000/" // Default local emulator loopback IP
    private var baseWsUrl = "ws://10.0.2.2:3000/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private var retrofit: Retrofit? = null
    var apiService: TeenlancerApiService? = null
        private set

    private var okHttpClient: OkHttpClient? = null
    private var webSocket: WebSocket? = null
    private var currentActiveUserIdForWs: Int? = null

    // Shared flow to publish WS notification events back to the UI ViewModel
    private val _realtimeNotificationFlow = MutableSharedFlow<Notification>(extraBufferCapacity = 64)
    val realtimeNotificationFlow: SharedFlow<Notification> = _realtimeNotificationFlow

    init {
        rebuildNetworkClients()
    }

    @Synchronized
    fun updateServerAddress(newAddress: String) {
        var cleanAddress = newAddress.trim()
        if (cleanAddress.isEmpty()) {
            retrofit = null
            apiService = null
            disconnectWebSocket()
            return
        }

        if (!cleanAddress.endsWith("/")) {
            cleanAddress += "/"
        }
        if (!cleanAddress.startsWith("http://") && !cleanAddress.startsWith("https://")) {
            cleanAddress = "http://$cleanAddress"
        }

        baseHttpUrl = cleanAddress
        baseWsUrl = cleanAddress
            .replace("http://", "ws://")
            .replace("https://", "wss://")

        Log.d(TAG, "Server Address updated: HTTP -> $baseHttpUrl, WS -> $baseWsUrl")
        rebuildNetworkClients()
    }

    fun getServerAddress(): String {
        return baseHttpUrl
    }

    fun isEnabled(): Boolean {
        return apiService != null
    }

    private fun rebuildNetworkClients() {
        try {
            okHttpClient = OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build()

            retrofit = Retrofit.Builder()
                .baseUrl(baseHttpUrl)
                .client(okHttpClient!!)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()

            apiService = retrofit!!.create(TeenlancerApiService::class.java)
            Log.d(TAG, "Rebuilt Retrofit clients successfully targeting: $baseHttpUrl")

            // Re-connect WS with updated credentials if user is logged in
            currentActiveUserIdForWs?.let { reconnectWebSocket(it) }
        } catch (e: Exception) {
            Log.e(TAG, "Failed rebuilding Retrofit client modules: ${e.message}")
            apiService = null
        }
    }

    // ---------------------------------------------------------
    // WEBSOCKET REAL-TIME ENGINE
    // ---------------------------------------------------------
    fun reconnectWebSocket(userId: Int) {
        currentActiveUserIdForWs = userId
        if (okHttpClient == null || baseWsUrl.isEmpty()) return

        disconnectWebSocket()

        val request = Request.Builder()
            .url(baseWsUrl)
            .build()

        webSocket = okHttpClient!!.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d(TAG, "WebSocket Channel Connected! Submitting registration map for User $userId")
                // Submit authentication packet
                val regMsg = """{"action":"register","userId":$userId}"""
                webSocket.send(regMsg)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d(TAG, "Incoming WS packet: $text")
                try {
                    // Quick inspection to see if it is a new notification broadcast
                    if (text.contains("new_notification")) {
                        // Extract notification object
                        val jsonAdapter = moshi.adapter(NotificationPacket::class.java)
                        val packet = jsonAdapter.fromJson(text)
                        if (packet != null && packet.data != null) {
                            CoroutineScope(Dispatchers.IO).launch {
                                _realtimeNotificationFlow.emit(packet.data)
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Websocket parsed error: ${e.message}")
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "WebSocket failure, will retry standard keepalives: ${t.message}")
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "WebSocket connection closed: $reason")
            }
        })
    }

    fun disconnectWebSocket() {
        webSocket?.close(1000, "User switched connections")
        webSocket = null
    }

    // Convert file Uri to MultipartBody.Part
    fun prepareScreenshotMultipart(context: Context, uri: Uri): MultipartBody.Part? {
        return try {
            val contentResolver = context.contentResolver
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val file = File(context.cacheDir, "fampay_receipt_${System.currentTimeMillis()}.png")
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()

            val requestBody = file.asRequestBody("image/png".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("screenshot", file.name, requestBody)
        } catch (e: Exception) {
            Log.e(TAG, "Could not extract uploaded URI to multipart representation: ${e.message}")
            null
        }
    }
}

// Helper class for parsing WebSocket objects
data class NotificationPacket(val action: String, val data: Notification?)
