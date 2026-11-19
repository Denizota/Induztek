package cl.duoc.induztek.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import cl.duoc.induztek.ui.styles.EstilosBotonRedSocial
import cl.duoc.induztek.ui.theme.Dimens
import cl.duoc.induztek.ui.theme.FondoCampo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BotonRedSocial(
    @DrawableRes icono: Int,
    descripcion: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
){
    Surface(
        onClick = onClick,
        modifier = modifier.height(Dimens.alturaBotonRedsocial),
        shape = EstilosBotonRedSocial.forma,
        color = FondoCampo(),
        shadowElevation = Dimens.elevacionBotonRedsocial
    ) {
        Box(contentAlignment = Alignment.Center){
            Image(
                painter = painterResource(id = icono),
                contentDescription = descripcion,
                modifier = Modifier.size(Dimens.tamanoIconoRedSocial)
            )
        }
    }
}