# Config Server del facturador

Servidor Spring Cloud Config habilitado mediante `@EnableConfigServer`, con repositorio Git y puerto `8888` por defecto. Puede iniciarse sin Eureka, MySQL ni RabbitMQ.

## Arranque

Con JDK 25 y `JAVA_HOME` configurado, ejecutar desde esta carpeta:

```powershell
.\mvnw.cmd spring-boot:run
```

Por defecto clona el repositorio `https://github.com/alejandro-enrique-pirela-ramirez/Config-repo-facturador-electronico.git` y utiliza la rama `main`. `clone-on-start=true` permite detectar errores de acceso al repositorio al arrancar. El repositorio actual guarda `ms-factura.properties` en la raiz, por lo que no necesita `search-paths`.

Comprobar que sirve el archivo esperado:

```powershell
$config = Invoke-RestMethod http://localhost:8888/ms-factura/default/main
$config.propertySources | Select-Object name
```

Debe devolver HTTP 200 y un origen terminado en `ms-factura.properties`. Tambien funciona `/ms-factura/default`, que utiliza la rama por defecto. Si no hay propiedades para la aplicacion, el servidor devuelve HTTP 404 (`accept-empty=false`).

## Variables opcionales

| Variable | Valor por defecto |
| --- | --- |
| `CONFIG_SERVER_PORT` | `8888` |
| `CONFIG_GIT_URI` | URL del repositorio GitHub actual |
| `CONFIG_GIT_LABEL` | `main` |

Ejemplo para probar el repositorio Git local, sin publicar cambios en GitHub:

```powershell
$env:CONFIG_GIT_URI = 'file:///D:/cibertec/DAW2/trabajos/Config-repo-facturador-electronico'
.\mvnw.cmd spring-boot:run
```

Git lee los archivos confirmados en la rama seleccionada. Para volver a GitHub, eliminar esa variable de la terminal antes de reiniciar: `Remove-Item Env:CONFIG_GIT_URI`.

Para un repositorio privado mediante HTTPS, suministrar credenciales por variables de entorno, sin escribirlas en `application.properties`:

```powershell
$env:SPRING_CLOUD_CONFIG_SERVER_GIT_USERNAME = 'usuario-github'
$env:SPRING_CLOUD_CONFIG_SERVER_GIT_PASSWORD = '<token-con-lectura-del-repositorio>'
```

Factura debe tener `spring.application.name=ms-factura`, la dependencia `spring-cloud-starter-config` y `spring.config.import=configserver:${CONFIG_SERVER_URL:http://localhost:8888}`. Su `CONFIG_GIT_LABEL` debe existir en este repositorio.

## Pruebas

```powershell
.\mvnw.cmd test
```

Las pruebas crean un repositorio Git temporal con rama `main`, arrancan el Config Server y comprueban la lectura por HTTP y el HTTP 404 de una aplicacion inexistente. No dependen de GitHub.

Referencia: [Spring Cloud Config Git Backend](https://docs.spring.io/spring-cloud-config/reference/server/environment-repository/git-backend.html).
