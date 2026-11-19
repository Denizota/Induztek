
package cl.duoc.induztek.data

import cl.duoc.induztek.model.EstadoProtocolo
import cl.duoc.induztek.model.InicioTarea
import cl.duoc.induztek.model.Resultado

fun InduztekDbHelper.listarTareasInicio(antiguos: Boolean, idIngeniero: Int? = null): List<InicioTarea> {
    var filtro = ""
    val args = mutableListOf<Any?>()
    if (idIngeniero != null) {
        filtro = " AND (v.id_ingeniero_cargo=? OR EXISTS(" +
                "SELECT 1 FROM visita_participante vp WHERE vp.id_visita=v.id_visita AND vp.id_usuario=?))"
        args.add(idIngeniero)
        args.add(idIngeniero)
    }

    val consulta = if (antiguos) {
        "SELECT e.id_equipo,e.tag,te.nombre AS tipo,i.nombre AS instalacion,e.numero_serie," +
                "p.numero AS numero,p.estado AS estado," +
                "strftime('%d-%m-%Y',substr(p.fecha_hora_prueba,1,10)) AS fecha,0 AS atrasado, " +
                "(SELECT COUNT(*) FROM prueba pr WHERE pr.id_protocolo=p.id_protocolo) AS total_pruebas, " +
                "(SELECT COUNT(*) FROM prueba pr WHERE pr.id_protocolo=p.id_protocolo AND (pr.realizada=0 OR pr.resultado<>'APROBADO')) AS pendientes," +
                "i.id_usuario_cliente AS id_cliente,u.nombre AS cliente," +
                "IFNULL((SELECT c.nombre FROM usuario c WHERE c.id_empresa=i.id_usuario_cliente ORDER BY c.id_usuario LIMIT 1),'') AS contacto " +
                "FROM protocolo p JOIN equipo e ON e.id_equipo=p.id_equipo " +
                "JOIN tipo_equipo te ON te.id_tipo_equipo=e.id_tipo_equipo " +
                "JOIN instalacion i ON i.id_instalacion=e.id_instalacion " +
                "JOIN usuario u ON u.id_usuario=i.id_usuario_cliente " +
                "JOIN visita v ON v.id_visita=p.id_visita " +
                "WHERE p.estado IN ('FORMALIZADO','ANULADO') AND p.id_protocolo=" +
                "(SELECT MAX(q.id_protocolo) FROM protocolo q WHERE q.id_equipo=e.id_equipo AND q.estado IN ('FORMALIZADO','ANULADO'))" +
                filtro +
                " ORDER BY p.fecha_hora_prueba DESC"
    } else {
        "SELECT e.id_equipo,e.tag,te.nombre AS tipo,i.nombre AS instalacion,e.numero_serie," +
                "IFNULL(p.numero,'') AS numero,IFNULL(p.estado,'') AS estado," +
                "strftime('%d-%m-%Y',v.fecha) AS fecha, " +
                "CASE WHEN p.estado IN ('REGISTRADO','OBSERVADO') AND substr(p.fecha_hora_prueba,1,10)<date('now','localtime') THEN 1 ELSE 0 END AS atrasado, " +
                "(SELECT COUNT(*) FROM prueba pr WHERE pr.id_protocolo=p.id_protocolo) AS total_pruebas, " +
                "(SELECT COUNT(*) FROM prueba pr WHERE pr.id_protocolo=p.id_protocolo AND (pr.realizada=0 OR pr.resultado<>'APROBADO')) AS pendientes," +
                "i.id_usuario_cliente AS id_cliente,u.nombre AS cliente," +
                "IFNULL((SELECT c.nombre FROM usuario c WHERE c.id_empresa=i.id_usuario_cliente ORDER BY c.id_usuario LIMIT 1),'') AS contacto " +
                "FROM equipo e JOIN tipo_equipo te ON te.id_tipo_equipo=e.id_tipo_equipo " +
                "JOIN instalacion i ON i.id_instalacion=e.id_instalacion " +
                "JOIN usuario u ON u.id_usuario=i.id_usuario_cliente " +
                "JOIN visita v ON v.id_visita=(SELECT MAX(vi.id_visita) FROM visita vi " +
                "  WHERE vi.id_instalacion=e.id_instalacion AND vi.fecha_hora_cierre IS NULL) " +
                "LEFT JOIN protocolo p ON p.id_visita=v.id_visita AND p.id_equipo=e.id_equipo " +
                "WHERE (p.id_protocolo IS NULL OR p.estado NOT IN ('FORMALIZADO','ANULADO'))" +
                filtro +
                " ORDER BY v.fecha,e.tag"
    }

    val lista = mutableListOf<InicioTarea>()
    val registros = filas(consulta, args)
    for (filaTarea in registros) {
        val total = filaTarea.entero("total_pruebas")

        var estado: EstadoProtocolo? = null
        val codigoEstado = filaTarea["estado"]
        if (codigoEstado != null && codigoEstado.isNotEmpty()) {
            for (posible in EstadoProtocolo.values()) {
                if (posible.codigo == codigoEstado) {
                    estado = posible
                    break
                }
            }
        }

        var resultado: Resultado? = null
        if (total > 0) {
            if (filaTarea.entero("pendientes") > 0) {
                resultado = Resultado.OBSERVADO
            } else {
                resultado = Resultado.APROBADO
            }
        }

        var numero = filaTarea.texto("numero")
        if (numero.isEmpty()) numero = "N/A"

        val tarea = InicioTarea(
            idEquipo = filaTarea.entero("id_equipo"),
            tag = filaTarea.texto("tag"),
            tipoEquipo = filaTarea.texto("tipo"),
            instalacion = filaTarea.texto("instalacion"),
            idCliente = filaTarea.entero("id_cliente"),
            cliente = filaTarea.texto("cliente"),
            contacto = filaTarea.texto("contacto"),
            numeroSerie = filaTarea.texto("numero_serie"),
            numero = numero,
            fecha = filaTarea.texto("fecha"),
            estado = estado,
            resultado = resultado,
            atrasado = filaTarea.siNo("atrasado"),
            pruebasTotales = total,
            pruebasPendientes = filaTarea.entero("pendientes")
        )
        lista.add(tarea)
    }
    return lista
}
