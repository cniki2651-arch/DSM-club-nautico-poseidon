package com.dsm.clubnauticoposeidon.ui.components



import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dsm.clubnauticoposeidon.ui.theme.Gold400
import com.dsm.clubnauticoposeidon.ui.theme.Ink
import com.dsm.clubnauticoposeidon.ui.theme.Navy600
import com.dsm.clubnauticoposeidon.ui.theme.Navy700

// Medidas compartidas por los botones
val AlturaBoton = 48.dp
val MargenBoton = 32.dp

// Botón personalizado con imagen
@Composable
fun CustomButton(
    modifier: Modifier,
    painter: Painter,
    title: String,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = MargenBoton)
            .height(AlturaBoton)
            .clip(CircleShape)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        enabled = enabled,
                        onClick = onClick
                    )
                } else {
                    Modifier
                }
            )
            .background(Navy700)
            .border(
                1.dp,
                Navy600,
                CircleShape
            ),
        contentAlignment = Alignment.CenterStart
    ) {
        Image(
            painter = painter,
            contentDescription = null,
            modifier = Modifier
                .padding(start = 16.dp)
                .size(16.dp)
        )

        Text(
            text = title,
            color = Ink,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )
    }
}

// Botón personalizado con icono de huella
@Composable
fun CustomIconButton(
    modifier: Modifier,
    title: String
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = MargenBoton)
            .height(AlturaBoton)
            .clip(CircleShape)
            .background(Navy700)
            .border(
                1.dp,
                Navy600,
                CircleShape
            ),
        contentAlignment = Alignment.CenterStart
    ) {
        Icon(
            imageVector = Icons.Default.Fingerprint,
            contentDescription = null,
            tint = Gold400,
            modifier = Modifier
                .padding(start = 16.dp)
                .size(18.dp)
        )

        Text(
            text = title,
            color = Ink,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )
    }
}