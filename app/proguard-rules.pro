# Reglas de R8 para la compilación de release.
#
# La mayoría de las librerías del proyecto traen sus propias reglas dentro del
# artefacto y no hay que repetirlas aquí: Room, Hilt/Dagger, OkHttp, Coil,
# Compose y Retrofit. Las de Retrofit 2.9.0 conservan Signature, InnerClasses,
# EnclosingMethod, las anotaciones de @retrofit2.http y la propia interfaz del
# servicio — pero no bastan, ver el punto 3.
#
# Lo que queda son las tres cosas que ninguna librería puede saber por su cuenta.

# ---------------------------------------------------------------------------
# 1. Trazas de fallo legibles
# ---------------------------------------------------------------------------
# Sin estos atributos, un fallo en la prueba piloto llega con los nombres
# ofuscados y sin número de línea. El diccionario para traducirlas queda en
# app/build/outputs/mapping/release/mapping.txt y R8 lo sobrescribe en cada
# compilación: hay que archivarlo junto a cada APK que se reparta, o las trazas
# de ese APK ya no se podrán leer.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ---------------------------------------------------------------------------
# 2. Gson
# ---------------------------------------------------------------------------
# Gson empezó a incluir sus propias reglas en la versión 2.11.0; el proyecto usa
# la 2.10.1, así que van aquí.
#
# Los DTO se rellenan y se leen por reflexión, y R8 no ve esos accesos. Con las
# clases de respuesta el efecto es más bruto de lo que parece: el código nunca
# las construye —las construye Gson— y su tipo solo aparece en la firma
# genérica del servicio de Retrofit, así que en modo completo (el de AGP 8) R8
# las da por muertas y borra la clase entera. Medido en este proyecto:
# SyncReportResponse, RankingResponse, RankingEntryDto y CurrentUserRankingDto
# desaparecían del APK y la sincronización y el ranking se caían solo en
# release.
#
# Tiene que ser -keep y no -keepclassmembers: el segundo es condicional, solo
# conserva los campos SI la clase sobrevive, y aquí el problema es justo que no
# sobrevive. Con -keepclassmembers la regla no hacía nada.
-keep class com.universidad.reta2.data.remote.dto.** {
    <fields>;
}

# Lo mismo para cualquier campo anotado que se añada fuera de ese paquete.
# Aquí sí basta con -keepclassmembers: son clases que el código sí usa.
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}


# ---------------------------------------------------------------------------
# 3. Retrofit con funciones suspend, en modo completo de R8
# ---------------------------------------------------------------------------
# Las reglas que trae Retrofit 2.9.0 son de 2020 y se escribieron para el modo
# clásico. El modo completo, que AGP 8 activa por defecto, borra además los
# ARGUMENTOS de tipo de las firmas cuando la clase del argumento no está
# explícitamente conservada, aunque `-keepattributes Signature` esté activo.
#
# Una función `suspend` compila a un método que recibe un `Continuation` extra,
# y Retrofit saca de su argumento genérico el tipo que hay que deserializar.
# Medido en este proyecto, en el APK de release del 25/09/2026:
#
#   syncReport(SyncReportRequest, Continuation)
#     residualsignature: (L.../SyncReportRequest;Ld3/d;)Ljava/lang/Object;
#
# El `Continuation` quedó crudo, sin `<? super Response<SyncReportResponse>>`.
# Retrofit lo castea a ParameterizedType al construir el servicio y lanza
# «java.lang.Class cannot be cast to java.lang.reflect.ParameterizedType».
# Efecto en el teléfono: ninguna llamada de red llega a salir. La
# sincronización y el ranking fallan en silencio —el repositorio se traga la
# excepción en un Result.failure— y el panel no registra ni una petición.
# Solo ocurre en release; en debug no hay R8 y no se ve.
#
# Conservar las dos clases basta: se les permite renombrarse y encogerse, lo
# único que se impide es que R8 borre el argumento de tipo que las nombra.
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation
-keep,allowobfuscation,allowshrinking class retrofit2.Response
