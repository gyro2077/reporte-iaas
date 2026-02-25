package com.iaas.reportes.controller;

import com.iaas.reportes.dto.ReporteRequest;
import net.sf.jasperreports.engine.*;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/generador")
public class SaludoController {

    // Endpoint GET para healthcheck en Render (evitar que se duerma o saber si está
    // vivo)
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("API Generadora de Posts JasperReports está VIVA y lista.");
    }

    @CrossOrigin(origins = "*") // Para permitir peticiones desde n8n u otros orígenes
    @PostMapping(value = "/social-post", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> generarPost(@RequestBody ReporteRequest request) {
        try {
            // 1. Cargar plantilla .jrxml y compilarla
            InputStream template = getClass().getResourceAsStream("/reports/saludo_iaas.jrxml");
            if (template == null) {
                System.err.println("No se encontró la plantilla saludo_iaas.jrxml");
                return ResponseEntity.internalServerError().build();
            }
            JasperReport jasperReport = JasperCompileManager.compileReport(template);

            // 2. Parámetros
            Map<String, Object> params = new HashMap<>();

            // Lógica para extraer el nombre a mostrar
            String nombreMostrar = "";
            if (request.apodo() != null && !request.apodo().trim().isEmpty()) {
                nombreMostrar = request.apodo().trim().toUpperCase();
            } else if (request.nombreCompleto() != null && !request.nombreCompleto().trim().isEmpty()) {
                // Selecciona la primera palabra (primer nombre)
                nombreMostrar = request.nombreCompleto().trim().split("\\s+")[0].toUpperCase();
            }

            params.put("NOMBRE_USUARIO", nombreMostrar);

            // Inyectar el fondo usando InputStream para evitar problemas de rutas en
            // runtime
            InputStream bgImg = getClass().getResourceAsStream("/reports/background.jpg");
            if (bgImg != null) {
                params.put("BACKGROUND_IMG", bgImg);
            }

            String driveUrlCleaned = cleanDriveUrl(request.urlFotoPerfil());
            System.out.println("URL Drive limpia: " + driveUrlCleaned);
            params.put("FOTO_URL", driveUrlCleaned);

            // 3. Llenar reporte
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, new JREmptyDataSource());

            // 4. Renderizado a PNG
            // Zoom 2.5f para mejorar la calidad y densidad de píxeles para Instagram
            BufferedImage image = (BufferedImage) JasperPrintManager.printPageToImage(jasperPrint, 0, 2.5f);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);

            return ResponseEntity.ok(baos.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    private String cleanDriveUrl(String url) {
        if (url == null || url.trim().isEmpty())
            return "";

        // Manejar links que tienen múltiples IDs o están separados por coma (ejemplo
        // dado en el sheet)
        if (url.contains(",")) {
            url = url.split(",")[0].trim();
        }

        if (url.contains("id=")) {
            String id = url.split("id=")[1].split("&")[0];
            return "https://drive.google.com/uc?export=view&id=" + id;
        } else if (url.contains("/d/")) {
            // Formato alternativo de Drive: https://drive.google.com/file/d/ID/view
            String id = url.split("/d/")[1].split("/")[0];
            return "https://drive.google.com/uc?export=view&id=" + id;
        }
        return url;
    }
}
