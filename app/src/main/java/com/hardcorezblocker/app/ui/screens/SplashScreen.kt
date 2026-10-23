package com.hardcorezblocker.app.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hardcorezblocker.app.R
import com.hardcorezblocker.app.ui.theme.DarkBackground
import com.hardcorezblocker.app.ui.theme.TextMuted
import com.hardcorezblocker.app.ui.theme.TextPrimary
import com.hardcorezblocker.app.ui.theme.TextSecondary
import kotlinx.coroutines.delay

import com.hardcorezblocker.app.ui.i18n.LocalAppStrings

@Composable
fun SplashScreen(
    onTimeout: () -> Unit
) {
    val strings = LocalAppStrings.current
    var startAnimation by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (startAnimation) 1.0f else 0.72f,
        animationSpec = tween(durationMillis = 750, easing = FastOutSlowInEasing),
        label = "splash_logo_scale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (startAnimation) 1.0f else 0.0f,
        animationSpec = tween(durationMillis = 700, easing = LinearEasing),
        label = "splash_logo_alpha"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(1300)
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .scale(scale)
                .alpha(alpha)
                .padding(24.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_app_logo),
                contentDescription = "Logo HardcorezBlocker",
                modifier = Modifier.size(110.dp)
            )

            Spacer(modifier = Modifier.height(22.dp))

            Text(
                text = strings.appName,
                color = TextPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = strings.appSubtitle,
                color = TextSecondary,
                fontSize = 14.sp
            )
        }

        // Footer di bagian bawah
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = strings.splashSubtitle,
                color = TextMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
