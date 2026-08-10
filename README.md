# MrCanchas - Guía de Configuración de AWS Cognito

Esta guía describe detalladamente cómo crear y configurar un nuevo **User Pool en AWS Cognito**, configurar el flujo de autenticación, y conectar tanto la aplicación móvil Android como los microservicios del backend.

---

## 1. Crear un User Pool en AWS Cognito

Sigue estos pasos en la Consola de AWS para configurar el pool de usuarios:

1. Ve al servicio **Cognito** en la Consola de AWS y haz clic en **Create user pool**.
2. **Configure sign-in experience (Experiencia de inicio de sesión):**
   - Selecciona **Cognito user pool**.
   - Bajo **User pool sign-in options**, selecciona **Email**.
   - Haz clic en **Next**.
3. **Configure security requirements (Requisitos de seguridad):**
   - Elige la política de contraseñas de tu preferencia (ej. longitud mínima de 8 caracteres con números y símbolos).
   - En **Multi-factor authentication (MFA)**, selecciona **No MFA** para pruebas rápidas (o configúralo si lo necesitas).
   - En **User account recovery**, asegúrate de que esté habilitada la opción de autorecuperación por correo electrónico.
   - Haz clic en **Next**.
4. **Configure sign-up experience (Experiencia de registro):**
   - Deja las opciones de registro por defecto habilitadas (permitir el autoregistro de usuarios).
   - En **Required attributes**, asegúrate de que el atributo `email` esté seleccionado.
   - Haz clic en **Next**.
5. **Configure message delivery (Envío de mensajes):**
   - Selecciona **Send email with Cognito** para utilizar el límite gratuito de correos de Cognito diariamente, o asócialo con **Amazon SES** si tienes un entorno productivo.
   - Haz clic en **Next**.
6. **Integrate your app (Integrar tu aplicación):**
   - Introduce un **User pool name** (ej. `MrCanchasUserPool`).
   - Bajo **Hosted authentication pages**, marca **Use the Cognito Hosted UI** si deseas utilizar la interfaz de login provista por AWS.
   - Configura el **Domain**: elige un prefijo de dominio disponible para tu Hosted UI (ej. `mrcanchas-auth`).
   - En **Initial app client**:
     - Elige **Public client** (apropiado para aplicaciones móviles ya que no pueden guardar secretos de forma segura).
     - Asigna un **App client name** (ej. `MrCanchasAndroidApp`).
     - **Client secret**: Selecciona **Don't generate a client secret** (requisito crítico para aplicaciones Android públicas).
     - **Allowed callback URLs**: Añade la URL de callback que procesará la redirección tras el login, por ejemplo:
       - Para pruebas locales del backend: `http://localhost:8888/login/oauth2/code/cognito`
       - Para redirección profunda en la app móvil: `mrcanchas://callback`
     - **Allowed sign-out URLs**: Añade las URLs de cierre de sesión, por ejemplo: `mrcanchas://signout`
   - Expande **Advanced app client settings**:
     - Bajo **OAuth 2.0 grant types**, asegúrate de habilitar **Authorization code grant** e **Implicit grant**.
     - Bajo **OpenID Connect scopes**, selecciona **phone**, **email**, **openid** y **profile**.
   - Haz clic en **Next**.
7. Revisa toda la configuración y haz clic en **Create user pool**.

---

## 2. Configurar Grupos y Roles (Admin vs User)

El backend de MrCanchas cuenta con endpoints protegidos que requieren el rol `ADMIN`. Puedes manejar esto de la siguiente manera:

1. Dentro de tu User Pool creado, ve a la pestaña **Groups**.
2. Crea un grupo llamado `ADMIN` y otro llamado `USER`.
3. Para simular o asignar un rol administrativo a un usuario, simplemente agrégalo al grupo `ADMIN` desde la consola de Cognito. El token JWT devuelto contendrá un claim `cognito:groups` con valor `["ADMIN"]`.

---

## 3. Configuración en la Aplicación Móvil Android

En la aplicación móvil, utilizamos el token JWT (Access Token) que nos entrega Cognito para autorizar las solicitudes al API Gateway.

### A. Almacenamiento Seguro
Cuando un usuario inicia sesión en la Hosted UI de Cognito o mediante un flujo directo con Amplify/AWS SDK:
1. Obtenemos el `access_token` entregado por Cognito.
2. Guardamos este token de manera local en la app a través del `SessionManager`.
3. El `RetrofitClient` interceptará automáticamente todas las llamadas HTTPS de la app e inyectará la cabecera:
   ```http
   Authorization: Bearer <TU_ACCESS_TOKEN>
   ```

### B. Probar el flujo de Login en el Emulador
Dado que los microservicios están dockerizados en la máquina local, el emulador de Android accede al Proxy Inverso de la máquina host mediante la IP loopback especial `10.0.2.2`.
1. **Paso 1**: Obtén un `access_token` válido de tu Hosted UI de Cognito usando un navegador web o Postman.
2. **Paso 2**: Abre la app de MrCanchas en tu Emulador.
3. **Paso 3**: En la pantalla de Login, pega el `access_token` en el campo correspondiente.
4. **Paso 4**: Completa los datos de perfil (Nombre, Email, Teléfono) y selecciona si deseas simular el rol de Administrador.
5. **Paso 5**: Presiona **Registrar e Iniciar Sesión**. La app enviará estos datos al microservicio `users` utilizando el token de Cognito para dar de alta tu perfil local.

---

## 4. Configuración de Tu Nuevo Pool de Cognito (Móvil)

Has configurado exitosamente un nuevo User Pool para la aplicación móvil con los siguientes datos:
- **Dominio de Autenticación**: `https://us-east-17mxhfj2lt.auth.us-east-1.amazoncognito.com`
- **ID de Cliente (App Client ID)**: `5n067t1f01s9pn6f6a0qbpmamf`
- **Región**: `us-east-1`
- **Roles**: `USER` y `ADMIN` (Grupos en Cognito)

### Configuración en la Aplicación Móvil
La aplicación móvil está configurada para realizar la autenticación mediante **usuario y contraseña** directamente desde la interfaz móvil de la aplicación (flujo `USER_PASSWORD_AUTH`).

1. Se utiliza el App Client ID (`5n067t1f01s9pn6f6a0qbpmamf`) para comunicarse directamente con la API de Cognito: `https://cognito-idp.us-east-1.amazonaws.com`.
2. Al ingresar el usuario y contraseña, la aplicación ejecuta una solicitud segura `InitiateAuth` hacia Cognito.
3. Se extrae el `AccessToken` JWT que nos devuelve de forma segura y se almacena en SharedPreferences.
4. El token es utilizado para crear o sincronizar automáticamente el perfil de usuario contra el backend de MrCanchas.

---

## 5. Configuración en el Backend (Microservicios)

Para que los microservicios de Spring Boot (tanto `users` como `courts_service`) validen correctamente los tokens firmados por tu nuevo pool de Cognito, debes configurar la variable de entorno `COGNITO_ISSUER_URI` en tu archivo `docker-compose.yml` o en el entorno de ejecución:

```yaml
environment:
  COGNITO_ISSUER_URI: "https://cognito-idp.us-east-1.amazonaws.com/<ID_DE_TU_USER_POOL_DE_COGNITO>"
```

*(Nota: Puedes obtener el **User Pool ID** desde la consola de AWS Cognito, tiene un formato similar a `us-east-1_xxxxxxxxx`).*

Esto permite que Spring Security descargue automáticamente las claves públicas de firma de tokens (JWKS) desde la dirección de tu nuevo pool de Cognito para validar la autenticidad de los tokens enviados desde la aplicación móvil.
