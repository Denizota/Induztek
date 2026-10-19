package cl.duoc.induztek.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import cl.duoc.induztek.R
import cl.duoc.induztek.ui.theme.Blanco
import cl.duoc.induztek.ui.theme.Dimens
import cl.duoc.induztek.ui.theme.FondoPantalla

@Composable
fun FondoInicio(
    modifier: Modifier = Modifier,
    contenido: @Composable BoxScope.() -> Unit
) {
    Box(modifier = modifier.fillMaxSize().background(FondoPantalla)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(Dimens.proporcionAlturaBanner)
        ) {
            Image(
                painter = painterResource(id = R.drawable.banner_inicio),
                contentDescription = null, // es decorativa
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Blanco.copy(alpha = Dimens.opacidadVelo))
            )

            //Ultra mega gugleado
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0f to Color.Transparent,
                            Dimens.inicioDifuminado to Color.Transparent,
                            1f to FondoPantalla
                        )
                    )
            )
        }
        contenido()
    }
}