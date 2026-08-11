# Documentación de Requisitos, Casos de Uso y Registro de Decisiones Arquitectónicas (ADR) - MrCanchas

Este documento contiene la definición formal y detallada de los requisitos funcionales y no funcionales del sistema **MrCanchas**, la tabla de casos de uso y la justificación de las decisiones de priorización y desarrollo mediante un Registro de Decisión Arquitectónica (ADR).

---

## 1. Requisitos del Sistema

### 1.1. Requisitos Funcionales (RF)

Los requisitos funcionales definen los servicios, funcionalidades y comportamientos específicos que el sistema MrCanchas debe proporcionar, organizados de acuerdo con los roles de acceso definidos: **Invitado (No Autenticado)**, **Usuario (USER)** y **Administrador (ADMIN)**.

#### Grupo A: Gestión de Autenticación y Perfiles
*   **RF-01: Autenticación con AWS Cognito (Flujo Directo)**
    *   **Descripción:** El sistema de la aplicación móvil debe permitir el inicio de sesión directo mediante el flujo `USER_PASSWORD_AUTH` utilizando el nombre de usuario/correo electrónico y contraseña registrados en AWS Cognito. El token JWT de acceso (`access_token`) devuelto debe almacenarse de manera segura para su uso posterior.
*   **RF-02: Registro de Perfil del Usuario en el Backend**
    *   **Descripción:** Al iniciar sesión por primera vez, o mediante la pantalla de perfil, el sistema móvil debe sincronizar y registrar de manera automática o manual los datos adicionales del usuario (nombre completo, correo electrónico, teléfono) en el microservicio `users`. El `cognitoId` (valor del claim `sub` del token JWT) se extraerá del token JWT en el backend para asociar el perfil de forma unívoca y segura.
*   **RF-03: Consulta y Actualización del Perfil de Usuario**
    *   **Descripción:** El usuario autenticado debe poder visualizar y modificar sus datos personales propios (nombre, teléfono) a través de la interfaz de usuario. Las peticiones a `/api/users/me` se resolverán en el backend decodificando el `sub` del JWT del remitente, impidiendo que un usuario acceda o modifique perfiles ajenos.

#### Grupo B: Flujo Público / Invitado (Unauthenticated)
*   **RF-04: Consulta Pública de Canchas**
    *   **Descripción:** Cualquier usuario, incluso sin iniciar sesión (rol Invitado/Público), debe poder listar todas las canchas disponibles en el sistema y ver la información detallada de cada una (nombre, tipo de deporte, ubicación, descripción).
*   **RF-05: Consulta Pública de Horarios y Franjas Horarias (Time Slots)**
    *   **Descripción:** Los usuarios sin autenticar deben poder visualizar la lista completa de franjas horarias y filtrar los horarios correspondientes a una cancha específica para conocer la disponibilidad de juego.

#### Grupo C: Funcionalidades del Usuario Común (USER)
*   **RF-06: Creación de Reservas**
    *   **Descripción:** Un usuario autenticado con el rol `USER` debe poder seleccionar una cancha y una franja horaria disponible en una fecha determinada para realizar una reserva. El sistema creará la reserva y la marcará como ocupada para evitar conflictos de sobre-reserva.
*   **RF-07: Consulta de Reservas Propias**
    *   **Descripción:** El usuario con rol `USER` debe poder listar el historial completo de sus reservas realizadas ("Mis Reservas") con su respectivo estado de confirmación.
*   **RF-08: Cancelación de Reservas**
    *   **Descripción:** El usuario debe poder cancelar una reserva activa realizada previamente por él. Esto liberará automáticamente la franja horaria para que esté disponible para otros usuarios.
*   **RF-09: Consulta de Detalles de Reserva**
    *   **Descripción:** El usuario debe poder consultar la información detallada de una reserva específica de su pertenencia (detalles de la cancha, horario, fecha, estado).
*   **RF-10: Visualización de Resultados de Partidos**
    *   **Descripción:** El usuario común puede ver los resultados finales y marcadores de los partidos asociados a sus reservas (si ya han sido jugados y reportados por el administrador).

#### Grupo D: Funcionalidades del Administrador (ADMIN)
*   **RF-11: Creación, Modificación y Eliminación de Canchas**
    *   **Descripción:** Los usuarios con el rol `ADMIN` en Cognito deben tener permisos exclusivos para registrar nuevas canchas, editar la información de las canchas existentes y darlas de baja del sistema.
*   **RF-12: Gestión Integral de Franjas Horarias (Time Slots)**
    *   **Descripción:** El administrador debe poder crear, actualizar y eliminar franjas horarias (por ejemplo, definir bloques de juego de 08:00 a 09:30) para las canchas.
*   **RF-13: Registro y Publicación de Resultados de Partidos**
    *   **Descripción:** El administrador debe tener acceso exclusivo para ingresar los resultados finales de un partido (nombre del Equipo A, Equipo B, puntuación de A, puntuación de B, ganador y fecha de juego) asociado a una reserva de cancha específica.
*   **RF-14: Consulta General y Gestión de Usuarios**
    *   **Descripción:** El administrador debe poder listar y consultar a todos los usuarios del sistema registrados en el microservicio `users` o resolver perfiles individuales mediante su ID de base de datos o su `cognitoId`.
*   **RF-15: Consulta General de Reservas**
    *   **Descripción:** El administrador debe poder consultar cualquier reserva individual por ID en el sistema para realizar tareas de soporte o control.

#### Grupo E: Adaptabilidad y Control de la Interfaz Gráfica (UI)
*   **RF-16: Interfaz Dinámica Basada en Roles**
    *   **Descripción:** La aplicación móvil (Jetpack Compose) debe detectar de forma reactiva si el usuario actual es Invitado, `USER` o `ADMIN` inspeccionando los claims del token JWT decodificado.
    *   **Control del Administrador en la UI:** Para los administradores, la app debe ocultar los botones de "Reservar", "Mis Reservas" y "Cancelar Reserva", impidiendo así que realicen transacciones reservables, y en su lugar, habilitar la visualización del panel de reportes de marcadores.
    *   **Control de Usuario Común en la UI:** Para los usuarios comunes, la app debe ocultar todos los elementos interactivos de ingreso de marcadores/resultados para evitar peticiones fallidas y respuestas 403 (Forbidden) del backend.

---

### 1.2. Requisitos No Funcionales (RNF)

Los requisitos no funcionales definen los atributos de calidad, restricciones técnicas y estándares de rendimiento bajo los cuales debe operar el sistema de manera global.

#### Seguridad y Control de Acceso
*   **RNF-01: Control de Acceso Basado en Roles (RBAC) con JWT**
    *   **Descripción:** El backend (Spring Security) debe interceptar y validar la firma de cada petición HTTP utilizando el estándar OAuth2 Resource Server. Los roles `USER` y `ADMIN` deben ser mapeados estrictamente a partir del claim `cognito:groups` del token JWT proporcionado por AWS Cognito.
*   **RNF-02: Sensibilidad de Mayúsculas en Configuración de Cognito**
    *   **Descripción:** Las variables de entorno de emisión de tokens en los microservicios backend deben respetar exactamente el ID del pool de usuarios de Cognito respetando la distinción estricta de mayúsculas y minúsculas (ej: `us-east-1_7MxHFj2lT` con 'M' y 'H' en mayúsculas). De lo contrario, se rechazarán las firmas de tokens produciendo errores 401 Unauthorized de forma sistémica.
*   **RNF-03: Almacenamiento Seguro de Sesiones**
    *   **Descripción:** El frontend móvil debe encapsular el token JWT adquirido dentro de un gestor de sesión local (`SessionManager` usando `SharedPreferences` o almacenamiento cifrado) y adjuntarlo de forma automática como cabecera `Authorization: Bearer <token>` en todas las peticiones salientes.

#### Rendimiento y Escalabilidad
*   **RNF-04: Arquitectura Basada en Microservicios Independientes**
    *   **Descripción:** El sistema debe estructurarse en microservicios desacoplados: un microservicio de usuarios (`users`) y un microservicio de canchas y reservas (`courts_service`). Deben poder compilarse, desplegarse y escalar de manera independiente y conectarse a bases de datos PostgreSQL aisladas por servicio.
*   **RNF-05: Proxy de Entrada y Orquestación**
    *   **Descripción:** Se debe implementar un Proxy Inverso (Nginx) que unifique la exposición de los servicios del backend bajo un único puerto público (puerto `8888`), actuando como API Gateway para redirigir el tráfico (`/users` y `/courts`) hacia los contenedores correspondientes.

#### Resiliencia y Manejo de Excepciones (Robustez)
*   **RNF-06: Manejo Explicito de Excepciones de Red y Autenticación**
    *   **Descripción:** El frontend de la aplicación móvil debe capturar errores y excepciones de manera amigable mediante pantallas o diálogos interactivos que permitan reintentar la acción, cubriendo específicamente:
        *   Falta de conexión a internet (`UnknownHostException`).
        *   Caídas o inactividad del servidor backend (`ConnectException`, `SocketTimeoutException`).
        *   Entradas inválidas o credenciales vacías en el Login.
        *   Excepciones nativas de AWS Cognito (ej. `NotAuthorizedException` por clave incorrecta, `UserNotFoundException` o `UserNotConfirmedException`).
*   **RNF-07: Conectividad con el Emulador (Red Host-Loopback)**
    *   **Descripción:** El frontend de la aplicación móvil que corre en un emulador Android virtualizado debe apuntar al backend local a través de la dirección IP de loopback especial de puente de host `http://10.0.2.2:8888/` para evitar fallos de conexión por cortafuegos virtuales o IPs dinámicas locales.

#### Atributos de Calidad Visual e Identidad Corporativa (UI/UX)
*   **RNF-08: Paleta de Colores de Marca (Branding)**
    *   **Descripción:** La interfaz visual móvil desarrollada con Jetpack Compose debe utilizar estrictamente la paleta de colores oficial de la marca:
        *   **Primary (Azul Deportivo):** `#1B75BC` (para botones principales, barras de navegación y llamadas a la acción).
        *   **Secondary (Naranja Zorro):** `#EE6C24` (para acentos, estados seleccionados o destacados).
        *   **Tertiary (Amarillo Rayo):** `#F9A01B` (para advertencias o estados en progreso).
        *   **Background (Blanco Técnico):** `#F5F7FA` (fondo general de las vistas).
        *   **Surface (Blanco Puro):** `#FFFFFF` (tarjetas, contenedores interiores).
        *   **On-Surface Text (Azul Oscuro):** `#141C24` (texto principal, títulos).

---

## 2. Tabla de Casos de Uso (CU)

La siguiente tabla describe de manera detallada cada caso de uso del sistema MrCanchas, especificando los actores involucrados, precondiciones, flujo principal de eventos y las postcondiciones.

| ID | Caso de Uso | Actores | Precondiciones | Flujo Principal / Descripción | Postcondiciones | Endpoints Asociados |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **CU-01** | **Autenticar Usuario** | Usuario, Administrador | Estar registrado previamente en AWS Cognito. | 1. El actor introduce sus credenciales (email y clave).<br>2. La app móvil envía la solicitud a Cognito mediante flujo directo (`USER_PASSWORD_AUTH`).<br>3. Cognito valida y devuelve el token JWT.<br>4. La app guarda el JWT en `SessionManager`. | Se guarda la sesión activa en el teléfono móvil y se decodifica su rol. | N/A (Consumo directo de Cognito SDK/API) |
| **CU-02** | **Acceso como Invitado** | Invitado | Ninguna. | 1. El actor selecciona "Entrar como Invitado" en la pantalla de login.<br>2. La app inicializa la pantalla de inicio con privilegios de invitado (oculta funciones de reserva y resultados). | El actor navega por el catálogo de canchas sin proveer un token. | N/A (Flujo local de UI) |
| **CU-03** | **Registrar/Actualizar Perfil** | Usuario, Administrador | El actor ha iniciado sesión y cuenta con un JWT válido. | 1. Al detectar inicio de sesión, el móvil envía los datos personales decodificados del token o ingresados por el usuario.<br>2. El backend (`users`) extrae el `sub` del JWT y crea/actualiza el registro en la BD de usuarios. | Se crea o modifica el perfil de usuario de manera segura en la base de datos de usuarios. | `POST /api/users/me`<br>`PUT /api/users/me` |
| **CU-04** | **Consultar Canchas y Horarios** | Invitado, Usuario, Administrador | Ninguna. | 1. El actor accede a la pantalla de canchas.<br>2. La app solicita al backend el catálogo de canchas y franjas horarias.<br>3. El sistema responde con el listado de canchas y horarios asociados.<br>4. La app renderiza la información. | El actor visualiza el catálogo de canchas y horarios actualizados en tiempo real. | `GET /api/courts`<br>`GET /api/time-slots/court/{courtId}` |
| **CU-05** | **Reservar Cancha** | Usuario (`USER`) | El actor está autenticado con rol `USER` y tiene saldo/permisos de juego. | 1. El usuario selecciona una cancha, fecha y una franja horaria disponible.<br>2. Presiona "Reservar" en la app móvil.<br>3. El backend verifica la disponibilidad en `courts_service` y registra la reserva asignándola al `cognitoId` del usuario.<br>4. El estado de la franja horaria cambia. | La reserva queda registrada a nombre del usuario y el horario queda bloqueado. | `POST /api/reservations` |
| **CU-06** | **Visualizar Mis Reservas** | Usuario (`USER`) | El actor está autenticado con rol `USER`. | 1. El usuario presiona el botón "Mis Reservas".<br>2. El móvil realiza una petición GET al backend enviando el JWT.<br>3. El backend extrae el ID del usuario y retorna sus reservas activas y pasadas. | Se listan las reservas propias del usuario en la pantalla del móvil. | `GET /api/reservations/me` |
| **CU-07** | **Cancelar Reserva** | Usuario (`USER`) | El actor posee una reserva activa en estado pendiente/confirmada. | 1. El usuario selecciona la reserva activa en la app móvil y presiona "Cancelar".<br>2. El backend procesa la solicitud de cancelación, valida que el emisor de la petición sea el dueño de la reserva y la marca como eliminada/cancelada.<br>3. La franja horaria queda liberada. | Se anula la reserva en el sistema y el horario vuelve a estar disponible para el público. | `DELETE /api/reservations/{id}` |
| **CU-08** | **Administrar Canchas (CRUD)** | Administrador (`ADMIN`) | El actor está autenticado con rol `ADMIN`. | 1. El administrador ingresa al panel de administración.<br>2. Crea, actualiza o elimina una cancha enviando un JSON con los atributos de la cancha.<br>3. El backend valida los privilegios del rol `ADMIN` y actualiza la base de datos. | Las canchas disponibles en el sistema se actualizan correspondientemente. | `POST /api/courts`<br>`PUT /api/courts/{id}`<br>`DELETE /api/courts/{id}` |
| **CU-09** | **Administrar Horarios (CRUD)** | Administrador (`ADMIN`) | El actor está autenticado con rol `ADMIN`. | 1. El administrador accede al panel de franjas horarias.<br>2. Modifica, crea o elimina bloques de tiempo para canchas específicas.<br>3. El backend valida privilegios y procesa los cambios. | Se actualizan las horas de reserva disponibles para el público. | `POST /api/time-slots`<br>`PUT /api/time-slots/{id}`<br>`DELETE /api/time-slots/{id}` |
| **CU-10** | **Registrar Resultado de Partido** | Administrador (`ADMIN`) | El actor está autenticado con rol `ADMIN`. El partido asociado a la reserva ya debió transcurrir o jugarse. | 1. El administrador selecciona una reserva completada en la app.<br>2. Llena el formulario con los nombres de los equipos y puntajes correspondientes.<br>3. Presiona "Guardar Resultado".<br>4. El backend asocia y guarda la entidad `MatchResult`. | El resultado del partido se registra en el sistema y queda disponible para su consulta. | `POST /api/match-results/reservation/{reservationId}` |
| **CU-11** | **Consultar Resultado de Partido** | Usuario, Administrador | Contar con acceso autenticado. | 1. El actor entra a los detalles de una reserva completada.<br>2. El sistema solicita el resultado asociado a esa reserva.<br>3. El backend responde con el marcador del partido (Equipos y puntaje). | Se despliega el marcador final del partido en la interfaz de la aplicación. | `GET /api/match-results/reservation/{reservationId}` |

---

## 3. Registro de Decisiones Arquitectónicas (ADR)

Este apartado detalla el contexto, alternativas y justificaciones detrás del diseño, priorización de requisitos e implementación de MrCanchas mediante el formato de Architectural Decision Record (ADR).

### ADR #1: Priorización de Requerimientos y Estrategia de Entrega (Puesta en Práctica)

*   **Estado:** Aprobado
*   **Contexto:**
    Al iniciar el diseño del sistema de gestión deportiva **MrCanchas**, nos enfrentamos a limitaciones de tiempo y recursos para desarrollar un backend y una aplicación móvil nativa robustos. Teníamos una gama muy amplia de requerimientos potenciales (sistemas de pagos integrados, perfiles sociales de jugadores, geolocalización de canchas, chats en vivo y reservas directas). Necesitábamos una estrategia de priorización que asegurara un núcleo seguro, funcional y escalable, y definir claramente cómo se vería reflejada esta priorización en la práctica del desarrollo del software.

*   **Decisión de Priorización (Modelo MoSCoW):**
    Decidimos clasificar y priorizar los requerimientos en tres categorías estratégicas para gobernar el desarrollo:
    1.  **Must Have (Obligatorio - Núcleo de Seguridad y Transaccionalidad):**
        *   Autenticación centralizada y segura (AWS Cognito con token JWT).
        *   Creación, lectura y cancelación de reservas en tiempo real de forma transaccional.
        *   Gestión de perfiles de usuario desacoplada de la autenticación mediante el microservicio `users`.
        *   Seguridad estricta basada en roles (`USER` y `ADMIN`) tanto en backend como en la interfaz gráfica del frontend.
    2.  **Should Have (Deseable de Alta Prioridad):**
        *   Manejo integral de excepciones amigables para errores de conexión y de AWS Cognito en la app móvil (resiliencia de red).
        *   Exposición unificada de servicios mediante un API Gateway/Proxy inverso (Nginx) para simplificar la integración móvil.
        *   Administración remota del sistema por parte de un rol administrador (carga de canchas, franjas horarias y resultados).
    3.  **Could Have (Podría Incluirse - Baja Prioridad):**
        *   Pagos digitales integrados (Stripe/PayPal), que en su lugar se dejaron abiertos mediante un flujo de reserva directa sin transacciones monetarias integradas.
        *   Sistemas de geolocalización complejos de recintos deportivos.

*   **Puesta en Práctica (Implementación Real):**
    Esta priorización dictó la estructura del código y los flujos de trabajo de desarrollo de la siguiente manera:
    *   **Seguridad y Aislamiento de Roles en Backend:** Se configuró Spring Security en `courts_service` para aplicar control estricto de roles. Se garantizó que los administradores no pudiesen realizar reservas (evitando contaminar la lógica transaccional de usuarios) y que los usuarios no pudiesen subir resultados de partidos (marcando un error 403 Forbidden).
    *   **Puesta en Práctica en la UI del Móvil:** Para reflejar la seguridad de forma proactiva, en Jetpack Compose se implementaron condicionales basados en el rol extraído de los claims del token. El archivo `HomeScreen.kt` y los componentes de detalles de reserva ocultan los botones de reservas para administradores y ocultan los componentes de ingreso de resultados para usuarios estándar.
    *   **Priorización de la Resiliencia de Red:** Dada la importancia crítica del login y la conectividad del emulador con el host, se priorizó un interceptor de excepciones en la app móvil. Se implementó una lógica de captura de excepciones robusta para `UnknownHostException` (sin internet), `ConnectException` (backend apagado), y excepciones explícitas del SDK de Cognito (`NotAuthorizedException`, `UserNotFoundException`), asegurando que el flujo "Must Have" de login sea infalible.

*   **Consecuencias:**
    *   *Positivas:* Se obtuvo un MVP extremadamente seguro y estable. Se evitó la sobreingeniería en features no prioritarias (como pasarelas de pago no requeridas en esta fase). El sistema tiene un comportamiento seguro por diseño, bloqueando acciones inapropiadas en el frontend antes de enviar peticiones inútiles al backend.
    *   *Negativas:* Los administradores no pueden actuar como jugadores en la misma cuenta (deben registrar cuentas con roles distintos), lo cual es correcto desde el punto de vista arquitectónico pero requiere manejar múltiples usuarios para pruebas.

---

### ADR #2: Uso de AWS Cognito (USER_PASSWORD_AUTH) para la Autenticación del Cliente Móvil

*   **Estado:** Aprobado
*   **Contexto:**
    La aplicación móvil nativa MrCanchas requiere autenticar a los usuarios de manera segura. Tradicionalmente, implementar un sistema completo de registro, inicio de sesión, recuperación de contraseña y expiración de tokens en el propio servidor requiere semanas de desarrollo y es propenso a vulnerabilidades de seguridad. Necesitábamos un proveedor de identidad (IdP) confiable y fácil de integrar.

*   **Alternativas Consideradas:**
    1.  *Autenticación propia en base de datos local:* Desarrollar tablas de credenciales con hashes de contraseñas (BCrypt) en el microservicio `users`.
        *   *Pros:* Control total de los flujos de datos.
        *   *Contras:* Responsabilidad total sobre la seguridad de las claves, sin soporte nativo para MFA ni flujos avanzados de recuperación de contraseñas.
    2.  *OAuth2 con Redes Sociales (Google / Apple / Facebook).*
        *   *Pros:* Excelente experiencia de usuario.
        *   *Contras:* Complejidad inicial de registro de apps de desarrollo en múltiples plataformas.
    3.  *AWS Cognito User Pools (Seleccionado):*
        *   *Pros:* Servicio administrado de alta seguridad que gestiona el registro, verificación de email y entrega tokens JWT compatibles con OAuth2 estándar. Permite crear grupos (`USER`, `ADMIN`) para asignar roles fácilmente de forma dinámica.
        *   *Contras:* Dependencia técnica de AWS.

*   **Justificación de la Decisión:**
    Se seleccionó AWS Cognito con el flujo directo `USER_PASSWORD_AUTH` porque proporciona un nivel de seguridad industrial sin costo de desarrollo de infraestructura de seguridad. Esto nos permite:
    *   Delegar la validación y almacenamiento de contraseñas a AWS.
    *   Utilizar tokens JWT autodescriptivos firmados por el proveedor, que pueden ser verificados criptográficamente de manera local y descentralizada por nuestros microservicios (`users` y `courts_service`) sin necesidad de realizar una consulta externa por cada petición HTTP.
    *   Sincronizar el ID único de usuario de Cognito (`sub`) directamente en nuestra base de datos relacional para asociar las reservas de canchas en el microservicio de usuarios de forma transparente.

*   **Puesta en Práctica en el Código:**
    *   En el backend, se configuró el decodificador de JWT (`JwtDecoder`) apuntando al emisor de tokens Cognito configurado de manera estricta y sensible a mayúsculas (`COGNITO_ISSUER_URI: https://cognito-idp.us-east-1.amazonaws.com/us-east-1_7MxHFj2lT`).
    *   En el frontend, el módulo de Retrofit adjunta el `access_token` en las cabeceras HTTP de forma dinámica tras recuperarlo del almacenamiento local persistente administrado por el `SessionManager`.

---

### ADR #3: Arquitectura Basada en Microservicios Independientes con Proxy Unificado (Nginx)

*   **Estado:** Aprobado
*   **Contexto:**
    El sistema deportivo MrCanchas tiene dos áreas de responsabilidad claramente identificables y con diferentes patrones de carga:
    1.  La gestión de usuarios y sus perfiles personales (que cambia con poca frecuencia).
    2.  La gestión transaccional de canchas, horarios disponibles, reservas y resultados de partidos (que recibe una carga masiva de consultas y escrituras constantes).
    La separación lógica y física del sistema en dos microservicios independientes (`users` y `courts_service`) es ideal para permitir el escalado independiente y evitar que un fallo en la lógica de canchas impida el acceso general del usuario. Sin embargo, para la aplicación móvil Android, interactuar con múltiples endpoints distribuidos en diferentes puertos de desarrollo (ej. `8686` para usuarios y `8080` para canchas) añade complejidad y expone al sistema a fallos de CORS y conectividad de red.

*   **Decisión:**
    Implementar una arquitectura de dos microservicios con bases de datos PostgreSQL independientes, orquestados mediante Docker Compose, e introducir un Proxy Inverso unificado (Nginx) que actúe como un único Gateway de entrada en el puerto local `8888`.

*   **Puesta en Práctica en la Infraestructura:**
    Se configuró un contenedor Nginx con la siguiente estructura de enrutamiento:
    *   Peticiones dirigidas a `http://localhost:8888/users/*` se reenvían internamente al microservicio `users` en el puerto `8686`.
    *   Peticiones dirigidas a `http://localhost:8888/courts/*` se reenvían internamente al microservicio `courts_service` en el puerto `8080`.
    *   Se configuró el alias especial de red para el emulador Android (`http://10.0.2.2:8888/`) como la URL base global de la aplicación móvil, simplificando radicalmente la configuración del cliente Retrofit en Kotlin.

*   **Consecuencias:**
    *   *Positivas:* El cliente móvil tiene un único punto de acceso confiable, lo que simplifica enormemente la gestión de seguridad de red de Android (incluyendo el archivo `network_security_config.xml`). Los microservicios están totalmente desacoplados a nivel de datos y código fuente, lo que facilita el mantenimiento.
    *   *Negativas:* Se introduce un punto único de fallo (el contenedor de Nginx), el cual debe monitorearse y mantenerse altamente disponible en entornos productivos.
