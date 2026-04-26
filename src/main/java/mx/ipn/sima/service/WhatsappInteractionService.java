package mx.ipn.sima.service;

import mx.ipn.sima.dto.AuthenticatedUser;
import mx.ipn.sima.dto.WhatsappResponse;
import mx.ipn.sima.model.*;
import mx.ipn.sima.repository.InteraccionClienteRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class WhatsappInteractionService {

    private static final String ACTION_MAS_INFO = "MAS_INFO";
    private static final String ACTION_HABLAR_ASESOR = "HABLAR_ASESOR";

    private final WhatsappService whatsappService;
    private final WhatsappConversationContextService contextService;
    private final InteraccionClienteRepository interaccionClienteRepository;
    private final AlmacenService almacenService;
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
            InteraccionClienteRepository interaccionClienteRepository,
            AlmacenService almacenService,
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
        this.interaccionClienteRepository = interaccionClienteRepository;
        this.almacenService = almacenService;
        this.defaultCoordinatorPhone = defaultCoordinatorPhone;
        this.defaultAdvisorName = defaultAdvisorName;
        this.defaultAdvisorPhone = defaultAdvisorPhone;
        this.defaultMasInfoText = defaultMasInfoText;
        this.masInfoPdfUrl = masInfoPdfUrl;
        this.masInfoPdfFilename = masInfoPdfFilename;
        this.masInfoPdfCaption = masInfoPdfCaption;
        this.assignmentsByProduct = parseAssignments(productRoutingConfig);
    }

    @Transactional
    public boolean handleInteractiveReply(WhatsappResponse.Message message) {
        String action = resolveAction(message);
        if (action == null) {
            return false;
        }

        String clientPhone = message.from;
        WhatsappConversationContextService.ConversationContextData contextData = contextService.getLastContext(clientPhone);
        Cliente cliente = contextData != null && contextData.cliente() != null
                ? contextData.cliente()
                : almacenService.findClienteByTelefono(clientPhone);
        CampanaEnvio campana = contextData != null ? contextData.campana() : null;
        Anuncio anuncio = contextData != null ? contextData.anuncio() : null;
        String productName = resolveProductName(message, contextData);
        Empleado jefeResponsable = resolveResponsibleLead(contextData, cliente, productName);

        if (ACTION_MAS_INFO.equals(action)) {
            String detailText = buildMasInfoText(productName, anuncio);
            whatsappService.sendMessage(clientPhone, detailText);

            DocumentPayload payload = resolveDocumentPayload(anuncio);
            if (payload != null) {
                whatsappService.sendDocumentMessage(clientPhone, payload.url(), payload.fileName(), payload.caption());
            }

            InteraccionCliente interaccion = buildInteraction(
                    cliente, campana, anuncio, jefeResponsable,
                    TipoInteraccionCliente.MAS_INFO, clientPhone,
                    getButtonText(message), "Contenido adicional enviado automaticamente."
            );
            interaccion.setEstadoSeguimiento(EstadoSeguimiento.NOTIFICADO);
            interaccionClienteRepository.save(interaccion);
            return true;
        }

        if (ACTION_HABLAR_ASESOR.equals(action)) {
            String advisorName = jefeResponsable != null && jefeResponsable.getNombre() != null && !jefeResponsable.getNombre().isBlank()
                    ? jefeResponsable.getNombre()
                    : resolveAdvisorName(productName);
            String advisorPhone = jefeResponsable != null && jefeResponsable.getTelefono() != null && !jefeResponsable.getTelefono().isBlank()
                    ? jefeResponsable.getTelefono()
                    : resolveAdvisorPhone(productName);

            String clientNotice = "Te contactara el asesor " + advisorName;
            whatsappService.sendMessage(clientPhone, clientNotice);

            String advisorNotice = "El cliente " + clientPhone
                    + " se intereso en la promocion y quiere que lo contacten."
                    + "\nProducto: " + productName
                    + (cliente != null && cliente.getSucursal() != null ? "\nSucursal: " + cliente.getSucursal().getNombre() : "");
            whatsappService.sendMessage(advisorPhone, advisorNotice);

            String coordinatorNotice = "*SIMA - Seguimiento*\n"
                    + "Producto: " + productName + "\n"
                    + "Cliente: " + clientPhone + "\n"
                    + "Responsable: " + advisorName;
            whatsappService.sendMessage(defaultCoordinatorPhone, coordinatorNotice);

            InteraccionCliente interaccion = buildInteraction(
                    cliente, campana, anuncio, jefeResponsable,
                    TipoInteraccionCliente.QUIERE_CONTACTO, clientPhone,
                    getButtonText(message), advisorNotice
            );
            interaccion.setEstadoSeguimiento(EstadoSeguimiento.NOTIFICADO);
            interaccionClienteRepository.save(interaccion);
            return true;
        }

        return false;
    }

    @Transactional
    public void registerFreeTextReply(String clientPhone, String text) {
        WhatsappConversationContextService.ConversationContextData contextData = contextService.getLastContext(clientPhone);
        Cliente cliente = contextData != null && contextData.cliente() != null
                ? contextData.cliente()
                : almacenService.findClienteByTelefono(clientPhone);
        CampanaEnvio campana = contextData != null ? contextData.campana() : null;
        Anuncio anuncio = contextData != null ? contextData.anuncio() : null;
        String productName = contextData != null && contextData.productoNombre() != null
                ? contextData.productoNombre()
                : "Producto sin especificar";
        Empleado jefeResponsable = resolveResponsibleLead(contextData, cliente, productName);

        InteraccionCliente interaccion = buildInteraction(
                cliente, campana, anuncio, jefeResponsable,
                TipoInteraccionCliente.MENSAJE_LIBRE, clientPhone,
                text, "Mensaje libre reenviado al coordinador."
        );
        interaccionClienteRepository.save(interaccion);
    }

    public String getDefaultCoordinatorPhone() {
        return defaultCoordinatorPhone;
    }

    public List<InteraccionCliente> getInteractionsForUser(AuthenticatedUser user) {
        if (user == null) {
            return List.of();
        }
        if (hasAnyToken(user.roles(), "GERENTE", "ADMIN")) {
            return interaccionClienteRepository.findAllByActiveTrueOrderByFechaInteraccionDesc();
        }
        Empleado empleado = almacenService.findEmpleadoByLoginContext(user.userId(), user.email());
        if (empleado == null) {
            return List.of();
        }
        return interaccionClienteRepository.findAllByActiveTrueAndJefeResponsableOrderByFechaInteraccionDesc(empleado);
    }

    private InteraccionCliente buildInteraction(Cliente cliente,
                                                CampanaEnvio campana,
                                                Anuncio anuncio,
                                                Empleado jefeResponsable,
                                                TipoInteraccionCliente tipo,
                                                String clientPhone,
                                                String incomingMessage,
                                                String internalNotification) {
        InteraccionCliente interaccion = new InteraccionCliente();
        interaccion.setCliente(cliente);
        interaccion.setCampana(campana);
        interaccion.setAnuncio(anuncio);
        interaccion.setJefeResponsable(jefeResponsable);
        interaccion.setTipo(tipo);
        interaccion.setTelefonoCliente(clientPhone);
        interaccion.setProductoNombre(anuncio != null ? anuncio.getTitulo() : null);
        interaccion.setMensajeCliente(incomingMessage);
        interaccion.setNotificacionInterna(internalNotification);
        interaccion.setFechaInteraccion(LocalDateTime.now());
        return interaccion;
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

    private String resolveProductName(WhatsappResponse.Message message, WhatsappConversationContextService.ConversationContextData contextData) {
        String selectedAction = getSelectedAction(message);
        if (selectedAction != null && selectedAction.contains(":")) {
            return extractProductName(selectedAction);
        }
        if (contextData != null && contextData.productoNombre() != null && !contextData.productoNombre().isBlank()) {
            return contextData.productoNombre();
        }
        return "Producto sin especificar";
    }

    private Empleado resolveResponsibleLead(WhatsappConversationContextService.ConversationContextData contextData,
                                            Cliente cliente,
                                            String productName) {
        if (contextData != null && contextData.jefeResponsable() != null) {
            return contextData.jefeResponsable();
        }
        Empleado clienteOwner = almacenService.findEmpleadoResponsableDeCliente(cliente);
        if (clienteOwner != null) {
            return clienteOwner;
        }

        String configuredAdvisorPhone = resolveAdvisorPhone(productName);
        return almacenService.getJefesSucursal().stream()
                .filter(empleado -> empleado.getTelefono() != null && empleado.getTelefono().replaceAll("\\D", "").equals(configuredAdvisorPhone.replaceAll("\\D", "")))
                .findFirst()
                .orElse(null);
    }

    private DocumentPayload resolveDocumentPayload(Anuncio anuncio) {
        if (anuncio == null || anuncio.getInformacionExtraValor() == null || anuncio.getInformacionExtraValor().isBlank()) {
            if (masInfoPdfUrl == null || masInfoPdfUrl.isBlank()) {
                return null;
            }
            return new DocumentPayload(masInfoPdfUrl, masInfoPdfFilename, masInfoPdfCaption);
        }

        if (anuncio.getInformacionExtraTipo() == InformacionExtraTipo.PDF) {
            return new DocumentPayload(
                    anuncio.getInformacionExtraValor(),
                    masInfoPdfFilename,
                    "Informacion detallada de " + anuncio.getTitulo()
            );
        }

        if (masInfoPdfUrl == null || masInfoPdfUrl.isBlank()) {
            return null;
        }
        return new DocumentPayload(masInfoPdfUrl, masInfoPdfFilename, masInfoPdfCaption);
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

        if (message.text != null && message.text.body != null && !message.text.body.isBlank()) {
            return message.text.body;
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

    private String buildMasInfoText(String productName, Anuncio anuncio) {
        if (anuncio != null && anuncio.getInformacionExtraTipo() == InformacionExtraTipo.TEXTO
                && anuncio.getInformacionExtraValor() != null && !anuncio.getInformacionExtraValor().isBlank()) {
            return "Informacion adicional de " + productName + ":\n" + anuncio.getInformacionExtraValor();
        }
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

    private boolean hasAnyToken(List<String> roles, String... tokens) {
        if (roles == null || roles.isEmpty()) {
            return false;
        }
        return roles.stream()
                .filter(role -> role != null && !role.isBlank())
                .map(role -> role.toUpperCase(Locale.ROOT))
                .anyMatch(role -> {
                    for (String token : tokens) {
                        if (role.contains(token)) {
                            return true;
                        }
                    }
                    return false;
                });
    }

    private record Assignment(String advisorName, String advisorPhone) {
    }

    private record DocumentPayload(String url, String fileName, String caption) {
    }
}