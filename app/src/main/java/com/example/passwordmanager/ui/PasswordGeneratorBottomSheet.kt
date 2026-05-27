package com.example.passwordmanager.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.passwordmanager.utils.PasswordGenerator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordGeneratorBottomSheet(
    onDismiss: () -> Unit,
    onPasswordGenerated: (String) -> Unit
) {
    var length by remember { mutableFloatStateOf(16f) }
    var useUpper by remember { mutableStateOf(true) }
    var useLower by remember { mutableStateOf(true) }
    var useNumbers by remember { mutableStateOf(true) }
    var useSymbols by remember { mutableStateOf(true) }

    var generatedPassword by remember { mutableStateOf("") }

    val generate = {
        generatedPassword = PasswordGenerator.generatePassword(
            length = length.toInt(),
            useUpper = useUpper,
            useLower = useLower,
            useNumbers = useNumbers,
            useSymbols = useSymbols
        )
    }

    // Generate initial password
    LaunchedEffect(length, useUpper, useLower, useNumbers, useSymbols) {
        generate()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color(0xFF1C1C1E)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Tạo Mật khẩu",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Password Display Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = generatedPassword,
                        color = Color(0xFF0A84FF),
                        fontSize = 18.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f),
                        maxLines = 2,
                        textAlign = TextAlign.Center
                    )
                    
                    IconButton(onClick = generate) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Refresh", tint = Color.Gray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Length Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Độ dài", color = Color.White, fontSize = 16.sp)
                Text(text = "${length.toInt()}", color = Color(0xFF0A84FF), fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            
            Slider(
                value = length,
                onValueChange = { length = it },
                valueRange = 8f..64f,
                steps = 55, // 64 - 8 - 1 = 55 steps? (64-8=56, so 55 steps between)
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Color(0xFF0A84FF),
                    inactiveTrackColor = Color.DarkGray
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Options
            GeneratorOptionRow("Chữ hoa (A-Z)", useUpper) { useUpper = it; if (!it && !useLower && !useNumbers && !useSymbols) useUpper = true }
            GeneratorOptionRow("Chữ thường (a-z)", useLower) { useLower = it; if (!useUpper && !it && !useNumbers && !useSymbols) useLower = true }
            GeneratorOptionRow("Chữ số (0-9)", useNumbers) { useNumbers = it; if (!useUpper && !useLower && !it && !useSymbols) useNumbers = true }
            GeneratorOptionRow("Ký tự đặc biệt (!@#)", useSymbols) { useSymbols = it; if (!useUpper && !useLower && !useNumbers && !it) useSymbols = true }

            Spacer(modifier = Modifier.height(32.dp))

            // Use Password Button
            Button(
                onClick = { onPasswordGenerated(generatedPassword) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A84FF)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Sử dụng mật khẩu này", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun GeneratorOptionRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onCheckedChange(!checked) },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color.White, fontSize = 16.sp)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF34C759),
                uncheckedThumbColor = Color.Gray,
                uncheckedTrackColor = Color(0xFF3A3A3C)
            )
        )
    }
}
