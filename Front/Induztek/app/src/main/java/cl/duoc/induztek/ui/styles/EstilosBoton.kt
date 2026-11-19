package cl.duoc.induztek.ui.styles

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import cl.duoc.induztek.ui.theme.BotonDeshabilitado
import cl.duoc.induztek.ui.theme.Dimens
import cl.duoc.induztek.ui.theme.AzulMarca

object EstilosBoton {

    val forma: Shape = RoundedCornerShape(Dimens.radioBoton)

    val relleno = PaddingValues(horizontal = 20.dp, vertical = 0.dp)

    @Composable
    fun coloresPrincipal(): ButtonColors = ButtonDefaults.buttonColors(
        containerColor = AzulMarca(),
        contentColor = MaterialTheme.colorScheme.onPrimary,
        disabledContainerColor = BotonDeshabilitado(),
        disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    )

    @Composable
    fun elevacion(): ButtonElevation = ButtonDefaults.buttonElevation(
        defaultElevation = Dimens.elevacionBoton,
        pressedElevation = Dimens.elevacionBoton,
        disabledElevation = Dimens.elevacionBoton
    )
}

fun Modifier.estiloAltoBoton(): Modifier = this.height(Dimens.alturaBoton)