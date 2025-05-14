package com.sowmya.security


import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.sowmya.security.ui.theam.DiagonalGlowingLinesBackground
import com.sowmya.security.ui.theam.*
import com.sowmya.security.viewmodel.AuthViewModel
enum class LoginStatus { SUCCESS, FAILURE }
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    onAuthSuccess: (isAdmin: Boolean) -> Unit
) {
//    MovingGlowingLinesBackground()
    SparkBurstBackground()
//    DiagonalGlowingLinesBackground()
//    AnimatedECGLine()
    var triggerPulse by remember { mutableStateOf(true) }
    val emailState = remember { mutableStateOf("") }
    val passwordState = remember { mutableStateOf("") }
    val isLoginMode = remember { mutableStateOf(true) }
    val context = LocalContext.current
    var loginStatus by remember { mutableStateOf<LoginStatus?>(null) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        SparkBurstBackground(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    alpha = 0.8f
                    shadowElevation = 12f
                }
        )

        LightningStreaks(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    alpha = 0.6f
                    shadowElevation = 16f
                }
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                AnimatedECGLineWithGlow(trigger = triggerPulse)
            }
            Text(
                text = if (isLoginMode.value) "Login" else "Sign Up",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            NeumorphicTextField(value = emailState.value, onValueChange = { emailState.value = it }, hint = "Email")
            NeumorphicTextField(value = passwordState.value, onValueChange = { passwordState.value = it }, hint = "Password")
            Spacer(modifier = Modifier.height(8.dp))


            Spacer(modifier = Modifier.height(16.dp))

            NeumorphicButton(text = if (isLoginMode.value) "Login" else "Sign Up") {
                if (isLoginMode.value) {
                    // Login flow
                    viewModel.loginWithEmailPassword(
                        email = emailState.value.trim(),
                        password = passwordState.value.trim(),
                        onSuccess = { isAdmin -> onAuthSuccess(isAdmin)
                            LoginStatus.SUCCESS},
                        onFailure = { exception ->
                            LoginStatus.FAILURE
                            Toast.makeText(context, exception.message ?: "Login failed", Toast.LENGTH_SHORT).show()
                        }
                    )
                } else {
                    // Sign up flow
                    viewModel.signUpWithEmailPassword(
                        email = emailState.value.trim(),
                        password = passwordState.value.trim(),
                        onSuccess = { isAdmin -> onAuthSuccess(isAdmin)
                            LoginStatus.SUCCESS},
                        onFailure = { exception ->
                            LoginStatus.FAILURE
                            Toast.makeText(context, exception.message ?: "Sign Up failed", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isLoginMode.value) "Don't have an account? Sign Up" else "Already have an account? Login",
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.clickable { isLoginMode.value = !isLoginMode.value }
            )
        }
        RedShockSparkEffect(trigger = loginStatus == LoginStatus.FAILURE)
        ParticleBurstEffect(trigger = loginStatus == LoginStatus.SUCCESS)

    }
}
