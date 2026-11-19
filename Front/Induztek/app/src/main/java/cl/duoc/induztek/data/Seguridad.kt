package cl.duoc.induztek.data

import at.favre.lib.crypto.bcrypt.BCrypt

object Seguridad {

    private const val COSTO = 10

    fun hashear(clave: String): String {
        return BCrypt.withDefaults().hashToString(COSTO, clave.toCharArray())
    }

    fun verificar(clave: String, claveCifrada: String): Boolean {
        return BCrypt.verifyer().verify(clave.toCharArray(), claveCifrada).verified
    }
}
