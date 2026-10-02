package com.dsm.clubnauticoposeidon.utils

import android.content.Context
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore

fun poblarBaseDeDatos(context: Context) {
    val db = FirebaseFirestore.getInstance()
    val batch = db.batch() // Permite enviar múltiples documentos en una sola petición

    // 1. POBLAR RADAS (R-01 al R-20)
    for (i in 1..20) {
        val numeroFormateado = i.toString().padStart(2, '0') // 01, 02, etc.
        val codigoRada = "R-$numeroFormateado"
        val radaRef = db.collection("radas").document(codigoRada)

        val radaData = hashMapOf(
            "codigo" to codigoRada,
            "muelle" to "Muelle Principal",
            "estado" to "disponible",
            "tipo" to if (i <= 10) "yate_mediano" else "yate_grande"
        )
        batch.set(radaRef, radaData)
    }

    // 2. POBLAR CATÁLOGO DE SERVICIOS
    val servicios = listOf(
        // Catering
        hashMapOf("modulo" to "catering", "categoria" to "Piqueos", "nombre" to "Ceviche Clásico", "precio" to 45.00, "descripcion" to "Pesca del día con limón sutil.", "disponible" to true),
        hashMapOf("modulo" to "catering", "categoria" to "Bebidas", "nombre" to "Chilcano Tradicional", "precio" to 25.00, "descripcion" to "Pisco Queirolo, Ginger Ale y limón.", "disponible" to true),
        hashMapOf("modulo" to "catering", "categoria" to "Piqueos", "nombre" to "Tabla de Piqueos Náuticos", "precio" to 60.00, "descripcion" to "Mariscos y quesos.", "disponible" to true),

        // Suministros
        hashMapOf("modulo" to "suministros", "categoria" to "Consumibles", "nombre" to "Bolsas de Hielo (5kg)", "precio" to 15.00, "descripcion" to "Hielo filtrado en cubos.", "disponible" to true),
        hashMapOf("modulo" to "suministros", "categoria" to "Consumibles", "nombre" to "Agua Potable (Galones)", "precio" to 8.00, "descripcion" to "Recarga directa al tanque.", "disponible" to true),
        hashMapOf("modulo" to "suministros", "categoria" to "Combustible", "nombre" to "Balón de Gas (10kg)", "precio" to 50.00, "descripcion" to "Gas propano.", "disponible" to true),

        // Asistencia
        hashMapOf("modulo" to "asistencia", "categoria" to "Maniobras", "nombre" to "Atraque / Zarpe", "precio" to 0.00, "descripcion" to "Asistencia de marinero en muelle.", "disponible" to true)
    )

    for (servicio in servicios) {
        val servicioRef = db.collection("catalogo_servicios").document() // ID autogenerado
        batch.set(servicioRef, servicio)
    }

    // EJECUTAR EL LOTE
    batch.commit()
        .addOnSuccessListener {
            Toast.makeText(context, "¡BD poblada con éxito!", Toast.LENGTH_LONG).show()
        }
        .addOnFailureListener { e ->
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
}

fun poblarSociosYEmbarcaciones(context: Context) {
    val db = FirebaseFirestore.getInstance()
    val batch = db.batch()

    // 1. POBLAR SOCIOS DE PRUEBA (Membresía)
    val socios = listOf(
        hashMapOf("nombre" to "Carlos", "apellidos" to "Mendoza", "dni" to "11111111", "correo" to "carlos@test.com", "rol" to "socio"),
        hashMapOf("nombre" to "Lucía", "apellidos" to "Fernández", "dni" to "22222222", "correo" to "lucia@test.com", "rol" to "socio")
    )

    // Usaremos los DNIs como IDs de documento temporalmente para facilitar la relación manual
    for (socio in socios) {
        val dni = socio["dni"] as String
        val socioRef = db.collection("usuarios").document("test_uid_$dni")
        batch.set(socioRef, socio)
    }

    // 2. POBLAR EMBARCACIONES (Conectadas a los DNIs de los socios)
    val embarcaciones = listOf(
        hashMapOf("matricula" to "PT-001", "nombre_nave" to "Mar de Plata", "eslora" to 35.0, "dni_socio" to "11111111", "tipo" to "yate_mediano"),
        hashMapOf("matricula" to "PT-002", "nombre_nave" to "Viento Sur", "eslora" to 42.0, "dni_socio" to "11111111", "tipo" to "yate_grande"),
        hashMapOf("matricula" to "PT-003", "nombre_nave" to "Poseidón I", "eslora" to 28.0, "dni_socio" to "22222222", "tipo" to "lancha")
    )

    for (nave in embarcaciones) {
        val naveRef = db.collection("embarcaciones").document()
        batch.set(naveRef, nave)
    }

    batch.commit()
        .addOnSuccessListener {
            Toast.makeText(context, "Socios y Naves poblados con éxito", Toast.LENGTH_LONG).show()
        }
        .addOnFailureListener { e ->
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
}

fun poblarServiciosYPlatosAdicionales(context: Context) {
    val db = FirebaseFirestore.getInstance()
    val batch = db.batch()

    // Lista exclusiva con nuevos platos del menú y nuevos servicios que no estaban en el primer script
    val elementosAdicionales = listOf(
        // --- NUEVOS PLATOS (CATERING) ---
        // Desayunos
        hashMapOf("modulo" to "catering", "categoria" to "Desayunos", "nombre" to "Huevos Benedictinos con Salmón", "precio" to 35.00, "descripcion" to "Huevos pochados sobre pan muffin con salmón ahumado y salsa holandesa.", "disponible" to true),
        hashMapOf("modulo" to "catering", "categoria" to "Desayunos", "nombre" to "Desayuno Náutico (Chicharrón)", "precio" to 28.00, "descripcion" to "Clásico pan con chicharrón, camote frito y salsa criolla. Incluye café.", "disponible" to true),
        hashMapOf("modulo" to "catering", "categoria" to "Desayunos", "nombre" to "Bowl de Acai y Frutas", "precio" to 24.00, "descripcion" to "Mix de frutos rojos, acai, plátano, granola artesanal y miel.", "disponible" to true),

        // Piqueos adicionales
        hashMapOf("modulo" to "catering", "categoria" to "Piqueos", "nombre" to "Pulpo al Olivo", "precio" to 48.00, "descripcion" to "Láminas de pulpo bañadas en suave crema de aceitunas de botija con galletas.", "disponible" to true),
        hashMapOf("modulo" to "catering", "categoria" to "Piqueos", "nombre" to "Tequeños de Lomo Saltado", "precio" to 32.00, "descripcion" to "Masa wantán frita rellena de lomo saltado, acompañada de salsa de palta.", "disponible" to true),

        // Platos Fuertes
        hashMapOf("modulo" to "catering", "categoria" to "Platos Fuertes", "nombre" to "Lomo Saltado Poseidón", "precio" to 58.00, "descripcion" to "Fino corte de lomo flambeado al pisco, con papas nativas crujientes y arroz.", "disponible" to true),
        hashMapOf("modulo" to "catering", "categoria" to "Platos Fuertes", "nombre" to "Arroz con Mariscos", "precio" to 52.00, "descripcion" to "Arroz cremoso con selección de mariscos, toque de ají amarillo y chicha de jora.", "disponible" to true),
        hashMapOf("modulo" to "catering", "categoria" to "Platos Fuertes", "nombre" to "Pesca del Día a la Parrilla", "precio" to 55.00, "descripcion" to "Filete de pescado fresco a la parrilla con guarnición de vegetales salteados.", "disponible" to true),
        hashMapOf("modulo" to "catering", "categoria" to "Platos Fuertes", "nombre" to "Fetuccini a la Huancaína con Lomo", "precio" to 50.00, "descripcion" to "Pasta artesanal en salsa huancaína acompañada de medallones de lomo.", "disponible" to true),

        // Postres
        hashMapOf("modulo" to "catering", "categoria" to "Postres", "nombre" to "Suspiro a la Limeña", "precio" to 20.00, "descripcion" to "Clásico postre con manjar blanco de yemas y merengue al oporto.", "disponible" to true),
        hashMapOf("modulo" to "catering", "categoria" to "Postres", "nombre" to "Volcán de Chocolate", "precio" to 25.00, "descripcion" to "Bizcocho de chocolate caliente con centro líquido y helado de vainilla.", "disponible" to true),

        // Bebidas adicionales
        hashMapOf("modulo" to "catering", "categoria" to "Bebidas", "nombre" to "Pisco Sour Catedral", "precio" to 30.00, "descripcion" to "Doble medida de Pisco Quebranta, limón, jarabe de goma y clara de huevo.", "disponible" to true),
        hashMapOf("modulo" to "catering", "categoria" to "Bebidas", "nombre" to "Limonada Frozen con Menta", "precio" to 15.00, "descripcion" to "Refrescante limonada licuada con hielo y hojas de menta fresca.", "disponible" to true),
        hashMapOf("modulo" to "catering", "categoria" to "Bebidas", "nombre" to "Cerveza Artesanal IPA", "precio" to 22.00, "descripcion" to "Cerveza artesanal peruana de amargor intenso y notas cítricas.", "disponible" to true),

        // --- NUEVOS SUMINISTROS ---
        hashMapOf("modulo" to "suministros", "categoria" to "Combustible", "nombre" to "Balón de Gas (10kg)", "precio" to 50.00, "descripcion" to "Gas propano para cocina a bordo.", "disponible" to true),
        hashMapOf("modulo" to "suministros", "categoria" to "Combustible", "nombre" to "Recarga de Batería 12V", "precio" to 45.00, "descripcion" to "Servicio de asistencia eléctrica y carga.", "disponible" to true),
        hashMapOf("modulo" to "suministros", "categoria" to "Combustible", "nombre" to "Aceite de Motor Marino", "precio" to 85.00, "descripcion" to "Lubricante especializado para motores fuera de borda.", "disponible" to true),
        hashMapOf("modulo" to "suministros", "categoria" to "Limpieza", "nombre" to "Kit de Limpieza Biodegradable", "precio" to 60.00, "descripcion" to "Productos ecológicos para cuidado de gelcoat y fibra de vidrio.", "disponible" to true),
        hashMapOf("modulo" to "suministros", "categoria" to "Limpieza", "nombre" to "Bolsas de Basura Industriales", "precio" to 12.00, "descripcion" to "Paquete de 10 unidades de alta resistencia.", "disponible" to true),

        // --- NUEVAS ASISTENCIAS / SERVICIOS DE MUELLE ---
        hashMapOf("modulo" to "asistencia", "categoria" to "Maniobras", "nombre" to "Revisión de Cabos", "precio" to 0.00, "descripcion" to "Inspección de amarras, boyas y defensas en rada.", "disponible" to true),
        hashMapOf("modulo" to "asistencia", "categoria" to "Maniobras", "nombre" to "Remolque Menor", "precio" to 0.00, "descripcion" to "Servicio de remolque de emergencia dentro de la dársena.", "disponible" to true),
        hashMapOf("modulo" to "asistencia", "categoria" to "Maniobras", "nombre" to "Traslado en Bote", "precio" to 0.00, "descripcion" to "Transporte rápido desde el muelle principal hacia embarcación fondeada.", "disponible" to true)
    )

    for (elemento in elementosAdicionales) {
        val ref = db.collection("catalogo_servicios").document()
        batch.set(ref, elemento)
    }

    batch.commit()
        .addOnSuccessListener {
            Toast.makeText(context, "¡Catálogo y servicios ampliados con éxito!", Toast.LENGTH_LONG).show()
        }
        .addOnFailureListener { e ->
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
}