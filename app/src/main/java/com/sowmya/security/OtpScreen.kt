package com.sowmya.security

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider

@Composable
fun OtpScreen(navController: NavController) {
    var otp by remember { mutableStateOf("") }
    val auth = FirebaseAuth.getInstance()
    val verificationId = navController.currentBackStackEntry?.arguments?.getString("verificationId")

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Enter OTP")
        BasicTextField(
            value = otp,
            onValueChange = { otp = it },
            modifier = Modifier.padding(16.dp)
        )
        Button(onClick = { verifyOtp(auth, verificationId, otp, navController) }) {
            Text("Verify OTP")
        }
    }
}

fun verifyOtp(auth: FirebaseAuth, verificationId: String?, otp: String, navController: NavController) {
    if (verificationId != null) {
        val credential = PhoneAuthProvider.getCredential(verificationId, otp)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    navController.navigate("home")
                } else {
                    Log.e("OtpScreen", "Verification Failed: ${task.exception?.message}")
                }
            }
    }
}
