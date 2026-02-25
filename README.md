# IAAS Generador de Posts (JasperReports API)

API REST en Spring Boot 3 con Java 17 para generar imágenes (Posts Verticales o Historias de Instagram) usando JasperReports. Diseñado para consumir datos de n8n extraídos de Google Sheets.

## Requisitos
- JDK 17
- Maven (Opcional si usas el wrapper incluido)

## Ejecutar el proyecto
```bash
mvn clean spring-boot:run
```
La API estará disponible en `http://localhost:8080`.

## Endpoint de Generación
**POST** `/api/v1/generador/social-post`

**Headers:**
`Content-Type: application/json`

**Body (Ejemplo):**
```json
{
  "nombreCompleto": "Verónica Luciana Zurita Sánchez",
  "apodo": "Lu",
  "urlFotoPerfil": "https://drive.google.com/open?id=1jToQAtJsnYZEJJoUWPnBh-UA-WirMRVT"
}
```

La respuesta será un archivo binario `image/png` descargable.

## Edición en Jaspersoft Studio
1. Abre Jaspersoft Studio (versión 6.21.0 recomendada).
2. Abre el archivo `src/main/resources/reports/saludo_iaas.jrxml`.
3. Ajusta la estética (posición del texto, clip circular de la foto, etc.) con las herramientas visuales.
4. **Nota sobre imagen de fondo**: El diseño cuenta con un fondo que carga desde el classpath `reports/background.jpg` al momento de ejecutar la API.

## Despliegue en Render (Docker)
Este proyecto está 100% optimizado para ser desplegado en [Render.com](https://render.com) utilizando su infraestructura Docker, ya que JasperReports necesita empaquetar librerías gráficas (fuentes) a nivel de sistema operativo para generar los PDFs/Imágenes correctamente.

### Pasos
1. Sube este repositorio a **GitHub**.
2. En tu dashboard de Render, crea un nuevo **Web Service**.
3. Conecta tu repositorio de GitHub.
4. En **Environment**, elige `Docker` (Render lo detectará automáticamente por el `Dockerfile`).
5. Haz clic en **Create Web Service**.

> ✨ El `Dockerfile` incluido ya cuenta con un proceso de compilación Multi-stage (construye el `.jar` en una máquina y luego lo pasa a un entorno superligero Alpine de Java 17). Además, incluye `fontconfig` y `ttf-dejavu` que previenen el popular error de JasperReports *"java.lang.NullPointerException en sun.awt.FontConfiguration"*.
