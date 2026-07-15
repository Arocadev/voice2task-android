<div align="center">

# Voice2Task — Android

**App Android que convierte notas de voz en tareas estructuradas con IA**  
*Android app that converts voice notes into structured tasks using AI*

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0-purple?logo=kotlin)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-BOM_2025-green?logo=android)](https://developer.android.com/jetpack/compose)
[![Android](https://img.shields.io/badge/Android-8.0%2B-green?logo=android)](https://android.com)
[![Groq](https://img.shields.io/badge/AI-Groq-orange)](https://console.groq.com)

> Evolución del proyecto [bot-to-trello](https://github.com/ArocaDev/bot-to-trello): de un bot de Telegram a una app Android propia.

</div>

---

## ¿Qué es Voice2Task?

Voice2Task convierte notas de voz en tareas estructuradas usando inteligencia artificial. El problema que resuelve es simple: se te ocurre algo mientras estás haciendo otra cosa y si no lo apuntas en ese momento te olvidas. Pues lo dices y listo.

Puedes guardar la tarea en la propia app, mandarla a Trello, a Notion, o a los tres a la vez.

---

## ✨ Funcionalidades

### 🎙️ Captura por voz
Graba una nota de voz describiendo la tarea. La IA la transcribe con Whisper, extrae título, descripción, fecha límite y prioridad, y te la presenta para revisar antes de guardar.

### 📋 Gestión de tareas y listas
Organiza tus tareas en listas personalizadas. Filtra por estado (todas, pendientes, completadas, importantes), busca por texto y consulta el detalle completo de cada tarea.

### 📅 Calendario
Vista mensual con las tareas que tienen fecha límite marcadas como puntos. Toca cualquier día para ver las tareas de ese día.

### ⭐ Importantes
Tab dedicada a las tareas marcadas como importantes para acceso rápido.

### 🔌 Integraciones externas
Conecta Trello y Notion desde los ajustes. Al crear una tarea por voz, elige en qué destinos guardarla: Voice2Task, Trello, Notion, o todos a la vez.

### ⚙️ Ajustes
- Groq API Key para procesar los audios
- Trello: selección de tablero y lista en 3 pasos
- Notion: selección de base de datos
- Cambio de contraseña
- Guía de uso integrada
- FAQ y aviso legal

### 🌍 Bilingüe
Interfaz completa en español e inglés. Cambia automáticamente según el idioma del dispositivo.

---

## 🛠️ Stack tecnológico

| Capa | Tecnología |
|------|-----------|
| Lenguaje | Kotlin 2.0 |
| UI | Jetpack Compose + Material 3 |
| Arquitectura | MVVM + StateFlow |
| Networking | Retrofit 2 + OkHttp |
| Serialización | Gson |
| Almacenamiento local | DataStore Preferences |
| Calendario | Kizitonwose Calendar Compose |
| Audio | MediaRecorder (OGG/Opus) |
| Inyección de dependencias | Manual (no Hilt) |
| Mínimo SDK | Android 8.0 (API 26) |

---

## 📁 Estructura del proyecto

```
app/src/main/java/dev/aroca/voice2taskapp/
├── data/
│   ├── api/
│   │   ├── ApiClient.kt          # Retrofit con interceptor JWT
│   │   ├── AuthApi.kt            # Endpoints de autenticación
│   │   ├── ListasApi.kt          # Endpoints de listas
│   │   ├── TareasApi.kt          # Endpoints de tareas
│   │   ├── ExternalApiClient.kt  # Retrofit para Trello y Notion
│   │   ├── TrelloApi.kt          # API Trello (tableros, listas, tarjetas)
│   │   └── NotionApi.kt          # API Notion (bases de datos, páginas)
│   ├── model/
│   │   ├── Tarea.kt
│   │   ├── Lista.kt
│   │   └── AudioProcesamientoResponse.kt
│   └── repository/
│       ├── TokenRepository.kt    # DataStore: JWT, Groq Key, Trello, Notion
│       ├── TareasRepository.kt
│       └── ListasRepository.kt
├── ui/
│   ├── screens/
│   │   ├── HomeScreen.kt         # Listas + tabs de Importantes y Calendario
│   │   ├── ListaDetailScreen.kt  # Tareas de una lista con filtros y búsqueda
│   │   ├── TareaDetailScreen.kt  # Detalle y edición de tarea
│   │   ├── GrabarAudioScreen.kt  # Grabación, procesado y confirmación de tarea
│   │   ├── CalendarioScreen.kt   # Calendario mensual con tareas por fecha
│   │   ├── ImportantesScreen.kt  # Tab de tareas importantes
│   │   └── SettingsScreen.kt     # Ajustes, integraciones, FAQ y aviso legal
│   └── theme/
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
├── viewmodel/
│   ├── AuthViewModel.kt
│   ├── ListasViewModel.kt
│   ├── TareasViewModel.kt
│   └── GrabarAudioViewModel.kt   # Estados de grabación y procesamiento
└── MainActivity.kt
```

---

## 🚀 Instalación

### Requisitos
- Android Studio Hedgehog o superior
- Android 8.0+ en el dispositivo o emulador
- Backend Voice2Task corriendo (local o desplegado)
- API Key de Groq (gratuita en [console.groq.com](https://console.groq.com))

### Pasos

```bash
git clone https://github.com/ArocaDev/voice2task-android.git
```

1. Abre el proyecto en Android Studio
2. En `data/api/ApiClient.kt` cambia la `BASE_URL` a la URL de tu backend
3. Compila y ejecuta en tu dispositivo o emulador
4. Regístrate, ve a Ajustes → Integraciones → Groq API Key y añade tu clave

---

## 📱 Flujo principal

```
Abrir lista → Pulsar micrófono → Grabar nota de voz → Finalizar
     ↓
Transcribiendo → Entendiendo → Creando tarea
     ↓
Revisar tarea (editar título, descripción, fecha, prioridad)
     ↓
Seleccionar destinos (Voice2Task / Trello / Notion)
     ↓
Guardar → Tarea creada en todos los destinos seleccionados
```

---

## 🗺️ Roadmap

- [ ] Google Calendar como integración
- [ ] Widgets Android para creación rápida desde la pantalla de inicio
- [ ] Notificaciones push para tareas con fecha límite próxima
- [ ] Estadísticas de productividad
- [ ] Sincronización en tiempo real con WebSockets

---

## 🔗 Repositorios del proyecto

| Componente | Repositorio |
|---|---|
| App Android (este repo) | [voice2task-android](https://github.com/ArocaDev/voice2task-android) |
| Backend API REST | [voice2task](https://github.com/ArocaDev/voice2task) |
| Landing web | [voice2task-web](https://github.com/ArocaDev/voice2task-web) |

---

## 👤 Autor

**Alejandro Rodríguez Calabuig**  
[github.com/ArocaDev](https://github.com/ArocaDev) · [LinkedIn](https://linkedin.com/in/alejandro-rodriguez-calabuig-a871a1230)

---

## 📄 Licencia

Proyecto personal en desarrollo. No licenciado para uso comercial.  
*Personal project under development. Not licensed for commercial use.*
