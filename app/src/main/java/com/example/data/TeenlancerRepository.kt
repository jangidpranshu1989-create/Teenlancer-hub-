package com.example.data

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.data.network.NetworkManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TeenlancerRepository(private val dao: TeenlancerDao) {

    // Profiles
    val allProfiles: Flow<List<UserProfile>> = dao.getAllProfiles()
    
    fun getProfileById(id: Int): Flow<UserProfile?> = dao.getProfileById(id)
    
    suspend fun getProfileByIdOneShot(id: Int): UserProfile? = dao.getProfileByIdOneShot(id)
    
    suspend fun insertProfile(profile: UserProfile) {
        dao.insertProfile(profile)
    }

    suspend fun updateProfile(profile: UserProfile) {
        dao.updateProfile(profile)
        if (NetworkManager.isEnabled()) {
            withContext(Dispatchers.IO) {
                try {
                    NetworkManager.apiService?.updateProfile(profile.id, profile)
                } catch (e: Exception) {
                    Log.e("NetworkSync", "updateProfile REST error: ${e.message}")
                }
            }
        }
    }

    // Gigs
    val allGigs: Flow<List<Gig>> = dao.getAllGigs()
    
    fun getGigById(id: Int): Flow<Gig?> = dao.getGigById(id)
    
    suspend fun createGig(gig: Gig) {
        dao.insertGig(gig)
        if (NetworkManager.isEnabled()) {
            withContext(Dispatchers.IO) {
                try {
                    NetworkManager.apiService?.createGig(gig)
                } catch (e: Exception) {
                    Log.e("NetworkSync", "createGig REST error: ${e.message}")
                }
            }
        }
    }

    // Matchmaking Posts
    val allMatchmakingPosts: Flow<List<MatchmakingPost>> = dao.getAllMatchmakingPosts()
    
    suspend fun createMatchmakingPost(post: MatchmakingPost) {
        dao.insertMatchmakingPost(post)
        if (NetworkManager.isEnabled()) {
            withContext(Dispatchers.IO) {
                try {
                    NetworkManager.apiService?.createMatchmakingPost(post)
                } catch (e: Exception) {
                    Log.e("NetworkSync", "createMatchmakingPost REST error: ${e.message}")
                }
            }
        }
    }

    // Co-founder Applications
    fun getApplicationsByPostId(postId: Int): Flow<List<Application>> = dao.getApplicationsByPostId(postId)

    suspend fun applyToMatchmakingPost(
        postId: Int,
        postTitle: String,
        applicant: UserProfile,
        pitch: String
    ) {
        // Find creatorId
        val post = dao.getMatchmakingPostByIdOneShot(postId) ?: return
        
        // Insert application
        val application = Application(
            postId = postId,
            postTitle = postTitle,
            applicantId = applicant.id,
            applicantName = applicant.name,
            pitch = pitch,
            status = "Pending"
        )
        dao.insertApplication(application)

        // Notify creator
        val notification = Notification(
            receiverId = post.creatorId,
            senderId = applicant.id,
            senderName = applicant.name,
            title = "New Team Application!",
            message = "applied to your team post: \"$postTitle\". Tap to check their pitch!",
            type = "COFOUNDER_APPLY",
            relatedId = postId
        )
        dao.insertNotification(notification)

        if (NetworkManager.isEnabled()) {
            withContext(Dispatchers.IO) {
                try {
                    NetworkManager.apiService?.applyToPost(postId, application)
                    NetworkManager.apiService?.createNotification(notification)
                } catch (e: Exception) {
                    Log.e("NetworkSync", "applyToMatchmakingPost REST error: ${e.message}")
                }
            }
        }
    }

    suspend fun updateApplicationStatus(applicationId: Int, accept: Boolean) {
        val app = dao.getApplicationByIdOneShot(applicationId) ?: return
        val newStatus = if (accept) "Accepted" else "Declined"
        val updatedApp = app.copy(status = newStatus)
        dao.updateApplication(updatedApp)

        // Find post creator
        val post = dao.getMatchmakingPostByIdOneShot(app.postId) ?: return

        // Notify applicant
        val notification = Notification(
            receiverId = app.applicantId,
            senderId = post.creatorId,
            senderName = post.creatorName,
            title = if (accept) "Application Accepted! 🎉" else "Application Declined",
            message = if (accept) {
                "accepted your application for \"${post.title}\". Match made!"
            } else {
                "declined your application for \"${post.title}\"."
            },
            type = "STATUS_UPDATE",
            relatedId = post.id
        )
        dao.insertNotification(notification)

        if (NetworkManager.isEnabled()) {
            withContext(Dispatchers.IO) {
                try {
                    NetworkManager.apiService?.updateApplicationStatus(applicationId, com.example.data.network.StatusUpdateBody(newStatus))
                    NetworkManager.apiService?.createNotification(notification)
                } catch (e: Exception) {
                    Log.e("NetworkSync", "updateApplicationStatus REST error: ${e.message}")
                }
            }
        }
    }

    // Tech Forum Threads
    val allThreads: Flow<List<TechThread>> = dao.getAllThreads()
    
    fun getThreadById(id: Int): Flow<TechThread?> = dao.getThreadById(id)

    suspend fun createThread(thread: TechThread) {
        dao.insertThread(thread)
        if (NetworkManager.isEnabled()) {
            withContext(Dispatchers.IO) {
                try {
                    NetworkManager.apiService?.createThread(thread)
                } catch (e: Exception) {
                    Log.e("NetworkSync", "createThread REST error: ${e.message}")
                }
            }
        }
    }

    // Replies
    fun getRepliesForThread(threadId: Int): Flow<List<ThreadReply>> = dao.getRepliesForThread(threadId)

    suspend fun createReply(threadId: Int, replier: UserProfile, replyText: String) {
        val reply = ThreadReply(
            threadId = threadId,
            creatorId = replier.id,
            creatorName = replier.name,
            replyText = replyText
        )
        dao.insertReply(reply)

        // Notify thread creator
        val thread = dao.getThreadByIdOneShot(threadId) ?: return
        var notification: Notification? = null
        if (thread.creatorId != replier.id) {
            notification = Notification(
                receiverId = thread.creatorId,
                senderId = replier.id,
                senderName = replier.name,
                title = "New Forum Reply 💬",
                message = "replied to your thread: \"${thread.title}\"",
                type = "FORUM_REPLY",
                relatedId = threadId
            )
            dao.insertNotification(notification)
        }

        if (NetworkManager.isEnabled()) {
            withContext(Dispatchers.IO) {
                try {
                    NetworkManager.apiService?.createReply(threadId, reply)
                    notification?.let { NetworkManager.apiService?.createNotification(it) }
                } catch (e: Exception) {
                    Log.e("NetworkSync", "createReply REST error: ${e.message}")
                }
            }
        }
    }

    // Reviews
    fun getReviewsForUser(userId: Int): Flow<List<Review>> = dao.getReviewsForUser(userId)

    suspend fun insertReview(review: Review) {
        dao.insertReview(review)
    }

    // Notifications
    fun getNotificationsForUser(userId: Int): Flow<List<Notification>> = dao.getNotificationsForUser(userId)

    fun getUnreadNotificationCount(userId: Int): Flow<Int> = dao.getUnreadNotificationCount(userId)

    suspend fun markAllNotificationsRead(userId: Int) = dao.markAllNotificationsRead(userId)

    suspend fun markNotificationRead(notificationId: Int) = dao.markNotificationRead(notificationId)

    suspend fun updateGig(gig: Gig) {
        dao.updateGig(gig)
        if (NetworkManager.isEnabled()) {
            withContext(Dispatchers.IO) {
                try {
                    NetworkManager.apiService?.updateGig(gig.id, gig)
                } catch (e: Exception) {
                    Log.e("NetworkSync", "updateGig REST error: ${e.message}")
                }
            }
        }
    }

    suspend fun insertNotificationLocally(notification: Notification) {
        dao.insertNotification(notification)
    }

    suspend fun trySyncFromNetwork() = withContext(Dispatchers.IO) {
        if (!NetworkManager.isEnabled()) return@withContext
        val api = NetworkManager.apiService ?: return@withContext
        try {
            Log.d("NetworkSync", "Executing full model sync from backend API...")
            val remoteProfiles = api.getProfiles()
            remoteProfiles.forEach { dao.insertProfile(it) }

            val remoteGigs = api.getGigs()
            remoteGigs.forEach { dao.insertGig(it) }

            val remoteMatchmaking = api.getMatchmakingPosts()
            remoteMatchmaking.forEach { dao.insertMatchmakingPost(it) }

            val remoteThreads = api.getThreads()
            remoteThreads.forEach { dao.insertThread(it) }
            Log.d("NetworkSync", "Model synchronization complete.")
        } catch (e: Exception) {
            Log.e("NetworkSync", "Data fetch sync failed: ${e.message}")
        }
    }

    // Manual FamPay Payment Process Flows
    suspend fun submitPaymentProof(
        context: Context?,
        gigId: Int,
        buyer: UserProfile,
        proofUri: String,
        imageUri: Uri? = null
    ) {
        val gig = dao.getGigByIdOneShot(gigId) ?: return
        
        var finalProofUri = proofUri
        if (context != null && imageUri != null && NetworkManager.isEnabled()) {
            withContext(Dispatchers.IO) {
                try {
                    val part = NetworkManager.prepareScreenshotMultipart(context, imageUri)
                    if (part != null) {
                        val uploadRes = NetworkManager.apiService?.uploadPaymentProof(gigId, part)
                        if (uploadRes != null) {
                            finalProofUri = uploadRes.proofUri
                            Log.d("NetworkSync", "Upload FamPay screen success: $finalProofUri")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("NetworkSync", "Multipart upload FamPay screen failure: ${e.message}")
                }
            }
        }

        // Update Gig status
        val updatedGig = gig.copy(
            status = "Proof_Submitted",
            paymentProofUri = finalProofUri,
            assigneeId = buyer.id,
            assigneeName = buyer.name
        )
        updateGig(updatedGig)

        // Notify Seller (Gig Creator)
        val notification = Notification(
            receiverId = gig.creatorId,
            senderId = buyer.id,
            senderName = buyer.name,
            title = "Payment Proof Uploaded! 💳",
            message = "submitted Transaction Proof for \"${gig.title}\". Review details now.",
            type = "PAYMENT_PROOF",
            relatedId = gigId
        )
        dao.insertNotification(notification)

        if (NetworkManager.isEnabled()) {
            withContext(Dispatchers.IO) {
                try {
                    NetworkManager.apiService?.createNotification(notification)
                } catch (e: Exception) {
                    Log.e("NetworkSync", "submitPaymentProof notification sync error: ${e.message}")
                }
            }
        }
    }

    suspend fun approvePaymentProof(gigId: Int) {
        val gig = dao.getGigByIdOneShot(gigId) ?: return
        if (gig.status != "Proof_Submitted") return

        // 1. Mark Gig Completed
        val completedGig = gig.copy(status = "Completed")
        updateGig(completedGig)

        // 2. Award score, streaks, and check milestone accomplishments for both
        // For Seller (gig.creatorId): They successfully executed work or sold a gig
        val seller = dao.getProfileByIdOneShot(gig.creatorId)
        if (seller != null) {
            val newScore = seller.score + gig.budget  // Add budget to score/XP points
            val completedGigsCount = seller.completedGigs + 1
            var updatedStreak = seller.streak + 1
            if (updatedStreak == 0) updatedStreak = 1 // Basic streak check

            // Milestone Badges Logic
            // Streaks & Count Milestones
            val badgeList = seller.badges.split(",").map { it.trim() }.toMutableList()
            
            if (updatedStreak >= 3 && !badgeList.contains("🔥 Streak 3")) {
                badgeList.add("🔥 Streak 3")
            }
            if (updatedStreak >= 7 && !badgeList.contains("🚀 Streak 7")) {
                badgeList.add("🚀 Streak 7")
            }
            if (updatedStreak >= 10 && !badgeList.contains("👑 Streak Master 10")) {
                badgeList.add("👑 Streak Master 10")
            }
            
            if (completedGigsCount >= 5 && !badgeList.contains("💼 Pro Seller 5")) {
                badgeList.add("💼 Pro Seller 5")
            }
            if (newScore >= 1000 && !badgeList.contains("⭐ Under18 Rich 1K")) {
                badgeList.add("⭐ Under18 Rich 1K")
            }

            val updatedRank = when {
                newScore >= 3000 -> "Legend Teenlancer"
                newScore >= 1500 -> "Expert Editor"
                newScore >= 800 -> "Rising Star"
                else -> seller.rank
            }

            val updatedSeller = seller.copy(
                score = newScore,
                completedGigs = completedGigsCount,
                streak = updatedStreak,
                rank = updatedRank,
                badges = badgeList.filter { it.isNotEmpty() }.joinToString(", ")
            )
            updateProfile(updatedSeller)
        }

        // For Buyer (gig.assigneeId): They paid and got delivery
        val buyerId = gig.assigneeId
        if (buyerId != null) {
            val buyer = dao.getProfileByIdOneShot(buyerId)
            if (buyer != null) {
                val newScore = buyer.score + 100 // XP for completing payments too
                var updatedStreak = buyer.streak + 1
                
                val badgeList = buyer.badges.split(",").map { it.trim() }.toMutableList()
                if (updatedStreak >= 5 && !badgeList.contains("🔥 Patron Streak 5")) {
                    badgeList.add("🔥 Patron Streak 5")
                }
                if (!badgeList.contains("🤝 Trusted Buyer")) {
                    badgeList.add("🤝 Trusted Buyer")
                }

                val updatedBuyer = buyer.copy(
                    score = newScore,
                    streak = updatedStreak,
                    badges = badgeList.filter { it.isNotEmpty() }.joinToString(", ")
                )
                updateProfile(updatedBuyer)

                // Notify Buyer of approval!
                val notification = Notification(
                    receiverId = buyerId,
                    senderId = gig.creatorId,
                    senderName = gig.creatorName,
                    title = "Payment Approved! 🎉",
                    message = "approved your payment proof for \"${gig.title}\". Your work is delivered!",
                    type = "STATUS_UPDATE",
                    relatedId = gigId
                )
                dao.insertNotification(notification)

                if (NetworkManager.isEnabled()) {
                    withContext(Dispatchers.IO) {
                        try {
                            NetworkManager.apiService?.createNotification(notification)
                        } catch (e: Exception) {
                            Log.e("NetworkSync", "approvePaymentProof notification sync error: ${e.message}")
                        }
                    }
                }
            }
        }
    }

    suspend fun declinePaymentProof(gigId: Int, feedback: String) {
        val gig = dao.getGigByIdOneShot(gigId) ?: return
        if (gig.status != "Proof_Submitted") return

        // Return status back to Open so they can submit again
        val resetGig = gig.copy(status = "Open")
        updateGig(resetGig)

        val buyerId = gig.assigneeId
        if (buyerId != null) {
            // Notify Buyer it was declined
            val notification = Notification(
                receiverId = buyerId,
                senderId = gig.creatorId,
                senderName = gig.creatorName,
                title = "Payment Proof Declined ❌",
                message = "declined your proof on \"${gig.title}\": \"$feedback\". Please resubmit.",
                type = "STATUS_UPDATE",
                relatedId = gigId
            )
            dao.insertNotification(notification)

            if (NetworkManager.isEnabled()) {
                withContext(Dispatchers.IO) {
                    try {
                        NetworkManager.apiService?.createNotification(notification)
                    } catch (e: Exception) {
                        Log.e("NetworkSync", "declinePaymentProof notification sync error: ${e.message}")
                    }
                }
            }
        }
    }

    // Streaks and target tracking
    suspend fun incrementStreak(userId: Int) {
        val user = dao.getProfileByIdOneShot(userId) ?: return
        val updatedUser = user.copy(streak = user.streak + 1)
        
        // badge checks
        val badgeList = updatedUser.badges.split(",").map { it.trim() }.toMutableList()
        val currentStreak = updatedUser.streak
        
        if (currentStreak >= 3 && !badgeList.contains("🔥 Streak 3")) {
            badgeList.add("🔥 Streak 3")
        }
        if (currentStreak >= 7 && !badgeList.contains("🚀 Streak 7")) {
            badgeList.add("🚀 Streak 7")
        }
        if (currentStreak >= 10 && !badgeList.contains("👑 Streak Master 10")) {
            badgeList.add("👑 Streak Master 10")
        }
        
        dao.updateProfile(updatedUser.copy(badges = badgeList.filter { it.isNotEmpty() }.joinToString(", ")))
    }

    // Database pre-seeding
    suspend fun seedIfNeeded() = withContext(Dispatchers.IO) {
        val profiles = dao.getAllProfiles().first()
        if (profiles.isNotEmpty()) {
            return@withContext // DB already occupied
        }

        // 1. Seed User Profiles
        val p1 = UserProfile(
            id = 1,
            username = "aarav_edits",
            name = "Aarav Sharma",
            bio = "17yo Video editing specialist. Built 3 channels from 0 to 100k subscribers. Works fast, high quality animations.",
            score = 1500,
            streak = 12,
            rank = "Expert Editor",
            fampayUpi = "aarav@fam",
            fampayQrUri = "simulated_qr_aarav",
            badges = "🔥 10-Day Streak, 🎬 Editor Pro, 👑 Top Earner",
            targetGoalGigs = 5,
            completedGigs = 4,
            password = "aarav123",
            location = "Jaipur, India"
        )
        val p2 = UserProfile(
            id = 2,
            username = "riya_codes",
            name = "Riya Patel",
            bio = "16yo Self-taught Next.js, React, and Kotlin Developer. Hackathon winner. Passionate about building sleek SaaS platforms.",
            score = 2500,
            streak = 5,
            rank = "NextJS Wizard",
            fampayUpi = "riya@fam",
            fampayQrUri = "simulated_qr_riya",
            badges = "🚀 Innovator, 🤝 Team Player, 💡 Tech Geek",
            targetGoalGigs = 10,
            completedGigs = 7,
            password = "riya123",
            location = "London, UK"
        )
        val p3 = UserProfile(
            id = 3,
            username = "ishaan_designs",
            name = "Ishaan Gupta",
            bio = "15yo UI/UX enthusiast. Mobile App Designer. Making clean, Material 3 prototypes in Figma. Seeking coders to team up.",
            score = 300,
            streak = 1,
            rank = "UI Apprentice",
            fampayUpi = "ishaan@fam",
            fampayQrUri = null,
            badges = "🌱 Newcomer, 🎨 Palette King",
            targetGoalGigs = 3,
            completedGigs = 1,
            password = "ishaan123",
            location = "Boston, US"
        )

        dao.insertProfile(p1)
        dao.insertProfile(p2)
        dao.insertProfile(p3)

        // 2. Seed Gigs
        val g1 = Gig(
            id = 1,
            title = "Gaming Video Editor Needed",
            description = "Need clean Minecraft gameplay editing. Expect tight transition cuts, sound effects (like funny memes), and stylized captions. Budget ₹500. Expected delivery: 2 days.",
            budget = 500,
            category = "Video Editing",
            creatorId = 2, // Creator: Riya
            creatorName = "Riya Patel",
            status = "Open"
        )
        val g2 = Gig(
            id = 2,
            title = "Build landing page in NextJS",
            description = "Need a single product page designed beautifully with dynamic testimonials and an interactive roadmap block. Budget ₹1200. I can provide the Figma wireframes.",
            budget = 1200,
            category = "Web Dev",
            creatorId = 3, // Creator: Ishaan
            creatorName = "Ishaan Gupta",
            status = "Open"
        )
        val g3 = Gig(
            id = 3,
            title = "Need Minimalist Brand Logo & Identity",
            description = "Designing a new webapp for teen task tracking. Need a modern logo plus secondary vector assets for icons (SVG form). Budget ₹300.",
            budget = 300,
            category = "Design",
            creatorId = 1, // Creator: Aarav
            creatorName = "Aarav Sharma",
            status = "Open"
        )
        dao.insertGig(g1)
        dao.insertGig(g2)
        dao.insertGig(g3)

        // 3. Seed Matchmaking Posts
        val m1 = MatchmakingPost(
            id = 1,
            title = "React Developer under 18 for School SaaS",
            description = "Building a neat automated homework manager. I already designed the Figma wireframes, but need a developer partner to implement it on the web. Split profits evenly!",
            roleRequired = "React Developer",
            creatorId = 3, // Ishaan
            creatorName = "Ishaan Gupta"
        )
        val m2 = MatchmakingPost(
            id = 2,
            title = "Need UI/UX designer for Step-Tracker",
            description = "Making an Android application using Room & Compose. Looking for a designer with outstanding attention-to-detail who can output pixel-perfect Material 3 themes.",
            roleRequired = "UI/UX Designer",
            creatorId = 1, // Aarav
            creatorName = "Aarav Sharma"
        )
        dao.insertMatchmakingPost(m1)
        dao.insertMatchmakingPost(m2)

        // 4. Seed Tech Forum Threads
        val t1 = TechThread(
            id = 1,
            title = "Is Gemini 2.0 Flash the king for hobby bots?",
            content = "Seriously, the response latency is below 300ms, and with free-tier quotas, we can build massive teenage utility bots without ever entering a credit card. Who has tried deploying to Vercel/Android?",
            category = "AI Tools",
            creatorId = 3, // Ishaan
            creatorName = "Ishaan Gupta"
        )
        val t2 = TechThread(
            id = 2,
            title = "Help resolve hydration mismatch in Next.js 🚀",
            content = "I'm rendering client-side streak badges based on local time. In development, it throws a hydration error saying: Text content does not match server-rendered HTML. Is standard suppressHydrationWarning the only way?",
            category = "Bugs",
            creatorId = 1, // Aarav
            creatorName = "Aarav Sharma"
        )
        dao.insertThread(t1)
        dao.insertThread(t2)

        // 5. Seed replies
        val r1 = ThreadReply(
            id = 1,
            threadId = 2,
            creatorId = 2, // Riya replies to Aarav
            creatorName = "Riya Patel",
            replyText = "The issue occurs because SSR renders a static greeting while the client uses local browser time. Try wrapping your time-based render in a useEffect that triggers after mounting, so it only client-renders!"
        )
        dao.insertReply(r1)

        // 6. Seed Reviews
        val rev1 = Review(
            id = 1,
            reviewerId = 2,
            reviewerName = "Riya Patel",
            targetUserId = 1, // Reviews Aarav
            gigId = 1,
            gigTitle = "Minecraft Highlight Reel",
            rating = 5.0f,
            comment = "Outstanding delivery. Aarav literally built custom transitions from scratch and added hilarious background memes. Highly recommended!"
        )
        dao.insertReview(rev1)
    }
}
