package cl.duoc.induztek.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import cl.duoc.induztek.ui.theme.Dimens

//segun mis calculos de lo q hice en canva SI ES necesario. ok. gracias !!
@Composable
fun FilaVacia(salto: Dp = Dimens.salto5) = Spacer(Modifier.height(salto))
