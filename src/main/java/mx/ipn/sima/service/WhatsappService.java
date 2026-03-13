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

import java.util.Arrays;
import java.util.HashMap;
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
     * Envía un mensaje de texto libre (útil para reenvíos a coordinadores).
     * Nota: Solo funciona si hay una ventana de 24h abierta o el número es de prueba configurado.
     * @param to Número de destino (ej. 5215512345678)
     * @param text Cuerpo del mensaje
     */
    public void sendMessage(String to, String text) {
        try {
            log.info("Enviando mensaje de texto libre a: {}", to);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(whatsappConfig.getAccessToken());

            // Construcción del payload manual para mensaje de texto
            Map<String, Object> body = new HashMap<>();
            body.put("messaging_product", "whatsapp");
            body.put("recipient_type", "individual");
            body.put("to", to);
            body.put("type", "text");

            Map<String, String> textContent = new HashMap<>();
            textContent.put("body", text);
            body.put("text", textContent);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    whatsappConfig.getFullUrl(),
                    entity,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Mensaje de texto enviado exitosamente a: {}", to);
            }
        } catch (HttpClientErrorException e) {
            log.error("Error de Meta al enviar texto libre: {}", e.getResponseBodyAsString());
            // Si falla por ventana de 24h, podrías intentar enviar una plantilla de notificación aquí
        } catch (Exception e) {
            log.error("Error crítico enviando mensaje de texto: {}", e.getMessage());
        }
    }

    /**
     * Envia un documento (por URL publica) al cliente.
     * Meta descarga el archivo desde la URL, por eso debe ser accesible desde internet.
     */
    public void sendDocumentMessage(String to, String documentUrl, String fileName, String caption) {
        if (documentUrl == null || documentUrl.isBlank()) {
            log.warn("No se envio documento porque la URL del PDF esta vacia.");
            return;
        }

        try {
            log.info("Enviando documento a: {}", to);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(whatsappConfig.getAccessToken());

            Map<String, Object> body = new HashMap<>();
            body.put("messaging_product", "whatsapp");
            body.put("recipient_type", "individual");
            body.put("to", to);
            body.put("type", "document");

            Map<String, Object> document = new HashMap<>();
            document.put("link", documentUrl);
            if (fileName != null && !fileName.isBlank()) {
                document.put("filename", fileName);
            }
            if (caption != null && !caption.isBlank()) {
                document.put("caption", caption);
            }
            body.put("document", document);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(
                    whatsappConfig.getFullUrl(),
                    entity,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Documento enviado exitosamente a: {}", to);
            }
        } catch (HttpClientErrorException e) {
            log.error("Error de Meta al enviar documento: {}", e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Error critico enviando documento: {}", e.getMessage());
        }
    }

    /**
     * Envía una plantilla con imagen y múltiples variables de texto.
     * @param phoneNumber Número del cliente
     * @param imageUrl    URL pública de la imagen
     * @param bodyValues  Lista de strings para {{1}}, {{2}}...
     * @return true si el envío fue exitoso
     */
    public boolean sendTemplateWithImage(String phoneNumber, String imageUrl, List<String> bodyValues) {
        final String templateName = "sima_respuesta";
        final String languageCode = "es_MX";
        try {
            WhatsappRequest request = new WhatsappRequest(
                    phoneNumber,
                    templateName,
                    languageCode,
                    imageUrl,
                    bodyValues
            );

            log.info("Payload WhatsApp (Plantilla): to={}, template={}", phoneNumber, templateName);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(whatsappConfig.getAccessToken());

            HttpEntity<WhatsappRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    whatsappConfig.getFullUrl(),
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Plantilla enviada exitosamente a: {}", phoneNumber);
                return true;
            } else {
                log.warn("Respuesta no exitosa de Meta: {}", response.getBody());
                return false;
            }

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
                log.warn("No hay whatsapp.api.waba-id configurado.");
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
                log.error("Error consultando plantillas del WABA {}: {}", wabaId, ex.getResponseBodyAsString());
                return;
            }

            Map<String, Object> templatesNode = templatesJson != null ? jsonParser.parseMap(templatesJson) : Map.of();
            log.info("Diagnóstico plantillas en WABA {}: {}", wabaId, templatesNode);

            boolean exactMatch = false;
            Object dataObj = templatesNode.get("data");
            if (dataObj instanceof List<?> dataList) {
                for (Object itemObj : dataList) {
                    if (itemObj instanceof Map<?, ?> item) {
                        if (templateName.equals(item.get("name")) && languageCode.equals(item.get("language"))) {
                            exactMatch = true;
                            break;
                        }
                    }
                }
            }

            if (!exactMatch) {
                log.error("No existe coincidencia exacta para template='{}' y language='{}'", templateName, languageCode);
            } else {
                log.info("Sí existe coincidencia exacta de template e idioma en el WABA.");
            }

        } catch (Exception diagEx) {
            log.warn("No se pudo completar diagnóstico de plantilla: {}", diagEx.getMessage());
        }
    }
}