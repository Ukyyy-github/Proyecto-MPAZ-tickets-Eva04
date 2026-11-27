# MPAZ Ticket

Aplicación móvil Android para centralizar los reportes de mantención (infraestructura, mobiliario y tecnología) de la escuela.
Reemplaza el aviso verbal por un ticket con número, estado y trazabilidad completa.

> Proyecto del ramo **Desarrollo de Aplicaciones Móviles (DSY1105)** – Evaluación Parcial 4.

## Integrantes

| Integrante | Responsabilidad principal |
|---|---|
| Mati *(completar apellido)* | Aplicación Android (Kotlin + Jetpack Compose), UI, Room |
| *(compañera)* | Microservicios Spring Boot, API externa, pruebas, APK firmado |

## Funcionalidades (app móvil)

- **Registro e inicio de sesión** con formularios validados (correo, contraseña con letras y números, confirmación).
- **Tres roles**: Funcionario, Encargado de TI y Personal auxiliar. **Todos ven todos los tickets** (pestañas *Todos / Míos / Asignados a mí*), pero cada rol solo puede actuar según sus permisos; quien no participa en un ticket lo ve en solo lectura:
  - *Funcionario*: crea tickets (título, categoría, ubicación, descripción y documentos opcionales), edita o elimina los suyos mientras estén *Pendiente* y cierra los *Solucionado*.
  - *Encargado de TI*: define prioridad, asigna el ticket a personal auxiliar o a un encargado de TI (incluido él mismo), cambia el estado y comenta.
  - *Personal auxiliar*: pasa los tickets que tiene asignados a *En proceso* o *Solucionado*.
- **Perfil de usuario**: datos de la cuenta, resumen de tickets creados/asignados/resueltos, edición del nombre y cambio de contraseña.
- **Documentos adjuntos de cualquier tipo** (PDF, Word, Excel, imágenes, etc.): hasta 10 por ticket y 10 MB cada uno, con vista previa de imágenes y apertura con otras aplicaciones.
- **Estados**: Pendiente → Revisado → Asignado → En proceso → Solucionado → Cerrado.
- **Número de ticket** (`MPZ-0001`), filtro por estado, detalle con foto.
- **Historial y comentarios**: cada cambio de estado, prioridad o asignación queda registrado con autor y fecha.
- **Almacenamiento local con Room (SQLite)** para seguir trabajando sin conexión.

## Arquitectura

MVVM por paquetes (`cl.mpaz.ticket`):

```
model/        Entidades y enums (Usuario, Ticket, Comentario, Rol, EstadoTicket...)
data/local/   Room: AppDatabase y DAOs
data/repository/  Interfaces + implementaciones locales (el microservicio se enchufa aquí)
session/      Sesión activa
util/         Validadores, reglas por rol, formato
ui/           Pantallas Compose + ViewModels (auth, tickets, perfil), componentes, navegación, tema
```

Los repositorios son interfaces (`UsuarioRepository`, `TicketRepository`): la implementación remota con Retrofit se agrega
en `AppContainer.kt` sin modificar pantallas ni ViewModels.

## Endpoints utilizados

*(completar al integrar los microservicios y la API externa)*

| Origen | Método | Endpoint | Uso |
|---|---|---|---|
| Microservicio de usuarios | | | |
| Microservicio de tickets | | | |
| API externa | | | |

## Pasos para ejecutar

1. Abrir la carpeta del proyecto en **Android Studio** y esperar el *Sync* de Gradle.
2. Ejecutar en un emulador o dispositivo con **Android 13 (API 33) o superior**.
3. Cuentas de prueba (clave `Mpaz1234`): `ti@mpaz.cl` (Encargado TI), `auxiliar@mpaz.cl` y `auxiliar2@mpaz.cl` (Auxiliares), `funcionario@mpaz.cl` (Funcionario).
   También se puede crear una cuenta nueva desde *Regístrate*.

## APK firmado y .jks

*(agregar captura del APK firmado y del archivo .jks)*
