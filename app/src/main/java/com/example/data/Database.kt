package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val name: String,
    val bio: String,
    val score: Int = 0,
    val streak: Int = 0,
    val rank: String = "Novice",
    val fampayUpi: String? = null,
    val fampayQrUri: String? = null,
    val badges: String = "", // Comma-separated list of badges like "Early Bird,Streak 7"
    val targetGoalGigs: Int = 5,
    val completedGigs: Int = 0,
    val password: String = "1234",
    val location: String = "Jaipur, India"
)

@Entity(tableName = "gigs")
data class Gig(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val budget: Int,
    val category: String,
    val creatorId: Int,
    val creatorName: String,
    val assigneeId: Int? = null,
    val assigneeName: String? = null,
    val status: String = "Open", // "Open", "In_Progress", "Proof_Submitted", "Completed"
    val paymentProofUri: String? = null, // base64 or mock indicator
    val createdTime: Long = System.currentTimeMillis()
)

@Entity(tableName = "matchmaking_posts")
data class MatchmakingPost(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val roleRequired: String,
    val creatorId: Int,
    val creatorName: String,
    val createdTime: Long = System.currentTimeMillis()
)

@Entity(tableName = "co_founder_applications")
data class Application(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val postId: Int,
    val postTitle: String,
    val applicantId: Int,
    val applicantName: String,
    val pitch: String,
    val status: String = "Pending", // "Pending", "Accepted", "Declined"
    val applyTime: Long = System.currentTimeMillis()
)

@Entity(tableName = "tech_threads")
data class TechThread(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val category: String,
    val creatorId: Int,
    val creatorName: String,
    val imageUri: String? = null, // Mock Screenshot link / description or base64
    val createdTime: Long = System.currentTimeMillis()
)

@Entity(tableName = "thread_replies")
data class ThreadReply(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val threadId: Int,
    val creatorId: Int,
    val creatorName: String,
    val replyText: String,
    val createdTime: Long = System.currentTimeMillis()
)

@Entity(tableName = "reviews")
data class Review(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val reviewerId: Int,
    val reviewerName: String,
    val targetUserId: Int,
    val gigId: Int,
    val gigTitle: String,
    val rating: Float,
    val comment: String,
    val createdTime: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class Notification(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val receiverId: Int,
    val senderId: Int,
    val senderName: String,
    val title: String,
    val message: String,
    val type: String, // "PAYMENT_PROOF", "COFOUNDER_APPLY", "FORUM_REPLY", "STATUS_UPDATE"
    val relatedId: Int, // id of Gig, Post or Thread
    val isRead: Boolean = false,
    val createdTime: Long = System.currentTimeMillis()
)

@Dao
interface TeenlancerDao {
    // User Profiles
    @Query("SELECT * FROM user_profiles")
    fun getAllProfiles(): Flow<List<UserProfile>>

    @Query("SELECT * FROM user_profiles WHERE id = :id")
    fun getProfileById(id: Int): Flow<UserProfile?>

    @Query("SELECT * FROM user_profiles WHERE id = :id")
    suspend fun getProfileByIdOneShot(id: Int): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: UserProfile)

    @Update
    suspend fun updateProfile(profile: UserProfile)

    // Gigs
    @Query("SELECT * FROM gigs ORDER BY createdTime DESC")
    fun getAllGigs(): Flow<List<Gig>>

    @Query("SELECT * FROM gigs WHERE id = :id")
    fun getGigById(id: Int): Flow<Gig?>

    @Query("SELECT * FROM gigs WHERE id = :id")
    suspend fun getGigByIdOneShot(id: Int): Gig?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGig(gig: Gig)

    @Update
    suspend fun updateGig(gig: Gig)

    // Matchmaking (Co-Founder)
    @Query("SELECT * FROM matchmaking_posts ORDER BY createdTime DESC")
    fun getAllMatchmakingPosts(): Flow<List<MatchmakingPost>>

    @Query("SELECT * FROM matchmaking_posts WHERE id = :id")
    suspend fun getMatchmakingPostByIdOneShot(id: Int): MatchmakingPost?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatchmakingPost(post: MatchmakingPost)

    // Co-founder Applications
    @Query("SELECT * FROM co_founder_applications WHERE postId = :postId ORDER BY applyTime DESC")
    fun getApplicationsByPostId(postId: Int): Flow<List<Application>>

    @Query("SELECT * FROM co_founder_applications WHERE id = :id")
    suspend fun getApplicationByIdOneShot(id: Int): Application?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApplication(application: Application)

    @Update
    suspend fun updateApplication(application: Application)

    // Tech Forum Threads
    @Query("SELECT * FROM tech_threads ORDER BY createdTime DESC")
    fun getAllThreads(): Flow<List<TechThread>>

    @Query("SELECT * FROM tech_threads WHERE id = :id")
    fun getThreadById(id: Int): Flow<TechThread?>

    @Query("SELECT * FROM tech_threads WHERE id = :id")
    suspend fun getThreadByIdOneShot(id: Int): TechThread?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertThread(thread: TechThread)

    // Thread Replies
    @Query("SELECT * FROM thread_replies WHERE threadId = :threadId ORDER BY createdTime ASC")
    fun getRepliesForThread(threadId: Int): Flow<List<ThreadReply>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReply(reply: ThreadReply)

    // Reviews
    @Query("SELECT * FROM reviews WHERE targetUserId = :userId ORDER BY createdTime DESC")
    fun getReviewsForUser(userId: Int): Flow<List<Review>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: Review)

    // Notifications
    @Query("SELECT * FROM notifications WHERE receiverId = :userId ORDER BY createdTime DESC")
    fun getNotificationsForUser(userId: Int): Flow<List<Notification>>

    @Query("SELECT COUNT(*) FROM notifications WHERE receiverId = :userId AND isRead = 0")
    fun getUnreadNotificationCount(userId: Int): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: Notification)

    @Query("UPDATE notifications SET isRead = 1 WHERE receiverId = :userId")
    suspend fun markAllNotificationsRead(userId: Int)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :notificationId")
    suspend fun markNotificationRead(notificationId: Int)
}

@Database(
    entities = [
        UserProfile::class,
        Gig::class,
        MatchmakingPost::class,
        Application::class,
        TechThread::class,
        ThreadReply::class,
        Review::class,
        Notification::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun teenlancerDao(): TeenlancerDao
}
