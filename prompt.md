Crea una app nativa de Android en Kotlin que monitorea el tipo de cambio USD→MXN,
dispara notificaciones locales cuando cruza umbrales definidos por el usuario, y
muestra una gráfica histórica del tipo de cambio similar a la de Google.

## Stack y configuración
- Kotlin + Jetpack Compose (Material 3), un solo módulo.
- minSdk 26, targetSdk la más reciente estable, Gradle con Kotlin DSL (build.gradle.kts).
- Arquitectura MVVM con patrón Repository. Inyección de dependencias con Hilt
  (configúralo completo). Organiza el código en capas: data / domain / ui.
- Networking: Retrofit + OkHttp + kotlinx.serialization, todo con coroutines/Flow.

## Fuente de datos (IMPORTANTE: hazla intercambiable)
- Define una interfaz `ExchangeRateRepository` con:
    - `suspend fun getLatestUsdMxn(): Result<RateSnapshot>`
    - `suspend fun getUsdMxnSeries(from: LocalDate, to: LocalDate): Result<List<RatePoint>>`
- Implementación por defecto: Frankfurter (sin API key).
    - Valor actual:  https://api.frankfurter.dev/v1/latest?base=USD&symbols=MXN
      Respuesta: {"base":"USD","date":"2026-06-24","rates":{"MXN":17.6197}}
    - Serie histórica (time-series):
      https://api.frankfurter.dev/v1/{from}..{to}?base=USD&symbols=MXN
      Respuesta: {"base":"USD","start_date":"...","end_date":"...",
                  "rates":{"2026-05-26":{"MXN":17.41}, ...}}
    - Nota: Frankfurter da datos DIARIOS (cierre BCE), una actualización al día.
- Deja la URL base y el proveedor en un único archivo de configuración para poder
  cambiar fácil a otro proveedor (ExchangeRate-API, API del Banxico con token, etc.)
  sin tocar el resto del código.
- `RateSnapshot` = valor + timestamp de la consulta. `RatePoint` = fecha + valor.

## Pantalla de gráfica (estilo Google)
- Gráfica de LÍNEA/ÁREA (no histograma) del tipo de cambio en el tiempo.
- Selector de periodo: 1S, 1M, 1A, Máx. Cada botón calcula el rango de fechas y
  pide la serie al endpoint time-series de Frankfurter. Para estas vistas históricas
  NO se almacena nada localmente: se consume directo de la API.
- Vista adicional "Hoy" (intradía): se construye con los snapshots guardados en Room
  (ver persistencia), porque la API no da datos por hora.
- Usa la librería Vico para Compose para dibujar la gráfica. Si Vico complica algo,
  como alternativa dibuja un sparkline con Canvas de Compose. NO uses MPAndroidChart.
- Muestra eje de fechas, valor mínimo/máximo del periodo y el último valor resaltado.
- Maneja estados de carga, error de red y "sin datos".

## Alertas y notificaciones (sin backend, todo en el dispositivo)
- El usuario define umbrales: "avísame si SUBE por encima de X" y/o "si BAJA por debajo de Y".
- Usa WorkManager con un PeriodicWorkRequest para consultar `getLatestUsdMxn` en
  segundo plano (intervalo configurable; mínimo 15 min, que es el mínimo de WorkManager).
- Cuando el valor cruza un umbral, dispara una notificación LOCAL con NotificationManager
  y un NotificationChannel propio. Al tocarla, que abra la app.
- Anti-spam: NO notifiques en cada ciclo mientras siga cruzado. Notifica solo en el
  evento de cruce (compara contra el último estado guardado) y reactiva cuando vuelva a cruzar.
- Maneja el permiso runtime POST_NOTIFICATIONS (Android 13+) y el permiso INTERNET.

## Persistencia
- DataStore Preferences: umbrales, intervalo de consulta, preferencias de UI y el
  último valor conocido (para detectar cruces).
- Room (sobre SQLite): tabla de snapshots `RateSnapshotEntity` (valor + timestamp).
  Cada ejecución del worker inserta un snapshot. Esto habilita la gráfica intradía
  y que la app funcione sin conexión. Incluye un DAO con consultas por rango de fechas
  y una política simple de limpieza (p. ej. conservar solo los últimos N días).

## UI (Compose)
- Pantalla principal: valor USD→MXN actual, "última actualización", botón refrescar
  manual, y la gráfica con su selector de periodo.
- Configuración: definir umbral superior e inferior, activar/desactivar cada alerta,
  elegir intervalo de consulta, botón para probar la notificación, y opción para
  limpiar el historial guardado.
- Estados de carga, error de red y "sin conexión" bien manejados en todas las pantallas.

## Entregables
- Proyecto Gradle completo y compilable, organizado por capas (data/domain/ui).
- Comentarios claros en el código.
- README con: cómo abrir en Android Studio, cómo compilar, qué endpoints usa,
  cómo cambiar de proveedor de datos, explicación de cuándo se usa la API time-series
  (histórico diario) vs. Room (intradía/offline), y una nota de que esto usa polling +
  notificaciones locales (no push de servidor), con instrucciones de cómo migrar a
  Firebase Cloud Messaging si en el futuro quiero push real desde un backend.

## Flujo de trabajo (síguelo en este orden)
1. Primero entrégame SOLO la estructura de carpetas propuesta y el build.gradle.kts
   completo con todas las dependencias (Compose, Hilt, Retrofit, kotlinx.serialization,
   Room, WorkManager, Vico) y sus versiones. Detente ahí y espera mi confirmación.
2. Verifica explícitamente que las versiones de las dependencias sean compatibles entre
   sí y con la versión de Kotlin/AGP elegida (esto es lo que más suele romper la
   compilación en Android). Si hay riesgo de conflicto, avísamelo antes de seguir.
3. Tras mi OK, implementa capa por capa (data → domain → ui), deteniéndote en cada
   capa para que yo pueda revisar y compilar.
4. Si en algún punto te comparto un error de compilación textual, corrígelo antes de
   avanzar a la siguiente capa.
5. Pregúntame si alguna decisión de diseño necesita mi confirmación en lugar de asumir.
