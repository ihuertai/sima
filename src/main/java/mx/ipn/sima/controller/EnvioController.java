package mx.ipn.sima.controller;

import mx.ipn.sima.model.Anuncio;
import mx.ipn.sima.model.Cliente;
import mx.ipn.sima.service.AlmacenService;
import mx.ipn.sima.service.WhatsappConversationContextService;
import mx.ipn.sima.service.WhatsappService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/envio")
public class EnvioController {

    private final AlmacenService almacenService;
    private final WhatsappService whatsappService;
    private final WhatsappConversationContextService conversationContextService;

    public EnvioController(
            AlmacenService almacenService,
            WhatsappService whatsappService,
            WhatsappConversationContextService conversationContextService
    ) {
        this.almacenService = almacenService;
        this.whatsappService = whatsappService;
        this.conversationContextService = conversationContextService;
    }

    @GetMapping
    public String mostrarPantalla(Model model) {
        model.addAttribute("clientes", almacenService.getClientes());
        model.addAttribute("anuncios", almacenService.getAnuncios());
        return "envio-form";
    }

    @PostMapping("/enviar")
    public String enviarMensaje(@RequestParam int clienteIndex,
                                @RequestParam int anuncioIndex,
                                Model model) {

        Cliente cliente = almacenService.getClientes().get(clienteIndex);
        Anuncio anuncio = almacenService.getAnuncios().get(anuncioIndex);

        boolean enviado = whatsappService.sendTemplateWithImage(
                cliente.getTelefono(),
                anuncio.getImagen(),
                List.of(anuncio.getTexto())
        );

        if (enviado) {
            // Se guarda contexto para resolver el producto cuando el boton no trae payload.
            conversationContextService.registerLastSentProduct(cliente.getTelefono(), anuncio.getTexto());
        }

        String resultado = enviado ? "Mensaje 1 enviado correctamente\n " : "Error al enviar mensaje 1\n ";
        
        model.addAttribute("resultado", resultado);
        return "envio-resultado";
    }
}
