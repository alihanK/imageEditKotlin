package com.package.picchhanger

import android.Manifest
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.activity.result.contract.ActivityResultContracts.TakePicture
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.core.content.ContextCompat
import com.package.picchhanger.repository.StorageRepository
import com.package.picchhanger.ui.theme.CropAppTheme
import javax.inject.Inject
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.os.Environment
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {

    @Inject
    lateinit var storageRepository: StorageRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as CropApp).appComponent.inject(this)
        super.onCreate(savedInstanceState)
        setContent {
            CropAppTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "main") {
                    composable("main") {
                        MainScreen(
                            onImageSelected = { uri ->
                                navController.navigate("editor?imageUri=$uri")
                            },
                            onImageCaptured = { uri ->
                                navController.navigate("editor?imageUri=$uri")
                            }
                        )
                    }
                    composable(
                        route = "editor?imageUri={imageUri}",
                        arguments = listOf(navArgument("imageUri") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val imageUri = backStackEntry.arguments?.getString("imageUri") ?: ""
                        EditorScreen(
                            imageUri = imageUri,
                            storageRepository = storageRepository
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MainScreen(
    onImageSelected: (String) -> Unit,
    onImageCaptured: (String) -> Unit
) {
    val context = LocalContext.current
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }
    val galleryLauncher = rememberLauncherForActivityResult(contract = GetContent()) { uri: Uri? ->
        uri?.let { onImageSelected(it.toString()) }
    }
    val cameraLauncher = rememberLauncherForActivityResult(contract = TakePicture()) { success: Boolean ->
        if (success && cameraImageUri != null) {
            onImageCaptured(cameraImageUri.toString())
        }
    }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            createCameraImageUri(context)?.let { uri ->
                cameraImageUri = uri
                cameraLauncher.launch(uri)
            }
        }
    }
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Button(
            onClick = { galleryLauncher.launch("image/*") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Chose a photo from gallery")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                when {
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED -> {
                        createCameraImageUri(context)?.let { uri ->
                            cameraImageUri = uri
                            cameraLauncher.launch(uri)
                        }
                    }
                    else -> {
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Take a photo with phone")
        }
    }
}

private fun createCameraImageUri(context: Context): Uri? {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val imageFileName = "JPEG_${timeStamp}_"

    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, imageFileName)
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        }
    }

    return try {
        context.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        )
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
