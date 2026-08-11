# MrCanchas - Guía de Configuración y Arranque del Proyecto

Este repositorio contiene tanto los microservicios del **Backend** (`backend/`) como la aplicación móvil **Frontend** Android (`frontend/`) para el sistema de reserva de canchas deportivas **MrCanchas**.

---

## 1. Requisitos Previos

Asegúrate de tener instalados los siguientes componentes antes de iniciar:
- **Docker** y **Docker Compose** (Para orquestar el Backend).
- **Java JDK 17** (Requerido tanto para el Backend como para compilar la app móvil).
- **Android Studio** (Recomendado para editar, emular y depurar la aplicación móvil).
- Una cuenta de **AWS** con un **User Pool de Cognito** configurado.

---

## 2. Cómo Levantar el Backend (Microservicios)

El backend consta de microservicios de Spring Boot (`users` y `courts_service`) detrás de un Proxy Inverso (Nginx) y bases de datos PostgreSQL.

1. Abre una terminal en la raíz del proyecto.
2. Navega al directorio del backend:
   ```bash
   cd backend
   ```
3. Crea/Configura las variables de entorno necesarias en un archivo `.env` en esa misma carpeta, o configúralas en tu terminal. Las variables clave son:
   - `USERS_DB_USER`, `USERS_DB_PASSWORD`, `USERS_DB_NAME` (Bases de datos de usuarios).
   - `COURTS_DB_USER`, `COURTS_DB_PASSWORD`, `COURTS_DB_NAME` (Bases de datos de canchas).
   - `COGNITO_ISSUER_URI`: `https://cognito-idp.us-east-1.amazonaws.com/us-east-1_7mxhfj2lt` (Reemplaza con tu User Pool ID).
4. Levanta todos los contenedores con Docker Compose:
   ```bash
   docker-compose up --build -d
   ```
5. Esto levantará:
   - El Proxy Inverso (Nginx) expuesto públicamente en el puerto **`8888`** (ej. `http://localhost:8888`).
   - El microservicio de usuarios en `/users` (`http://localhost:8888/users`).
   - El microservicio de canchas en `/courts` (`http://localhost:8888/courts`).
   - PGAdmin expuesto en el puerto `5051` para la administración de las bases de datos.

---

## 3. Cómo Levantar la Aplicación Móvil Android (Frontend)

La aplicación móvil está construida con **Jetpack Compose** y consume las APIs del backend mediante Retrofit.

### Opción A: Desde Android Studio (Recomendado)
1. Inicia **Android Studio**.
2. Haz clic en **Open an Existing Project** y selecciona el directorio `frontend` de este repositorio.
3. Espera a que Android Studio descargue las dependencias y sincronice el proyecto con Gradle.
4. Conecta un dispositivo físico con depuración USB activada o inicia un **Dispositivo Virtual (Emulador)** desde el Device Manager.
5. Haz clic en el botón de reproducción verde **Run 'app'** en la barra de herramientas superior para instalar y ejecutar la aplicación.

### Opción B: Desde la Línea de Comandos (Command Line)
Si prefieres no usar la interfaz de Android Studio, puedes compilar e instalar la app directamente con Gradle:

1. Abre tu terminal en la raíz del proyecto y entra al directorio `frontend`:
   ```bash
   cd frontend
   ```
2. Asegúrate de dar permisos de ejecución al wrapper de Gradle (solo Unix/macOS):
   ```bash
   chmod +x gradlew
   ```
3. Compila el proyecto para verificar que no haya errores de sintaxis:
   ```bash
   ./gradlew compileDebugSources
   ```
4. Ejecuta las pruebas unitarias del frontend:
   ```bash
   ./gradlew test
   ```
5. Genera el archivo APK de depuración instalable:
   ```bash
   ./gradlew assembleDebug
   ```
   *(El archivo APK generado estará disponible en `app/build/outputs/apk/debug/app-debug.apk`)*.
6. Si tienes un emulador corriendo o un teléfono conectado por ADB, instálalo directamente ejecutando:
   ```bash
   ./gradlew installDebug
   ```

---

## 4. Pruebas y Conectividad (Emulador vs Host)

Cuando ejecutas la aplicación en un **Emulador de Android**, este corre en una red virtual aislada. Para que la aplicación pueda conectarse a los microservicios que tienes levantados en tu máquina local:
- La app móvil está configurada por defecto para comunicarse con la IP **`http://10.0.2.2:8888/`**.
- `10.0.2.2` es un alias especial del emulador de Android que redirige automáticamente las peticiones al puerto `8888` de la máquina host local (donde se ejecuta tu proxy inverso de Nginx).

---

## 5. Guía de Configuración de AWS Cognito

### A. Crear el User Pool en Cognito
Sigue estos pasos en la Consola de AWS para configurar el pool de usuarios para la app móvil:
1. Ve al servicio **Cognito** y haz clic en **Create user pool**.
2. **Configure sign-in experience**: Selecciona **Cognito user pool** y marca **Email**.
3. **Configure security requirements**: Selecciona **No MFA** para pruebas rápidas, o el de tu preferencia.
4. **Configure sign-up experience**: Deja activado el autoregistro y selecciona `email` como atributo requerido.
5. **Configure message delivery**: Elige **Send email with Cognito** para utilizar la cuota gratuita diaria.
6. **Integrate your app**:
   - Da un nombre al User Pool (ej. `MrCanchasUserPool`).
   - En **Initial app client**, selecciona **Public client** (las apps móviles no deben guardar secretos).
   - Elige un nombre para el cliente (ej. `MrCanchasAndroidApp`).
   - En **Client secret**, asegúrate de marcar **Don't generate a client secret**.
   - En **Allowed callback URLs**, ingresa `mrcanchas://callback` y para pruebas de backend `http://localhost:8888/login/oauth2/code/cognito`.
   - Bajo **Advanced app client settings**: Asegúrate de activar **USER_PASSWORD_AUTH** (para permitir el login directo por usuario y contraseña desde la app móvil).
7. Revisa toda la configuración y haz clic en **Create user pool**.

### B. Configurar Grupos y Roles (Admin vs User)
El backend de MrCanchas cuenta con endpoints protegidos que requieren el rol `ADMIN`.
1. Dentro de tu User Pool, ve a la pestaña **Groups**.
2. Crea un grupo llamado `ADMIN` y otro llamado `USER`.
3. Para asignar un rol de administrador a un usuario, agrégalo al grupo `ADMIN` desde la consola. La aplicación móvil decodificará automáticamente el token JWT y detectará si pertenece a dicho grupo para habilitar o restringir vistas.

### C. Parámetros de Tu Pool Configurado
- **Dominio de Autenticación**: `https://us-east-17mxhfj2lt.auth.us-east-1.amazoncognito.com`
- **ID de Cliente (App Client ID)**: `5n067t1f01s9pn6f6a0qbpmamf`
- **Región**: `us-east-1`
- **Roles**: `USER` y `ADMIN` (Grupos en Cognito)
