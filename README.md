# ZenithSky-Android

App Android 17 (API 37) — Kotlin + Jetpack Compose + Retrofit + Room. Proyecto 2 IF0009 / IF4101.

## Estructura (`app/src/main/java/com/zenithsky/app`)

```
MainActivity.kt                 → panel principal (Luis)
data/
├── remote/                     → RetrofitClient, ApiService (Kevin)
│   └── dto/                    → data class de la API (Airport, Airline, Airplane, AirplaneType)
└── local/                      → Room (Jefferson)
    ├── entity/                 → OperacionLog, AvionDestacado
    ├── dao/                    → DAOs (suspend + Flow)
    └── ZenithSkyDatabase.kt    → @Database Singleton
ui/
├── components/                 → ZenithScaffold (Scaffold + TopAppBar común)
├── airport/                    → AirportList/FormActivity (Luis)
├── airline/                    → AirlineList/FormActivity (Jefferson)
├── airplane/                   → AirplaneList/FormActivity (Kevin)
└── theme/
```

## Antes de correr

1. Levantar `ZenithSky-API` (perfil `http`, puerto 5080).
2. Emulador con API 37. La app llama a `http://10.0.2.2:5080/`.

## Uso de la API en una Activity

```kotlin
val scope = rememberCoroutineScope()
scope.launch {
    val response = RetrofitClient.api.deleteAirport(id)
    if (response.isSuccessful) { /* ok */ }
    else if (response.code() == 409) { mensaje = response.errorMessage() }
}
```

Formularios en modo edición: `intent.putExtra(XFormActivity.EXTRA_ID, id)`.

## Ramas

`main` (entregas) ← `develop` ← `feature/airports`, `feature/airlines`, `feature/airplanes`
