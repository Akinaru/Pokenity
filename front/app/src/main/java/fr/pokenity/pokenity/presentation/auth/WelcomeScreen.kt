package fr.pokenity.pokenity.presentation.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WelcomeScreen(
    onStart: () -> Unit,
    modifier: Modifier = Modifier
) {
    AuthBackgroundContainer(
        backgroundDrawableName = "welcome_background",
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {
            Button(
                onClick = onStart,
                modifier = Modifier.fillMaxWidth(0.72f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFCC18),
                    contentColor = Color.Black
                )
            ) {
                Text(
                    text = "Commencer",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 22.sp,
                        fontFamily = AuthFontFamily
                    )
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Commence l’aventure en créant ton compte",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = AuthFontFamily,
                    fontWeight = FontWeight.Medium
                ),
                textAlign = TextAlign.Center,
                color = Color(0xFFD0EAFD)
            )
        }
    }
}
