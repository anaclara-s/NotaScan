package com.example.notascan
import com.example.notascan.models.Nota

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import androidx.compose.runtime.*
import coil.compose.rememberAsyncImagePainter
import com.example.notascan.utils.extrairTextoDaImagem
import java.util.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            var photoUriTemp by remember { mutableStateOf<Uri?>(null) }
            var pendingPhotoUri by remember { mutableStateOf<Uri?>(null) }  // URI aguardando processamento
            val notas = remember { mutableStateListOf<Nota>() }

            fun createImageFile(context: Context): File {
                val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
                val storageDir = context.getExternalFilesDir("Pictures")!!
                return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
            }

            val takePictureLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.TakePicture()
            ) { success ->
                if (success) {
                    // Só aqui, depois de tirar foto, dispara o processamento
                    pendingPhotoUri = photoUriTemp
                }
                photoUriTemp = null
            }

            LaunchedEffect(pendingPhotoUri) {
                pendingPhotoUri?.let { uri ->
                    val texto = extrairTextoDaImagem(context, uri)
                    notas.add(Nota(imagemUri = uri, texto = texto))
                    pendingPhotoUri = null
                }
            }

            fun openCamera() {
                val photoFile = createImageFile(context)
                photoUriTemp = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    photoFile
                )
                takePictureLauncher.launch(photoUriTemp!!)
            }

            HomeScreen(notas = notas, onCameraClick = { openCamera() })
        }


    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(notas: List<Nota>, onCameraClick: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notas") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF7BC9BF),
                    titleContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = { }) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Buscar",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCameraClick,
                containerColor = Color(0xFF7BBAC9)
            ) {
                Icon(
                    Icons.Default.CameraAlt,
                    contentDescription = "Adicionar com Câmera",
                    tint = Color.White
                )
            }
        },
        content = { paddingValues ->
            if (notas.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Notas vazias",
                            color = Color(0xFF7BC98A),
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Adione a sua primeira nota",
                            color = Color(0xFF7BC9A4),
                            fontSize = 16.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(notas) { nota ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFEFFCF9)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Image(
                                    painter = rememberAsyncImagePainter(model = nota.imagemUri),
                                    contentDescription = "Imagem da nota",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = nota.texto.ifBlank { "Nenhum texto encontrado" },
                                    color = Color.DarkGray
                                )
                            }
                        }
                    }
                }
            }
        }
    )
}
