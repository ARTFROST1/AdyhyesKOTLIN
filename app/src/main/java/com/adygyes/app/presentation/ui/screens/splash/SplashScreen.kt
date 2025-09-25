package com.adygyes.app.presentation.ui.screens.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adygyes.app.R

/**
 * Splash screen with background image and styled button
 */
@Composable
fun SplashScreen(
    onNavigateToMain: () -> Unit
) {
    // Downloadable Google Font: Raleway with specific weights
    val provider = GoogleFont.Provider(
        providerAuthority = "com.google.android.gms.fonts",
        providerPackage = "com.google.android.gms",
        certificates = com.adygyes.app.R.array.com_google_android_gms_fonts_certs
    )
    val raleway = GoogleFont("Raleway")
    val ralewayFontFamily = FontFamily(
        androidx.compose.ui.text.googlefonts.Font(raleway, provider, weight = FontWeight.Normal),
        androidx.compose.ui.text.googlefonts.Font(raleway, provider, weight = FontWeight.SemiBold),
        androidx.compose.ui.text.googlefonts.Font(raleway, provider, weight = FontWeight.ExtraBold),
        androidx.compose.ui.text.googlefonts.Font(raleway, provider, weight = FontWeight.Black)
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Background image
        Image(
            painter = painterResource(id = R.drawable.dombay_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )
        
        // Content overlay
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Centered title/subtitle block
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Main title - Black 54px
                    Text(
                        text = "AdygGis",
                        fontSize = 54.sp,
                        fontFamily = ralewayFontFamily,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFF6CA5F),
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    // Subtitle - ExtraBold 22px
                    Text(
                        text = "Шъукъеблагъ !",
                        fontSize = 22.sp,
                        fontFamily = ralewayFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Bottom action and caption
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Start button with airplane icon
                Row(
                    modifier = Modifier
                        .defaultMinSize(minWidth = 194.dp, minHeight = 40.dp)
                        .background(
                            color = Color(0xFF0C5329),
                            shape = RoundedCornerShape(25.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = Color(0xFFF6CA5F),
                            shape = RoundedCornerShape(25.dp)
                        )
                        .clip(RoundedCornerShape(25.dp))
                        .clickable { onNavigateToMain() }
                        .padding(horizontal = 24.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Airplane icon
                    Icon(
                        painter = painterResource(id = R.drawable.ic_airplane),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = Color(0xFFF6CA5F)
                    )
                    
                    Spacer(modifier = Modifier.width(10.dp))
                    
                    // Button text - Semibold 16px
                    Text(
                        text = "В путешествие",
                        fontSize = 16.sp,
                        fontFamily = ralewayFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFF6CA5F),
                        maxLines = 1
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Bottom text - slightly larger
                Text(
                    text = "Created by FrostMoon Tech",
                    fontSize = 12.sp,
                    fontFamily = ralewayFontFamily,
                    fontWeight = FontWeight.Normal,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
