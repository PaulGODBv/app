package com.universidad.reta2.data.source

import com.universidad.reta2.domain.models.Question
import com.universidad.reta2.domain.models.QuestionOption

object CompetencyData {

    fun getQuestionsByCompetenceAndLevel(competenceId: Int, levelId: Int): List<Question> {
        return when (competenceId) {
            1 -> getLecturaCriticaQuestions(levelId)
            2 -> getRazonamientoCuantitativoQuestions(levelId)
            3 -> getInglesQuestions(levelId)
            4 -> getCompetenciasCiudadanasQuestions(levelId)
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
                    explanation = "El texto indica que la medicina oficial cubre apenas al 20% de la población debido a problemas de acceso y aseguramiento, especialmente para la población pobre.",
                    readingText = "La medicina popular colombiana conserva una amplia serie de conocimientos empíricos sobre gran diversidad de recursos botánicos, que han sido esenciales para el cuidado de la salud. Pero, con la oficialización de la medicina a finales del siglo XIX, la creación de las primeras escuelas de medicina y de farmacia y la creación de una legislación que buscaba regular estas disciplinas, muchos de estos conocimientos fueron rechazados al igual que sus prácticas médicas.\n\nParadójicamente, mientras la medicina oficial negaba los conocimientos populares, se servía de ellos para desarrollar muchos de los avances farmacéuticos de los que se vale la medicina oficial para sus tratamientos.\n\nPor otro lado, según la Organización Mundial de la Salud, la atención primaria en salud por parte de la medicina oficial cubre apenas a un 20 % de la población en los países en vías de desarrollo, mientras que el 80 % tiene que recurrir a otras prácticas médicas populares y lo hace en gran parte por la poca garantía de acceso y de aseguramiento de la población pobre.\n\nLo anterior sigue generando inconformidad en quienes practican la medicina oficial, pues consideran que la medicina popular no representa un conocimiento válido porque no se fundamenta en pruebas científicas y no está regulada. Sin embargo, ambas medicinas pueden llegar a ser complementarias si se estudian de manera juiciosa y se determinan cuáles de las prácticas y de los recursos botánicos usados por la medicina popular son perjudiciales y cuáles brindan alternativas de salud a las poblaciones necesitadas.\n\nDe esta manera, también se promueve la inclusión, el reconocimiento y el respeto por la diversidad cultural y por los conocimientos populares.\n\nTomado y adaptado de: Prieto Gaona, Oriana (2011). Proyecto de investigación sobre medicina popular."
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
                    explanation = "Truman propone que 'la clave para producir más es una aplicación mayor y más vigorosa del conocimiento técnico y científico moderno'.",
                    readingText = "En su discurso de posesión como presidente de Estados Unidos el 20 de enero de 1949, Harry Truman anunció al mundo entero su concepto de \"trato justo\"." +
                            "\n\nUn componente esencial del concepto era su llamado a Estados Unidos y al mundo para resolver los problemas de las \"áreas subdesarrolladas\" del globo:\n" +
                            "\nLa doctrina Truman inició una nueva era en la comprensión y el manejo de los asuntos mundiales, en particular de aquellos que se referían a los países económicamente menos avanzados.\n" +
                            "\nEl propósito era bastante ambicioso: crear las condiciones necesarias para reproducir en todo el mundo los rasgos característicos de las sociedades avanzadas de la época: altos niveles de industrialización y urbanización, " +
                            "tecnificación de la agricultura, rápido crecimiento de la producción material y de los niveles de vida, y adopción generalizada de la educación y los valores culturales modernos.\n\nEn concepto de Truman, el capital, la ciencia y la tecnología eran " +
                            "los principales componentes que harían posible tal revolución masiva. Solo así el sueño americano de paz y abundancia podría extenderse a todos los pueblos del planeta.\n\n\"Más de la mitad de la población del mundo vive en condiciones cercanas a la miseria. " +
                            "Su alimentación es inadecuada, es víctima de la enfermedad. Su vida económica es primitiva y está estancada. Su pobreza constituye un obstáculo y una amenaza tanto para ellos como para las áreas más prósperas." +
                            "\n\nPor primera vez en la historia, la humanidad posee el conocimiento y la capacidad para aliviar el sufrimiento de estas gentes… Creo que deberíamos poner a disposición de los amantes de la paz los beneficios de nuestro acervo de conocimiento " +
                            "técnico para ayudarlos a lograr sus aspiraciones de una vida mejor…\n\nLo que tenemos en mente es un programa de desarrollo basado en los conceptos del trato justo y democrático… " +
                            "Producir más es la clave para la paz y la prosperidad. Y la clave para producir más es una aplicación mayor y más vigorosa del conocimiento técnico y científico moderno." +
                            "\"\n\n(Truman, 1964; citado en \"La invención del Tercer Mundo\", de Arturo Escobar)"
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
                    correctOptionId = 1,
                    explanation = "El texto indica que la doctrina Truman inició 'una nueva era en la comprensión y el manejo de los asuntos mundiales'.",
                    readingText = "En su discurso de posesión como presidente de Estados Unidos el 20 de enero de 1949, Harry Truman anunció al mundo entero su concepto de \"trato justo\"." +
                            "\n\nUn componente esencial del concepto era su llamado a Estados Unidos y al mundo para resolver los problemas de las \"áreas subdesarrolladas\" del globo:\n" +
                            "\nLa doctrina Truman inició una nueva era en la comprensión y el manejo de los asuntos mundiales, en particular de aquellos que se referían a los países económicamente menos avanzados.\n" +
                            "\nEl propósito era bastante ambicioso: crear las condiciones necesarias para reproducir en todo el mundo los rasgos característicos de las sociedades avanzadas de la época: altos niveles de industrialización y urbanización, " +
                            "tecnificación de la agricultura, rápido crecimiento de la producción material y de los niveles de vida, y adopción generalizada de la educación y los valores culturales modernos.\n\nEn concepto de Truman, el capital, la ciencia y la tecnología eran " +
                            "los principales componentes que harían posible tal revolución masiva. Solo así el sueño americano de paz y abundancia podría extenderse a todos los pueblos del planeta.\n\n\"Más de la mitad de la población del mundo vive en condiciones cercanas a la miseria. " +
                            "Su alimentación es inadecuada, es víctima de la enfermedad. Su vida económica es primitiva y está estancada. Su pobreza constituye un obstáculo y una amenaza tanto para ellos como para las áreas más prósperas." +
                            "\n\nPor primera vez en la historia, la humanidad posee el conocimiento y la capacidad para aliviar el sufrimiento de estas gentes… Creo que deberíamos poner a disposición de los amantes de la paz los beneficios de nuestro acervo de conocimiento " +
                            "técnico para ayudarlos a lograr sus aspiraciones de una vida mejor…\n\nLo que tenemos en mente es un programa de desarrollo basado en los conceptos del trato justo y democrático… " +
                            "Producir más es la clave para la paz y la prosperidad. Y la clave para producir más es una aplicación mayor y más vigorosa del conocimiento técnico y científico moderno." +
                            "\"\n\n(Truman, 1964; citado en \"La invención del Tercer Mundo\", de Arturo Escobar)"
                ),
                Question(
                    id = 104,
                    text = "La bicicleta averiada le da a Felipe una oportunidad de:",
                    options = listOf(
                        QuestionOption(id = 1, text = "Insultar a su padre.", originalOrder = 0),
                        QuestionOption(id = 2, text = "Buscar el apoyo de su padre.", originalOrder = 1),
                        QuestionOption(id = 3, text = "Mostrar su resentimiento contra Isabel.", originalOrder = 2),
                        QuestionOption(id = 4, text = "Demostrarle a Isabel que él es casi un adulto.", originalOrder = 3)
                    ),
                    correctOptionId = 3,
                    explanation = "Felipe usa el incidente para expresar su resentimiento contra Isabel, culpándola y exigiendo que le compre una nueva bicicleta.",
                    readingText = "Recientemente, Isabel se convirtió en madrastra de Felipe. Ella acaba de estrellar accidentalmente la bicicleta de Felipe porque él la dejó a la entrada del garaje. Felipe entra en la cocina con la llanta trasera de su bicicleta doblada." +
                            "\n\nFelipe: Te anotaste un punto. ¡Gracias por dejarme sin bicicleta!\nIsabel: ¿Y cómo querías que la viera?\nFelipe: ¡Pues usando el espejo retrovisor, así se hace!\nIsabel: Perdón, pero escuché a tu padre decirte que dejaras tu bicicleta lejos." +
                            "\nFelipe: Ah. (Se desploma en una silla y se sienta con la cabeza inclinada.)\nIsabel: Bueno, ¿y cómo te fue en el colegio hoy? (Le pasa una gaseosa.)\nFelipe: (Hace ruido de desprecio.)\nIsabel: ¿Hiciste educación física hoy?\nFelipe: No.\nIsabel: Entonces apuesto a que tuviste matemáticas." +
                            "\nFelipe: Sí. ¿Y qué?\nIsabel: ¿Y qué hiciste…?\n(En este momento entra el padre de Felipe.)\nFelipe: (Sosteniendo su llanta doblada en las manos) ¡Mira lo que ella hizo!\nPadre: En primer lugar, por favor, no le digas a Isabel \"ella\". Eso es muy grosero. " +
                            "En segundo lugar, te he dicho que no dejes tu bicicleta a la entrada del garaje. " +
                            "No me sorprende que eso haya pasado.\nFelipe: ¿Por qué siempre me echan la culpa? ¿Por qué no le dices a ella que tenga más cuidado? ¡Ella… Isabel me tiene que " +
                            "comprar una nueva!\nPadre: ¡Por nada del mundo! Puedes empezar a ahorrar para comprar una, y a lo mejor la cuidarás más cuando la compres con tu propio dinero." +
                            "\nFelipe: Te advierto, me voy a ir de la casa apenas tenga la edad para hacerlo. ¡Eres la persona más tacaña que haya conocido!\n(Se va dando un portazo.)"
                ),
                Question(
                    id = 105,
                    text = "Cuando Helena está discutiendo con David, ella:",
                    options = listOf(
                        QuestionOption(id = 1, text = "Enfatiza sus puntos de vista.", originalOrder = 0),
                        QuestionOption(id = 2, text = "Indaga por los intereses del otro.", originalOrder = 1),
                        QuestionOption(id = 3, text = "Se concentra en la búsqueda de un acuerdo.", originalOrder = 2),
                        QuestionOption(id = 4, text = "Convierte la discusión en un ataque personal.", originalOrder = 3)
                    ),
                    correctOptionId = 1,
                    explanation = "Helena enfatiza sus preferencias estéticas ('arbustos con buena floración', 'camelias', 'celindas') y critica la elección de David.",
                    readingText = "El siguiente pasaje es tomado de una novela ambientada en la década de 1930. David y su esposa, Helena, han estado viviendo en una urbanización nueva, " +
                            "La Arboleda. David es el que habla en el pasaje.\n\nCuando regresé a La Arboleda descargué el pequeño árbol a la entrada cerca de la puerta principal y " +
                            "me dirigí a la cocina, y Helena dijo:\n\n\"David, ¿dónde diablos has estado? No me dijiste que ibas a salir. Pensé que aún estabas trabajando en el techo " +
                            "cuando te llamé para que tomaras una taza de té.\"\n\n— \"Me fui y compré un árbol\", le dije.\n\n— \"¿Un árbol?\"\n\n— \"Un árbol para el jardín. " +
                            "Fui a los viveros de La Siembra Verde. Solamente costó seis chelines\", agregué orgullosamente.\n\n— \"'¡Maravilloso, David!' exclamó ella. '¿Dónde está?'" +
                            "\"\n\n— \"Afuera, a la entrada. Ven a verlo.\"\n\nSu expresión cambió cuando lo vio, y tengo que admitir que se veía más bien sucio, débil y gris, con sus " +
                            "raíces apretadas como un nudo de estopa mojada sin forma.\n\n— \"Sí, pero ¿qué es?\", preguntó ella. \"Parece un árbol de caucho.\"\n\n— \"Es un árbol de " +
                            "caucho. Caucho de azúcar.\"\n\n— \"¡Oh!\", dijo ella, y por unos cuantos segundos se quedó en blanco, y luego dijo: \"¿Dónde vas a ponerlo?, es decir, " +
                            "¿dónde quieres que crezca?\"\n\n— \"Allá.\" Le señalé. \"¡Justo allá, en toda la mitad del jardín!\"\n\n— \"¡Oh!\"\n\n— \"¿Por qué solo dices: " +
                            "'Oh'?\"\n\n— \"Por nada… Es decir, si va a quedar allá, justo en la mitad de todo, yo hubiera pensado en algo más pequeño, o incluso…\"\n\n— \"¿Qué tiene de " +
                            "malo el árbol de caucho?\"\n\n— \"Bueno, si quieres saberlo, querido, personalmente creo que son más bien ordinarios. Son tan monótonos, David. Honestamente " +
                            "prefiero algo decorativo, especialmente para allá, justo en el frente de la casa; algo como arbustos con buena floración, o camelias, o celindas. Lo que se " +
                            "vería hermoso, sería uno de esos arces japoneses enanos.\"\n\n— \"¡Ni se te ocurra, querida! ¡No quiero nada enano! Yo quiero un árbol. ¡Un árbol de verdad! " +
                            "¿Te das cuenta? No hay ni un solo árbol en toda esta maldita calle, quizás ni siquiera en la ciudad.\""
                ),
                Question(
                    id = 106,
                    text = "Helena cambió su expresión porque:",
                    options = listOf(


                        QuestionOption(id = 1, text = "David compró un árbol.", originalOrder = 0),
                        QuestionOption(id = 2, text = "El árbol le pareció costoso.", originalOrder = 1),
                        QuestionOption(id = 3, text = "El árbol no tenía la mejor apariencia.", originalOrder = 2),
                        QuestionOption(id = 4, text = "David quería sembrar el árbol en el jardín.", originalOrder = 3)
                    ),
                    correctOptionId = 3,
                    explanation = "El texto describe que el árbol se veía 'sucio, débil y gris con sus raíces apretadas como un nudo de estopa mojada sin forma'.",
                    readingText = "El siguiente pasaje es tomado de una novela ambientada en la década de 1930. David y su esposa, Helena, han estado viviendo en una " +
                            "urbanización nueva, La Arboleda. David es el que habla en el pasaje.\n\nCuando regresé a La Arboleda descargué el pequeño árbol a la entrada" +
                            " cerca de la puerta principal y me dirigí a la cocina, y Helena dijo:\n\n\"David, ¿dónde diablos has estado? No me dijiste que ibas a salir. " +
                            "Pensé que aún estabas trabajando en el techo cuando te llamé para que tomaras una taza de té.\"\n\n— \"Me fui y compré un árbol\", " +
                            "le dije.\n\n— \"¿Un árbol?\"\n\n— \"Un árbol para el jardín. Fui a los viveros de La Siembra Verde. Solamente costó seis chelines\", " +
                            "agregué orgullosamente.\n\n— \"'¡Maravilloso, David!' exclamó ella. '¿Dónde está?'\"\n\n— \"Afuera, a la entrada. Ven a verlo.\"\n\nSu " +
                            "expresión cambió cuando lo vio, y tengo que admitir que se veía más bien sucio, débil y gris, con sus raíces apretadas como un nudo de " +
                            "estopa mojada sin forma.\n\n— \"Sí, pero ¿qué es?\", preguntó ella. \"Parece un árbol de caucho.\"\n\n— \"Es un árbol de caucho. Caucho de " +
                            "azúcar.\"\n\n— \"¡Oh!\", dijo ella, y por unos cuantos segundos se quedó en blanco, y luego dijo: \"¿Dónde vas a ponerlo?, es decir, " +
                            "¿dónde quieres que crezca?\"\n\n— \"Allá.\" Le señalé. \"¡Justo allá, en toda la mitad del jardín!\"\n\n— \"¡Oh!\"\n\n— \"¿Por qué solo " +
                            "dices: 'Oh'?\"\n\n— \"Por nada… Es decir, si va a quedar allá, justo en la mitad de todo, yo hubiera pensado en algo más pequeño, o " +
                            "incluso…\"\n\n— \"¿Qué tiene de malo el árbol de caucho?\"\n\n— \"Bueno, si quieres saberlo, querido, personalmente creo que son más bien " +
                            "ordinarios. Son tan monótonos, David. Honestamente prefiero algo decorativo, especialmente para allá, justo en el frente de la casa; algo " +
                            "como arbustos con buena floración, o camelias, o celindas. Lo que se vería hermoso, sería uno de esos arces japoneses enanos.\"\n\n— \"¡Ni " +
                            "se te ocurra, querida! ¡No quiero nada enano! Yo quiero un árbol. ¡Un árbol de verdad! ¿Te das cuenta? No hay ni un solo árbol en toda esta " +
                            "maldita calle, quizás ni siquiera en la ciudad.\""
                )
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
                    explanation = "El texto sugiere que estudiar las prácticas populares llevaría a determinar cuáles son beneficiosas y cuáles perjudiciales, lo que implicaría regulación.",
                    readingText = "La medicina popular colombiana conserva una amplia serie de conocimientos empíricos sobre gran diversidad de recursos botánicos, que han sido esenciales para el cuidado de la salud. Pero, con la oficialización de la medicina a finales del siglo XIX, la creación de las primeras escuelas de medicina y de farmacia y la creación de una legislación que buscaba regular estas disciplinas, muchos de estos conocimientos fueron rechazados al igual que sus prácticas médicas.\n\nParadójicamente, mientras la medicina oficial negaba los conocimientos populares, se servía de ellos para desarrollar muchos de los avances farmacéuticos de los que se vale la medicina oficial para sus tratamientos.\n\nPor otro lado, según la Organización Mundial de la Salud, la atención primaria en salud por parte de la medicina oficial cubre apenas a un 20 % de la población en los países en vías de desarrollo, mientras que el 80 % tiene que recurrir a otras prácticas médicas populares y lo hace en gran parte por la poca garantía de acceso y de aseguramiento de la población pobre.\n\nLo anterior sigue generando inconformidad en quienes practican la medicina oficial, pues consideran que la medicina popular no representa un conocimiento válido porque no se fundamenta en pruebas científicas y no está regulada. Sin embargo, ambas medicinas pueden llegar a ser complementarias si se estudian de manera juiciosa y se determinan cuáles de las prácticas y de los recursos botánicos usados por la medicina popular son perjudiciales y cuáles brindan alternativas de salud a las poblaciones necesitadas.\n\nDe esta manera, también se promueve la inclusión, el reconocimiento y el respeto por la diversidad cultural y por los conocimientos populares.\n\nTomado y adaptado de: Prieto Gaona, Oriana (2011). Proyecto de investigación sobre medicina popular."
                ),
                Question(
                    id = 202,
                    text = "En el segundo párrafo, se desvirtúa a los opositores de la tauromaquia:",
                    options = listOf(
                        QuestionOption(id = 1, text = "Citando la opinión de una fuente reconocida como David Silveti.", originalOrder = 0),
                        QuestionOption(id = 2, text = "Denunciando su falta de comprensión del arte del toreo.", originalOrder = 1),
                        QuestionOption(id = 3, text = "Alabando la forma en que se tortura al animal en el ruedo.", originalOrder = 2),
                        QuestionOption(id = 4, text = "Señalando el carácter natural de la muerte del toro.", originalOrder = 3)
                    ),
                    correctOptionId = 2,
                    explanation = "El autor afirma que los detractores 'no entienden en lo más mínimo lo que es el toreo', desacreditando su oposición por falta de comprensión.",
                    readingText = "La fiesta de los toros es uno de los espectáculos más antiguos de la humanidad, sus primeros antecedentes datan de la antigua Grecia. Desde entonces, este juego tuvo muchos detractores. Los que están en contra de la fiesta argumentan que se \"tortura\" al toro con las banderillas, y claro, que la res es muerta en el ruedo.\n\nSin embargo, creo que los opositores no entienden en lo más mínimo lo que es el toreo. La tauromaquia es, ante todo, un arte pues posee dos características fundamentales: la estética y la técnica, aunque el arte es un concepto subjetivo, casi siempre cumple con estos dos elementos. Pero el arte del toreo tiene algo más: el peligro. Decía David Silveti que el don más generoso de un artista es poner en juego la vida misma.\n\nPara los que no sepan, los puyazos que dan los picadores tienen un objetivo claro. La carne del toro se come y como esta se contamina por la adrenalina, el puyazo sirve para descongestionar al toro; mientras sangra, se va purificando. Por otra parte, la muerte del toro debe ser entendida como algo natural: toda vida termina en la muerte, la única certeza que tenemos siempre.\n\nFinalmente se habla de los derechos de los animales. Bueno, esto se oye bien pero es una falacia: el término \"derecho\" es un concepto meramente humano. Lo importante en una sociedad es garantizar los derechos de los hombres.\n\nAdaptado de: Nacif, Jorge. \"Defensa de la fiesta brava\"."
                ),
                Question(
                    id = 203,
                    text = "Entre las razones dadas por Jorge Nacif para sustentar su tesis, resulta una falacia:",
                    options = listOf(
                        QuestionOption(id = 1, text = "Decir que es un arte, pues para otras personas la tauromaquia puede no tener nada de artístico.", originalOrder = 0),
                        QuestionOption(id = 2, text = "Citar a David Silveti, pues él es un torero que evidentemente tendrá la misma opinión.", originalOrder = 1),
                        QuestionOption(id = 3, text = "Recurrir a un referente histórico para mostrar que la tauromaquia ha tenido detractores.", originalOrder = 2),
                        QuestionOption(id = 4, text = "Indicar que el toro, como todos los seres, debe morir, porque establece una analogía entre un hecho natural y la muerte provocada.", originalOrder = 3)
                    ),
                    correctOptionId = 4,
                    explanation = "Es una falacia equiparar la muerte natural con la muerte provocada en la tauromaquia, ya que son situaciones completamente diferentes.",
                    readingText = "La fiesta de los toros es uno de los espectáculos más antiguos de la humanidad, sus primeros antecedentes datan de la antigua Grecia. Desde entonces, este juego tuvo muchos detractores. Los que están en contra de la fiesta argumentan que se \"tortura\" al toro con las banderillas, y claro, que la res es muerta en el ruedo.\n\nSin embargo, creo que los opositores no entienden en lo más mínimo lo que es el toreo. La tauromaquia es, ante todo, un arte pues posee dos características fundamentales: la estética y la técnica, aunque el arte es un concepto subjetivo, casi siempre cumple con estos dos elementos. Pero el arte del toreo tiene algo más: el peligro. Decía David Silveti que el don más generoso de un artista es poner en juego la vida misma.\n\nPara los que no sepan, los puyazos que dan los picadores tienen un objetivo claro. La carne del toro se come y como esta se contamina por la adrenalina, el puyazo sirve para descongestionar al toro; mientras sangra, se va purificando. Por otra parte, la muerte del toro debe ser entendida como algo natural: toda vida termina en la muerte, la única certeza que tenemos siempre.\n\nFinalmente se habla de los derechos de los animales. Bueno, esto se oye bien pero es una falacia: el término \"derecho\" es un concepto meramente humano. Lo importante en una sociedad es garantizar los derechos de los hombres.\n\nAdaptado de: Nacif, Jorge. \"Defensa de la fiesta brava\"."
                ),
                Question(
                    id = 204,
                    text = "El mensaje que el publicista quiso enviar al poner una vaca y referirse al envase de Tetra Pak se podría enunciar así:",
                    options = listOf(
                        QuestionOption(id = 1, text = "\"Proteja el planeta utilizando envases reciclables y renovables.\"", originalOrder = 0),
                        QuestionOption(id = 2, text = "\"Lleve la leche con la frescura que tiene en su envase natural.\"", originalOrder = 1),
                        QuestionOption(id = 3, text = "\"Mantenga por más tiempo sus alimentos en óptimas condiciones.\"", originalOrder = 2),
                        QuestionOption(id = 4, text = "\"Protéjase de la luz y del aire utilizando materias primas recicladas.\"", originalOrder = 3)
                    ),
                    correctOptionId = 2,
                    explanation = "La vaca en el carrito simboliza que con Tetra Pak 'te llevas a casa lo más natural', conectando el envase con la frescura natural de la leche.",
                    readingText = "Los envases de Tetra Pak están fabricados con una materia prima natural, renovable y reciclable —papel—, y no dejan pasar ni la luz ni el aire.\n\nPor eso los alimentos se conservan en óptimas condiciones y mantienen todo su sabor, vitaminas y propiedades intactas hasta el momento de su consumo.\n\nCon los envases de Tetra Pak te llevas a casa lo más natural.\n\nTomado de: marketingnews.es"
                ),
                Question(
                    id = 205,
                    text = "Este texto induce al lector a pensar erróneamente que:",
                    options = listOf(
                        QuestionOption(id = 1, text = "Los alimentos no se conservarán frescos si no se utilizan envases Tetra Pak.", originalOrder = 0),
                        QuestionOption(id = 2, text = "Como el Tetra Pak está hecho con materiales reciclables, no deja pasar la luz ni el aire.", originalOrder = 1),
                        QuestionOption(id = 3, text = "Ya que los productos son envasados en Tetra Pak, tienen una duración ilimitada.", originalOrder = 2),
                        QuestionOption(id = 4, text = "Los envases que conservan los alimentos y el ambiente son los Tetra Pak.", originalOrder = 3)
                    ),
                    correctOptionId = 1,
                    explanation = "El texto sugiere erróneamente que solo los envases Tetra Pak pueden conservar los alimentos frescos, cuando hay otras opciones.",
                    readingText = "Considere el siguiente enunciado del anuncio:\n\n\"Los envases de Tetra Pak están fabricados con una materia prima natural renovable y reciclable ‒papel‒ y no dejan pasar NI LA LUZ NI EL AIRE. Por eso los alimentos se conservan en óptimas condiciones y mantienen todo su sabor, vitaminas y propiedades intactas hasta el momento de su consumo.\"\n\nLos envases de Tetra Pak están fabricados con una materia prima natural, renovable y reciclable —papel—, y no dejan pasar ni la luz ni el aire.\n\nPor eso los alimentos se conservan en óptimas condiciones y mantienen todo su sabor, vitaminas y propiedades intactas hasta el momento de su consumo.\n\nCon los envases de Tetra Pak te llevas a casa lo más natural.\n\nTomado de: marketingnews.es"
                ),
                Question(
                    id = 206,
                    text = "De las siguientes ideas, la que se opone a la idea central defendida por el autor es:",
                    options = listOf(
                        QuestionOption(id = 1, text = "Las corridas de toros deben prohibirse.", originalOrder = 0),
                        QuestionOption(id = 2, text = "Las fiestas taurinas son rituales de tortura.", originalOrder = 1),
                        QuestionOption(id = 3, text = "La crianza de animales es una acción hipócrita.", originalOrder = 2),
                        QuestionOption(id = 4, text = "La tolerancia es el medio para superar el desacuerdo.", originalOrder = 3)
                    ),
                    correctOptionId = 2,
                    explanation = "El autor defiende la tolerancia hacia las corridas, por lo que la idea de que son rituales de tortura se opone a su posición central.",
                    readingText = "Estoy en contra de las corridas de toros, pero también en contra de su prohibición.\n\nSi uno acepta que una mayoría puede prohibir los toros por compasión animal, tendrá que aceptar que un día otra mayoría prohíba a todos los ciudadanos matar animales para comer carne.\n\nLa tolerancia consiste en no prohibir lo que no nos gusta. Tolero las peleas de boxeo o la prostitución: no me gustan, pero tampoco las prohibiría.\n\nEl argumento más fuerte de los antitaurinos es que en las corridas se tortura a muerte a un animal. No pongo en duda que el animal sufre horriblemente. Pongo en duda la consciencia plena de ese dolor, porque no sabemos cómo funciona la mente animal.\n\nEs una hipocresía discurrir contra las corridas de toros por la mañana y al mediodía comerse un sanguinolento filete de res. Me dirán: ese lomito no se lo obtuvo con tortura. ¿Cómo saben? Los criamos para montarlos o para comerlos. Olvidar esto es hipocresía.\n\nNo voy a corridas. Me parecen un espectáculo primitivo. Quizás en eso consiste su encanto y su horror. Pero soy carnívoro. No veo que haya acuerdo entre las personas sensatas y morales sobre si las corridas deban prohibirse o no.\n\nTomado y adaptado de: Héctor Abad Faciolince, \"En contra (y a favor) de las corridas\", El Espectador."
                ),
                Question(
                    id = 207,
                    text = "La afirmación \"La era de la mencionada sociedad de la información es también la de la producción de estados mentales\" es:",
                    options = listOf(
                        QuestionOption(id = 1, text = "Una conclusión a la que llega la autora del texto después de reconocer que hay un nuevo tipo de consumidor.", originalOrder = 0),
                        QuestionOption(id = 2, text = "Una idea que se supone verdadera así no se planteen evidencias para comprobarla.", originalOrder = 1),
                        QuestionOption(id = 3, text = "Una inferencia que resulta de relacionar las ideas del texto con la imagen del consumidor de la cultura light.", originalOrder = 2),
                        QuestionOption(id = 4, text = "Un argumento sustentado desde el concepto de sociedad de la información asumido en el texto.", originalOrder = 3)
                    ),
                    correctOptionId = 1,
                    explanation = "La autora llega a esta conclusión después de analizar las características del nuevo consumidor y su comportamiento.",
                    readingText = "Luis Enrique Alonso, autor español reconocido por sus estudios sociológicos sobre el nuevo consumidor, afirma que los consumidores del cambio del milenio superaron la llamada cultura light posmoderna y que hoy estamos frente al consumidor del ajuste: uno con fuerte tendencia a la personalización de las referencias y la individualización de los argumentos en la presentación de la identidad en la vida cotidiana.\n\nDice, además, que la cultura de masas se ha vuelto más selectiva y el consumo se convierte en testigo de la afirmación de la personalidad, siguiendo pautas no tanto de adhesión a macrogrupos abstractos e indiferenciados, como a círculos, relatos y símbolos fuertemente particularistas.\n\nEse nuevo consumidor socializado exige y escoge con mayor libertad e inteligencia. Es una mezcla de convicción, impulso de compra y reflexión; su compra está determinada por las percepciones, las representaciones y los valores que conforman su estilo de vida, su ámbito privado y su pertenencia a un grupo que legitima su actuar.\n\nBien decía Mattelart que \"la era de la mencionada sociedad de la información es también la de la producción de estados mentales\".\n\nY es que este consumidor compra y consume de acuerdo con su estado mental. Por eso, cada día con más fuerza, es necesario alcanzar a este consumidor en su cotidianidad y sorprenderlo no solo con la fuerza del mensaje, sino con la novedad del medio: altas dosis de sorpresa y sentido de oportunidad.\n\nLo ingenioso son los nuevos canales, las formas de entrometerse en su cotidianidad, generando percepciones favorables, amigables, positivas. Hoy la producción de \"estados mentales\" es garantía de posicionamiento, de recordación, de percepciones acordes al producto y la marca; porque en definitiva las personas no actúan por lo que oyen, sino por lo que \"ven\" en su imaginación cuando oyen, como lo dice Santiago Rodríguez: \"la palabra mar no ahoga a nadie. El concepto que lleva dentro, sí.\"\n\nAdaptado de: Ramírez, Nohra (2011). Revista P&M."
                ),
                Question(
                    id = 208,
                    text = "De los siguientes enunciados, ¿cuál NO se puede deducir correctamente de la afirmación \"un hombre es justo solo si su conducta se adecúa a las normas de un orden social supuestamente justo\"?",
                    options = listOf(
                        QuestionOption(id = 1, text = "Un hombre que se comporta justamente sigue las normas de un orden social.", originalOrder = 0),
                        QuestionOption(id = 2, text = "Todo hombre que no se adecúe a las normas de un orden social justo es injusto.", originalOrder = 1),
                        QuestionOption(id = 3, text = "Hay hombres que se adecúan a un orden social supuestamente justo y sin embargo son injustos.", originalOrder = 2),
                        QuestionOption(id = 4, text = "No hay hombres que se adecúen a un orden social supuestamente justo y sin embargo sean injustos.", originalOrder = 3)
                    ),
                    correctOptionId = 3,
                    explanation = "Según la afirmación, si un hombre se adecúa a las normas de un orden social justo, entonces es justo. No puede ser injusto.",
                    readingText = "La Justicia es, en primer lugar, una cualidad posible, pero no necesaria, de un orden social que regula las relaciones mutuas entre los hombres.\n\nSolo secundariamente es una virtud humana, ya que un hombre es justo solo si su conducta se adecúa a las normas de un orden social supuestamente justo.\n\nPero ¿qué significa decir que un orden social es justo?\n\nSignifica que este orden social regula la conducta de los hombres de un modo satisfactorio para todos; es decir, que todos los hombres encuentran en él la felicidad.\n\nLa búsqueda de la Justicia es la eterna búsqueda de la felicidad humana.\n\nEs una finalidad que el hombre no puede encontrar por sí mismo y, por ello, la busca en la sociedad. La Justicia es la felicidad social, garantizada por un orden social.\n\nPlatón, identificando la Justicia con la felicidad, sostiene que un hombre justo es feliz y un hombre injusto es infeliz.\n\nEvidentemente, la afirmación según la cual la Justicia es la felicidad no es una respuesta definitiva, sino una forma de eludir el problema. Pues inmediatamente se plantea la cuestión:\n\n\"¿Qué es la felicidad?\"\n\nTomado de: Kelsen, H. (1992). ¿Qué es justicia? Planeta-De Agostini."
                ),
                Question(
                    id = 209,
                    text = "Las palabras subrayadas indican respectivamente:",
                    options = listOf(
                        QuestionOption(id = 1, text = "Una oposición y una salvedad.", originalOrder = 0),
                        QuestionOption(id = 2, text = "Una aclaración y una razón.", originalOrder = 1),
                        QuestionOption(id = 3, text = "Una aclaración y una oposición.", originalOrder = 2),
                        QuestionOption(id = 4, text = "Una oposición y una conclusión.", originalOrder = 3)
                    ),
                    correctOptionId = 2,
                    explanation = "'En primer lugar' es una aclaración que introduce el orden de ideas, y 'ya que' introduce una razón o justificación.",
                    readingText = "Considere el siguiente fragmento:\n\n\"La Justicia es en primer lugar una cualidad posible, pero no necesaria, de un orden social que regula las relaciones mutuas entre los hombres. Solo secundariamente es una virtud humana, ya que un hombre es justo solo si su conducta se adecúa a las normas de un orden social supuestamente justo.\"\n\nLa Justicia es, en primer lugar, una cualidad posible, pero no necesaria, de un orden social que regula las relaciones mutuas entre los hombres.\n\nSolo secundariamente es una virtud humana, ya que un hombre es justo solo si su conducta se adecúa a las normas de un orden social supuestamente justo.\n\nPero ¿qué significa decir que un orden social es justo?\n\nSignifica que este orden social regula la conducta de los hombres de un modo satisfactorio para todos; es decir, que todos los hombres encuentran en él la felicidad.\n\nLa búsqueda de la Justicia es la eterna búsqueda de la felicidad humana.\n\nEs una finalidad que el hombre no puede encontrar por sí mismo y, por ello, la busca en la sociedad. La Justicia es la felicidad social, garantizada por un orden social.\n\nPlatón, identificando la Justicia con la felicidad, sostiene que un hombre justo es feliz y un hombre injusto es infeliz.\n\nEvidentemente, la afirmación según la cual la Justicia es la felicidad no es una respuesta definitiva, sino una forma de eludir el problema. Pues inmediatamente se plantea la cuestión:\n\n\"¿Qué es la felicidad?\"\n\nTomado de: Kelsen, H. (1992). ¿Qué es justicia? Planeta-De Agostini."
                ),
                Question(
                    id = 210,
                    text = "Según el enunciado \"La búsqueda de la Justicia es la eterna búsqueda de la felicidad humana\", ¿cuál de las siguientes opciones se puede concluir?",
                    options = listOf(
                        QuestionOption(id = 1, text = "Quien busca la justicia no encuentra la felicidad.", originalOrder = 0),
                        QuestionOption(id = 2, text = "Quien busca la justicia también busca la felicidad.", originalOrder = 1),
                        QuestionOption(id = 3, text = "El que busca la justicia nunca encuentra la felicidad.", originalOrder = 2),
                        QuestionOption(id = 4, text = "El que busca la justicia siempre encuentra la felicidad.", originalOrder = 3)
                    ),
                    correctOptionId = 2,
                    explanation = "Si la búsqueda de la justicia es la búsqueda de la felicidad, entonces quien busca justicia también busca felicidad.",
                    readingText = "La Justicia es, en primer lugar, una cualidad posible, pero no necesaria, de un orden social que regula las relaciones mutuas entre los hombres.\n\nSolo secundariamente es una virtud humana, ya que un hombre es justo solo si su conducta se adecúa a las normas de un orden social supuestamente justo.\n\nPero ¿qué significa decir que un orden social es justo?\n\nSignifica que este orden social regula la conducta de los hombres de un modo satisfactorio para todos; es decir, que todos los hombres encuentran en él la felicidad.\n\nLa búsqueda de la Justicia es la eterna búsqueda de la felicidad humana.\n\nEs una finalidad que el hombre no puede encontrar por sí mismo y, por ello, la busca en la sociedad. La Justicia es la felicidad social, garantizada por un orden social.\n\nPlatón, identificando la Justicia con la felicidad, sostiene que un hombre justo es feliz y un hombre injusto es infeliz.\n\nEvidentemente, la afirmación según la cual la Justicia es la felicidad no es una respuesta definitiva, sino una forma de eludir el problema. Pues inmediatamente se plantea la cuestión:\n\n\"¿Qué es la felicidad?\"\n\nTomado de: Kelsen, H. (1992). ¿Qué es justicia? Planeta-De Agostini."
                ),
                Question(
                    id = 211,
                    text = "Por un lado, el autor afirma (i) que el miedo origina las sociedades. Por otro lado, (ii) que si no existiera el miedo, el hombre buscaría dominar a los demás. ¿Cuál es la relación argumentativa entre estas dos afirmaciones?",
                    options = listOf(
                        QuestionOption(id = 1, text = "(i) es una premisa y (ii) la conclusión.", originalOrder = 0),
                        QuestionOption(id = 2, text = "(ii) es una premisa y (i) la conclusión.", originalOrder = 1),
                        QuestionOption(id = 3, text = "(i) y (ii) son premisas de un mismo argumento.", originalOrder = 2),
                        QuestionOption(id = 4, text = "(i) y (ii) presentan la misma conclusión de diferente manera.", originalOrder = 3)
                    ),
                    correctOptionId = 3,
                    explanation = "Ambas afirmaciones son premisas que sustentan la conclusión de que el origen de las sociedades se debe al miedo mutuo, no a la benevolencia.",
                    readingText = "\"Aunque las comodidades de esta vida pueden aumentarse con la ayuda mutua, sin embargo, como eso se puede conseguir dominando a los demás mejor que asociándose con ellos, nadie debe dudar de que los hombres, por su naturaleza, si no existiera el miedo, se verían inclinados más al dominio que a la sociedad.\n\nPor lo tanto, hay que afirmar que el origen de las sociedades grandes y duraderas no se ha debido a la mutua benevolencia de los hombres, sino al miedo mutuo.\"\n\nTomado de: Hobbes, T. (1999). \"Tratado sobre el ciudadano\". Trad. Joaquín Rodríguez Feo, Madrid, Trotta."
                ),
                Question(
                    id = 212,
                    text = "¿Qué función cumple el conector \"sin embargo\" en el texto anterior?",
                    options = listOf(
                        QuestionOption(id = 1, text = "Aclarar la idea de que la ayuda mutua aumenta las comodidades con la idea de que el dominio de los demás es una mejor forma de aumentarlas.", originalOrder = 0),
                        QuestionOption(id = 2, text = "Desmentir la idea de que la ayuda mutua aumenta las comodidades y afirmar que es por medio del dominio de los demás que se logra.", originalOrder = 1),
                        QuestionOption(id = 3, text = "Contrastar la idea de que la ayuda mutua aumenta las comodidades, con la idea de que es más efectivo dominar a los demás para aumentarlas.", originalOrder = 2),
                        QuestionOption(id = 4, text = "Cuestionar la idea de que la ayuda mutua aumenta las comodidades al afirmar que en realidad el dominio puede aumentarlas.", originalOrder = 3)
                    ),
                    correctOptionId = 3,
                    explanation = "'Sin embargo' establece un contraste entre la ayuda mutua y el dominio como formas de aumentar las comodidades.",
                    readingText = "\"Aunque las comodidades de esta vida pueden aumentarse con la ayuda mutua, sin embargo, como eso se puede conseguir dominando a los demás mejor que asociándose con ellos, nadie debe dudar de que los hombres, por su naturaleza, si no existiera el miedo, se verían inclinados más al dominio que a la sociedad.\n\nPor lo tanto, hay que afirmar que el origen de las sociedades grandes y duraderas no se ha debido a la mutua benevolencia de los hombres, sino al miedo mutuo.\"\n\nTomado de: Hobbes, T. (1999). \"Tratado sobre el ciudadano\". Trad. Joaquín Rodríguez Feo, Madrid, Trotta."
                ),
                Question(
                    id = 213,
                    text = "¿Cuál de las siguientes afirmaciones expresa de manera exacta la antítesis de la tesis principal del texto anterior?",
                    options = listOf(
                        QuestionOption(id = 1, text = "El origen de las sociedades grandes y duraderas se debe a la desconfianza de los hombres que las conforman.", originalOrder = 0),
                        QuestionOption(id = 2, text = "El origen de las sociedades grandes y duraderas se explica por la indiferencia de los hombres que las conforman.", originalOrder = 1),
                        QuestionOption(id = 3, text = "El origen de las sociedades grandes y duraderas se halla en la obediencia recíproca de los hombres que las conforman.", originalOrder = 2),
                        QuestionOption(id = 4, text = "El origen de las sociedades grandes y duraderas resulta de la colaboración desinteresada de los hombres que las conforman.", originalOrder = 3)
                    ),
                    correctOptionId = 4,
                    explanation = "La tesis de Hobbes es que las sociedades se originan por el miedo mutuo, no por la benevolencia. La antítesis sería la colaboración desinteresada.",
                    readingText = "\"Aunque las comodidades de esta vida pueden aumentarse con la ayuda mutua, sin embargo, como eso se puede conseguir dominando a los demás mejor que asociándose con ellos, nadie debe dudar de que los hombres, por su naturaleza, si no existiera el miedo, se verían inclinados más al dominio que a la sociedad.\n\nPor lo tanto, hay que afirmar que el origen de las sociedades grandes y duraderas no se ha debido a la mutua benevolencia de los hombres, sino al miedo mutuo.\"\n\nTomado de: Hobbes, T. (1999). \"Tratado sobre el ciudadano\". Trad. Joaquín Rodríguez Feo, Madrid, Trotta."
                )
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
                    explanation = "Mafalda usa la tortuga para criticar la lentitud y el comportamiento caprichoso de la burocracia.",
                    readingText = "Mafalda le da a la tortuga el nombre \"Burocracia\" con el propósito de destacar que la burocracia es lenta y caprichosa en su comportamiento, haciendo una crítica social al ritmo pausado e ineficiente de las instituciones públicas.\n\n(Fragmento interpretado del cómic de Quino.)"
                ),
                Question(
                    id = 302,
                    text = "La afirmación de Luisa, \"Si estoy con un man que me gusta porque sí, ¿por qué no voy a estar con otro por plata?\", implica que ella:",
                    options = listOf(
                        QuestionOption(id = 1, text = "Toma decisiones dentro de las normas de una comunidad.", originalOrder = 0),
                        QuestionOption(id = 2, text = "No le da más importancia a los sentimientos que al dinero.", originalOrder = 1),
                        QuestionOption(id = 3, text = "Gusta de los hombres que tienen dinero.", originalOrder = 2),
                        QuestionOption(id = 4, text = "Cree que la opinión de los demás es importante a la hora de decidir.", originalOrder = 3)
                    ),
                    correctOptionId = 2,
                    explanation = "Luisa equipara las relaciones por gusto con las relaciones por dinero, sugiriendo que no hay diferencia moral entre ambas.",
                    readingText = "A finales de 2000, Wendy, una adolescente hondureña, fue violada en grupo por pandilleros de la Mara Salvatrucha. Tras el ritual conocido como \"el trencito\", los mareros decidieron hacer negocio y corrieron la voz de que cobraban cincuenta lempiras a quien quisiera tener relaciones con la muchacha.\n\nEl pasado diciembre, la policía detuvo en Málaga a una rumana que había firmado un contrato para vender sus dos hijas a unos proxenetas. Por 5.000 euros aceptó que fueran llevadas a España a prostituirse.\n\nLuisa, universitaria bogotana, empezó en un videochat. Le pagaban por desnudarse ante la cámara. De allí pasó a concertar citas vía celular y, ya con clientes, se enroló en un lujoso burdel:\n\n\"Si estoy con un man que me gusta porque sí, ¿por qué no voy a estar con otro por plata?\"\n\nLa Valeska vive en función de la plata. Ejerce la prostitución desde los 17 años, cuando aburrida del maltrato de su padre dejó la comodidad del barrio Laureles para ofrecerse en Bogotá.\n\nPoca gente pasa el umbral, pero son varias las vías para llegar al sexo pago. A pesar de esta verdad de a puño, muchos se resisten a la evidencia disponible y enfatizan una doctrina cada vez más terca e improcedente para la prevención: \"la prostitución siempre es forzada\".\n\nSin embargo, ¿cuántas personas venden su cuerpo empujadas por la miseria, cuántas obligadas por proxenetas, cuántas seducidas y abandonadas, cuántas huyendo del abuso, cuántas por morbo o curiosidad, cuántas por arribistas, cuántas por la adrenalina, cuántas por hipersexuales? ¿Cuántas Wendys por cada Valeska o cada Luisa? Nadie sabe; las respuestas no son obvias, e incluso la disponibilidad de testimonios puede estar sesgada.\n\nEn Colombia, aunque tenemos indicios de que el negocio de las prepagos está en franca expansión, no conocemos el tamaño de la actividad ni su composición. Nadie comprende bien por qué se inician, por qué se mantienen o por qué dejan la actividad, y cada vez es mayor la influencia de quienes no están interesados en que se sepa.\n\nLa industria del rescate es ya una poderosa alianza multinacional de burócratas, periodistas y oenegés (ONG) que logró simplificar hasta el absurdo el diagnóstico, demostrando de paso que no solo tiene más prejuicios que la Iglesia, los viejos criminólogos o los médicos higienistas, sino que carece de cualquier vocación para entender lo que ocurre, lo que piensan o lo que quieren las víctimas. Esa alianza pretende intervenir un mercado sobre el que se sabe no solo poco, sino cada vez menos.\n\n\"No me arrepiento absolutamente de nada\", dice una prostituta. \"Los momentos en el burdel fueron unos de los mejores de mi vida, por el simple hecho de haber conocido a Giovanni y haber encontrado esa mujer nueva que soy ahora… Utilizar el sexo como medio para encontrar lo que todo el mundo busca: reconocimiento, placer, autoestima y, en definitiva, amor y cariño... ¿Qué hay de patológico en eso?\"\n\nTomado y adaptado de: Rubio, M. (2012, junio). \"Wendy, Valérie y todas las demás\". El Malpensante, vol. 131."
                ),
                Question(
                    id = 303,
                    text = "¿Cuál de las siguientes opciones es una antítesis de las ideas presentadas?",
                    options = listOf(
                        QuestionOption(id = 1, text = "El orden social justo beneficia a todos los miembros de la comunidad donde tal orden rige.", originalOrder = 0),
                        QuestionOption(id = 2, text = "Un orden social no puede ser justo y no regular de modo satisfactorio el comportamiento de todos los miembros.", originalOrder = 1),
                        QuestionOption(id = 3, text = "Los órdenes sociales justos proveen felicidad para todos los miembros de la comunidad.", originalOrder = 2),
                        QuestionOption(id = 4, text = "Un orden social puede ser justo y aun así no regular de modo satisfactorio el comportamiento de todos los miembros.", originalOrder = 3)
                    ),
                    correctOptionId = 4,
                    explanation = "La antítesis sería que un orden social puede ser justo sin regular satisfactoriamente a todos, contradiciendo la definición presentada.",
                    readingText = "Considere el siguiente fragmento:\n\n\"¿Qué significa decir que un orden social es justo? Significa que este orden social regula la conducta de los hombres de un modo satisfactorio para todos, es decir, que todos los hombres encuentran en él la felicidad.\"\n\nLa Justicia es, en primer lugar, una cualidad posible, pero no necesaria, de un orden social que regula las relaciones mutuas entre los hombres.\n\nSolo secundariamente es una virtud humana, ya que un hombre es justo solo si su conducta se adecúa a las normas de un orden social supuestamente justo.\n\nPero ¿qué significa decir que un orden social es justo?\n\nSignifica que este orden social regula la conducta de los hombres de un modo satisfactorio para todos; es decir, que todos los hombres encuentran en él la felicidad.\n\nLa búsqueda de la Justicia es la eterna búsqueda de la felicidad humana.\n\nEs una finalidad que el hombre no puede encontrar por sí mismo y, por ello, la busca en la sociedad. La Justicia es la felicidad social, garantizada por un orden social.\n\nPlatón, identificando la Justicia con la felicidad, sostiene que un hombre justo es feliz y un hombre injusto es infeliz.\n\nEvidentemente, la afirmación según la cual la Justicia es la felicidad no es una respuesta definitiva, sino una forma de eludir el problema. Pues inmediatamente se plantea la cuestión:\n\n\"¿Qué es la felicidad?\"\n\nTomado de: Kelsen, H. (1992). ¿Qué es justicia? Planeta-De Agostini."
                ),
                Question(
                    id = 304,
                    text = "De acuerdo con el texto, ¿cuál de las siguientes afirmaciones es compatible con la posición del autor sobre la inclinación natural del hombre hacia la dominación de los demás?",
                    options = listOf(
                        QuestionOption(id = 1, text = "El dominio es una inclinación desfavorable en tanto que no aumenta las comodidades de la vida.", originalOrder = 0),
                        QuestionOption(id = 2, text = "El dominio es una inclinación deseable en tanto que favorece la cooperación entre los hombres.", originalOrder = 1),
                        QuestionOption(id = 3, text = "El dominio es una inclinación ineludible, pues resulta más eficiente para alcanzar comodidades que la ayuda mutua.", originalOrder = 2),
                        QuestionOption(id = 4, text = "El dominio es una inclinación admirable en tanto que causa el miedo que conlleva a la creación de la sociedad.", originalOrder = 3)
                    ),
                    correctOptionId = 3,
                    explanation = "Según Hobbes, el dominio es más eficiente que la ayuda mutua para aumentar las comodidades, por lo que es una inclinación natural e ineludible.",
                    readingText = "\"Aunque las comodidades de esta vida pueden aumentarse con la ayuda mutua, sin embargo, como eso se puede conseguir dominando a los demás mejor que asociándose con ellos, nadie debe dudar de que los hombres, por su naturaleza, si no existiera el miedo, se verían inclinados más al dominio que a la sociedad.\n\nPor lo tanto, hay que afirmar que el origen de las sociedades grandes y duraderas no se ha debido a la mutua benevolencia de los hombres, sino al miedo mutuo.\"\n\nTomado de: Hobbes, T. (1999). \"Tratado sobre el ciudadano\". Trad. Joaquín Rodríguez Feo, Madrid, Trotta."
                ),
                Question(
                    id = 305,
                    text = "De acuerdo con las ideas expresadas en el texto de Hobbes, si \"los hombres por su naturaleza están más inclinados al dominio que a la sociedad\", ¿por qué aparecieron sociedades grandes y duraderas?",
                    options = listOf(
                        QuestionOption(id = 1, text = "Es natural para el hombre asociarse con otros para ejercer su dominio con seguridad.", originalOrder = 0),
                        QuestionOption(id = 2, text = "Para ampliar su capacidad de dominio, al hombre le resulta más efectivo vivir en sociedad.", originalOrder = 1),
                        QuestionOption(id = 3, text = "Es propio del hombre evitar todo tipo de dominio a través de las relaciones confiables que le brinda la vida en sociedad.", originalOrder = 2),
                        QuestionOption(id = 4, text = "El hombre busca la seguridad y es más seguro para él vivir en comunidad que estar expuesto a ser dominado por otro.", originalOrder = 3)
                    ),
                    correctOptionId = 4,
                    explanation = "Según Hobbes, el hombre forma sociedades por miedo mutuo, buscando seguridad ante la posibilidad de ser dominado por otros.",
                    readingText = "\"Aunque las comodidades de esta vida pueden aumentarse con la ayuda mutua, sin embargo, como eso se puede conseguir dominando a los demás mejor que asociándose con ellos, nadie debe dudar de que los hombres, por su naturaleza, si no existiera el miedo, se verían inclinados más al dominio que a la sociedad.\n\nPor lo tanto, hay que afirmar que el origen de las sociedades grandes y duraderas no se ha debido a la mutua benevolencia de los hombres, sino al miedo mutuo.\"\n\nTomado de: Hobbes, T. (1999). \"Tratado sobre el ciudadano\". Trad. Joaquín Rodríguez Feo, Madrid, Trotta."
                ),
                Question(
                    id = 306,
                    text = "En la propaganda, la expresión \"ESCOGE LA MADRE DE TUS HIJOS\" busca dar a entender que:",
                    options = listOf(
                        QuestionOption(id = 1, text = "Un contrato conlleva responsabilidades mutuas.", originalOrder = 0),
                        QuestionOption(id = 2, text = "Al elegir los servicios de la entidad, se aseguran las condiciones para conformar una familia.", originalOrder = 1),
                        QuestionOption(id = 3, text = "El dinero garantiza la armonía de las parejas.", originalOrder = 2),
                        QuestionOption(id = 4, text = "Escoger un banco es tan importante como compartir tiempo con la pareja.", originalOrder = 3)
                    ),
                    correctOptionId = 1,
                    explanation = "La metáfora sugiere que elegir una entidad financiera es como elegir una pareja, implicando responsabilidades mutuas y compromisos a largo plazo.",
                    readingText = "En la propaganda, la expresión \"ESCOGE LA MADRE DE TUS HIJOS\" busca atraer la atención del público mediante una analogía entre el acto de elegir pareja y la decisión de escoger un banco o entidad financiera, apelando a la importancia de las decisiones a largo plazo y la confianza mutua.\n\nEsta metáfora publicitaria pretende resaltar que, así como se escoge una madre responsable para formar una familia, debe elegirse cuidadosamente la institución con la que se establecerán compromisos económicos y personales."
                ),
                Question(
                    id = 307,
                    text = "De acuerdo con las ideas expresadas en el texto de Hobbes, si \"los hombres por su naturaleza están más inclinados al dominio que a la sociedad\", ¿por qué viven en comunidad?",
                    options = listOf(
                        QuestionOption(id = 1, text = "Para ejercer el dominio de forma colectiva y más segura.", originalOrder = 0),
                        QuestionOption(id = 2, text = "Para ampliar su capacidad de dominio.", originalOrder = 1),
                        QuestionOption(id = 3, text = "Para evitar el dominio mediante relaciones confiables.", originalOrder = 2),
                        QuestionOption(id = 4, text = "Porque busca seguridad frente al dominio de otros.", originalOrder = 3)
                    ),
                    correctOptionId = 4,
                    explanation = "Según Hobbes, el hombre forma sociedades por miedo mutuo, buscando seguridad ante la posibilidad de ser dominado por otros.",
                    readingText = "\"Aunque las comodidades de esta vida pueden aumentarse con la ayuda mutua, sin embargo, como eso se puede conseguir dominando a los demás mejor que asociándose con ellos, nadie debe dudar de que los hombres, por su naturaleza, si no existiera el miedo, se verían inclinados más al dominio que a la sociedad.\n\nPor lo tanto, hay que afirmar que el origen de las sociedades grandes y duraderas no se ha debido a la mutua benevolencia de los hombres, sino al miedo mutuo.\"\n\nTomado de: Hobbes, T. (1999). \"Tratado sobre el ciudadano\". Trad. Joaquín Rodríguez Feo, Madrid, Trotta."
                )
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
                    explanation = "La tabla muestra que a mayor magnitud, menor cantidad de sismos registrados.",
                    readingText = "La tabla muestra el total de sismos registrados en el planeta durante la primera década del siglo XXI y la distribución de aquellos con magnitud mayor a 5,0.",
                    contextImage = "imagen_sismos" // Tabla de sismos por magnitud
                ),
                Question(
                    id = 402,
                    text = "A partir de los datos, una persona predice que en el 2011 se presentarán exactamente 173 sismos de magnitud igual o superior a 6,0 grados. Que suceda lo que esta persona predice es:",
                    options = listOf(
                        QuestionOption(id = 1, text = "imposible, pues el número de sismos, de cualquier magnitud, ha ido disminuyendo desde 2007.", originalOrder = 0),
                        QuestionOption(id = 2, text = "poco probable, porque, de acuerdo con la tendencia, el número de sismos en el 2011 será mayor que 173.", originalOrder = 1),
                        QuestionOption(id = 3, text = "incierto, pues a partir del número de sismos de cualquier magnitud presentado en el pasado no se puede predecir el número de sismos futuros.", originalOrder = 2),
                        QuestionOption(id = 4, text = "seguro, pues la tendencia de los dos años anteriores a 2011 indica que se presentarán 151 sismos de magnitud entre 6,0 y 6,9; 21 de magnitud entre 7,0 y 7,9, y 1 de magnitud superior a 8,0.", originalOrder = 3)
                    ),
                    correctOptionId = 3,
                    explanation = "Es incierto porque no se puede predecir eventos futuros basándose únicamente en datos históricos.",
                    readingText = "La tabla muestra el total de sismos registrados en el planeta durante la primera década del siglo XXI y la distribución de aquellos con magnitud mayor a 5,0.",
                    contextImage = "imagen_sismos" // Tabla de sismos por magnitud
                ),
                Question(
                    id = 403,
                    text = "¿Cuál de los siguientes cocientes permite estimar la cantidad de sismos mensuales?",
                    options = listOf(
                        QuestionOption(id = 1, text = "Total de sismos sobre meses del año.", originalOrder = 0),
                        QuestionOption(id = 2, text = "Total de sismos por año sobre meses del año.", originalOrder = 1),
                        QuestionOption(id = 3, text = "Total de sismos por año sobre días del año.", originalOrder = 2),
                        QuestionOption(id = 4, text = "Total de sismos sobre su magnitud.", originalOrder = 3)
                    ),
                    correctOptionId = 2,
                    explanation = "Para estimar sismos mensuales se divide el total anual entre 12 meses.",
                    readingText = "La tabla muestra el total de sismos registrados en el planeta durante la primera década del siglo XXI y la distribución de aquellos con magnitud mayor a 5,0.",
                    contextImage = "imagen_sismos" // Tabla de sismos por magnitud
                ),
                Question(
                    id = 404,
                    text = "El promedio anual de sismos en la primera década del siglo XXI fue 3.783. Los años con el número de sismos más cercano y más lejano al promedio son:",
                    options = listOf(
                        QuestionOption(id = 1, text = "2007 y 2010, respectivamente.", originalOrder = 0),
                        QuestionOption(id = 2, text = "2006 y 2005, respectivamente.", originalOrder = 1),
                        QuestionOption(id = 3, text = "2005 y 2002, respectivamente.", originalOrder = 2),
                        QuestionOption(id = 4, text = "2002 y 2008, respectivamente.", originalOrder = 3)
                    ),
                    correctOptionId = 2,
                    explanation = "2006 tiene 3.783 sismos (igual al promedio) y 2005 tiene la mayor diferencia.",
                    readingText = "La tabla muestra el total de sismos registrados en el planeta durante la primera década del siglo XXI y la distribución de aquellos con magnitud mayor a 5,0.",
                    contextImage = "imagen_sismos" // Tabla de sismos por magnitud
                ),
                Question(
                    id = 405,
                    text = "En la primera década del siglo XXI, la proporción de sismos de magnitud entre 8,0 y 8,9 es de, aproximadamente:",
                    options = listOf(
                        QuestionOption(id = 1, text = "1 de cada 3.000 sismos.", originalOrder = 0),
                        QuestionOption(id = 2, text = "1 de cada 12 sismos.", originalOrder = 1),
                        QuestionOption(id = 3, text = "12 de cada 18.000 sismos.", originalOrder = 2),
                        QuestionOption(id = 4, text = "12 de cada 4.000 sismos.", originalOrder = 3)
                    ),
                    correctOptionId = 1,
                    explanation = "Sismos de magnitud 8,0-8,9: 12. Total: 37.830. Proporción: 12/37.830 ≈ 1/3.000.",
                    readingText = "La tabla muestra el total de sismos registrados en el planeta durante la primera década del siglo XXI y la distribución de aquellos con magnitud mayor a 5,0.",
                    contextImage = "imagen_sismos" // Tabla de sismos por magnitud
                ),
                Question(
                    id = 406,
                    text = "Patricia está muy contenta, pues afirma que, de la forma en que su tía repartió el dinero de sus bienes, ella obtendrá más dinero que si la herencia se dividiera en partes iguales entre los familiares vivos de la tía según el esquema.\n\nLa afirmación de Patricia es",
                    options = listOf(
                        QuestionOption(id = 1, text = "incorrecta, pues de cualquiera de las dos formas los herederos reciben \$32.000.000.", originalOrder = 0),
                        QuestionOption(id = 2, text = "correcta, pues según el testamento la herencia se distribuye entre 6 personas; de la otra forma se debe repartir entre 8.", originalOrder = 1),
                        QuestionOption(id = 3, text = "incorrecta, pues Patricia recibirá 10% de la herencia, que es menos que el 12,5% que recibiría con la otra distribución.", originalOrder = 2),
                        QuestionOption(id = 4, text = "correcta, pues el dinero se divide solo entre ella y su hermano.", originalOrder = 3)
                    ),
                    correctOptionId = 3,
                    explanation = "Patricia recibiría menos dinero con la distribución del testamento que con una distribución equitativa.",
                    readingText = "Antes de fallecer, la señora Antonia organizará su testamento en el que hereda a sus sobrinos y a los hijos de estos, pues, son sus familiares más cercanos. La señora Antonia tiene una casa que actualmente vale \$240.000.000 y un porcentaje en un apartamento que actualmente vale \$160.000.000.",
                    contextImage = "imagen_bienes" // Esquema de herederos y testamento
                ),
                Question(
                    id = 407,
                    text = "¿Qué parte de la herencia le corresponde a Juan?",
                    options = listOf(
                        QuestionOption(id = 1, text = "La quinta parte.", originalOrder = 0),
                        QuestionOption(id = 2, text = "La mitad.", originalOrder = 1),
                        QuestionOption(id = 3, text = "La octava parte.", originalOrder = 2),
                        QuestionOption(id = 4, text = "La tercera parte.", originalOrder = 3)
                    ),
                    correctOptionId = 1,
                    explanation = "Según el esquema de distribución, Juan recibe una quinta parte de la herencia.",
                    readingText = "Antes de fallecer, la señora Antonia organizará su testamento en el que hereda a sus sobrinos y a los hijos de estos, pues, son sus familiares más cercanos. La señora Antonia tiene una casa que actualmente vale \$240.000.000 y un porcentaje en un apartamento que actualmente vale \$160.000.000.",
                    contextImage = "imagen_bienes" // Esquema de herederos y testamento
                ),
                Question(
                    id = 408,
                    text = "Camilo quiere inscribirse a las clases de pilates ofrecidas por el instructor y escoger el total de sesiones mensual en la que el costo por sesión sea de menor precio. Camilo elige tomar 2 sesiones por semana con saldo. ¿Logra Camilo cumplir su propósito de que el costo por sesión sea el de menor precio?",
                    options = listOf(
                        QuestionOption(id = 1, text = "No, pues el costo por sesión de menor precio lo obtiene si toma 4 sesiones por semana.", originalOrder = 0),
                        QuestionOption(id = 2, text = "Sí, pues tomar 2 sesiones por semana tiene el menor costo mensual de todas las opciones.", originalOrder = 1),
                        QuestionOption(id = 3, text = "No, pues se paga un menor precio por sesión si toma 3 sesiones por semana.", originalOrder = 2),
                        QuestionOption(id = 4, text = "Sí, pues tomar menos sesiones garantiza pagar menos por cada una de ellas.", originalOrder = 3)
                    ),
                    correctOptionId = 1,
                    explanation = "El costo por sesión más bajo se obtiene con 4 sesiones por semana.",
                    readingText = "Un instructor de pilates tiene un estudio con los equipos necesarios para que una persona reciba entrenamiento personalizado. La tabla 1 muestra la cantidad de sesiones por semana, el total en el mes y el costo mensual que una persona tendría que pagar por el entrenamiento.",
                    contextImage = "imagen_3" // Tablas de sesiones y horarios de pilates
                ),
                Question(
                    id = 409,
                    text = "¿Cuál de las siguientes afirmaciones es incorrecta?",
                    options = listOf(
                        QuestionOption(id = 1, text = "Hay más horas disponibles de 8 a.m. a 1 p.m., que de 1 p.m. a 7 p.m.", originalOrder = 0),
                        QuestionOption(id = 2, text = "Todos los días hay 5 horas disponibles.", originalOrder = 1),
                        QuestionOption(id = 3, text = "Hay más horas disponibles de jueves a sábado, que de lunes a miércoles.", originalOrder = 2),
                        QuestionOption(id = 4, text = "El sábado de 12 m. a 7 p.m. no hay clases asignadas.", originalOrder = 3)
                    ),
                    correctOptionId = 2,
                    explanation = "No todos los días hay exactamente 5 horas disponibles según la tabla de horarios.",
                    readingText = "Un instructor de pilates tiene un estudio con los equipos necesarios para que una persona reciba entrenamiento personalizado. La tabla 2 muestra, en gris, los momentos del día que ya tiene clase con alguna persona, cada semana.",
                    contextImage = "imagen_3" // Tablas de sesiones y horarios de pilates
                )
            )
            2 -> listOf(
                Question(
                    id = 410,
                    text = "Durante el período 1996 – 2002, los años en los que se hizo mayor inversión en seguridad vial fueron",
                    options = listOf(
                        QuestionOption(id = 1, text = "1997, 1998, 1999 y 2000.", originalOrder = 0),
                        QuestionOption(id = 2, text = "2000, 2001 y 2002.", originalOrder = 1),
                        QuestionOption(id = 3, text = "1999, 2000 y 2001.", originalOrder = 2),
                        QuestionOption(id = 4, text = "1996, 1997, 1998 y 1999.", originalOrder = 3)
                    ),
                    correctOptionId = 2,
                    explanation = "Los años 2000, 2001 y 2002 muestran las mayores inversiones en la gráfica.",
                    readingText = "La gráfica muestra la inversión que hizo un país, en temas de seguridad vial, durante 7 años.",
                    contextImage = "grafica_inversion" // Gráfica de inversión en seguridad vial
                ),
                Question(
                    id = 411,
                    text = "La inversión en seguridad se realiza el 10 de enero de cada año. En enero 10 de 2002, un euro equivalía a 2.800 pesos colombianos, aproximadamente. Se proponen los siguientes procedimientos para hallar el valor de la inversión en seguridad en pesos colombianos:\n\nI. Convertir 194,39 millones de euros a pesos colombianos.\nII. Convertir 2.800 pesos colombianos a euros.\nIII. Multiplicar 194,39 por 2.800 y luego dividir entre el total de años.\n\n¿Cuál o cuáles de los procedimientos es correcto para hallar lo solicitado?",
                    options = listOf(
                        QuestionOption(id = 1, text = "I y III solamente.", originalOrder = 0),
                        QuestionOption(id = 2, text = "I solamente.", originalOrder = 1),
                        QuestionOption(id = 3, text = "II y III solamente.", originalOrder = 2),
                        QuestionOption(id = 4, text = "II solamente.", originalOrder = 3)
                    ),
                    correctOptionId = 2,
                    explanation = "Solo el procedimiento I es correcto para convertir la inversión a pesos colombianos.",
                    readingText = "La gráfica muestra la inversión que hizo un país, en temas de seguridad vial, durante 7 años.",
                    contextImage = "grafica_inversion" // Gráfica de inversión en seguridad vial
                ),
                Question(
                    id = 412,
                    text = "Se realizó una campaña de reciclaje durante tres días en una unidad residencial, en la que se recogieron 2 toneladas diarias de papel y cartón; por tanto, se evitó la tala de 2 x 3 x 17 = 102 árboles adultos.\n\nSi esta campaña se efectuara durante 20 días en la misma unidad y se recolectara la misma cantidad se podría ahorrar:",
                    options = listOf(
                        QuestionOption(id = 1, text = "680 litros de agua.", originalOrder = 0),
                        QuestionOption(id = 2, text = "5.600 litros de agua.", originalOrder = 1),
                        QuestionOption(id = 3, text = "300.000 litros de agua.", originalOrder = 2),
                        QuestionOption(id = 4, text = "2.000.000 litros de agua.", originalOrder = 3)
                    ),
                    correctOptionId = 4,
                    explanation = "20 días × 2 toneladas/día × 50.000 litros/tonelada = 2.000.000 litros de agua.",
                    readingText = "En una ciudad se producen en promedio 600 toneladas diarias de residuos domésticos, de las cuales el 25% corresponde a papel y cartón, materiales fácilmente reciclables; además, por cada tonelada de papel y cartón que se recicla\n\n- se evita la tala de 17 árboles adultos y la plantación masiva de especies para la producción de pasta de papel.\n- se ahorran 140 litros de petróleo y 50.000 litros de agua."
                ),
                Question(
                    id = 413,
                    text = "Una persona afirma:\n\n\"Como al día se ahorran 140 litros de petróleo por cada tonelada de papel y cartón reciclado en la ciudad, durante un mes se ahorrarían exactamente 30 veces 140 litros de petróleo\".\n\nSu afirmación es",
                    options = listOf(
                        QuestionOption(id = 1, text = "correcta, porque el número 30 indica el número de días que tiene un mes.", originalOrder = 0),
                        QuestionOption(id = 2, text = "incorrecta, porque debe tener en cuenta las 150 toneladas de papel y cartón reciclado por día.", originalOrder = 1),
                        QuestionOption(id = 3, text = "correcta, porque tiene en cuenta que día tras día hay 140 litros más de petróleo ahorrado.", originalOrder = 2),
                        QuestionOption(id = 4, text = "incorrecta, porque debe tener en cuenta las 25 toneladas de papel y cartón reciclado por día.", originalOrder = 3)
                    ),
                    correctOptionId = 2,
                    explanation = "Debe considerar las 150 toneladas diarias (25% de 600 toneladas) de papel y cartón reciclable.",
                    readingText = "En una ciudad se producen en promedio 600 toneladas diarias de residuos domésticos, de las cuales el 25% corresponde a papel y cartón, materiales fácilmente reciclables; además, por cada tonelada de papel y cartón que se recicla\n\n- se evita la tala de 17 árboles adultos y la plantación masiva de especies para la producción de pasta de papel.\n- se ahorran 140 litros de petróleo y 50.000 litros de agua."
                ),
                Question(
                    id = 414,
                    text = "Los resultados indican que el ave 5 tarda más alimentándose que desplazándose. Esto es correcto, puesto que el tiempo en alimentación excede al de desplazamiento en",
                    options = listOf(
                        QuestionOption(id = 1, text = "20 minutos.", originalOrder = 0),
                        QuestionOption(id = 2, text = "25 minutos.", originalOrder = 1),
                        QuestionOption(id = 3, text = "33 minutos.", originalOrder = 2),
                        QuestionOption(id = 4, text = "45 minutos.", originalOrder = 3)
                    ),
                    correctOptionId = 2,
                    explanation = "Alimentación: 45 min, Desplazamiento: 20 min. Diferencia: 25 minutos.",
                    readingText = "Un científico estudia el comportamiento de cinco aves a lo largo de cuatro sesiones de 30 minutos cada una. Durante las sesiones, el científico mide el tiempo que le toma a cada ave realizar cada una de sus actividades y lo registra en la tabla.",
                    contextImage = "imagen_aves" // Tabla de actividades de las aves
                ),
                Question(
                    id = 415,
                    text = "Al analizar los resultados, el científico afirma que la relación entre cada tiempo de las actividades del ave 1 y del ave 5 es 3:2.\n\nLa afirmación del científico es",
                    options = listOf(
                        QuestionOption(id = 1, text = "correcta, porque el tiempo invertido en las actividades 2, 5 y 6 por el ave 1 es igual al tiempo invertido en las actividades 4 y 7 por el ave 5.", originalOrder = 0),
                        QuestionOption(id = 2, text = "incorrecta, porque el tiempo invertido en las actividades 3, 6 y 7 por el ave 1 es igual al tiempo invertido en las actividades 4, 6 y 7 por el ave 5.", originalOrder = 1),
                        QuestionOption(id = 3, text = "correcta, porque para la actividad 'Comunicación' la relación entre los tiempos está dada por 15/10 = 3/2.", originalOrder = 2),
                        QuestionOption(id = 4, text = "incorrecta, porque para la actividad 'Alimentación' la relación entre los tiempos está dada por 30/45 = 2/3.", originalOrder = 3)
                    ),
                    correctOptionId = 4,
                    explanation = "La relación no es constante para todas las actividades, como muestra la alimentación (30/45 = 2/3).",
                    readingText = "Un científico estudia el comportamiento de cinco aves a lo largo de cuatro sesiones de 30 minutos cada una. Durante las sesiones, el científico mide el tiempo que le toma a cada ave realizar cada una de sus actividades y lo registra en la tabla.",
                    contextImage = "imagen_aves" // Tabla de actividades de las aves
                ),
                Question(
                    id = 416,
                    text = "El científico quiere identificar cuál de las aves presenta las características de la siguiente descripción:\n\n- Tarda el doble del tiempo en alimentarse que en descansar.\n- La defecación dura menos del 10% del tiempo total de las sesiones.\n\nEstas características corresponden al ave",
                    options = listOf(
                        QuestionOption(id = 1, text = "1", originalOrder = 0),
                        QuestionOption(id = 2, text = "2", originalOrder = 1),
                        QuestionOption(id = 3, text = "3", originalOrder = 2),
                        QuestionOption(id = 4, text = "5", originalOrder = 3)
                    ),
                    correctOptionId = 1,
                    explanation = "Ave 1: Alimentación 30 min, Descanso 15 min (30 = 2×15). Defecación 5 min (<10% de 120 min).",
                    readingText = "Un científico estudia el comportamiento de cinco aves a lo largo de cuatro sesiones de 30 minutos cada una. Durante las sesiones, el científico mide el tiempo que le toma a cada ave realizar cada una de sus actividades y lo registra en la tabla.",
                    contextImage = "imagen_aves" // Tabla de actividades de las aves
                ),
                Question(
                    id = 417,
                    text = "Una pista marcada en un extremo con el número 24, en el extremo opuesto está marcada con el número",
                    options = listOf(
                        QuestionOption(id = 1, text = "06", originalOrder = 0),
                        QuestionOption(id = 2, text = "18", originalOrder = 1),
                        QuestionOption(id = 3, text = "36", originalOrder = 2),
                        QuestionOption(id = 4, text = "42", originalOrder = 3)
                    ),
                    correctOptionId = 1,
                    explanation = "El extremo opuesto se calcula: (24 × 10) - 180 = 240 - 180 = 60 → 06",
                    readingText = "Las pistas de aterrizaje de los aeropuertos se marcan en sus extremos de acuerdo con su alineación con el norte magnético. Cada pista recibe dos números, uno en cada extremo, según la dirección en la que se orienta la aeronave.",
                    contextImage = "imagen_brujula" // Diagrama de pistas de aterrizaje
                ),
                Question(
                    id = 418,
                    text = "Al piloto de un avión que está alineado para aterrizar en el extremo 24 se le pide que cambie su rumbo girando 30 grados a su derecha para que use una pista libre. El número que encuentra en la nueva pista es",
                    options = listOf(
                        QuestionOption(id = 1, text = "06", originalOrder = 0),
                        QuestionOption(id = 2, text = "21", originalOrder = 1),
                        QuestionOption(id = 3, text = "27", originalOrder = 2),
                        QuestionOption(id = 4, text = "54", originalOrder = 3)
                    ),
                    correctOptionId = 2,
                    explanation = "24 corresponde a 240°. Girar 30° derecha: 240° - 30° = 210° → 21",
                    readingText = "Las pistas de aterrizaje de los aeropuertos se marcan en sus extremos de acuerdo con su alineación con el norte magnético. Cada pista recibe dos números, uno en cada extremo, según la dirección en la que se orienta la aeronave.",
                    contextImage = "imagen_brujula" // Diagrama de pistas de aterrizaje
                ),
                Question(
                    id = 419,
                    text = "Un avión que despega en dirección al extremo 32, va hacia el",
                    options = listOf(
                        QuestionOption(id = 1, text = "sureste.", originalOrder = 0),
                        QuestionOption(id = 2, text = "noreste.", originalOrder = 1),
                        QuestionOption(id = 3, text = "suroeste.", originalOrder = 2),
                        QuestionOption(id = 4, text = "noroeste.", originalOrder = 3)
                    ),
                    correctOptionId = 1,
                    explanation = "32 corresponde a 320° (N40°O), pero despegando del extremo opuesto 14 (140° - SE)",
                    readingText = "Las pistas de aterrizaje de los aeropuertos se marcan en sus extremos de acuerdo con su alineación con el norte magnético. Cada pista recibe dos números, uno en cada extremo, según la dirección en la que se orienta la aeronave.",
                    contextImage = "imagen_brujula" // Diagrama de pistas de aterrizaje
                )
            )
            3 -> listOf(
                Question(
                    id = 420,
                    text = "Un tanque almacena exactamente la cantidad de jabón líquido necesaria para envasar exactamente 50 unidades de cada tipo de contenido. Teniendo en cuenta que 1 Litro contiene 1.000 mL, ¿cuál es la capacidad del tanque?",
                    options = listOf(
                        QuestionOption(id = 1, text = "15 litros.", originalOrder = 0),
                        QuestionOption(id = 2, text = "75 litros.", originalOrder = 1),
                        QuestionOption(id = 3, text = "1.500 litros.", originalOrder = 2),
                        QuestionOption(id = 4, text = "75.000 litros.", originalOrder = 3)
                    ),
                    correctOptionId = 2,
                    explanation = "50×(300+600+900)mL = 50×1800mL = 90.000mL = 90L para jabón líquido",
                    readingText = "Una microempresa de productos de aseo elabora jabón de tocador en dos presentaciones, y ofrece tres contenidos en cada una. Cada presentación y contenido se encuentra disponible en tres aromas: natural, coco y vainilla.",
                    contextImage = "imagen_jabones" // Tabla de presentaciones y precios de jabones
                ),
                Question(
                    id = 421,
                    text = "De acuerdo con la información de la tabla, si se conservara la relación entre el contenido y el precio por unidad, ¿cuál debería ser el precio de la presentación de jabón líquido con contenido de 1.800 ml?",
                    options = listOf(
                        QuestionOption(id = 1, text = "\$15.300", originalOrder = 0),
                        QuestionOption(id = 2, text = "\$18.000", originalOrder = 1),
                        QuestionOption(id = 3, text = "\$30.600", originalOrder = 2),
                        QuestionOption(id = 4, text = "\$31.660", originalOrder = 3)
                    ),
                    correctOptionId = 3,
                    explanation = "Precio por mL: 5.100/300 = 17. 1.800×17 = 30.600",
                    readingText = "Una microempresa de productos de aseo elabora jabón de tocador en dos presentaciones, y ofrece tres contenidos en cada una. Cada presentación y contenido se encuentra disponible en tres aromas: natural, coco y vainilla.",
                    contextImage = "imagen_jabones" // Tabla de presentaciones y precios de jabones
                ),
                Question(
                    id = 422,
                    text = "La etiqueta del jabón debe especificar tres aspectos: presentación, contenido y aroma. ¿Cuántas etiquetas diferentes debe utilizar la fábrica?",
                    options = listOf(
                        QuestionOption(id = 1, text = "2", originalOrder = 0),
                        QuestionOption(id = 2, text = "6", originalOrder = 1),
                        QuestionOption(id = 3, text = "12", originalOrder = 2),
                        QuestionOption(id = 4, text = "18", originalOrder = 3)
                    ),
                    correctOptionId = 4,
                    explanation = "2 presentaciones × 3 contenidos × 3 aromas = 18 combinaciones",
                    readingText = "Una microempresa de productos de aseo elabora jabón de tocador en dos presentaciones, y ofrece tres contenidos en cada una. Cada presentación y contenido se encuentra disponible en tres aromas: natural, coco y vainilla.",
                    contextImage = "imagen_jabones" // Tabla de presentaciones y precios de jabones
                ),
                Question(
                    id = 423,
                    text = "¿A cuál o cuáles de los vendedores se debe dar el incentivo?",
                    options = listOf(
                        QuestionOption(id = 1, text = "I solamente.", originalOrder = 0),
                        QuestionOption(id = 2, text = "III solamente.", originalOrder = 1),
                        QuestionOption(id = 3, text = "I y II solamente.", originalOrder = 2),
                        QuestionOption(id = 4, text = "I, II y III.", originalOrder = 3)
                    ),
                    correctOptionId = 3,
                    explanation = "Vendedor I: \$520.000, Vendedor II: \$510.000 (ambos > \$500.000), Vendedor III: \$490.000",
                    readingText = "La microempresa otorga incentivos a los vendedores cuyas ventas semanales sean superiores a \$500.000.",
                    contextImage = "imagen_jabones_ventas" // Tabla de ventas de vendedores
                ),
                Question(
                    id = 424,
                    text = "El estudiante concluye que el diámetro es 106 millones de años luz. El anterior procedimiento es incorrecto, porque",
                    options = listOf(
                        QuestionOption(id = 1, text = "el denominador de la fracción debe expresarse en potencias de diez.", originalOrder = 0),
                        QuestionOption(id = 2, text = "no se tiene en cuenta la equivalencia de unidades entre las magnitudes involucradas.", originalOrder = 1),
                        QuestionOption(id = 3, text = "para obtener el diámetro se debe determinar el producto entre ambas medidas relacionadas.", originalOrder = 2),
                        QuestionOption(id = 4, text = "el resultado no se expresa en potencias de diez como los otros datos.", originalOrder = 3)
                    ),
                    correctOptionId = 2,
                    explanation = "No se convirtieron correctamente las unidades (metros a kilómetros para usar la equivalencia de año luz).",
                    readingText = "Usualmente, las distancias en el espacio se miden en años luz. Un año luz corresponde a la distancia que recorre la luz en un año (aproximadamente 9,46 x 10¹² km).",
                    contextImage = "imagen_10" // Expresión matemática para cálculo de años luz
                ),
                Question(
                    id = 425,
                    text = "El organizador de la fiesta quiere estimar cuál es la capacidad de la fuente, para lo cual mide la altura y el radio del recipiente en el nivel inferior.\n\nDe las medidas halladas por el organizador para estimar la capacidad total de la fuente, es verdadero afirmar que",
                    options = listOf(
                        QuestionOption(id = 1, text = "no son suficientes, pues falta conocer el peso del chocolate y la resistencia que tiene el material de los recipientes.", originalOrder = 0),
                        QuestionOption(id = 2, text = "son suficientes, pues se llenan los otros recipientes, el chocolate se saldrá de la fuente cuando esta comience a operar.", originalOrder = 1),
                        QuestionOption(id = 3, text = "no son suficientes, pues no toman en cuenta la capacidad de los otros recipientes y el chocolate en el tubo de circulación.", originalOrder = 2),
                        QuestionOption(id = 4, text = "son suficientes, pues el recipiente más bajo es el que recibe el chocolate que se vierte de los otros dos.", originalOrder = 3)
                    ),
                    correctOptionId = 3,
                    explanation = "Se necesita conocer las capacidades de los tres recipientes y el tubo para calcular la capacidad total.",
                    readingText = "Para una fiesta infantil se tiene una fuente de chocolate con tres niveles, cuyos recipientes son cilíndricos. El tubo cilíndrico que los une permite que el chocolate suba desde el nivel más bajo hasta el más alto.",
                    contextImage = "imagen_11" // Diagrama de fuente de chocolate de tres niveles
                )
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
                    correctOptionId = 6,
                    explanation = "People cry when they feel sad."
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
                    correctOptionId = 5,
                    explanation = "When we feel hungry, we want to eat."
                ),
                Question(
                    id = 703,
                    text = "When we hate something, we sometimes feel like this.",
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
                    correctOptionId = 2,
                    explanation = "Hate often makes people feel angry."
                ),
                Question(
                    id = 704,
                    text = "Some people usually feel like this when it's hot, and need to drink something.",
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
                    correctOptionId = 7,
                    explanation = "When it's hot, people feel thirsty and need to drink."
                ),
                Question(
                    id = 705,
                    text = "A person feels like this when they need to sleep after a long day.",
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
                    correctOptionId = 8,
                    explanation = "After a long day, people feel tired and need to sleep."
                )
            )
            2 -> listOf(
                Question(
                    id = 706,
                    text = "Do you prefer cats or dogs?",
                    options = listOf(
                        QuestionOption(id = 1, text = "Both are nice.", originalOrder = 0),
                        QuestionOption(id = 2, text = "It's not OK.", originalOrder = 1),
                        QuestionOption(id = 3, text = "All right.", originalOrder = 2)
                    ),
                    correctOptionId = 1,
                    explanation = "'Both are nice' is the appropriate response to a preference question."
                ),
                Question(
                    id = 707,
                    text = "I forgot to turn the lights off.",
                    options = listOf(
                        QuestionOption(id = 1, text = "How about this?", originalOrder = 0),
                        QuestionOption(id = 2, text = "Are you sure?", originalOrder = 1),
                        QuestionOption(id = 3, text = "Do it this way.", originalOrder = 2)
                    ),
                    correctOptionId = 2,
                    explanation = "'Are you sure?' is a natural response to someone expressing forgetfulness."
                ),
                Question(
                    id = 708,
                    text = "I am afraid my sister is sick.",
                    options = listOf(
                        QuestionOption(id = 1, text = "Oh, I'm sorry.", originalOrder = 0),
                        QuestionOption(id = 2, text = "Too late.", originalOrder = 1),
                        QuestionOption(id = 3, text = "Can I go now?", originalOrder = 2)
                    ),
                    correctOptionId = 1,
                    explanation = "'Oh, I'm sorry' shows empathy for someone's concern about illness."
                ),
                Question(
                    id = 709,
                    text = "Let's go to the park next weekend.",
                    options = listOf(
                        QuestionOption(id = 1, text = "As soon as possible.", originalOrder = 0),
                        QuestionOption(id = 2, text = "Hope it is.", originalOrder = 1),
                        QuestionOption(id = 3, text = "Great idea.", originalOrder = 2)
                    ),
                    correctOptionId = 3,
                    explanation = "'Great idea' shows agreement with a suggestion."
                ),
                Question(
                    id = 710,
                    text = "Can I talk to you for a minute?",
                    options = listOf(
                        QuestionOption(id = 1, text = "Be careful.", originalOrder = 0),
                        QuestionOption(id = 2, text = "Of course.", originalOrder = 1),
                        QuestionOption(id = 3, text = "Just one.", originalOrder = 2)
                    ),
                    correctOptionId = 2,
                    explanation = "'Of course' is a polite way to grant permission for a conversation."
                )
            )
            3 -> listOf(
                Question(
                    id = 711,
                    text = "Complete: 'Sadly, many people today ___ know the differences between Greeks and Romans.'",
                    options = listOf(
                        QuestionOption(id = 1, text = "doesn't", originalOrder = 0),
                        QuestionOption(id = 2, text = "don't", originalOrder = 1),
                        QuestionOption(id = 3, text = "didn't", originalOrder = 2)
                    ),
                    correctOptionId = 2,
                    explanation = "'don't' is the correct plural form for 'many people'",
                    readingText = "Complete the text about Greek and Roman cultural differences.",
                    contextImage = "geek_and_roman_culture" // Imagen sobre cultura griega y romana
                ),
                Question(
                    id = 712,
                    text = "Complete: 'In fact, the two are very different ___ one another.'",
                    options = listOf(
                        QuestionOption(id = 1, text = "among", originalOrder = 0),
                        QuestionOption(id = 2, text = "against", originalOrder = 1),
                        QuestionOption(id = 3, text = "from", originalOrder = 2)
                    ),
                    correctOptionId = 3,
                    explanation = "'different from' is the correct prepositional phrase",
                    readingText = "Complete the text about Greek and Roman cultural differences.",
                    contextImage = "geek_and_roman_culture" // Imagen sobre cultura griega y romana
                ),
                Question(
                    id = 713,
                    text = "Complete: '___ Greeks and Romans were great architects.'",
                    options = listOf(
                        QuestionOption(id = 1, text = "Either", originalOrder = 0),
                        QuestionOption(id = 2, text = "Both", originalOrder = 1),
                        QuestionOption(id = 3, text = "Each", originalOrder = 2)
                    ),
                    correctOptionId = 2,
                    explanation = "'Both' is used to refer to two groups positively",
                    readingText = "Complete the text about Greek and Roman cultural differences.",
                    contextImage = "geek_and_roman_culture" // Imagen sobre cultura griega y romana
                ),
                Question(
                    id = 714,
                    text = "Complete: 'Greeks used to ___ more about shape than function.'",
                    options = listOf(
                        QuestionOption(id = 1, text = "worry", originalOrder = 0),
                        QuestionOption(id = 2, text = "worries", originalOrder = 1),
                        QuestionOption(id = 3, text = "worrying", originalOrder = 2)
                    ),
                    correctOptionId = 1,
                    explanation = "'used to worry' is the correct verb form for past habits",
                    readingText = "Complete the text about Greek and Roman cultural differences.",
                    contextImage = "geek_and_roman_culture" // Imagen sobre cultura griega y romana
                ),
                Question(
                    id = 715,
                    text = "Complete: 'They ___ the most important thing was making beautiful buildings.'",
                    options = listOf(
                        QuestionOption(id = 1, text = "believed", originalOrder = 0),
                        QuestionOption(id = 2, text = "believes", originalOrder = 1),
                        QuestionOption(id = 3, text = "believe", originalOrder = 2)
                    ),
                    correctOptionId = 1,
                    explanation = "'believed' is the past tense for 'they'",
                    readingText = "Complete the text about Greek and Roman cultural differences.",
                    contextImage = "geek_and_roman_culture" // Imagen sobre cultura griega y romana
                ),
                Question(
                    id = 716,
                    text = "Complete: '___, Romans were perfect engineers.'",
                    options = listOf(
                        QuestionOption(id = 1, text = "Almost", originalOrder = 0),
                        QuestionOption(id = 2, text = "However", originalOrder = 1),
                        QuestionOption(id = 3, text = "Indeed", originalOrder = 2)
                    ),
                    correctOptionId = 2,
                    explanation = "'However' shows contrast with the previous statement about Greeks",
                    readingText = "Complete the text about Greek and Roman cultural differences.",
                    contextImage = "geek_and_roman_culture" // Imagen sobre cultura griega y romana
                ),
                Question(
                    id = 717,
                    text = "Complete: 'For ___ street planning and use had the greatest importance.'",
                    options = listOf(
                        QuestionOption(id = 1, text = "you", originalOrder = 0),
                        QuestionOption(id = 2, text = "her", originalOrder = 1),
                        QuestionOption(id = 3, text = "them", originalOrder = 2)
                    ),
                    correctOptionId = 3,
                    explanation = "'them' refers to the Romans mentioned earlier",
                    readingText = "Complete the text about Greek and Roman cultural differences.",
                    contextImage = "geek_and_roman_culture" // Imagen sobre cultura griega y romana
                ),
                Question(
                    id = 718,
                    text = "Complete: 'Greeks admired poets and philosophers, ___ Romans admired their soldiers.'",
                    options = listOf(
                        QuestionOption(id = 1, text = "but", originalOrder = 0),
                        QuestionOption(id = 2, text = "or", originalOrder = 1),
                        QuestionOption(id = 3, text = "so", originalOrder = 2)
                    ),
                    correctOptionId = 1,
                    explanation = "'but' shows contrast between what Greeks and Romans admired",
                    readingText = "Complete the text about Greek and Roman cultural differences.",
                    contextImage = "geek_and_roman_culture" // Imagen sobre cultura griega y romana
                )
            )
            4 -> listOf(
                Question(
                    id = 719,
                    text = "John Humphrey Noyes travelled to New York State",
                    options = listOf(
                        QuestionOption(id = 1, text = "to visit his family.", originalOrder = 0),
                        QuestionOption(id = 2, text = "to change his way of life.", originalOrder = 1),
                        QuestionOption(id = 3, text = "to know more about his country.", originalOrder = 2)
                    ),
                    correctOptionId = 2,
                    explanation = "He wanted to live according to his beliefs and create a more equal society.",
                    contextImage = "social_experiment_text"
                ),
                Question(
                    id = 720,
                    text = "Nowadays Oneida is",
                    options = listOf(
                        QuestionOption(id = 1, text = "a hotel.", originalOrder = 0),
                        QuestionOption(id = 2, text = "a display.", originalOrder = 1),
                        QuestionOption(id = 3, text = "a school.", originalOrder = 2)
                    ),
                    correctOptionId = 1,
                    explanation = "The building now functions as a hotel with guest rooms.",
                    contextImage = "social_experiment_text"
                ),
                Question(
                    id = 721,
                    text = "The writer visited the place because he",
                    options = listOf(
                        QuestionOption(id = 1, text = "had particular social beliefs.", originalOrder = 0),
                        QuestionOption(id = 2, text = "had family who had lived there.", originalOrder = 1),
                        QuestionOption(id = 3, text = "had interest in historical buildings.", originalOrder = 2)
                    ),
                    correctOptionId = 2,
                    explanation = "The writer mentions having relatives among the 19th century dreamers.",
                    contextImage = "social_experiment_text"
                ),
                Question(
                    id = 722,
                    text = "If you want to see the building,",
                    options = listOf(
                        QuestionOption(id = 1, text = "you need to pay to have a guided visit.", originalOrder = 0),
                        QuestionOption(id = 2, text = "you can join a small group of people.", originalOrder = 1),
                        QuestionOption(id = 3, text = "you will have your own guide included.", originalOrder = 2)
                    ),
                    correctOptionId = 3,
                    explanation = "The \$100 fee includes a private tour of the building.",
                    contextImage = "social_experiment_text"
                ),
                Question(
                    id = 723,
                    text = "All visitors to the Oneida historical site",
                    options = listOf(
                        QuestionOption(id = 1, text = "have access to most parts of the house.", originalOrder = 0),
                        QuestionOption(id = 2, text = "have to go to the 19th century library.", originalOrder = 1),
                        QuestionOption(id = 3, text = "are not allowed to see certain places.", originalOrder = 2)
                    ),
                    correctOptionId = 1,
                    explanation = "The library, grounds, and several public rooms are open to guests.",
                    contextImage = "social_experiment_text"
                ),
                Question(
                    id = 724,
                    text = "The old library",
                    options = listOf(
                        QuestionOption(id = 1, text = "has changed a little.", originalOrder = 0),
                        QuestionOption(id = 2, text = "has remained the same.", originalOrder = 1),
                        QuestionOption(id = 3, text = "has been damaged.", originalOrder = 2)
                    ),
                    correctOptionId = 2,
                    explanation = "The library is described as 'unchanged from the original construction'.",
                    contextImage = "social_experiment_text"
                ),
                Question(
                    id = 725,
                    text = "People at Oneida studied",
                    options = listOf(
                        QuestionOption(id = 1, text = "languages.", originalOrder = 0),
                        QuestionOption(id = 2, text = "politics.", originalOrder = 1),
                        QuestionOption(id = 3, text = "anatomy.", originalOrder = 2)
                    ),
                    correctOptionId = 1,
                    explanation = "They studied Latin and Greek, which are languages.",
                    contextImage = "social_experiment_text"
                )
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
                    explanation = "La Constitución establece el principio de solidaridad como uno de los fundamentos del Estado social de derecho.",
                    readingText = "Después de una fuerte temporada de lluvias, el Gobierno nacional despliega ayudas para atender a la población afectada.\n\nNo obstante, los daños materiales y a la salud son muy grandes. Se hace un llamado a la ciudadanía para que aporte dinero y materiales y así poder brindar una mejor respuesta a la emergencia. Tras este llamado, la respuesta de la ciudadanía es casi nula."
                ),
                Question(
                    id = 1002,
                    text = "¿Cuál de las siguientes soluciones vulnera el derecho a la educación?",
                    options = listOf(
                        QuestionOption(id = 1, text = "Organizar esquemas de transporte para que los estudiantes vayan a clases en escuelas no afectadas por las inundaciones.", originalOrder = 0),
                        QuestionOption(id = 2, text = "Ajustar el calendario para incluir los sábados como día escolar y que no haya clases en las épocas de inundación.", originalOrder = 1),
                        QuestionOption(id = 3, text = "Dar una parte de las clases a través de tutores a domicilio para reducir los días en que los niños tienen que asistir presencialmente al colegio.", originalOrder = 2),
                        QuestionOption(id = 4, text = "Trasladar a los niños a escuelas no inundadas dos veces por semana y reducir el número de clases presenciales.", originalOrder = 3)
                    ),
                    correctOptionId = 4,
                    explanation = "Reducir significativamente las clases presenciales vulnera el derecho a la educación al limitar el acceso regular y continuo a la educación.",
                    readingText = "Durante las épocas de lluvias, en muchas zonas rurales de Colombia se inundan escuelas y se interrumpen los caminos para llegar a estas."
                ),
                Question(
                    id = 1003,
                    text = "¿Por qué este proyecto no podría aprobarse?",
                    options = listOf(
                        QuestionOption(id = 1, text = "Porque contradice lo dicho en la Constitución, la cual protege todas las creencias religiosas y no solo las de la mayoría.", originalOrder = 0),
                        QuestionOption(id = 2, text = "Porque contradice los derechos de los legisladores que pertenezcan a una religión minoritaria, quienes tendrían que renunciar al Congreso.", originalOrder = 1),
                        QuestionOption(id = 3, text = "Porque contradice las normas internacionales, las cuales establecen que las creencias religiosas son del dominio de las iglesias y no de los Estados.", originalOrder = 2),
                        QuestionOption(id = 4, text = "Porque contradice las leyes actuales, las cuales establecen que la religión es un asunto privado.", originalOrder = 3)
                    ),
                    correctOptionId = 1,
                    explanation = "La Constitución garantiza la libertad de cultos y la igualdad de todas las religiones, no solo las mayoritarias.",
                    readingText = "Recientemente se propuso un proyecto de ley ordinaria que condena el satanismo, porque es contrario a las creencias religiosas de la mayoría de la población colombiana."
                ),
                Question(
                    id = 1004,
                    text = "De acuerdo con la Constitución Política de Colombia, ¿podrían los ciudadanos acudir a un mecanismo de participación directa para pronunciarse sobre este tema?",
                    options = listOf(
                        QuestionOption(id = 1, text = "No, los asuntos penales son competencia exclusiva de la Rama Judicial.", originalOrder = 0),
                        QuestionOption(id = 2, text = "No, la decisión debe ser tomada por el presidente vía decreto.", originalOrder = 1),
                        QuestionOption(id = 3, text = "Sí, a través de un referendo constitucional.", originalOrder = 2),
                        QuestionOption(id = 4, text = "No, solo el Congreso puede modificar la Constitución mediante acto legislativo.", originalOrder = 3)
                    ),
                    correctOptionId = 3,
                    explanation = "El referendo constitucional es un mecanismo de participación ciudadana para modificar la Constitución.",
                    readingText = "Suponga que en el país se discute la conveniencia de reformar la Constitución para permitir la pena de muerte."
                ),
                Question(
                    id = 1005,
                    text = "¿Cuál de los siguientes es el organismo encargado de elegir al Contralor General de la República?",
                    options = listOf(
                        QuestionOption(id = 1, text = "La Presidencia de la República.", originalOrder = 0),
                        QuestionOption(id = 2, text = "La Corte Constitucional.", originalOrder = 1),
                        QuestionOption(id = 3, text = "El Congreso de la República.", originalOrder = 2),
                        QuestionOption(id = 4, text = "El Consejo de Estado.", originalOrder = 3)
                    ),
                    correctOptionId = 3,
                    explanation = "Según la Constitución, el Contralor General de la República es elegido por el Congreso de la República."
                ),
                Question(
                    id = 1006,
                    text = "¿Tiene razón el funcionario al hacerle a la mujer esta solicitud?",
                    options = listOf(
                        QuestionOption(id = 1, text = "Sí, porque Colombia es un país católico.", originalOrder = 0),
                        QuestionOption(id = 2, text = "Sí, porque el Estado colombiano es laico.", originalOrder = 1),
                        QuestionOption(id = 3, text = "No, porque le está vulnerando el derecho a la privacidad.", originalOrder = 2),
                        QuestionOption(id = 4, text = "No, porque le está vulnerando el derecho a la igualdad.", originalOrder = 3)
                    ),
                    correctOptionId = 4,
                    explanation = "La Constitución garantiza la igualdad y la libertad de cultos, por lo que no se puede discriminar por motivos religiosos.",
                    readingText = "Una colombiana es devota del islam y todos los días cubre su cabeza con una pañoleta.\n\nEn una entrevista, después de aprobar con éxito una convocatoria para un cargo en la alcaldía de su ciudad, el funcionario entrevistador le advierte que no puede tomar el trabajo si no acepta llevar la cabeza descubierta durante las horas laborales."
                )
            )
            2 -> listOf(
                Question(
                    id = 1007,
                    text = "Las divergencias entre científicos y activistas radican en que:",
                    options = listOf(
                        QuestionOption(id = 1, text = "los activistas quieren oponerse al desarrollo económico, mientras que los científicos quieren mejorar la vida de todos.", originalOrder = 0),
                        QuestionOption(id = 2, text = "los activistas quieren cuestionar la efectividad de la ciencia, mientras que los científicos quieren evadir el control a su trabajo.", originalOrder = 1),
                        QuestionOption(id = 3, text = "los científicos quieren utilizar a los animales como instrumentos de investigación, mientras que los activistas quieren que todos desarrollen sentimientos de apego hacia los animales.", originalOrder = 2),
                        QuestionOption(id = 4, text = "los científicos quieren priorizar la vida humana, mientras que los activistas quieren proteger lo que consideran un derecho de los animales a su propia vida.", originalOrder = 3)
                    ),
                    correctOptionId = 4,
                    explanation = "La divergencia central está en la priorización de valores: vida humana vs. derechos de los animales.",
                    readingText = "Un grupo de activistas que defiende los derechos de los animales busca evitar que un centro de investigación siga utilizando cerdos para sus experimentos, pues los tratamientos incluyen procedimientos que se consideran maltrato y que en algunos casos terminan en la muerte de los animales.\n\nPor su parte, los investigadores defienden su uso, pues estos animales proveen la mejor plataforma para desarrollar sus medicinas sin poner en riesgo vidas humanas. Además, mencionan que el laboratorio no viola ninguna de las normas existentes sobre investigación con animales y que su labor permite proteger la vida de las personas."
                ),
                Question(
                    id = 1008,
                    text = "A la hora de evaluar la propuesta, ¿qué intereses podrían entrar en conflicto?",
                    options = listOf(
                        QuestionOption(id = 1, text = "Los de las empresas de seguridad privada que buscan expandir su negocio y los de los residentes de los barrios cerrados quienes tendrían que soportar controles de seguridad a diario.", originalOrder = 0),
                        QuestionOption(id = 2, text = "Los de los residentes que buscan su seguridad, y los de los demás habitantes de la ciudad que dejarían de tener acceso a las vías y otros espacios públicos de los sectores encerrados.", originalOrder = 1),
                        QuestionOption(id = 3, text = "Los de los residentes que buscan su beneficio particular y los intereses del gobierno local que debe encargarse del bienestar de la comunidad.", originalOrder = 2),
                        QuestionOption(id = 4, text = "Los de las empresas de seguridad privada que serían contratadas para cuidar los sectores residenciales.", originalOrder = 3)
                    ),
                    correctOptionId = 2,
                    explanation = "El conflicto principal es entre la seguridad privada de algunos y el acceso público de todos a espacios comunes.",
                    readingText = "Ante los crecientes robos a residencias en algunos sectores de estratos altos de una ciudad, el alcalde está considerando autorizar el cierre de grandes sectores residenciales que agrupen varios barrios de la ciudad, para que sean vigilados por seguridad privada y para que solo se permita la entrada a los residentes o a personas autorizadas por estos."
                ),
                Question(
                    id = 1009,
                    text = "En esta situación, ¿qué aspectos están en conflicto?",
                    options = listOf(
                        QuestionOption(id = 1, text = "Las democracias de los países europeos industrializados y las teocracias de algunos países de Oriente.", originalOrder = 0),
                        QuestionOption(id = 2, text = "Las religiones occidentales como el cristianismo y el judaísmo, y las religiones orientales como el islam.", originalOrder = 1),
                        QuestionOption(id = 3, text = "La libertad de expresión de los caricaturistas y las creencias religiosas de un grupo de personas.", originalOrder = 2),
                        QuestionOption(id = 4, text = "Los valores culturales de Europa y las tradiciones ancestrales de África y Asia.", originalOrder = 3)
                    ),
                    correctOptionId = 3,
                    explanation = "El conflicto central es entre el derecho a la libertad de expresión y el respeto a las creencias religiosas.",
                    readingText = "A raíz de la autocensura de varios medios europeos acerca de temas de religión, en 2005 un periódico danés le pidió a varios caricaturistas expresar sus impresiones y sentimientos sobre el Islam.\n\nComo resultado, el periódico publicó doce caricaturas sobre Mahoma, las cuales despertaron la rabia de muchos musulmanes que protestaron por este hecho."
                ),
                Question(
                    id = 1010,
                    text = "En esta situación, ¿cuáles de los siguientes aspectos están en conflicto?",
                    options = listOf(
                        QuestionOption(id = 1, text = "El medio ambiente en la capital y la voluntad del gobierno departamental.", originalOrder = 0),
                        QuestionOption(id = 2, text = "La economía del municipio y las políticas medioambientales de la capital.", originalOrder = 1),
                        QuestionOption(id = 3, text = "La salubridad de la capital y la voluntad de los habitantes del municipio.", originalOrder = 2),
                        QuestionOption(id = 4, text = "El interés comercial del gobierno departamental y el bienestar social del municipio.", originalOrder = 3)
                    ),
                    correctOptionId = 3,
                    explanation = "El conflicto principal es entre la necesidad de salubridad de la capital y la voluntad democrática de los habitantes del municipio afectado.",
                    readingText = "Para solucionar los problemas de basura de la capital del departamento, se decidió construir un relleno sanitario en un lote de otro municipio cercano, al cual se le compensaría económicamente por prestar este servicio a la capital.\n\nSegún el gobierno departamental, la escogencia del lote se hizo conjuntamente con las autoridades ambientales y con la alcaldía del municipio.\n\nEl proyecto del relleno sanitario cuenta con todas las licencias ambientales requeridas y su construcción es necesaria para la salubridad de la capital.\n\nEn una consulta popular, los habitantes del municipio cercano votaron 96% en contra de la construcción del relleno, porque consideran que este es una amenaza para la salud de la población y el ecosistema, y disminuiría el valor de la tierra circundante."
                ),
                Question(
                    id = 1011,
                    text = "En la descripción de esta situación, se le da mayor énfasis a:",
                    options = listOf(
                        QuestionOption(id = 1, text = "las consecuencias económicas de la discriminación a un pueblo con grandes capitales.", originalOrder = 0),
                        QuestionOption(id = 2, text = "los efectos de la decisión del juez en las relaciones internacionales alemanas.", originalOrder = 1),
                        QuestionOption(id = 3, text = "las creencias religiosas y tradiciones que respaldan la práctica de la circuncisión.", originalOrder = 2),
                        QuestionOption(id = 4, text = "las medidas judiciales que prohíben la práctica de las religiones minoritarias.", originalOrder = 3)
                    ),
                    correctOptionId = 3,
                    explanation = "El texto enfatiza las tradiciones religiosas y culturales que sustentan la práctica de la circuncisión.",
                    readingText = "Durante siglos, los practicantes judíos y musulmanes han practicado la circuncisión a sus hijos.\n\nEn Alemania, un juez tomó la decisión de prohibir la circuncisión a niños menores de 14 años de edad.\n\nA esa edad, cada niño decidirá por sí mismo si se la realiza.\n\nLas grandes comunidades de judíos y musulmanes piensan que este es un ataque en contra de sus creencias e invitan a seguir practicando la circuncisión a temprana edad.\n\nEl asunto ha generado polémica, pues se considera un nuevo ataque a las costumbres y herencia judía y musulmana, pueblos que siguen siendo discriminados por su cultura a pesar de sus grandes aportes económicos a los países europeos."
                ),
                Question(
                    id = 1012,
                    text = "¿Cuál de los siguientes argumentos respalda esta decisión de la Corte?",
                    options = listOf(
                        QuestionOption(id = 1, text = "Se requiere garantizar la igualdad de derechos para todos los ciudadanos.", originalOrder = 0),
                        QuestionOption(id = 2, text = "Es necesaria la protección de los derechos de los ciudadanos pensionados.", originalOrder = 1),
                        QuestionOption(id = 3, text = "Se requiere proteger los derechos derivados del matrimonio entre hombres y mujeres.", originalOrder = 2),
                        QuestionOption(id = 4, text = "La Constitución no reconoce los derechos patrimoniales de las parejas del mismo sexo.", originalOrder = 3)
                    ),
                    correctOptionId = 1,
                    explanation = "La Corte Constitucional fundamentó su decisión en el principio de igualdad consagrado en la Constitución.",
                    readingText = "Entre 2007 y 2008, la Corte Constitucional extendió a parejas homosexuales los beneficios del régimen contributivo de salud y del derecho a la pensión de sobrevivientes."
                )
            )
            3 -> listOf(
                Question(
                    id = 1013,
                    text = "¿Cuál de los siguientes es un argumento válido para contradecir la postura expuesta?",
                    options = listOf(
                        QuestionOption(id = 1, text = "Los policías no deben ser cuestionados, ya que representan a una institución que está hecha para ayudar.", originalOrder = 0),
                        QuestionOption(id = 2, text = "El hecho de que los policías estén donde hay grandes congestiones de vehículos no quiere decir que ellos sean quienes las originen.", originalOrder = 1),
                        QuestionOption(id = 3, text = "Los policías hacen adecuadamente su labor, pero no son suficientes para evitar todas las congestiones de vehículos que se presentan.", originalOrder = 2),
                        QuestionOption(id = 4, text = "El hecho de que haya grandes congestiones vehiculares no significa que los policías no estén capacitados.", originalOrder = 3)
                    ),
                    correctOptionId = 2,
                    explanation = "Este argumento identifica correctamente la falacia de correlación-causalidad en la crítica a los policías.",
                    readingText = "Algunas personas critican a los policías de tránsito y los culpan de las grandes congestiones de vehículos en las ciudades, porque observan que cuando hay una gran congestión es frecuente que haya un policía guiando el tránsito."
                ),
                Question(
                    id = 1014,
                    text = "¿Cuál de los siguientes es una consecuencia de lo dicho por el gerente?",
                    options = listOf(
                        QuestionOption(id = 1, text = "Los miembros de la junta directiva de la empresa respaldarían el pronunciamiento porque implica seleccionar a los mejores trabajadores.", originalOrder = 0),
                        QuestionOption(id = 2, text = "Los empresarios del país respaldarían el pronunciamiento porque significaría un aumento de empleo en los municipios de tierra fría.", originalOrder = 1),
                        QuestionOption(id = 3, text = "Representantes del Gobierno se opondrían a la pretensión de la empresa de aprovechar la política para reducir sus impuestos.", originalOrder = 2),
                        QuestionOption(id = 4, text = "Representantes de los trabajadores se opondrían porque se basa en un prejuicio personal.", originalOrder = 3)
                    ),
                    correctOptionId = 4,
                    explanation = "El gerente expresa un prejuicio discriminatorio que generaría oposición de los representantes de los trabajadores.",
                    readingText = "Un Gobierno ofreció reducir impuestos a las empresas privadas que generen nuevos empleos.\n\nEl gerente de una reconocida empresa que anualmente paga importantes sumas de dinero en impuestos, dijo:\n\n\"Para acogerse al beneficio, mi empresa puede abrir 100 nuevas plazas para operarios, siempre y cuando quienes se postulen hayan nacido en municipios de tierra fría, porque los habitantes de tierra caliente no trabajan igual. Estoy seguro de que la junta directiva de la empresa estará de acuerdo conmigo\"."
                ),
                Question(
                    id = 1015,
                    text = "Según la información presentada, la afirmación del funcionario podría tener como consecuencia que:",
                    options = listOf(
                        QuestionOption(id = 1, text = "se cuestione la idoneidad de los investigadores que realizaron el estudio.", originalOrder = 0),
                        QuestionOption(id = 2, text = "se entienda la necesidad de contar con estrategias efectivas para prevenir la violencia contra las mujeres.", originalOrder = 1),
                        QuestionOption(id = 3, text = "se reconozca la gravedad de los actos de violencia en contra de las mujeres.", originalOrder = 2),
                        QuestionOption(id = 4, text = "se minimice la responsabilidad de los victimarios en los casos de violencia en contra de las mujeres.", originalOrder = 3)
                    ),
                    correctOptionId = 4,
                    explanation = "Al buscar \"justificaciones\" para la violencia, el funcionario minimiza la responsabilidad de los agresores.",
                    readingText = "El Fondo Monetario Internacional (FMI) enfatizó que en América Latina se evidencian altas tasas de participación laboral y resaltó la necesidad de una mayor innovación tecnológica como principal reto para el desarrollo económico.\n\nEn respuesta, una analista económica sostuvo que \"el verdadero problema en esta región es que el 50% de las mujeres en edad de trabajar —en promedio más educadas que los hombres— se dedican a la economía del cuidado, es decir, a la atención del hogar, sin ningún tipo de salario o reconocimiento económico ni social\"."
                ),
                Question(
                    id = 1016,
                    text = "¿Cuál de los siguientes es el argumento que está a favor de la propuesta de legalización?",
                    options = listOf(
                        QuestionOption(id = 1, text = "Una gran parte de la violencia mundial está asociada al tráfico y consumo de drogas, por tanto, su comercialización debe tener estrictos controles.", originalOrder = 0),
                        QuestionOption(id = 2, text = "En los países productores no existe ninguna responsabilidad en el tráfico de drogas; la responsabilidad debe recaer sobre los controles de los países consumidores.", originalOrder = 1),
                        QuestionOption(id = 3, text = "Una gran parte de quienes consumen drogas son personas enfermas que necesitan que el Estado garantice ambientes sanos para que puedan superar su adicción.", originalOrder = 2),
                        QuestionOption(id = 4, text = "En los países productores, la prohibición ha generado problemas sociales más graves que aquellos efectos económicos que se querían evitar con esa medida.", originalOrder = 3)
                    ),
                    correctOptionId = 4,
                    explanation = "Este argumento sostiene que la prohibición genera más problemas sociales que los que pretende evitar.",
                    readingText = "El problema del tráfico de drogas ilícitas ha generado que gobernantes, académicos y miembros de la sociedad civil de países productores debatan sobre el asunto con el fin de encontrar soluciones.\n\nAlgunos proponen que, para defender el bien común, se deben legalizar las drogas."
                ),
                Question(
                    id = 1017,
                    text = "Al tomar esta decisión final, ¿qué dimensión está privilegiando la paciente?",
                    options = listOf(
                        QuestionOption(id = 1, text = "sus creencias religiosas y el estilo de vida que ellas exigen.", originalOrder = 0),
                        QuestionOption(id = 2, text = "las consecuencias en su salud y estado físico en un futuro tras la cirugía.", originalOrder = 1),
                        QuestionOption(id = 3, text = "las recomendaciones médicas y evidencia científica que respalda las transfusiones.", originalOrder = 2),
                        QuestionOption(id = 4, text = "la decisión familiar como base para el consentimiento médico.", originalOrder = 3)
                    ),
                    correctOptionId = 1,
                    explanation = "La paciente privilegia sus creencias religiosas sobre las recomendaciones médicas y su salud.",
                    readingText = "Una paciente debe someterse a cirugía. El médico tratante conoce las prohibiciones que, por su religión, tiene la paciente en cuanto a las transfusiones de sangre y le advierte que podría haber hemorragia durante la intervención quirúrgica.\n\nEl médico diligencia los formularios de rutina y la paciente firma el \"consentimiento informado\" para recibir transfusiones si las llegara a necesitar.\n\nDías después de la cirugía presenta una fuerte hemorragia, por lo que se hacen necesarias las transfusiones.\n\nLos padres de la paciente le dicen a los médicos que para ellos es más importante la salud de su hija que la religión.\n\nSin embargo, la paciente le dice al cirujano que cambió de opinión y que no autoriza la transfusión."
                ),
                Question(
                    id = 1018,
                    text = "En relación con lo anterior, se podría afirmar que el argumento de la analista económica es válido al:",
                    options = listOf(
                        QuestionOption(id = 1, text = "respaldar el concepto del FMI, pues muestra que se subutiliza el potencial que las mujeres tienen para hacer de la región un área más tecnificada.", originalOrder = 0),
                        QuestionOption(id = 2, text = "refutar el análisis del FMI, pues demuestra que la desigualdad de género es el principal obstáculo para el desarrollo económico y no la falta de tecnología.", originalOrder = 1),
                        QuestionOption(id = 3, text = "rechazar el diagnóstico del FMI, pues demuestra que para América Latina la innovación tecnológica no tiene ningún efecto en el desarrollo económico.", originalOrder = 2),
                        QuestionOption(id = 4, text = "apoyar la explicación del FMI, pues ambos muestran que la población femenina ha aumentado su participación en el mercado laboral.", originalOrder = 3)
                    ),
                    correctOptionId = 2,
                    explanation = "La analista refuta el análisis del FMI al mostrar que la desigualdad de género es un obstáculo más fundamental que la falta de tecnología.",
                    readingText = "El Fondo Monetario Internacional (FMI) enfatizó que en América Latina se evidencian altas tasas de participación laboral y resaltó la necesidad de una mayor innovación tecnológica como principal reto para el desarrollo económico.\n\nEn respuesta, una analista económica sostuvo que \"el verdadero problema en esta región es que el 50% de las mujeres en edad de trabajar —en promedio más educadas que los hombres— se dedican a la economía del cuidado, es decir, a la atención del hogar, sin ningún tipo de salario o reconocimiento económico ni social\"."
                )
            )
            else -> emptyList()
        }
    }
}