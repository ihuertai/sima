package mx.ipn.sima.service;

import mx.ipn.sima.config.WhatsappConfig;
import mx.ipn.sima.dto.WhatsappRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

@Service
public class WhatsappService {

    private static final Logger log = LoggerFactory.getLogger(WhatsappService.class);
    private final WhatsappConfig whatsappConfig;
    private final RestTemplate restTemplate;

    public WhatsappService(WhatsappConfig whatsappConfig) {
        this.whatsappConfig = whatsappConfig;
        this.restTemplate = new RestTemplate();
    }

    /**
     * Envía una plantilla con imagen y múltiples variables de texto.
     * * @param phoneNumber Número del cliente (ej. 5215512345678)
     * @param imageUrl    URL pública de la imagen para el Header
     * @param bodyValues  Lista de strings en orden para {{1}}, {{2}}, {{3}}...
     * @return true si el envío fue exitoso
     */
    public boolean sendTemplateWithImage(String phoneNumber, String imageUrl, List<String> bodyValues) {
        try {
            // Construimos la petición usando la lista de valores
            WhatsappRequest request = new WhatsappRequest(
                phoneNumber, 
                "promocion_sima", // Asegúrate que este nombre coincida con el de Meta
                "es_MX", 
                imageUrl, 
                bodyValues
            );

            // Configuración de encabezados con el Token Permanente
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(whatsappConfig.getAccessToken());

            HttpEntity<WhatsappRequest> entity = new HttpEntity<>(request, headers);

            // Llamada a la API de Meta
            ResponseEntity<String> response = restTemplate.exchange(
                whatsappConfig.getFullUrl(),
                HttpMethod.POST,
                entity,
                String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Mensaje enviado exitosamente a: {}", phoneNumber);
                return true;
            } else {
                log.warn("Respuesta no exitosa de Meta: {}", response.getBody());
                return false;
            }

        } catch (Exception e) {
            log.error("Error crítico enviando a WhatsApp: {}", e.getMessage());
            return false;
        }
    }
}