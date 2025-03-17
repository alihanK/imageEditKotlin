package com.package.picchhanger

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.ImageDecoder
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.package.picchhanger.repository.StorageRepository
import com.yalantis.ucrop.UCrop
import java.io.File

@Composable
fun EditorScreen(
    imageUri: String,
    storageRepository: StorageRepository
) {
    val context = LocalContext.current
    var currentImageUri by remember { mutableStateOf(imageUri) }
    var originalBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var filteredBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val filterOptions = listOf("Normal", "Red", "Green", "Blue")
    var selectedFilter by remember { mutableStateOf("Normal") }

    // Galeri için launcher ekle
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            currentImageUri = it.toString()
            selectedFilter = "Normal" // Yeni resim seçildiğinde filtre sıfırlanıyor
        }
    }

    // Resmi yükle
    LaunchedEffect(currentImageUri) {
        try {
            val uri = Uri.parse(currentImageUri)
            originalBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                }
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            }
            // Yeni resim yüklendiğinde direkt olarak orijinal bitmap'i göster
            filteredBitmap = originalBitmap
        } catch (e: Exception) {
            Toast.makeText(context, "Resim yüklenemedi: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    // Renk filtre uygulaması - sadece selectedFilter değiştiğinde çalışacak
    LaunchedEffect(selectedFilter) {
        if (selectedFilter == "Normal") {
            filteredBitmap = originalBitmap
        } else {
            originalBitmap?.let { bmp ->
                val cm = when (selectedFilter) {
                    "Red" -> ColorMatrix().apply {
                        setScale(1f, 0f, 0f, 1f)
                    }
                    "Green" -> ColorMatrix().apply {
                        setScale(0f, 1f, 0f, 1f)
                    }
                    "Blue" -> ColorMatrix().apply {
                        setScale(0f, 0f, 1f, 1f)
                    }
                    else -> ColorMatrix().apply {
                        setScale(1f, 1f, 1f, 1f)
                    }
                }

                val paint = Paint().apply {
                    colorFilter = ColorMatrixColorFilter(cm)
                }

                try {
                    val filtered = Bitmap.createBitmap(
                        bmp.width,
                        bmp.height,
                        Bitmap.Config.ARGB_8888
                    ).apply {
                        Canvas(this).drawBitmap(bmp, 0f, 0f, paint)
                    }
                    filteredBitmap = filtered
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(context, "Filtre uygulanırken hata oluştu", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // uCrop launcher için hata yönetimi eklendi
    val cropLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        try {
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val resultUri = UCrop.getOutput(result.data!!)
                if (resultUri != null) {
                    currentImageUri = resultUri.toString()
                    selectedFilter = "Normal" // Kırpma sonrası filtreyi sıfırla
                }
            } else if (result.resultCode == UCrop.RESULT_ERROR && result.data != null) {
                val cropError = UCrop.getError(result.data!!)
                Toast.makeText(context, "Kırpma hatası: ${cropError?.message}", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Kırpma işlemi sırasında hata oluştu", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Ana içerik: Resim önizlemesi
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            filteredBitmap?.let { bmp ->
                Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = "Önizleme",
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.6f)
                )
            }
        }

        // Alt kontrol alanı
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Bottom
        ) {
            // Filtre seçenekleri ve kırp butonu
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                filterOptions.forEach { option ->
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(
                                color = when (option) {
                                    "Red" -> Color.Red
                                    "Green" -> Color.Green
                                    "Blue" -> Color.Blue
                                    else -> Color.LightGray
                                }
                            )
                            .clickable { selectedFilter = option },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = option, color = Color.White)
                    }

                    if (option != filterOptions.last()) {
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(Color.DarkGray)
                        .clickable {
                            originalBitmap?.let { bmp ->
                                val sourceUri = Uri.parse(currentImageUri)
                                try {
                                    val destFile = File.createTempFile("cropped_", ".jpg", context.cacheDir)
                                    val destUri = Uri.fromFile(destFile)
                                    val uCrop = UCrop.of(sourceUri, destUri)
                                        .withAspectRatio(1f, 1f)
                                        .withMaxResultSize(bmp.width, bmp.height)
                                    cropLauncher.launch(uCrop.getIntent(context))
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Could not start cropping: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Crop", color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Galeriye git butonu
            Button(
                onClick = {
                    galleryLauncher.launch("image/*")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Look Another photos in gallery")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Kaydet butonu
            Button(
                onClick = {
                    filteredBitmap?.let { bmp ->
                        val newUri = storageRepository.saveNewImage(bmp)
                        if (newUri != null) {
                            Toast.makeText(context, "Photo Saved", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Save Error", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "SAVE")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Uygulama çıkış ve logo alanı, ekranın en altında
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sol tarafta boş spacer
                Spacer(modifier = Modifier.width(40.dp))

                // Ortada logo
                Image(
                    painter = painterResource(id = R.drawable.app_logo),
                    contentDescription = "App Logo",
                    modifier = Modifier
                        .size(40.dp)
                        .padding(4.dp)
                )

                // Sağ tarafta çıkış butonu
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clickable {
                            (context as? Activity)?.finish()
                        }
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "X",
                        color = Color.Black,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
