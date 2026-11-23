package com.dsm.clubnauticoposeidon.ui.screens.mantenimiento

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import android.media.ExifInterface
import com.dsm.clubnauticoposeidon.model.Embarcacion
import com.dsm.clubnauticoposeidon.model.EvaluacionAveria
import com.dsm.clubnauticoposeidon.model.GravedadAveria
import com.dsm.clubnauticoposeidon.model.ReporteDanio
import com.dsm.clubnauticoposeidon.ui.theme.Gold400
import com.dsm.clubnauticoposeidon.ui.theme.Gold500
import com.dsm.clubnauticoposeidon.ui.theme.Ink
import com.dsm.clubnauticoposeidon.ui.theme.Muted
import com.dsm.clubnauticoposeidon.ui.theme.Navy700
import com.dsm.clubnauticoposeidon.ui.theme.Navy800
import com.dsm.clubnauticoposeidon.ui.theme.Navy900
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
    val matrix = android.graphics.Matrix().apply { postRotate(degrees) }
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EvaluacionAveriasScreen(
    onBackClick: () -> Unit,
    viewModel: EvaluacionAveriasViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var tabSeleccionado by remember { mutableIntStateOf(0) }
    var reporteSeleccionadoDialog by remember { mutableStateOf<EvaluacionAveria?>(null) }

    var tempPhotoFile by remember { mutableStateOf<File?>(null) }

    // Launcher con FileProvider para la cámara
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        val file = tempPhotoFile
        if (success && file != null && file.exists() && file.length() > 0) {
            try {
                val exif = ExifInterface(file.absolutePath)
                val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
                val originalBmp = BitmapFactory.decodeFile(file.absolutePath)
                if (originalBmp != null) {
                    val finalBmp = when (orientation) {
                        ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(originalBmp, 90f)
                        ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(originalBmp, 180f)
                        ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(originalBmp, 270f)
                        else -> originalBmp
                    }
                    viewModel.onFotoCapturada(finalBmp)
                }
            } catch (_: Exception) {
                Toast.makeText(context, "Error al procesar fotografía capturada", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val previewCameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            viewModel.onFotoCapturada(bitmap)
        } else {
            Toast.makeText(context, "No se capturó fotografía.", Toast.LENGTH_SHORT).show()
        }
    }

    val ejecutarCamara = {
        try {
            val file = File.createTempFile("averia_", ".jpg", context.cacheDir).apply {
                createNewFile()
                deleteOnExit()
            }
            tempPhotoFile = file
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            takePictureLauncher.launch(uri)
        } catch (_: Exception) {
            try {
                previewCameraLauncher.launch(null)
            } catch (_: Exception) {
                Toast.makeText(context, "No se pudo iniciar la cámara. Usa la opción Galería.", Toast.LENGTH_LONG).show()
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            ejecutarCamara()
        } else {
            Toast.makeText(context, "Se requiere permiso de cámara para fotografiar averías", Toast.LENGTH_LONG).show()
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                if (bitmap != null) {
                    viewModel.onFotoCapturada(bitmap)
                }
            } catch (_: Exception) {
                Toast.makeText(context, "Error al cargar imagen seleccionada", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val activarCamara = {
        if (!uiState.naveEstaSeleccionada) {
            Toast.makeText(context, "Selecciona primero una embarcación registrada para iniciar", Toast.LENGTH_SHORT).show()
        } else {
            val permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                ejecutarCamara()
            } else {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    LaunchedEffect(uiState.mensajeExito, uiState.mensajeError) {
        uiState.mensajeExito?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.limpiarMensajes()
            tabSeleccionado = 1
        }
        uiState.mensajeError?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.limpiarMensajes()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Evaluación de Averías",
                                color = Gold500,
                                fontWeight = FontWeight.Bold,
                                fontSize = 19.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = Gold500.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Gold500.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = "IA Visión",
                                    color = Gold500,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Diagnóstico técnico naval con IA",
                            color = Muted,
                            fontSize = 12.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Gold500
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Navy900)
            )
        },
        containerColor = Navy900
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            PrimaryTabRow(
                selectedTabIndex = tabSeleccionado,
                containerColor = Navy900,
                contentColor = Gold500
            ) {
                Tab(
                    selected = tabSeleccionado == 0,
                    onClick = { tabSeleccionado = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Diagnóstico Técnico", fontWeight = if (tabSeleccionado == 0) FontWeight.Bold else FontWeight.Normal)
                        }
                    },
                    selectedContentColor = Gold500,
                    unselectedContentColor = Muted
                )
                Tab(
                    selected = tabSeleccionado == 1,
                    onClick = { tabSeleccionado = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.History, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Historial (${uiState.reportesHistorial.size})", fontWeight = if (tabSeleccionado == 1) FontWeight.Bold else FontWeight.Normal)
                        }
                    },
                    selectedContentColor = Gold500,
                    unselectedContentColor = Muted
                )
            }

            if (tabSeleccionado == 0) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    // PASO 1: SELECCIÓN EXCLUSIVA DE EMBARCACIÓN REGISTRADA
                    item {
                        CardSeleccionEmbarcacionExclusiva(
                            uiState = uiState,
                            onSeleccionarNave = { viewModel.seleccionarEmbarcacion(it) },
                            onDeseleccionar = { viewModel.deseleccionarEmbarcacion() }
                        )
                    }

                    // PASO 2: CAPTURA FOTOGRÁFICA (Solo permitida si hay nave registrada seleccionada)
                    item {
                        if (!uiState.naveEstaSeleccionada) {
                            CardPasoBloqueado(
                                titulo = "Paso 2: Diagnóstico Fotográfico Bloqueado",
                                descripcion = "Únicamente se pueden diagnosticar embarcaciones registradas del club. Selecciona una en el selector del Paso 1 para activar la cámara."
                            )
                        } else {
                            CardCapturaCamara(
                                naveNombre = uiState.nombreNaveActiva,
                                foto = uiState.fotoOriginal,
                                evaluando = uiState.evaluando,
                                onTomarFoto = activarCamara,
                                onSeleccionarGaleria = {
                                    galleryLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                onReintentarEvaluacion = { viewModel.clasificarAveriaAutomatica() },
                                onDescartar = { viewModel.descartarFoto() }
                            )
                        }
                    }

                    // PASO 3: RESULTADOS CLASIFICADOS POR IA
                    item {
                        AnimatedVisibility(
                            visible = uiState.resultado != null,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            uiState.resultado?.let { resultado ->
                                CardResultadoGravedad(resultado = resultado)
                            }
                        }
                    }

                    // PASO 4: REGISTRO OBLIGATORIO DEL ENCARGADO DE MANTENIMIENTO
                    if (uiState.resultado != null) {
                        item {
                            CardRegistroEncargadoMantenimiento(
                                tipoDanio = uiState.tipoDanioEditado,
                                horasReparacion = uiState.horasReparacionEditadas,
                                observaciones = uiState.observaciones,
                                guardando = uiState.guardando,
                                onTipoDanioChange = { viewModel.onTipoDanioChanged(it) },
                                onHorasReparacionChange = { viewModel.onHorasReparacionChanged(it) },
                                onObservacionesChange = { viewModel.onObservacionesChanged(it) },
                                onGuardar = { viewModel.guardarReporte() }
                            )
                        }
                    }
                }
            } else {
                HistorialAveriasView(
                    cargando = uiState.cargandoHistorial,
                    reportes = uiState.reportesHistorial,
                    onVerDetalle = { reporteSeleccionadoDialog = it }
                )
            }
        }
    }

    reporteSeleccionadoDialog?.let { reporte ->
        DetalleAveriaDialog(
            reporte = reporte,
            onDismiss = { reporteSeleccionadoDialog = null },
            onCambiarEstado = { nuevoEstado ->
                viewModel.cambiarEstadoReporte(reporte.id, nuevoEstado)
                reporteSeleccionadoDialog = null
            }
        )
    }
}

/**
 * PASO 1: Selector exclusivo de embarcaciones REGISTRADAS.
 * Si un socio reportó daño en la embarcación, destaca con una NOTIFICACIÓN AL COSTADO.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardSeleccionEmbarcacionExclusiva(
    uiState: EvaluacionAveriasUiState,
    onSeleccionarNave: (Embarcacion) -> Unit,
    onDeseleccionar: () -> Unit
) {
    var dropdownExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Navy800),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.5.dp, if (uiState.naveEstaSeleccionada) Gold500 else Color(0xFFFFA000))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DirectionsBoat,
                        contentDescription = null,
                        tint = Gold500,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "1. Embarcación Registrada a Diagnosticar",
                        color = Ink,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                if (uiState.naveEstaSeleccionada) {
                    Surface(
                        color = Color(0xFF4CAF50).copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, Color(0xFF4CAF50)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "REGISTRADA",
                            color = Color(0xFF4CAF50),
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (uiState.naveEstaSeleccionada) {
                // EMBARCACIÓN YA SELECCIONADA
                Surface(
                    color = Navy900,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Navy700),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = uiState.nombreNaveActiva,
                                        color = Gold500,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    // Notificación al costado destacada si el socio reportó daño
                                    if (uiState.reporteDanioSeleccionado != null) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        BadgeNotificacionDanio()
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Matrícula: ${uiState.matriculaNaveActiva} · ${uiState.embarcacionSeleccionada?.tipo ?: "Yate"} (${uiState.embarcacionSeleccionada?.eslora ?: 0.0}m)",
                                    color = Ink,
                                    fontSize = 12.sp
                                )
                            }
                            OutlinedButton(
                                onClick = onDeseleccionar,
                                border = BorderStroke(1.dp, Navy700),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Cambiar", color = Muted, fontSize = 12.sp)
                            }
                        }

                        // Detalle de la notificación del socio
                        uiState.reporteDanioSeleccionado?.let { rep ->
                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = Navy700)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = Color(0xFFE57373),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Incidencia reportada por el Socio:",
                                    color = Color(0xFFE57373),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = rep.descripcion,
                                color = Ink,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            } else {
                // SELECTOR EXCLUSIVO DE YATES REGISTRADOS
                Text(
                    text = "Selecciona una embarcación autorizada del club. Aquellas con daños reportados por socios destacan con notificación roja al costado:",
                    color = Muted,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                ExposedDropdownMenuBox(
                    expanded = dropdownExpanded,
                    onExpandedChange = { dropdownExpanded = !dropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = "Toca para elegir embarcación registrada...",
                        onValueChange = {},
                        readOnly = true,
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.DirectionsBoat, contentDescription = null, tint = Gold500)
                        },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Gold500,
                            unfocusedBorderColor = Navy700,
                            focusedTextColor = Ink,
                            unfocusedTextColor = Ink,
                            focusedContainerColor = Navy900,
                            unfocusedContainerColor = Navy900
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    ExposedDropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false },
                        modifier = Modifier.background(Navy800)
                    ) {
                        if (uiState.embarcaciones.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("No hay embarcaciones registradas en el club", color = Muted) },
                                onClick = { dropdownExpanded = false }
                            )
                        } else {
                            uiState.embarcacionesOrdenadas.forEach { nave ->
                                val tieneReporteSocio = uiState.reporteParaNave(nave) != null

                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        nave.nombreNave,
                                                        color = Ink,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 14.sp
                                                    )
                                                    // NOTIFICACIÓN AL COSTADO
                                                    if (tieneReporteSocio) {
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        BadgeNotificacionDanio()
                                                    }
                                                }
                                                Text(
                                                    "${nave.tipo} · Matrícula: ${nave.matricula} · ${nave.eslora}m",
                                                    color = if (tieneReporteSocio) Gold500 else Muted,
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        onSeleccionarNave(nave)
                                        dropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Badge o notificación al costado que destaca a las embarcaciones con reporte de daño activo.
 */
@Composable
fun BadgeNotificacionDanio() {
    Surface(
        color = Color(0xFFE57373).copy(alpha = 0.2f),
        border = BorderStroke(1.dp, Color(0xFFE57373)),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE57373))
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.NotificationsActive,
                contentDescription = null,
                tint = Color(0xFFE57373),
                modifier = Modifier.size(11.dp)
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = "Daño Reportado",
                color = Color(0xFFE57373),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun CardPasoBloqueado(
    titulo: String,
    descripcion: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Navy800.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Navy700)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Navy900),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = Muted,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(text = titulo, color = Muted, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = descripcion, color = Muted.copy(alpha = 0.7f), fontSize = 12.sp)
            }
        }
    }
}

/**
 * PASO 2: Tarjeta para tomar la fotografía mediante la Cámara (Recurso Móvil)
 */
@Composable
fun CardCapturaCamara(
    naveNombre: String,
    foto: Bitmap?,
    evaluando: Boolean,
    onTomarFoto: () -> Unit,
    onSeleccionarGaleria: () -> Unit,
    onReintentarEvaluacion: () -> Unit,
    onDescartar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Navy800),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.5.dp, if (foto != null) Gold500 else Navy700)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null,
                    tint = Gold500,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "2. Inspección Visual de $naveNombre",
                    color = Ink,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (foto == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Navy900)
                        .border(1.dp, Navy700, RoundedCornerShape(12.dp))
                        .clickable { onTomarFoto() },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Gold500.copy(alpha = 0.15f))
                                .border(1.5.dp, Gold500, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Cámara",
                                tint = Gold500,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Tomar Foto del Daño",
                            color = Ink,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Apunta la cámara al área afectada del yate registrado",
                            color = Muted,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onTomarFoto,
                        colors = ButtonDefaults.buttonColors(containerColor = Gold500),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                    ) {
                        Icon(imageVector = Icons.Default.AddAPhoto, contentDescription = null, tint = Navy900, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cámara", color = Navy900, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onSeleccionarGaleria,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Ink),
                        border = BorderStroke(1.dp, Navy700),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Image, contentDescription = null, tint = Muted, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Galería", color = Ink)
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    Image(
                        bitmap = foto.asImageBitmap(),
                        contentDescription = "Foto avería",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    IconButton(
                        onClick = onDescartar,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Navy900.copy(alpha = 0.8f))
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Descartar", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (evaluando) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Navy900)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(color = Gold500, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = "IA clasificando gravedad . . .", color = Gold500, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(text = "Analizando fisuras y severidad naval", color = Muted, fontSize = 11.sp)
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = onTomarFoto,
                            border = BorderStroke(1.dp, Navy700),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, tint = Gold500, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Retomar", color = Ink, fontSize = 13.sp)
                        }

                        Button(
                            onClick = onReintentarEvaluacion,
                            colors = ButtonDefaults.buttonColors(containerColor = Gold500),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = Navy900, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Reanalizar IA", color = Navy900, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

/**
 * PASO 3: Diagnóstico de IA (Clasificación de gravedad)
 */
@Composable
fun CardResultadoGravedad(resultado: EvaluacionAveria) {
    val gravedad = resultado.gravedadEnum

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Navy800),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.5.dp, gravedad.color)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val icono = when (gravedad) {
                        GravedadAveria.LEVE -> Icons.Default.CheckCircle
                        GravedadAveria.MODERADA -> Icons.Default.Warning
                        GravedadAveria.GRAVE -> Icons.Default.Error
                    }
                    Icon(imageVector = icono, contentDescription = null, tint = gravedad.color, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Gravedad Sugerida por IA:",
                        color = Ink,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }

                Surface(
                    color = gravedad.color.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, gravedad.color),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = gravedad.titulo.uppercase(),
                        color = gravedad.color,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Índice de Severidad:", color = Muted, fontSize = 12.sp)
                Text(text = "${resultado.indiceSeveridad}%", color = gravedad.color, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { resultado.indiceSeveridad / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = gravedad.color,
                trackColor = Navy700
            )

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                color = Navy900,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, gravedad.color.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "ACCIÓN INMEDIATA RECOMENDADA POR IA:",
                        color = gravedad.color,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = resultado.accionInmediata, color = Ink, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text("Diagnóstico Naval IA:", color = Gold500, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = resultado.descripcionDetallada, color = Ink, fontSize = 12.sp, lineHeight = 16.sp)
        }
    }
}

/**
 * PASO 4: Formulario obligatorio para el Encargado de Mantenimiento:
 * Debe registrar tipo de daño, tiempo estimado de reparación y observaciones.
 */
@Composable
fun CardRegistroEncargadoMantenimiento(
    tipoDanio: String,
    horasReparacion: String,
    observaciones: String,
    guardando: Boolean,
    onTipoDanioChange: (String) -> Unit,
    onHorasReparacionChange: (String) -> Unit,
    onObservacionesChange: (String) -> Unit,
    onGuardar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Navy800),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.5.dp, Gold500)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Build,
                    contentDescription = null,
                    tint = Gold500,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "4. Registro Técnico del Encargado",
                    color = Ink,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Revisa y confirma los parámetros para registrar la avería en la bitácora:",
                color = Muted,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Campo: Tipo de daño (requerido)
            Text("Tipo de Daño o Falla:", color = Gold500, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = tipoDanio,
                onValueChange = onTipoDanioChange,
                placeholder = { Text("Ej. Fisura en gelcoat de proa", color = Muted) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Gold500,
                    unfocusedBorderColor = Navy700,
                    focusedTextColor = Ink,
                    unfocusedTextColor = Ink,
                    focusedContainerColor = Navy900,
                    unfocusedContainerColor = Navy900
                ),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Campo: Tiempo estimado de reparación (requerido)
            Text("Tiempo Estimado de Reparación (Horas):", color = Gold500, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = horasReparacion,
                onValueChange = onHorasReparacionChange,
                placeholder = { Text("Ej. 6", color = Muted) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = Gold500,
                        modifier = Modifier.size(18.dp)
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Gold500,
                    unfocusedBorderColor = Navy700,
                    focusedTextColor = Ink,
                    unfocusedTextColor = Ink,
                    focusedContainerColor = Navy900,
                    unfocusedContainerColor = Navy900
                ),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Campo: Observaciones del operario
            Text("Observaciones y Acciones del Taller:", color = Gold500, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = observaciones,
                onValueChange = onObservacionesChange,
                placeholder = { Text("Ej. Se traslada a dique seco para aplicación de resina epóxica...", color = Muted.copy(alpha = 0.5f)) },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Gold500,
                    unfocusedBorderColor = Navy700,
                    focusedTextColor = Ink,
                    unfocusedTextColor = Ink,
                    focusedContainerColor = Navy900,
                    unfocusedContainerColor = Navy900
                ),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = onGuardar,
                enabled = !guardando && tipoDanio.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Gold500),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                if (guardando) {
                    CircularProgressIndicator(color = Navy900, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Guardando e Iniciando Reparación...", color = Navy900, fontWeight = FontWeight.Bold)
                } else {
                    Icon(imageVector = Icons.Default.Save, contentDescription = null, tint = Navy900)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Guardar Reporte y Finalizar Inspección", color = Navy900, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}

/**
 * PESTAÑA 2: Historial de Evaluaciones de Averías
 */
@Composable
fun HistorialAveriasView(
    cargando: Boolean,
    reportes: List<EvaluacionAveria>,
    onVerDetalle: (EvaluacionAveria) -> Unit
) {
    if (cargando) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Gold500)
        }
        return
    }

    if (reportes.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Build,
                    contentDescription = null,
                    tint = Muted,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text("No hay averías inspeccionadas", color = Ink, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Usa la pestaña 'Diagnóstico Técnico' para evaluar daños.", color = Muted, fontSize = 13.sp, textAlign = TextAlign.Center)
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        items(reportes, key = { it.id }) { reporte ->
            ItemReporteAveria(reporte = reporte, onClick = { onVerDetalle(reporte) })
        }
    }
}

@Composable
fun ItemReporteAveria(
    reporte: EvaluacionAveria,
    onClick: () -> Unit
) {
    val gravedad = reporte.gravedadEnum
    val formatoFecha = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    val fechaStr = remember(reporte.fechaRegistro) {
        if (reporte.fechaRegistro > 0) formatoFecha.format(Date(reporte.fechaRegistro)) else "Fecha no reg."
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Navy800),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Navy700)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!reporte.fotoBase64.isNullOrBlank()) {
                val bitmapThumb = remember(reporte.fotoBase64) {
                    try {
                        val bytes = Base64.decode(reporte.fotoBase64, Base64.NO_WRAP)
                        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    } catch (_: Exception) {
                        null
                    }
                }

                if (bitmapThumb != null) {
                    Image(
                        bitmap = bitmapThumb.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, Navy700, RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Navy900)
                        .border(1.dp, Navy700, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsBoat,
                        contentDescription = null,
                        tint = Gold500,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = reporte.nombreNave.ifBlank { "Embarcación" },
                        color = Ink,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    Surface(
                        color = gravedad.color.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, gravedad.color),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = gravedad.titulo,
                            color = gravedad.color,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${reporte.tipoDanio} • ${reporte.estimacionHorasReparacion}h est.",
                    color = Muted,
                    fontSize = 12.sp,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = fechaStr,
                    color = Muted.copy(alpha = 0.7f),
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
fun DetalleAveriaDialog(
    reporte: EvaluacionAveria,
    onDismiss: () -> Unit,
    onCambiarEstado: (String) -> Unit
) {
    val gravedad = reporte.gravedadEnum

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Navy800,
            border = BorderStroke(1.5.dp, gravedad.color),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Detalle de Avería",
                        color = Gold500,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Cerrar", tint = Muted)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (!reporte.fotoBase64.isNullOrBlank()) {
                    val bitmap = remember(reporte.fotoBase64) {
                        try {
                            val bytes = Base64.decode(reporte.fotoBase64, Base64.NO_WRAP)
                            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        } catch (_: Exception) {
                            null
                        }
                    }

                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(10.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                Text(
                    text = "${reporte.nombreNave} (${reporte.matricula.ifBlank { "Sin matrícula" }})",
                    color = Ink,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )

                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Gravedad: ", color = Muted, fontSize = 12.sp)
                    Text(gravedad.titulo, color = gravedad.color, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text(" | Severidad: ", color = Muted, fontSize = 12.sp)
                    Text("${reporte.indiceSeveridad}%", color = Gold500, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text("Tipo de Daño: ${reporte.tipoDanio}", color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Text("Tiempo Estimado: ${reporte.estimacionHorasReparacion} horas", color = Gold500, fontSize = 12.sp)

                if (reporte.observacionesOperario.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Observaciones: ${reporte.observacionesOperario}", color = Muted, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { onCambiarEstado("en_reparacion") },
                        border = BorderStroke(1.dp, Gold500),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("En Reparación", color = Gold500, fontSize = 12.sp)
                    }

                    Button(
                        onClick = { onCambiarEstado("resuelto") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Resuelto", color = Navy900, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
