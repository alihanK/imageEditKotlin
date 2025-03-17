package com.package.picchhanger.repository

import android.app.PendingIntent
import android.content.ContentValues
import android.content.ContentResolver
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore

class StorageRepository(private val contentResolver: ContentResolver) {

    /**
     * API 32 ve altı cihazlarda veya API 29+ için gerekli alanlar eklenerek yeni dosya oluşturup resmi kaydeder.
     */
    fun saveNewImage(bitmap: Bitmap): Uri? {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "Cropped_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/MyApp")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }
        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        uri?.let {
            contentResolver.openOutputStream(it)?.use { os ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val updatedValues = ContentValues().apply {
                    put(MediaStore.Images.Media.IS_PENDING, 0)
                }
                contentResolver.update(it, updatedValues, null, null)
            }
        }
        return uri
    }

    /**
     * API 33+ cihazlarda, varolan dosya üzerinde güncelleme yapmak için MediaStore.createWriteRequest() ile
     * kullanıcı onayı almak üzere PendingIntent döner.
     *
     * Burada verilen URI'den _ID değeri sorgulanıp, geçerli bir URI oluşturuluyor.
     */
    fun createWriteRequest(uri: Uri): PendingIntent? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val projection = arrayOf(MediaStore.Images.Media._ID)
            contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val idIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                    val id = cursor.getLong(idIndex)
                    val validUri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id.toString())
                    return MediaStore.createWriteRequest(contentResolver, listOf(validUri))
                }
            }
            return null
        } else return null
    }

    /**
     * Kullanıcının onayı sonrası, dosya üzerine resmi günceller.
     *
     * Bu metod normalde URI üzerinden yazmayı deniyor ancak PhotoPicker URI’leri salt okunurdur.
     * Bu durumda hata alınır.
     */
    fun updateImage(uri: Uri, bitmap: Bitmap) {
        try {
            contentResolver.openOutputStream(uri)?.use { os ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os)
            }
        } catch (e: SecurityException) {
            // Eğer URI salt okunursa, bu metodda güncelleme yapılamaz.
            throw e
        }
    }
}
