package mx.ipn.sima.service;

import mx.ipn.sima.config.WhatsappConfig;
import mx.ipn.sima.dto.WhatsappRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.util.UriComponentsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Map;

@Service
public class WhatsappService {

    private static final Logger log = LoggerFactory.getLogger(WhatsappService.class);
    private final WhatsappConfig whatsappConfig;
    private final RestTemplate restTemplate;
    private final JsonParser jsonParser;
    private final String phoneNumberId;
    private final String wabaId;

    public WhatsappService(
        WhatsappConfig whatsappConfig,
        @Value("${whatsapp.api.phone-number-id}") String phoneNumberId,
        @Value("${whatsapp.api.waba-id:}") String wabaId
    ) {
        this.whatsappConfig = whatsappConfig;
        this.restTemplate = new RestTemplate();
        this.jsonParser = JsonParserFactory.getJsonParser();
        this.phoneNumberId = phoneNumberId;
        this.wabaId = wabaId;
    }

    /**
     * Envía una plantilla con imagen y múltiples variables de texto.
     * * @param phoneNumber Número del cliente (ej. 5215512345678)
     * @param imageUrl    URL pública de la imagen para el Header
     * @param bodyValues  Lista de strings en orden para {{1}}, {{2}}, {{3}}...
     * @return true si el envío fue exitoso
     */
    public boolean sendTemplateWithImage(String phoneNumber, String imageUrl, List<String> bodyValues) {
        final String templateName = "sima_imagen";
        final String languageCode = "es_MX";
        try {
            // Construimos la petición usando la lista de valores
            WhatsappRequest request = new WhatsappRequest(
                phoneNumber, 
                templateName,
                languageCode,
                imageUrl, 
                bodyValues
            );

            log.info("Payload WhatsApp (resumen): to={}, template={}, language={}, bodyParams={}",
                phoneNumber,
                templateName,
                languageCode,
                bodyValues != null ? bodyValues.size() : 0
            );
            log.info("URL WhatsApp usada: {}", whatsappConfig.getFullUrl());

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

        /* } catch (Exception e) {
            log.error("Error crítico enviando a WhatsApp: {}", e.getMessage());
            return false;
        }*/

        //inicio
        } catch (HttpClientErrorException e) {
            log.error("Error de Meta: {}", e.getResponseBodyAsString());
            if (e.getResponseBodyAsString() != null && e.getResponseBodyAsString().contains("\"code\":132001")) {
                runTemplateDiagnostics(templateName, languageCode);
            }
            return false;
        } catch (Exception e) {
            log.error("Error crítico: {}", e.getMessage());
            return false;
        }

        //fin

        
    }

    private void runTemplateDiagnostics(String templateName, String languageCode) {
        try {
            log.info("Iniciando diagnóstico de plantilla. template={}, language={}", templateName, languageCode);

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(whatsappConfig.getAccessToken());
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            String phoneInfoUrl = UriComponentsBuilder
                .fromUriString("https://graph.facebook.com/v22.0/" + phoneNumberId)
                .queryParam("fields", "id,display_phone_number,verified_name,account_mode,status,platform_type")
                .toUriString();

            String phoneInfoJson = restTemplate.exchange(
                phoneInfoUrl,
                HttpMethod.GET,
                entity,
                String.class
            ).getBody();

            log.info("Diagnóstico phone-number-id {}: {}", phoneNumberId, phoneInfoJson);

            if (wabaId == null || wabaId.isBlank()) {
                log.warn("No hay whatsapp.api.waba-id configurado. Configúralo para consultar plantillas por Graph y validar idioma/nombre exactos.");
                return;
            }

            String templatesUrl = UriComponentsBuilder
                .fromUriString("https://graph.facebook.com/v22.0/" + wabaId + "/message_templates")
                .queryParam("name", templateName)
                .queryParam("fields", "name,language,status,category")
                .toUriString();

            String templatesJson;
            try {
                templatesJson = restTemplate.exchange(
                    templatesUrl,
                    HttpMethod.GET,
                    entity,
                    String.class
                ).getBody();
            } catch (HttpClientErrorException ex) {
                String apiError = ex.getResponseBodyAsString();
                log.error("Error consultando plantillas del WABA {}: {}", wabaId, apiError);
                if (apiError != null && apiError.contains("nonexisting field (message_templates)")) {
                    log.error("El ID configurado en whatsapp.api.waba-id no parece ser un WABA ID válido; suele ocurrir cuando se coloca el Business Portfolio ID.");
                }
                return;
            }

            Map<String, Object> templatesNode = templatesJson != null ? jsonParser.parseMap(templatesJson) : Map.of();

            log.info("Diagnóstico plantillas en WABA {}: {}", wabaId, templatesNode);

            boolean exactMatch = false;
            Object dataObj = templatesNode.get("data");
            if (dataObj instanceof List<?> dataList) {
                for (Object itemObj : dataList) {
                    if (!(itemObj instanceof Map<?, ?> item)) {
                        continue;
                    }
                    Object nameObj = item.get("name");
                    Object langObj = item.get("language");
                    String name = nameObj != null ? String.valueOf(nameObj) : "";
                    String lang = langObj != null ? String.valueOf(langObj) : "";
                    if (templateName.equals(name) && languageCode.equals(lang)) {
                        exactMatch = true;
                        break;
                    }
                }
            }

            if (!exactMatch) {
                log.error("No existe coincidencia exacta para template='{}' y language='{}' en el WABA del phone-number-id configurado.", templateName, languageCode);
            } else {
                log.info("Sí existe coincidencia exacta de template e idioma en el WABA.");
            }

        } catch (Exception diagEx) {
            log.warn("No se pudo completar diagnóstico de plantilla: {}", diagEx.getMessage());
        }
    }
}