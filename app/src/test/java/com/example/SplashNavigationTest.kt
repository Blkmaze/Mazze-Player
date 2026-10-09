package com.example

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.ui.viewmodel.MaZzeViewModel
import com.example.ui.viewmodel.Screen
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SplashNavigationTest {

    @Test
    fun testInitialScreenIsSplashAndNavigatesToHome() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = MaZzeViewModel(app)

        // Initial screen must be Splash
        assertEquals(Screen.Splash, viewModel.currentScreen.value)

        // Navigate to Home after splash complete
        viewModel.navigateTo(Screen.Home)
        assertEquals(Screen.Home, viewModel.currentScreen.value)
    }
}
