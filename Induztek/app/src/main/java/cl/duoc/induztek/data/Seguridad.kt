package cl.duoc.induztek.data

import at.favre.lib.crypto.bcrypt.BCrypt


object Seguridad {
    private const val COSTO = 10


    fun hashear(clave: String): String =
        BCrypt.withDefaults().hashToString(COSTO, clave.toCharArray())


    fun verificar(clave: String, hash: String): Boolean =
        BCrypt.verifyer().verify(clave.toCharArray(), hash).verified
}