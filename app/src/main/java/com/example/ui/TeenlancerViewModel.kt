package com.example.ui

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.data.network.NetworkManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TeenlancerViewModel(private val repository: TeenlancerRepository) : ViewModel() {

    // Active User Selection (defaults to User 1 Aarav)
    private val _activeUserId = MutableStateFlow(1)
    val activeUserId: StateFlow<Int> = _activeUserId.asStateFlow()

    // Observe active user profile dynamically
    val activeProfile: StateFlow<UserProfile?> = _activeUserId
        .flatMapLatest { id -> repository.getProfileById(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Observe all user profiles
    val allProfiles: StateFlow<List<UserProfile>> = repository.allProfiles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Real Auth & Session State (defaults to 1 for backward compatibility)
    private val _loggedInUserId = MutableStateFlow<Int?>(1)
    val loggedInUserId: StateFlow<Int?> = _loggedInUserId.asStateFlow()

    // Login logic
    suspend fun loginUser(username: String, pass: String): Boolean {
        val user = repository.allProfiles.first().find { it.username.trim().lowercase() == username.trim().lowercase() }
        if (user != null && user.password == pass) {
            _activeUserId.value = user.id
            _loggedInUserId.value = user.id
            _toastMessage.emit("Welcome back, ${user.name}!")
            return true
        }
        _toastMessage.emit("Invalid username or password.")
        return false
    }

    // Register active user
    suspend fun registerUser(
        username: String,
        name: String,
        bio: String,
        location: String,
        pass: String,
        fampayUpi: String?,
        targetGoal: Int
    ): Boolean {
        val currentList = repository.allProfiles.first()
        if (currentList.any { it.username.trim().lowercase() == username.trim().lowercase() }) {
            _toastMessage.emit("Username @$username already exists!")
            return false
        }
        val newUser = UserProfile(
            username = username.trim(),
            name = name.trim(),
            bio = bio.trim(),
            location = location.trim(),
            password = pass,
            fampayUpi = fampayUpi?.trim(),
            targetGoalGigs = targetGoal,
            score = 100, // starting credit
            streak = 1,
            rank = "Global Novice",
            badges = "🌍 Global Teenlancer"
        )
        repository.insertProfile(newUser)
        val updatedList = repository.allProfiles.first()
        val created = updatedList.find { it.username.trim() == username.trim() }
        if (created != null) {
            _activeUserId.value = created.id
            _loggedInUserId.value = created.id
            _toastMessage.emit("Registered successfully as @${created.username}!")
            return true
        }
        return false
    }

    fun logout() {
        _loggedInUserId.value = null
        viewModelScope.launch {
            _toastMessage.emit("Logged out successfully.")
        }
    }

    // UI Toast System Integration
    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage: SharedFlow<String> = _toastMessage.asSharedFlow()

    // Filter states
    val selectedGigCategory = MutableStateFlow("All")
    val selectedForumCategory = MutableStateFlow("All")

    // Dynamic Lists
    val gigs: StateFlow<List<Gig>> = combine(
        repository.allGigs,
        selectedGigCategory
    ) { list, category ->
        if (category == "All") list else list.filter { it.category == category }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val matchmakingPosts: StateFlow<List<MatchmakingPost>> = repository.allMatchmakingPosts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val techThreads: StateFlow<List<TechThread>> = combine(
        repository.allThreads,
        selectedForumCategory
    ) { list, category ->
        if (category == "All") list else list.filter { it.category == category }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active user specific lists
    val activeNotifications: StateFlow<List<Notification>> = _activeUserId
        .flatMapLatest { id -> repository.getNotificationsForUser(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeUnreadNotificationCount: StateFlow<Int> = _activeUserId
        .flatMapLatest { id -> repository.getUnreadNotificationCount(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val activeReviews: StateFlow<List<Review>> = _activeUserId
        .flatMapLatest { id -> repository.getReviewsForUser(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Seed initial data if database is empty on start
        viewModelScope.launch {
            repository.seedIfNeeded()
        }

        // Sync data from network on start
        viewModelScope.launch {
            repository.trySyncFromNetwork()
        }

        // Listen to WebSocket notification events and commit them back to user's Room
        viewModelScope.launch {
            NetworkManager.realtimeNotificationFlow.collect { notification ->
                if (notification.receiverId == _activeUserId.value) {
                    repository.insertNotificationLocally(notification)
                    _toastMessage.emit("🔔 Real-Time Alert: ${notification.title}")
                }
            }
        }

        // Reconnect WebSockets automatically when active user is switched
        viewModelScope.launch {
            _activeUserId.collect { id ->
                if (NetworkManager.isEnabled()) {
                    NetworkManager.reconnectWebSocket(id)
                }
            }
        }

        // Set up real-time notification alert toast trigger
        viewModelScope.launch {
            var firstRun = true
            _activeUserId.flatMapLatest { id -> repository.getNotificationsForUser(id) }
                .collect { notifications ->
                    if (firstRun) {
                        firstRun = false
                        return@collect
                    }
                    val unreadNotifications = notifications.filter { !it.isRead }
                    if (unreadNotifications.isNotEmpty()) {
                        val latest = unreadNotifications.first()
                        _toastMessage.emit("${latest.senderName}: ${latest.message}")
                    }
                }
        }
    }

    // Server IP Config Setup for emulators / production
    fun updateServerAddress(address: String) {
        viewModelScope.launch {
            NetworkManager.updateServerAddress(address)
            if (NetworkManager.isEnabled()) {
                NetworkManager.reconnectWebSocket(_activeUserId.value)
                repository.trySyncFromNetwork()
                _toastMessage.emit("Target Server Updated to: ${NetworkManager.getServerAddress()}")
            } else {
                _toastMessage.emit("Server synchronization disabled.")
            }
        }
    }

    fun getServerAddress(): String {
        return NetworkManager.getServerAddress()
    }

    fun triggerServerManualSync() {
        viewModelScope.launch {
            repository.trySyncFromNetwork()
            _toastMessage.emit("Data synced with server successfully!")
        }
    }

    // Toggle/Switch active workspace user profile
    fun switchActiveUser(id: Int) {
        viewModelScope.launch {
            _activeUserId.value = id
            val profiles = allProfiles.value
            val switchedProfileName = profiles.find { it.id == id }?.name ?: "User"
            _toastMessage.emit("Switched workspace to $switchedProfileName")
        }
    }

    // Update Seller FamPay Checkout Profile
    fun updateSellerPaymentInfo(upiId: String, qrUri: String?) {
        viewModelScope.launch {
            val profile = activeProfile.value ?: return@launch
            val updated = profile.copy(
                fampayUpi = upiId.trim(),
                fampayQrUri = qrUri ?: "simulated_qr_uploaded"
            )
            repository.updateProfile(updated)
            _toastMessage.emit("FamPay UPI ID and QR Code updated successfully!")
        }
    }

    // Post custom Gig
    fun postGig(title: String, description: String, budget: Int, category: String) {
        viewModelScope.launch {
            val p = activeProfile.value ?: return@launch
            val newGig = Gig(
                title = title,
                description = description,
                budget = budget,
                category = category,
                creatorId = p.id,
                creatorName = p.name,
                status = "Open"
            )
            repository.createGig(newGig)
            _toastMessage.emit("Gig uploaded successfully!")
        }
    }

    // Buyer Checkout Modal -> manual submit
    fun submitPaymentProof(context: Context, gigId: Int, proofUri: String, imageUri: Uri? = null) {
        viewModelScope.launch {
            val activeId = activeUserId.value
            val pFromState = activeProfile.value
            val pFromDb = repository.getProfileByIdOneShot(activeId)
            println("DEBUG: submitPaymentProof: activeId=$activeId, pFromState=$pFromState, pFromDb=$pFromDb")
            val p = pFromState ?: pFromDb ?: run {
                println("DEBUG: submitPaymentProof: RETURNING EARLY because profile is null!")
                return@launch
            }
            repository.submitPaymentProof(context, gigId, p, proofUri, imageUri)
            _toastMessage.emit("Transaction Proof uploaded! Seller has been notified.")
        }
    }

    // Seller approves Manual Payment
    fun approvePaymentProof(gigId: Int) {
        viewModelScope.launch {
            // Check who owns the Gig
            val gig = repository.getGigById(gigId).first() ?: return@launch
            val sellerId = gig.creatorId
            if (sellerId != _activeUserId.value) {
                _toastMessage.emit("Only the seller (${gig.creatorName}) can approve this payment!")
                return@launch
            }
            repository.approvePaymentProof(gigId)
            _toastMessage.emit("Payment confirmed! Work marked completed & credits awarded.")
        }
    }

    // Seller declines payment proof
    fun declinePaymentProof(gigId: Int, reason: String) {
        viewModelScope.launch {
            val gig = repository.getGigById(gigId).first() ?: return@launch
            val sellerId = gig.creatorId
            if (sellerId != _activeUserId.value) {
                _toastMessage.emit("Only the seller (${gig.creatorName}) can decline this payment!")
                return@launch
            }
            repository.declinePaymentProof(gigId, reason)
            _toastMessage.emit("Payment proof declined. Buyer will be notified.")
        }
    }

    // Create Matchmaking Post
    fun postMatchmakingPost(title: String, description: String, role: String) {
        viewModelScope.launch {
            val p = activeProfile.value ?: return@launch
            val post = MatchmakingPost(
                title = title,
                description = description,
                roleRequired = role,
                creatorId = p.id,
                creatorName = p.name
            )
            repository.createMatchmakingPost(post)
            _toastMessage.emit("Matchmaking post created successfully!")
        }
    }

    // Apply to Co-Founder matchmaking post
    fun applyToMatchmakingPost(postId: Int, postTitle: String, pitch: String) {
        viewModelScope.launch {
            val p = activeProfile.value ?: return@launch
            repository.applyToMatchmakingPost(postId, postTitle, p, pitch)
            _toastMessage.emit("Application submitted! Co-Founder founder notified.")
        }
    }

    // Handle application accept or decline
    fun updateApplicationStatus(applicationId: Int, accept: Boolean) {
        viewModelScope.launch {
            repository.updateApplicationStatus(applicationId, accept)
            val text = if (accept) "Application accepted!" else "Application declined."
            _toastMessage.emit(text)
        }
    }

    // Create Tech Thread
    fun createTechThread(title: String, content: String, category: String, imageUri: String?) {
        viewModelScope.launch {
            val p = activeProfile.value ?: return@launch
            val newThread = TechThread(
                title = title,
                content = content,
                category = category,
                creatorId = p.id,
                creatorName = p.name,
                imageUri = imageUri
            )
            repository.createThread(newThread)
            _toastMessage.emit("Forum Thread posted!")
        }
    }

    // Reply to Tech Thread
    fun replyToTechThread(threadId: Int, text: String) {
        viewModelScope.launch {
            val p = activeProfile.value ?: return@launch
            repository.createReply(threadId, p, text)
            _toastMessage.emit("Reply posted to discussion board!")
        }
    }

    // Fetch replies for a specific thread
    fun getRepliesForThread(threadId: Int): Flow<List<ThreadReply>> {
        return repository.getRepliesForThread(threadId)
    }

    // Submit user Review
    fun submitReview(targetUserId: Int, rating: Float, comment: String, gigId: Int, gigTitle: String) {
        viewModelScope.launch {
            val p = activeProfile.value ?: return@launch
            val review = Review(
                reviewerId = p.id,
                reviewerName = p.name,
                targetUserId = targetUserId,
                gigId = gigId,
                gigTitle = gigTitle,
                rating = rating,
                comment = comment
            )
            repository.insertReview(review)
            _toastMessage.emit("Rating & Feedback submitted successfully!")
        }
    }

    // Trigger explicit Daily streak increments for fun
    fun triggerDailyStreakProgress() {
        viewModelScope.launch {
            val id = _activeUserId.value
            repository.incrementStreak(id)
            _toastMessage.emit("Awesome! Continued daily progress streak!")
        }
    }

    // Mark notifications as read
    fun markAllNotificationsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsRead(_activeUserId.value)
        }
    }

    fun markNotificationRead(notificationId: Int) {
        viewModelScope.launch {
            repository.markNotificationRead(notificationId)
        }
    }
}
