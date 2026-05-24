package com.example

import android.content.Context
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.TeenlancerRepository
import com.example.ui.TeenlancerViewModel
import com.example.ui.screens.TeenlancerApp
import com.example.ui.theme.TeenlancerHubTheme
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w480dp-h1200dp-xhdpi")
class FullPaymentFlowTest {

    @get:Rule val composeTestRule = createComposeRule()

    @Test
    fun testCheckoutAndSubmitProof() {
        com.example.data.network.NetworkManager.updateServerAddress("")
        val context = ApplicationProvider.getApplicationContext<Context>()
        
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
            
        val dao = db.teenlancerDao()
        val repository = TeenlancerRepository(dao)
        
        // Seed the database synchronously to eliminate background race conditions
        runBlocking {
            repository.seedIfNeeded()
        }
        
        val viewModel = TeenlancerViewModel(repository)

        // Set content
        composeTestRule.setContent {
            TeenlancerHubTheme {
                TeenlancerApp(viewModel = viewModel)
            }
        }

        // Switch to Marketplace tab
        composeTestRule.onNodeWithText("Marketplace").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.mainClock.advanceTimeBy(1000L)
        composeTestRule.waitForIdle()

        // Wait to find Proceed Checkout
        composeTestRule.onAllNodesWithText("Proceed Checkout").onFirst().assertExists()

        // Print all text elements on screen before click
        println("=== ALL COGNIZANT NODES BEFORE CLICK ===")
        val allNodesBefore = composeTestRule.onAllNodes(hasClickAction().or(hasText("", substring = true)))
        // We can check how many nodes exist
        val countBefore = allNodesBefore.fetchSemanticsNodes().size
        println("Count of interactable or text nodes before click: $countBefore")
        
        // Let's click Proceed Checkout
        composeTestRule.onAllNodesWithText("Proceed Checkout").onFirst().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.mainClock.advanceTimeBy(1000L)
        composeTestRule.waitForIdle()

        // Print all text elements on screen after click
        println("=== ALL COGNIZANT NODES AFTER CLICK ===")
        val allNodesAfter = composeTestRule.onAllNodes(hasClickAction().or(hasText("", substring = true)))
        val nodes = allNodesAfter.fetchSemanticsNodes()
        println("Count of interactable or text nodes after click: ${nodes.size}")
        for (i in 0 until nodes.size) {
            val semanticsNode = nodes[i]
            val config = semanticsNode.config
            println("Node #$i: layoutInfo: ${semanticsNode.id}, tags: ${semanticsNode.layoutInfo}, config: $config")
        }

        // Seek the text upload simulator "Tap to choose screenshot receipt"
        composeTestRule.onNodeWithText("Tap to choose screenshot receipt").performClick()

        // After clicking, the mock selection should update the text to "fampay_success_receipt_9921_fampay.png"
        composeTestRule.onNodeWithText("fampay_success_receipt_9921_fampay.png").assertExists()

        // Click on the submission button "Submit Verification Proof"
        composeTestRule.onNodeWithText("Submit Verification Proof").performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(800)
        composeTestRule.mainClock.advanceTimeBy(3000L)
        composeTestRule.waitForIdle()

        val gigInDb = runBlocking { dao.getGigByIdOneShot(1) }
        println("=== GIG IN DB STATUS AFTER CLICK SUBMIT: ${gigInDb?.status} ===")

        println("=== ALL COGNIZANT NODES AFTER SUBMIT CLICK ===")
        val allNodesAfterSubmit = composeTestRule.onAllNodes(hasClickAction().or(hasText("", substring = true)))
        val nodesAfterSubmit = allNodesAfterSubmit.fetchSemanticsNodes()
        println("Count of interactable or text nodes after submit: ${nodesAfterSubmit.size}")
        for (i in 0 until nodesAfterSubmit.size) {
            val semanticsNode = nodesAfterSubmit[i]
            val config = semanticsNode.config
            println("Node #$i: layoutInfo: ${semanticsNode.id}, config: $config")
        }

        // It should complete and dismiss the modal. Let's check if "Under Verification" is displayed on that item now
        composeTestRule.onNodeWithText("Under Verification", useUnmergedTree = true).assertExists()

        db.close()
    }
}
