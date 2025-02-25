package com.ralphmarondev.pixiecam

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.camera.core.CameraSelector
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cameraswitch
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import coil.compose.rememberAsyncImagePainter
import com.ralphmarondev.pixiecam.data.TfLiteLandmarkClassifier
import com.ralphmarondev.pixiecam.domain.Classification
import com.ralphmarondev.pixiecam.presentation.CameraPreview
import com.ralphmarondev.pixiecam.presentation.LandmarkImageAnalyzer
import com.ralphmarondev.pixiecam.ui.theme.PixieCamTheme
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (!hasRequiredPermissions()) {
            ActivityCompat.requestPermissions(
                this, CAMERAX_PERMISSIONS, 0
            )
        }

        setContent {
            PixieCamTheme {
                var classifications by remember {
                    mutableStateOf(emptyList<Classification>())
                }
                val analyzer = remember {
                    LandmarkImageAnalyzer(
                        classifier = TfLiteLandmarkClassifier(applicationContext),
                        onResults = {
                            classifications = it
                        }
                    )
                }

                val controller = remember {
                    LifecycleCameraController(applicationContext).apply {
                        setEnabledUseCases(CameraController.IMAGE_ANALYSIS)
                        setImageAnalysisAnalyzer(
                            ContextCompat.getMainExecutor(applicationContext),
                            analyzer
                        )
                    }
                }

                var isSplashDone by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) {
                    delay(5000)
                    isSplashDone = true
                }

                if (!isSplashDone) {
                    Scaffold { innerPadding ->
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(R.drawable.camera),
                                contentDescription = "Logo",
                                modifier = Modifier
                                    .size(200.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "PixieCam",
                                fontSize = 32.sp,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = "Smart, fun, and a little bit chaotic!",
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.secondary,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.weight(1f))
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Developed by:",
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.secondary,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "Ralph Maron Eda",
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.secondary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    Scaffold(
                        topBar = {
                            TopAppBar(
                                title = {
                                    Text(
                                        text = "PixieCam"
                                    )
                                },
                                actions = {
                                    IconButton(
                                        onClick = {
                                            controller.cameraSelector =
                                                if (controller.cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                                                    CameraSelector.DEFAULT_FRONT_CAMERA
                                                } else {
                                                    CameraSelector.DEFAULT_BACK_CAMERA
                                                }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Cameraswitch,
                                            contentDescription = "Switch camera"
                                        )
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            CameraPreview(
                                controller = controller,
                                modifier = Modifier
                                    .fillMaxSize()
                            )

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.BottomCenter)
                                    .padding(16.dp)
                            ) {
                                classifications.forEach {
                                    Text(
                                        text = it.name,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(MaterialTheme.colorScheme.secondaryContainer)
                                            .padding(8.dp),
                                        textAlign = TextAlign.Center,
                                        fontSize = 20.sp,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun hasRequiredPermissions(): Boolean {
        return CAMERAX_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(
                applicationContext,
                it
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    companion object {
        private val CAMERAX_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
    }
}