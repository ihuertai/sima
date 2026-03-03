package mx.ipn.sima.controller;

import mx.ipn.sima.model.Anuncio;
import mx.ipn.sima.model.Cliente;
import mx.ipn.sima.service.AlmacenService;
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

    public EnvioController(AlmacenService almacenService, WhatsappService whatsappService) {
        this.almacenService = almacenService;
        this.whatsappService = whatsappService;
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

        model.addAttribute("resultado", enviado ? "Mensaje enviado correctamente" : "Error al enviar mensaje");
        return "envio-resultado";
    }
}
