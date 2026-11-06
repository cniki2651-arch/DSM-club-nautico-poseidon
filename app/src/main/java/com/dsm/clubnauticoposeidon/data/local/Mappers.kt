package com.dsm.clubnauticoposeidon.data.local


import com.dsm.clubnauticoposeidon.data.local.entity.CategoriaServicioEntity
import com.dsm.clubnauticoposeidon.data.local.entity.ServicioEntity
import com.dsm.clubnauticoposeidon.ui.screens.Socios.PlatoItem
import com.dsm.clubnauticoposeidon.ui.screens.Socios.SuministroItemModel


fun String.aIdCategoria(): String =
    trim().lowercase().replace(Regex("\\s+"), "_").ifBlank { "sin_categoria" }

private fun categoriaDe(nombre: String) = CategoriaServicioEntity(
    id = nombre.aIdCategoria(),
    nombre = nombre.trim().ifBlank { "Sin categoría" }
)

// El ID es el del documento de Firestore (document.id)
fun PlatoItem.aServicioEntity(id: String) = ServicioEntity(
    id = id,
    categoriaId = categoria.aIdCategoria(),
    nombre = nombre,
    descripcion = descripcion,
    precio = precio,
    modulo = modulo
)

fun SuministroItemModel.aServicioEntity(id: String) = ServicioEntity(
    id = id,
    categoriaId = categoria.aIdCategoria(),
    nombre = nombre,
    descripcion = descripcion,
    precio = precio,
    modulo = modulo
)

fun PlatoItem.aCategoriaEntity() = categoriaDe(categoria)
fun SuministroItemModel.aCategoriaEntity() = categoriaDe(categoria)
