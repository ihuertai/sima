package mx.ipn.sima.service;

import mx.ipn.sima.dto.WhatsappResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Service
public class WhatsappInteractionService {

    private static final String ACTION_MAS_INFO = "MAS_INFO";
    private static final String ACTION_HABLAR_ASESOR = "HABLAR_ASESOR";

    private final WhatsappService whatsappService;
    private final WhatsappConversationContextService contextService;
    private final String defaultCoordinatorPhone;
    private final String defaultAdvisorName;
    private final String defaultAdvisorPhone;
    private final String defaultMasInfoText;
    private final String masInfoPdfUrl;
    private final String masInfoPdfFilename;
    private final String masInfoPdfCaption;
    private final Map<String, Assignment> assignmentsByProduct;

    public WhatsappInteractionService(
            WhatsappService whatsappService,
            WhatsappConversationContextService contextService,
            @Value("${whatsapp.flow.default.coordinator:526441177362}") String defaultCoordinatorPhone,
            @Value("${whatsapp.flow.default.asesor:Asesor SIMA}") String defaultAdvisorName,
            @Value("${whatsapp.flow.default.asesor-phone:}") String defaultAdvisorPhone,
            @Value("${whatsapp.flow.mas-info-text:Gracias por tu interes. En breve te compartimos informacion detallada del producto.}") String defaultMasInfoText,
            @Value("${whatsapp.flow.mas-info-pdf-url:}") String masInfoPdfUrl,
            @Value("${whatsapp.flow.mas-info-pdf-filename:informacion.pdf}") String masInfoPdfFilename,
            @Value("${whatsapp.flow.mas-info-pdf-caption:Informacion de la promocion}") String masInfoPdfCaption,
            @Value("${whatsapp.flow.product-routing:}") String productRoutingConfig
    ) {
        this.whatsappService = whatsappService;
        this.contextService = contextService;
        this.defaultCoordinatorPhone = defaultCoordinatorPhone;
        this.defaultAdvisorName = defaultAdvisorName;
        this.defaultAdvisorPhone = defaultAdvisorPhone;
        this.defaultMasInfoText = defaultMasInfoText;
        this.masInfoPdfUrl = masInfoPdfUrl;
        this.masInfoPdfFilename = masInfoPdfFilename;
        this.masInfoPdfCaption = masInfoPdfCaption;
        this.assignmentsByProduct = parseAssignments(productRoutingConfig);
    }

    public boolean handleInteractiveReply(WhatsappResponse.Message message) {
        String action = resolveAction(message);
        if (action == null) {
            return false;
        }

        String clientPhone = message.from;
        String productName = resolveProductName(message);

        if (ACTION_MAS_INFO.equals(action)) {
            String detailText = buildMasInfoText(productName);
            whatsappService.sendMessage(clientPhone, detailText);
            whatsappService.sendDocumentMessage(clientPhone, masInfoPdfUrl, masInfoPdfFilename, masInfoPdfCaption);
            return true;
        }

        if (ACTION_HABLAR_ASESOR.equals(action)) {
            String advisorName = resolveAdvisorName(productName);
            String advisorPhone = resolveAdvisorPhone(productName);

            String clientNotice = "Te contactara el asesor " + advisorName;
            whatsappService.sendMessage(clientPhone, clientNotice);

            String advisorNotice = "El cliente " + clientPhone
                + " se intereso en la promocion y quiere que le llamen."
                + "\nProducto: " + productName;
            whatsappService.sendMessage(advisorPhone, advisorNotice);

            String coordinatorNotice = "*SIMA - Seguimiento*\n"
                    + "Producto: " + productName + "\n"
                    + "Cliente: " + clientPhone + "\n"
                + "Asesor notificado: " + advisorName;
            whatsappService.sendMessage(defaultCoordinatorPhone, coordinatorNotice);
            return true;
        }

        return false;
    }

    public String getDefaultCoordinatorPhone() {
        return defaultCoordinatorPhone;
    }

    private String resolveAction(WhatsappResponse.Message message) {
        String selectedAction = getSelectedAction(message);
        if (selectedAction != null) {
            String actionFromPayload = resolveActionFromRaw(selectedAction);
            if (actionFromPayload != null) {
                return actionFromPayload;
            }
        }

        String buttonText = getButtonText(message);
        if (buttonText == null) {
            return null;
        }

        return resolveActionFromRaw(buttonText);
    }

    private String resolveActionFromRaw(String rawAction) {
        if (rawAction == null || rawAction.isBlank()) {
            return null;
        }

        String normalizedRaw = normalizeText(rawAction);
        if (normalizedRaw.contains(":")) {
            String action = normalizeAction(rawAction);
            if (ACTION_MAS_INFO.equals(action) || ACTION_HABLAR_ASESOR.equals(action)) {
                return action;
            }
        }

        if (normalizedRaw.contains("mas informacion") || normalizedRaw.contains("recibir mas informacion")) {
            return ACTION_MAS_INFO;
        }
        if (normalizedRaw.contains("llamada de un asesor") || normalizedRaw.contains("hablar con un asesor")) {
            return ACTION_HABLAR_ASESOR;
        }
        return null;
    }

    private String resolveProductName(WhatsappResponse.Message message) {
        String selectedAction = getSelectedAction(message);
        if (selectedAction != null && selectedAction.contains(":")) {
            return extractProductName(selectedAction);
        }

        String productFromContext = contextService.getLastProduct(message.from);
        if (productFromContext != null && !productFromContext.isBlank()) {
            return productFromContext;
        }

        return "Producto sin especificar";
    }

    private String getSelectedAction(WhatsappResponse.Message message) {
        if (message == null) {
            return null;
        }

        if (message.interactive != null
            && message.interactive.buttonReply != null
            && message.interactive.buttonReply.id != null
            && !message.interactive.buttonReply.id.isBlank()) {
            return message.interactive.buttonReply.id;
        }

        if (message.button != null && message.button.payload != null && !message.button.payload.isBlank()) {
            return message.button.payload;
        }

        return null;
    }

    private String getButtonText(WhatsappResponse.Message message) {
        if (message == null) {
            return null;
        }

        if (message.interactive != null
                && message.interactive.buttonReply != null
                && message.interactive.buttonReply.title != null
                && !message.interactive.buttonReply.title.isBlank()) {
            return message.interactive.buttonReply.title;
        }

        if (message.button != null && message.button.text != null && !message.button.text.isBlank()) {
            return message.button.text;
        }

        return null;
    }

    private String normalizeAction(String rawAction) {
        int separatorIndex = rawAction.indexOf(':');
        if (separatorIndex > -1) {
            return rawAction.substring(0, separatorIndex).trim().toUpperCase();
        }
        return rawAction.trim().toUpperCase();
    }

    private String normalizeText(String text) {
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return normalized.trim().toLowerCase(Locale.ROOT);
    }

    private String extractProductName(String rawAction) {
        int separatorIndex = rawAction.indexOf(':');
        if (separatorIndex > -1 && separatorIndex + 1 < rawAction.length()) {
            return rawAction.substring(separatorIndex + 1).trim();
        }
        return "Producto sin especificar";
    }

    private String buildMasInfoText(String productName) {
        return "Informacion adicional de " + productName + ":\n" + defaultMasInfoText;
    }

    private String resolveAdvisorName(String productName) {
        Assignment assignment = getAssignmentForProduct(productName);
        return assignment != null ? assignment.advisorName() : defaultAdvisorName;
    }

    private String resolveAdvisorPhone(String productName) {
        Assignment assignment = getAssignmentForProduct(productName);
        String phone = assignment != null ? assignment.advisorPhone() : defaultAdvisorPhone;
        if (phone == null || phone.isBlank()) {
            return defaultCoordinatorPhone;
        }
        return phone;
    }

    private Assignment getAssignmentForProduct(String productName) {
        if (productName == null) {
            return null;
        }
        String key = productName.trim().toLowerCase(Locale.ROOT);
        return assignmentsByProduct.get(key);
    }

    private Map<String, Assignment> parseAssignments(String config) {
        Map<String, Assignment> result = new HashMap<>();
        if (config == null || config.isBlank()) {
            return result;
        }

        // Formato: Producto A|Asesor Ana|5215511111111;Producto B|Asesor Luis|5215522222222
        String[] entries = config.split(";");
        for (String entry : entries) {
            String[] parts = entry.split("\\|");
            if (parts.length != 3) {
                continue;
            }

            String productKey = parts[0].trim().toLowerCase(Locale.ROOT);
            String advisor = parts[1].trim();
            String advisorPhone = parts[2].trim();
            if (!productKey.isBlank() && !advisor.isBlank() && !advisorPhone.isBlank()) {
                result.put(productKey, new Assignment(advisor, advisorPhone));
            }
        }
        return result;
    }

    private record Assignment(String advisorName, String advisorPhone) {
    }
}
