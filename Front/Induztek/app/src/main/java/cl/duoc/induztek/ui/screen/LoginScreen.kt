
package cl.duoc.induztek.ui.screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.induztek.R
import cl.duoc.induztek.viewmodel.LoginViewModel
import cl.duoc.induztek.viewmodel.ResultadoGoogle
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch
import org.json.JSONObject
import android.accounts.AccountManager
import android.app.Activity
import android.util.Base64

@Composable
fun LoginScreen(
    onIngresoExitoso: () -> Unit,
    onSolicitarAcceso: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val contexto = LocalContext.current
    val recursos = LocalResources.current
    val alcance = rememberCoroutineScope()
    val clienteGoogle = stringResource(R.string.google_web_client_id).trim()
    var mensajeGoogle by remember { mutableStateOf<String?>(null) }
    var googleEnCurso by remember { mutableStateOf(false) }

    fun entrarConCorreo(correo: String) {
        val comoEntro = viewModel.onIngresarConGoogle(correo)
        if (comoEntro == ResultadoGoogle.ENTRAR) {
            onIngresoExitoso()
        } else if (comoEntro == ResultadoGoogle.NO_REGISTRADO) {
            mensajeGoogle = recursos.getString(R.string.google_no_registrado, correo)
        } else if (comoEntro == ResultadoGoogle.INACTIVO) {
            mensajeGoogle = recursos.getString(R.string.google_inactivo, correo)
        }
    }

    val lanzadorCuentas = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { resultado ->
        googleEnCurso = false
        if (resultado.resultCode == Activity.RESULT_OK) {
            val datos = resultado.data
            if (datos != null) {
                val correo = datos.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
                if (correo != null && correo.isNotBlank()) entrarConCorreo(correo)
            }
        }
    }

    fun entrarConGoogle() {
        if (googleEnCurso) return
        mensajeGoogle = null
        if (clienteGoogle.isBlank()) {
            googleEnCurso = true
            try {
                lanzadorCuentas.launch(
                    AccountManager.newChooseAccountIntent(
                        null, null, arrayOf("com.google"), true, null, null, null, null
                    )
                )
            } catch (e: Exception) {
                googleEnCurso = false
                mensajeGoogle = recursos.getString(R.string.google_sin_cuentas)
            }
            return
        }
        googleEnCurso = true
        alcance.launch {
            try {
                val respuesta = CredentialManager.create(contexto).getCredential(
                    context = contexto,
                    request = GetCredentialRequest.Builder()
                        .addCredentialOption(
                            GetGoogleIdOption.Builder()
                                .setFilterByAuthorizedAccounts(false)
                                .setServerClientId(clienteGoogle)
                                .setAutoSelectEnabled(false)
                                .build()
                        )
                        .build()
                )
                val credencial = respuesta.credential
                val correo = if (credencial is CustomCredential &&
                    credencial.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    correoDelToken(
                        GoogleIdTokenCredential.createFrom(credencial.data).idToken,
                        clienteGoogle
                    )
                } else {
                    null
                }

                if (correo == null) {
                    mensajeGoogle = recursos.getString(R.string.google_error)
                } else {
                    entrarConCorreo(correo)
                }
            } catch (e: GetCredentialCancellationException) {
            } catch (e: NoCredentialException) {
                mensajeGoogle = recursos.getString(R.string.google_sin_cuentas)
            } catch (e: GetCredentialException) {
                mensajeGoogle = recursos.getString(R.string.google_error)
            } finally {
                googleEnCurso = false
            }
        }
    }

    LoginContent(
        uiState = uiState,
        onCorreoCambia = viewModel::onCorreoCambia,
        onContrasenaCambia = viewModel::onContrasenaCambia,
        onAlternarVisibilidad = viewModel::onAlternarVisibilidad,
        onIngresar = {
            viewModel.onIngresar { entro ->
                if (entro) onIngresoExitoso()
            }
        },
        onSolicitarAcceso = onSolicitarAcceso,
        onGoogle = { entrarConGoogle() },
        mensajeGoogle = mensajeGoogle,
        googleEnCurso = googleEnCurso
    )
}

private fun correoDelToken(idToken: String, clienteWeb: String): String? {
    try {
        val partes = idToken.split('.')
        if (partes.size < 2) return null
        val json = JSONObject(
            String(Base64.decode(partes[1], Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING))
        )
        val emisor = json.optString("iss")
        val paraEstaApp = emisor == "https://accounts.google.com" || emisor == "accounts.google.com"
        if (!paraEstaApp || json.optString("aud") != clienteWeb) return null
        val correo = json.optString("email")
        if (correo.isBlank()) return null
        return correo
    } catch (e: Exception) {
        return null
    }
}
