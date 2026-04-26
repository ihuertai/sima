package mx.ipn.sima.controller;

import mx.ipn.sima.dto.WhatsappResponse;
import mx.ipn.sima.service.WhatsappInteractionService;
import mx.ipn.sima.service.WhatsappService;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhook")
public class WhatsappWebhookController {

    private final WhatsappService whatsappService;
    private final WhatsappInteractionService interactionService;

    public WhatsappWebhookController(WhatsappService whatsappService,
                                     WhatsappInteractionService interactionService) {
        this.whatsappService = whatsappService;
        this.interactionService = interactionService;
    }

    @GetMapping
    public ResponseEntity<String> verifyWebhook(
            @RequestParam("hub.mode") String mode,
            @RequestParam("hub.verify_token") String token,
            @RequestParam("hub.challenge") String challenge) {
        if ("subscribe".equals(mode) && "SIMA_EDUCACION_UNIVERSO_7821".equals(token)) {
            return ResponseEntity.ok(challenge);
        }
        return ResponseEntity.status(403).build();
    }

    @PostMapping
    public ResponseEntity<Void> handleIncomingMessage(@RequestBody WhatsappResponse payload) {
        processAsync(payload);
        return ResponseEntity.ok().build();
    }

    @Async
    protected void processAsync(WhatsappResponse payload) {
        try {
            if (payload.entry != null && !payload.entry.isEmpty()
                    && payload.entry.get(0).changes != null && !payload.entry.get(0).changes.isEmpty()) {
                var value = payload.entry.get(0).changes.get(0).value;

                if (value.messages != null && !value.messages.isEmpty()) {
                    var message = value.messages.get(0);
                    String cliente = message.from;
                    String texto = (message.text != null && message.text.body != null && !message.text.body.isBlank())
                            ? message.text.body
                            : "(Mensaje sin texto)";

                    if (interactionService.handleInteractiveReply(message)) {
                        return;
                    }

                    interactionService.registerFreeTextReply(cliente, texto);

                    String coordinador = interactionService.getDefaultCoordinatorPhone();
                    String aviso = "*SIMA - Nuevo Mensaje*\nDe: " + cliente + "\nDice: " + texto;
                    whatsappService.sendMessage(coordinador, aviso);
                }
            }
        } catch (Exception e) {
            System.err.println("Error procesando DTO: " + e.getMessage());
        }
    }
}