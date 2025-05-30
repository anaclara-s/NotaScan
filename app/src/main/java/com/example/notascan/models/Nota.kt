package com.example.notascan.models

import android.net.Uri

data class Nota(
    val id: Long = System.currentTimeMillis(),
    val imagemUri: Uri,
    val texto: String
)
