package com.p2pmessenger.ui.screens.auth

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.TelephonyManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.p2pmessenger.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onLoginSuccess: () -> Unit
) {
    var isLogin by remember { mutableStateOf(true) }
    var phoneNumber by remember { mutableStateOf("") }
    var deviceName by remember { mutableStateOf("") }
    var imei by remember { mutableStateOf("") }
    var deviceModel by remember { mutableStateOf(Build.MODEL) }
    var isLoading by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    
    // Get device info
    LaunchedEffect(Unit) {
        deviceName = Build.BRAND + " " + Build.MODEL
        deviceModel = Build.MODEL
        // IMEI requires permission
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        
        // Logo
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Primary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Message,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = Color.White
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "P2P Messenger",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        
        Text(
            text = "Secure & Anonymous",
            fontSize = 16.sp,
            color = TextSecondary,
            modifier = Modifier.padding(top = 4.dp)
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Toggle Login/Register
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Surface)
                .padding(4.dp)
        ) {
            Button(
                onClick = { isLogin = true },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isLogin) Primary else Color.Transparent
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    "Login",
                    color = if (isLogin) Color.White else TextSecondary
                )
            }
            Button(
                onClick = { isLogin = false },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (!isLogin) Primary else Color.Transparent
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    "Register",
                    color = if (!isLogin) Color.White else TextSecondary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        if (isLogin) {
            LoginForm(
                phoneNumber = phoneNumber,
                onPhoneChange = { phoneNumber = it },
                onLogin = {
                    isLoading = true
                    onLoginSuccess()
                }
            )
        } else {
            RegisterForm(
                phoneNumber = phoneNumber,
                onPhoneChange = { phoneNumber = it },
                deviceName = deviceName,
                deviceModel = deviceModel,
                imei = imei,
                onRegister = {
                    isLoading = true
                    onLoginSuccess()
                }
            )
        }
        
        if (isLoading) {
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator(color = Primary)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginForm(
    phoneNumber: String,
    onPhoneChange: (String) -> Unit,
    onLogin: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { onPhoneChange(it) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Phone Number", color = TextSecondary) },
            placeholder = { Text("+1 234 567 8900") },
            leadingIcon = {
                Icon(Icons.Default.Phone, contentDescription = null, tint = Primary)
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                containerColor = Surface,
                focusedBorderColor = Primary,
                unfocusedBorderColor = Divider,
                focusedTextColor = Color.White
            )
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = "",
            onValueChange = { },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Password", color = TextSecondary) },
            leadingIcon = {
                Icon(Icons.Default.Lock, contentDescription = null, tint = Primary)
            },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                containerColor = Surface,
                focusedBorderColor = Primary,
                unfocusedBorderColor = Divider,
                focusedTextColor = Color.White
            )
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onLogin,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Primary),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                "Login",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        TextButton(onClick = { }) {
            Text("Forgot password?", color = TextSecondary)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterForm(
    phoneNumber: String,
    onPhoneChange: (String) -> Unit,
    deviceName: String,
    deviceModel: String,
    imei: String,
    onRegister: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Phone
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { onPhoneChange(it) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Phone Number", color = TextSecondary) },
            placeholder = { Text("+1 234 567 8900") },
            leadingIcon = {
                Icon(Icons.Default.Phone, contentDescription = null, tint = Primary)
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                containerColor = Surface,
                focusedBorderColor = Primary,
                unfocusedBorderColor = Divider,
                focusedTextColor = Color.White
            )
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Password
        OutlinedTextField(
            value = "",
            onValueChange = { },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Password", color = TextSecondary) },
            leadingIcon = {
                Icon(Icons.Default.Lock, contentDescription = null, tint = Primary)
            },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                containerColor = Surface,
                focusedBorderColor = Primary,
                unfocusedBorderColor = Divider,
                focusedTextColor = Color.White
            )
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Device info section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Surface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    "Device Information",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Primary
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Device name
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Devices,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Device",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = deviceName,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Model
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Model",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = deviceModel,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // IMEI (read-only, requires permission)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Fingerprint,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Device ID",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = imei.ifEmpty { "Required for verification" },
                            fontSize = 15.sp,
                            color = if (imei.isEmpty()) Error else Color.White
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onRegister,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Primary),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                "Create Account",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "By registering, you agree to our Terms of Service and Privacy Policy. Your device information is used for security purposes only.",
            fontSize = 12.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
