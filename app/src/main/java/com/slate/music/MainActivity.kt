package com.slate.music

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.slate.music.ui.theme.MusicTheme
import kotlinx.coroutines.delay
import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.slate.music.Heart.HeartEngine

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current

            val mediaPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Manifest.permission.READ_MEDIA_AUDIO
            } else {
                Manifest.permission.READ_EXTERNAL_STORAGE
            }

            val permissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                if (isGranted) {
                    HeartEngine.initialize(context)
                    HeartEngine.scanNow()
                }
            }

            LaunchedEffect(Unit) {
                HeartEngine.initialize(context)
                permissionLauncher.launch(mediaPermission)
            }

            MusicTheme {
                var showMainUI by rememberSaveable { mutableStateOf(false) }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AnimatedContent(
                        targetState = showMainUI,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(1000)) togetherWith
                            fadeOut(animationSpec = tween(1000))
                        },
                        label = "MainTransition"
                    ) { targetShowMainUI ->
                        if (targetShowMainUI) {
                            HomeScreen()
                        } else {
                            WelcomeScreen(onAnimationFinished = { showMainUI = true })
                        }
                    }
                }
            }
        }
    }
}

@Preview
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun WelcomeScreen(onAnimationFinished: () -> Unit = {}) {
    val context = LocalContext.current
    var isAppReady by remember { mutableStateOf(false) }
    var isEntranceComplete by rememberSaveable { mutableStateOf(false) }

    val brushedSteelBrush = remember {
        Brush.linearGradient(
            colors = listOf(
                Color(0xF06A6D73), // Slightly darker edge to anchor it
                Color(0xFF9BA0A5), // Smoother transition gray
                Color(0xFFEFB4E0), // Slightly Red, Broad highlight
                Color(0xFFD2D2FF), // Bluish center gleam
                Color(0xFF8D9096), // Stronger shadow right after the gleam
                Color(0xEBB8BCC2)  // Soft edge
            )
        )
    }

    LaunchedEffect(Unit) {
        delay(100)
        isAppReady = true
    }

    LaunchedEffect(isAppReady) {
        if (isAppReady && !isEntranceComplete) {
            // Wait for entrance to finish (stagger + duration) + 1s hold
            // Max stagger (2 * 100ms) + duration (1600ms) + hold (1000ms)
            delay(200 + 1600 + 1000)
            isEntranceComplete = true
            onAnimationFinished()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row {
                val headline = "3AM"

                headline.forEachIndexed { index, character ->
                    var isVisible by rememberSaveable { mutableStateOf(false) }
                    val blurAnim = remember { Animatable(if (isVisible) 0f else 1f) }

                    LaunchedEffect(isAppReady) {
                        if (isAppReady && !isVisible) {
                            delay(index * 100L)
                            isVisible = true

                            context.performHapticClick()

                            blurAnim.animateTo(
                                targetValue = 0f,
                                animationSpec = tween(
                                    durationMillis = 1600,
                                    easing = EaseOutBack
                                )
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = isVisible,
                        enter = slideInVertically(
                            initialOffsetY = { it },
                            animationSpec = tween(
                                durationMillis = 1000,
                                easing = EaseOutBack
                            )
                        ) + fadeIn(
                            animationSpec = tween(durationMillis = 300)
                        )
                    ) {
                        Text(
                            text = character.toString(),
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 2.sp,
                                brush = brushedSteelBrush,
                                shadow = Shadow(
                                    color = Color.Black.copy(alpha = 0.6f),
                                    offset = Offset(0f, 4f),
                                    blurRadius = 6f
                                )
                            ),
                            fontSize = 180.sp,
                            modifier = Modifier
                                .blur(radius = (blurAnim.value * 12).dp)
                                .graphicsLayer { alpha = 1f - (blurAnim.value * 0.3f) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
