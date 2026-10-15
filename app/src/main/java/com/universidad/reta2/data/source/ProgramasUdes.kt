package com.universidad.reta2.data.source

import java.text.Normalizer

/**
 * Los programas academicos de la UDES Bucaramanga.
 *
 * De donde sale: bucaramanga.udes.edu.co/estudia, leido el 15/10/2026. Solo
 * pregrados, tecnologias y tecnicos laborales — ni posgrados, ni diplomados,
 * ni cursos: la app es para Saber Pro, que se presenta al terminar pregrado.
 *
 * POR QUE ESTA LISTA EXISTE. El programa se escribia a mano en el registro, y
 * en el panel aparecian «Ingenieria de software» e «Ingenieria de Software»
 * como dos carreras distintas. Lo mismo iba a pasar con «Admon», «Admin» y
 * «Negocios Internacionales». Agrupar bien eso despues es imposible sin
 * adivinar, y adivinar mal mete un error silencioso en los resultados. Se
 * arregla donde nace: si solo se puede elegir de aqui, las cadenas son
 * identicas por construccion.
 *
 * VA EMPOTRADA Y NO SE DESCARGA. El registro ocurre antes de que haya sesion
 * y puede ocurrir sin datos; una lista que hubiese que pedir al servidor
 * dejaria la pantalla inservible justo cuando mas falta hace. El precio es que
 * actualizarla pide una version nueva de la app, y para una lista que cambia
 * una vez al anio es un precio barato.
 *
 * ORTOGRAFIA. Los nombres son los de la Universidad, con tres arreglos de
 * forma que se anotan por honradez, porque son cambios sobre la fuente:
 *  - «(virtual)» de Relaciones y Negocios Internacionales se escribe
 *    «(Virtual)», como en los otros cinco.
 *  - «Tecnico laboral en Auxiliar de Educacion...» lleva Laboral en mayuscula,
 *    como sus doce hermanos.
 *  - «Galponeria» lleva tilde: Galponeria -> Galponería.
 * Un catalogo que cura la inconsistencia no puede nacer inconsistente.
 */
object ProgramasUdes {

    /** Los 33 programas de pregrado. */
    val PREGRADOS = listOf(
        "Administración de Empresas (Virtual)",
        "Administración de Negocios Internacionales",
        "Administración Financiera",
        "Antropología",
        "Bacteriología y Laboratorio Clínico",
        "Comunicación Social y Periodismo",
        "Contaduría Pública",
        "Contaduría Pública (Virtual)",
        "Derecho",
        "Economía (Virtual)",
        "Enfermería",
        "Fisioterapia",
        "Fonoaudiología",
        "Geología",
        "Ingeniería Agroindustrial",
        "Ingeniería Ambiental",
        "Ingeniería Civil",
        "Ingeniería de Software",
        "Ingeniería de Software (Virtual)",
        "Ingeniería en Ciencias de la Computación e Inteligencia Artificial",
        "Ingeniería en Energías",
        "Ingeniería Industrial",
        "Ingeniería Industrial (Virtual)",
        "Ingeniería Petroquímica",
        "Instrumentación Quirúrgica",
        "Licenciatura en Educación Infantil",
        "Medicina",
        "Medicina Veterinaria",
        "Mercadeo y Publicidad",
        "Microbiología Industrial",
        "Psicología",
        "Relaciones y Negocios Internacionales (Virtual)",
        "Terapia Ocupacional"
    )

    /** Las 3 tecnologias. */
    val TECNOLOGIAS = listOf(
        "Tecnología en Comunicación Gráfica",
        "Tecnología en Gestión Logística del Comercio Exterior",
        "Tecnología en Supervisión de Obras Civiles"
    )

    /** Los 13 tecnicos laborales. */
    val TECNICOS = listOf(
        "Técnico Intermedio en Producción de Biomateriales en el Sector Avícola, Pecuario y Otros",
        "Técnico Laboral en Auxiliar Administrativo",
        "Técnico Laboral en Auxiliar de Avalúos",
        "Técnico Laboral en Auxiliar de Educación para la Primera Infancia",
        "Técnico Laboral en Auxiliar de Servicios de Recreación y Deportes",
        "Técnico Laboral en Auxiliar de Ventas por Canales Digitales",
        "Técnico Laboral en Información Turística",
        "Técnico Laboral en Información y Servicio al Cliente",
        "Técnico Laboral en Instalación y Mantenimiento de Sistemas Solares Fotovoltaicos",
        "Técnico Laboral en Manejo de Herramientas para Codificación de Software",
        "Técnico Laboral en Servicios de Cartera y Cobranzas",
        "Técnico Laboral por Competencias en Auxiliar de Clínica Veterinaria",
        "Técnico Laboral por Competencias en Auxiliar en Galponería Avícola"
    )

    /**
     * Los 49, en el orden en que se ofrecen: primero los pregrados, que son
     * de quienes presentan Saber Pro y seran casi todos los que elijan.
     */
    val TODOS: List<String> = PREGRADOS + TECNOLOGIAS + TECNICOS

    /**
     * Quita tildes, pasa a minusculas y colapsa espacios.
     *
     * Es lo que permite que quien escriba «ingenieria softw» —sin tilde, que
     * es como se teclea de verdad en un telefono— encuentre «Ingeniería de
     * Software». Sin esto la busqueda solo serviria a quien ya escribe el
     * nombre perfecto, que es precisamente quien no necesita ayuda.
     */
    fun normalizar(texto: String): String =
        Normalizer.normalize(texto.trim().lowercase(), Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")
            .replace(Regex("\\s+"), " ")

    /**
     * Los programas que casan con lo tecleado, de mas a menos relevante.
     *
     * Primero los que EMPIEZAN por lo escrito y despues los que lo contienen
     * en medio: quien teclea «ing» espera ver las ingenierias arriba, no
     * «Licenciatura en Educación Infantil» por el «in» de Infantil.
     *
     * Se buscan todas las palabras sueltas, no la frase entera, para que
     * «software ing» encuentre «Ingeniería de Software». El orden en que uno
     * recuerda el nombre de su carrera no tiene por que ser el oficial.
     */
    fun sugerencias(escrito: String, maximo: Int = 6): List<String> {
        val busqueda = normalizar(escrito)
        if (busqueda.isEmpty()) return emptyList()

        val palabras = busqueda.split(" ").filter { it.isNotEmpty() }
        val empiezan = mutableListOf<String>()
        val contienen = mutableListOf<String>()

        for (programa in TODOS) {
            val norma = normalizar(programa)
            if (!palabras.all { norma.contains(it) }) continue
            if (norma.startsWith(palabras.first())) empiezan.add(programa)
            else contienen.add(programa)
        }
        return (empiezan + contienen).take(maximo)
    }

    /** Si lo escrito es exactamente un programa del catalogo, cual. */
    fun exacto(escrito: String): String? {
        val norma = normalizar(escrito)
        return TODOS.firstOrNull { normalizar(it) == norma }
    }
}
