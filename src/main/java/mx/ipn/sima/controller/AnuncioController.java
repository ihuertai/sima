package mx.ipn.sima.controller;

import mx.ipn.sima.model.Anuncio;
import mx.ipn.sima.service.AlmacenService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/anuncios")
public class AnuncioController {

    private final AlmacenService almacenService;

    public AnuncioController(AlmacenService almacenService) {
        this.almacenService = almacenService;
    }

    @GetMapping("/nuevo")
    public String mostrarFormulario(Model model) {
        model.addAttribute("anuncio", new Anuncio());
        return "anuncio-form";
    }

    @PostMapping("/guardar")
    public String guardarAnuncio(@ModelAttribute Anuncio anuncio) {
        almacenService.guardarAnuncio(anuncio);
        return "redirect:/anuncios/lista";
    }

    @GetMapping("/lista")
    public String listarAnuncios(Model model) {
        model.addAttribute("anuncios", almacenService.getAnuncios());
        return "anuncio-lista";
    }
}