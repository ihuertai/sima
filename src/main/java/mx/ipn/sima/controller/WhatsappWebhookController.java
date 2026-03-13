package mx.ipn.sima.controller;

import mx.ipn.sima.dto.WhatsappResponse;
import mx.ipn.sima.service.WhatsappInteractionService;
import mx.ipn.sima.service.WhatsappService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhook")
public class WhatsappWebhookController {

    @Autowired
    private WhatsappService whatsappService;

    @Autowired
    private WhatsappInteractionService interactionService;

    // Handshake (Se mantiene igual)
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
        // Respuesta inmediata a Meta
        processAsync(payload);
        return ResponseEntity.ok().build();
    }

    

    @Async
    protected void processAsync(WhatsappResponse payload) {
        try {
            // Navegación segura con validaciones mínimas
            if (payload.entry != null && !payload.entry.isEmpty()) {
                var value = payload.entry.get(0).changes.get(0).value;
                
                if (value.messages != null && !value.messages.isEmpty()) {
                    var message = value.messages.get(0);
                    String cliente = message.from;
                    String texto = (message.text != null) ? message.text.body : "(Mensaje sin texto)";

                    System.out.println("Reenviando mensaje de: " + cliente);

                    // Si es respuesta de boton, se procesa por flujo de negocio y no se reenvia como texto normal.
                    if (interactionService.handleInteractiveReply(message)) {
                        return;
                    }

                    // Reenvío al Coordinador
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