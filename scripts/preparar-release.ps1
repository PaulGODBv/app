<#
    Comprueba y construye el APK que se va a repartir.

    Existe porque publicar una version son cinco pasos manuales y cuatro de
    ellos pueden salir mal EN SILENCIO. Ya pasó en la v1.1.0: la etiqueta quedó
    apuntando a un commit donde `versionCode` todavia era 1 y el proyecto ni
    siquiera sabia firmar, mientras el APK publicado venia de tres commits mas
    adelante. Nadie se entero, porque nada lo miraba.

    Esto no automatiza la publicacion: la hace el autor. Lo que hace es negarse
    a seguir cuando algo no cuadra, y al final imprimir las ordenes exactas.

    Uso, desde la raiz del repositorio:

        .\scripts\preparar-release.ps1

    Si alguna comprobacion falla, se para y dice por que.
#>

$ErrorActionPreference = 'Stop'

function Parar($mensaje) {
    Write-Host ""
    Write-Host "  DETENIDO: $mensaje" -ForegroundColor Red
    Write-Host ""
    exit 1
}

function Bien($mensaje) { Write-Host "  OK   $mensaje" -ForegroundColor Green }
function Aviso($mensaje) { Write-Host "  !    $mensaje" -ForegroundColor Yellow }

# La huella del certificado de publicacion. Es publica —va en cada copia del
# APK— y esta aqui para detectar el error que mas caro saldria: repartir algo
# firmado con la clave de depuracion, que es la misma en todas las maquinas del
# mundo y permite a cualquiera publicar una actualizacion de tu app.
$HUELLA_ESPERADA = '50120ce173b792961203e4f1840dfdf6a4c247f9c34b1a202e0287ff4817d292'

Write-Host ""
Write-Host "Preparando el APK de publicacion" -ForegroundColor Cyan
Write-Host ""

# --- 1. El arbol tiene que estar limpio ----------------------------------
# Un APK construido con cambios sin confirmar no corresponde a NINGUN commit,
# asi que nadie podria reproducirlo ni saber que lleva dentro.
$sucio = git status --porcelain
if ($sucio) {
    Write-Host $sucio
    Parar "hay cambios sin confirmar. El APK no corresponderia a ningun commit."
}
Bien "arbol limpio"

# --- 2. Y empujado -------------------------------------------------------
# La etiqueta va a apuntar a este commit; si no esta en el remoto, apuntaria a
# algo que nadie mas tiene.
git fetch --quiet origin
$rama = git rev-parse --abbrev-ref HEAD
$pendientes = (git rev-list --count "origin/$rama..HEAD")
if ($pendientes -ne '0') {
    Parar "$pendientes commit(s) sin empujar. Empuja antes de etiquetar."
}
$commit = git rev-parse --short HEAD
Bien "commit $commit, empujado en origin/$rama"

# --- 3. La version tiene que subir ---------------------------------------
$gradle = Get-Content 'app\build.gradle.kts' -Raw
if ($gradle -notmatch 'versionCode\s*=\s*(\d+)') { Parar "no se encontro versionCode en build.gradle.kts" }
$versionCode = [int]$Matches[1]
if ($gradle -notmatch 'versionName\s*=\s*"([^"]+)"') { Parar "no se encontro versionName" }
$versionName = $Matches[1]

$ultima = git tag -l 'v*' | Sort-Object -Property { [version]($_ -replace '^v') } | Select-Object -Last 1
if ($ultima) {
    # Out-String y no la salida tal cual: `git show` devuelve un ARRAY de
    # lineas, y en PowerShell `-match` sobre un array filtra elementos sin
    # rellenar $Matches, que entonces conserva lo que tuviera de antes. Costo
    # un fallo raro: $Matches[1] valia "1.1.0" al intentar leer un numero.
    $gradleViejo = (git show "${ultima}:app/build.gradle.kts") | Out-String
    if ($gradleViejo -match 'versionCode\s*=\s*(\d+)') {
        $anterior = [int]$Matches[1]
        if ($versionCode -le $anterior) {
            Parar "versionCode $versionCode no supera al de $ultima ($anterior). Android se niega a instalar encima y el estudiante solo ve 'no se pudo instalar'."
        }
        Bien "versionCode $versionCode (la anterior, $ultima, tenia $anterior)"
    }
} else {
    Aviso "no hay etiquetas previas; no se puede comparar el versionCode"
}

# --- 4. Construir --------------------------------------------------------
Write-Host ""
Write-Host "  Compilando (puede tardar unos minutos)..." -ForegroundColor Cyan
& .\gradlew.bat :app:assembleRelease --console=plain -q
if ($LASTEXITCODE -ne 0) { Parar "la compilacion fallo" }

$apk = 'app\build\outputs\apk\release\app-release.apk'
$sinFirmar = 'app\build\outputs\apk\release\app-release-unsigned.apk'
if (-not (Test-Path $apk)) {
    if (Test-Path $sinFirmar) {
        Parar "salio SIN FIRMAR. Falta RETA2_KEYSTORE en local.properties, y un APK sin firmar no se instala."
    }
    Parar "no se encontro $apk"
}
Bien "compilado: $([int]((Get-Item $apk).Length / 1MB)) MB"

# --- 5. La firma, que es lo que no se puede equivocar --------------------
$bt = Get-ChildItem "$env:LOCALAPPDATA\Android\Sdk\build-tools" -Directory |
      Sort-Object Name -Descending | Select-Object -First 1
$salida = & "$($bt.FullName)\apksigner.bat" verify --print-certs $apk 2>&1 | Out-String
if ($salida -match 'SHA-256 digest:\s*([0-9a-f]+)') { $huella = $Matches[1] } else { Parar "no se pudo leer la firma" }

if ($huella -ne $HUELLA_ESPERADA) {
    Write-Host "    esperada : $HUELLA_ESPERADA"
    Write-Host "    encontrada: $huella"
    if ($salida -match 'CN=Android Debug') {
        Parar "esta firmado con la CLAVE DE DEPURACION. Esa clave es la misma en todas las maquinas: cualquiera podria publicar una actualizacion de tu app."
    }
    Parar "la firma no es la de publicacion."
}
Bien "firmado con el certificado de publicacion"

# --- 6. Que el APK diga lo mismo que el codigo ---------------------------
$badging = & "$($bt.FullName)\aapt2.exe" dump badging $apk 2>&1 | Out-String
if ($badging -notmatch "versionCode='(\d+)'") { Parar "no se pudo leer la version del APK" }
$apkCode = [int]$Matches[1]
$badging -match "versionName='([^']+)'" | Out-Null
$apkName = $Matches[1]
if ($apkCode -ne $versionCode -or $apkName -ne $versionName) {
    Parar "el APK dice $apkName ($apkCode) y el codigo dice $versionName ($versionCode). Compilacion vieja: borra app\build y repite."
}
Bien "el APK declara $apkName ($apkCode), igual que el codigo"

# --- 7. El mapping, que solo existe ahora --------------------------------
$mapping = 'app\build\outputs\mapping\release\mapping.txt'
if (-not (Test-Path $mapping)) { Parar "falta $mapping" }
Bien "mapping.txt: $([int]((Get-Item $mapping).Length / 1MB)) MB"

# --- Las ordenes, ya con los valores puestos -----------------------------
$etiqueta = "v$versionName"
Write-Host ""
Write-Host "Todo cuadra. Para publicar:" -ForegroundColor Cyan
Write-Host ""
Write-Host "  git tag -a $etiqueta -m `"$etiqueta`""
Write-Host "  git push origin $etiqueta"
Write-Host ""
Write-Host "  Y en el Release de GitHub, adjunta LOS DOS:"
Write-Host "    $apk"
Write-Host "    $mapping"
Write-Host ""
Write-Host "  El mapping se sobrescribe en cada compilacion: el de ESTE APK solo existe ahora." -ForegroundColor Yellow
Write-Host "  Sin el, las trazas de fallo del piloto llegan ofuscadas e ilegibles." -ForegroundColor Yellow
Write-Host ""
