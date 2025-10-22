package com.universidad.reta2.data.source

import com.universidad.reta2.domain.models.Question
import com.universidad.reta2.domain.models.QuestionOption

object CompetencyData {

    fun getQuestionsByCompetenceAndLevel(competenceId: String, levelId: Int): List<Question> {
        return when (competenceId) {
            "lectura_critica" -> getLecturaCriticaQuestions(levelId)
            "razonamiento_cuantitativo" -> getRazonamientoCuantitativoQuestions(levelId)
            "ingles" -> getInglesQuestions(levelId)
            "competencias_ciudadanas" -> getCompetenciasCiudadanasQuestions(levelId)
            else -> emptyList()
        }
    }

    // Lectura Crítica - Nivel 1
    private fun getLecturaCriticaQuestions(levelId: Int): List<Question> {
        return when (levelId) {
            1 -> listOf(
                Question(
                    id = 101,
                    text = "Según el texto, para que la atención primaria en salud, por parte de la medicina oficial, pueda atender a más del 20 % de la población, sería necesario principalmente:",
                    options = listOf(
                        QuestionOption(id = 1, text = "Promover sistemas de financiación adecuados y equitativos que canalicen los recursos necesarios para lograr una atención de mejor calidad y con mayor cobertura.", originalOrder = 0),
                        QuestionOption(id = 2, text = "Prever el envejecimiento demográfico de la población, las estructuras familiares cambiantes y los mercados de trabajo informales.", originalOrder = 1),
                        QuestionOption(id = 3, text = "Respetar el derecho a los servicios de salud, las diferencias culturales y los principios de igualdad y no discriminación.", originalOrder = 2),
                        QuestionOption(id = 4, text = "Crear un sistema de información con un enfoque de Derechos Humanos que permita evaluar los avances en salud.", originalOrder = 3)
                    ),
                    correctOptionId = 1,
                    explanation = "El texto indica que la medicina oficial cubre apenas al 20% de la población debido a problemas de acceso y aseguramiento, especialmente para la población pobre."
                ),
                Question(
                    id = 102,
                    text = "Para alcanzar los propósitos de la doctrina, Harry Truman proponía como estrategia:",
                    options = listOf(
                        QuestionOption(id = 1, text = "Elevar los niveles de industrialización y urbanización.", originalOrder = 0),
                        QuestionOption(id = 2, text = "Tratar equitativamente a todas las naciones y pueblos del planeta.", originalOrder = 1),
                        QuestionOption(id = 3, text = "Producir más fortaleciendo la alianza entre capital, ciencia y tecnología.", originalOrder = 2),
                        QuestionOption(id = 4, text = "Mitigar las condiciones de pobreza, hambre y miseria en todas las áreas del globo.", originalOrder = 3)
                    ),
                    correctOptionId = 3,
                    explanation = "Truman propone que 'la clave para producir más es una aplicación mayor y más vigorosa del conocimiento técnico y científico moderno'."
                ),
                Question(
                    id = 103,
                    text = "De acuerdo con Arturo Escobar, el propósito de la doctrina Truman era:",
                    options = listOf(
                        QuestionOption(id = 1, text = "Crear condiciones para reproducir en todo el mundo los rasgos característicos de las sociedades avanzadas.", originalOrder = 0),
                        QuestionOption(id = 2, text = "Adoptar en el primer mundo el sueño americano de paz y abundancia, la educación y los valores culturales modernos.", originalOrder = 1),
                        QuestionOption(id = 3, text = "Iniciar una nueva era en la comprensión y manejo de los asuntos de los países económicamente más avanzados.", originalOrder = 2),
                        QuestionOption(id = 4, text = "Generar altos niveles de industrialización y urbanización, tecnificando la agricultura de los países desarrollados.", originalOrder = 3)
                    ),
                    correctOptionId = 3,
                    explanation = "El texto indica que la doctrina Truman inició 'una nueva era en la comprensión y el manejo de los asuntos mundiales'."
                )
                // Agrega aquí las demás preguntas del nivel 1...
            )
            2 -> listOf(
                Question(
                    id = 201,
                    text = "La promoción de estudios académicos para determinar cuáles de las prácticas usadas por la medicina popular son perjudiciales y cuáles brindan alternativas de salud implica que:",
                    options = listOf(
                        QuestionOption(id = 1, text = "Se prolongue la inconformidad de quienes practican la medicina oficial.", originalOrder = 0),
                        QuestionOption(id = 2, text = "Se inicien procesos de regulación oficiales sobre este tipo de prácticas.", originalOrder = 1),
                        QuestionOption(id = 3, text = "Se derogue la ley de que la medicina científica es la única oficial.", originalOrder = 2),
                        QuestionOption(id = 4, text = "Se extienda la brecha entre la medicina química y la medicina botánica.", originalOrder = 3)
                    ),
                    correctOptionId = 2,
                    explanation = "El texto sugiere que estudiar las prácticas populares llevaría a determinar cuáles son beneficiosas y cuáles perjudiciales, lo que implicaría regulación."
                )
                // Agrega aquí las demás preguntas del nivel 2...
            )
            3 -> listOf(
                Question(
                    id = 301,
                    text = "Mafalda le da a la tortuga el nombre \"Burocracia\" con el propósito de destacar que la burocracia es:",
                    options = listOf(
                        QuestionOption(id = 1, text = "Silenciosa y tranquila en su comportamiento.", originalOrder = 0),
                        QuestionOption(id = 2, text = "Tímida para relacionarse con personas desconocidas.", originalOrder = 1),
                        QuestionOption(id = 3, text = "Organizada para administrar su tiempo y el de los demás.", originalOrder = 2),
                        QuestionOption(id = 4, text = "Caprichosa en la organización de los horarios.", originalOrder = 3)
                    ),
                    correctOptionId = 4,
                    explanation = "Mafalda usa la tortuga para criticar la lentitud y el comportamiento caprichoso de la burocracia."
                )
                // Agrega aquí las demás preguntas del nivel 3...
            )
            else -> emptyList()
        }
    }

    // Razonamiento Cuantitativo
    private fun getRazonamientoCuantitativoQuestions(levelId: Int): List<Question> {
        return when (levelId) {
            1 -> listOf(
                Question(
                    id = 401,
                    text = "Un sismólogo afirma que en cualquier año era más probable que hubiese sismos de baja que de alta magnitud. Según el registro histórico, la relación que justifica la opinión del sismólogo es:",
                    options = listOf(
                        QuestionOption(id = 1, text = "A mayor magnitud, mayor cantidad de sismos.", originalOrder = 0),
                        QuestionOption(id = 2, text = "A mayor magnitud, menor cantidad de sismos.", originalOrder = 1),
                        QuestionOption(id = 3, text = "A mayor cantidad de sismos, menor magnitud de estos.", originalOrder = 2),
                        QuestionOption(id = 4, text = "A mayor cantidad de sismos, mayor magnitud de estos.", originalOrder = 3)
                    ),
                    correctOptionId = 2,
                    explanation = "La tabla muestra que a mayor magnitud, menor cantidad de sismos registrados."
                )
                // Agrega aquí las demás preguntas de razonamiento nivel 1...
            )
            2 -> listOf(
                // Preguntas nivel 2...
            )
            3 -> listOf(
                // Preguntas nivel 3...
            )
            else -> emptyList()
        }
    }

    // Inglés
    private fun getInglesQuestions(levelId: Int): List<Question> {
        return when (levelId) {
            1 -> listOf(
                Question(
                    id = 701,
                    text = "People often cry when they feel like this.",
                    options = listOf(
                        QuestionOption(id = 1, text = "afraid", originalOrder = 0),
                        QuestionOption(id = 2, text = "angry", originalOrder = 1),
                        QuestionOption(id = 3, text = "cold", originalOrder = 2),
                        QuestionOption(id = 4, text = "happy", originalOrder = 3),
                        QuestionOption(id = 5, text = "hungry", originalOrder = 4),
                        QuestionOption(id = 6, text = "sad", originalOrder = 5),
                        QuestionOption(id = 7, text = "thirsty", originalOrder = 6),
                        QuestionOption(id = 8, text = "tired", originalOrder = 7)
                    ),
                    correctOptionId = 6
                ),
                Question(
                    id = 702,
                    text = "If we feel like this, we want to eat something.",
                    options = listOf(
                        QuestionOption(id = 1, text = "afraid", originalOrder = 0),
                        QuestionOption(id = 2, text = "angry", originalOrder = 1),
                        QuestionOption(id = 3, text = "cold", originalOrder = 2),
                        QuestionOption(id = 4, text = "happy", originalOrder = 3),
                        QuestionOption(id = 5, text = "hungry", originalOrder = 4),
                        QuestionOption(id = 6, text = "sad", originalOrder = 5),
                        QuestionOption(id = 7, text = "thirsty", originalOrder = 6),
                        QuestionOption(id = 8, text = "tired", originalOrder = 7)
                    ),
                    correctOptionId = 5
                )
                // Agrega aquí las demás preguntas de inglés...
            )
            2 -> listOf(
                // Preguntas nivel 2...
            )
            3 -> listOf(
                // Preguntas nivel 3...
            )
            else -> emptyList()
        }
    }

    // Competencias Ciudadanas
    private fun getCompetenciasCiudadanasQuestions(levelId: Int): List<Question> {
        return when (levelId) {
            1 -> listOf(
                Question(
                    id = 1001,
                    text = "Teniendo en cuenta lo planteado en la Constitución Política de Colombia, esta respuesta de la ciudadanía refleja:",
                    options = listOf(
                        QuestionOption(id = 1, text = "la incapacidad del Gobierno nacional para atender la emergencia.", originalOrder = 0),
                        QuestionOption(id = 2, text = "la deficiencia en la infraestructura nacional para la prevención de desastres.", originalOrder = 1),
                        QuestionOption(id = 3, text = "la inobservancia del principio de solidaridad por parte de los ciudadanos.", originalOrder = 2),
                        QuestionOption(id = 4, text = "la falta de confianza en las instituciones públicas.", originalOrder = 3)
                    ),
                    correctOptionId = 3,
                    explanation = "La Constitución establece el principio de solidaridad como uno de los fundamentos del Estado social de derecho."
                )
                // Agrega aquí las demás preguntas de competencias ciudadanas...
            )
            2 -> listOf(
                // Preguntas nivel 2...
            )
            3 -> listOf(
                // Preguntas nivel 3...
            )
            else -> emptyList()
        }
    }
}