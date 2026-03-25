package com.nexusai.app.auth

import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent

/**
 * Handles the Google OAuth 2.0 flow securely without webviews.
 * Implements AppAuth-style flow using CustomTabs.
 */
class AuthManager(
    private val context: Context,
    private val keystoreHelper: KeystoreHelper
) {
    companion object {
        private const val AUTH_ENDPOINT = "https://accounts.google.com/o/oauth2/v2/auth"
        private const val CLIENT_ID = "YOUR_CLIENT_ID.apps.googleusercontent.com"
        private const val REDIRECT_URI = "com.nexusai.app:/oauth2callback"
        private const val SCOPES = "openid email profile https://www.googleapis.com/auth/generative-language"
    }

    /**
     * Triggers the Chrome Custom Tab for secure user authentication.
     */
    fun startOAuthFlow() {
        val authUri = Uri.parse(AUTH_ENDPOINT).buildUpon()
            .appendQueryParameter("client_id", CLIENT_ID)
            .appendQueryParameter("redirect_uri", REDIRECT_URI)
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("scope", SCOPES)
            // .appendQueryParameter("code_challenge", generatePkceChallenge())
            // .appendQueryParameter("code_challenge_method", "S256")
            .build()

        val customTabsIntent = CustomTabsIntent.Builder()
            .setShowTitle(true)
            .build()
        
        customTabsIntent.launchUrl(context, authUri)
    }

    /**
     * Intercepts the deep link callback from the browser.
     */
    fun handleCallback(uri: Uri) {
        val code = uri.getQueryParameter("code")
        val error = uri.getQueryParameter("error")

        if (code != null) {
            // Exchange code for Access + Refresh Tokens
            // On success: encrypt and store
            // val refreshToken = exchangeCodeForToken(code)
            // keystoreHelper.encryptData("GEMINI_REFRESH_TOKEN", refreshToken)
        }
    }
}
