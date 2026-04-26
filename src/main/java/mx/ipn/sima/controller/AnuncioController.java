package mx.ipn.sima.controller;

import jakarta.servlet.http.HttpSession;
import mx.ipn.sima.model.Anuncio;
import mx.ipn.sima.model.Empleado;
import mx.ipn.sima.model.InformacionExtraTipo;
import mx.ipn.sima.service.AlmacenService;
import mx.ipn.sima.service.RoleAccessService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/anuncios")
public class AnuncioController {

    private final AlmacenService almacenService;
    private final RoleAccessService roleAccessService;

    public AnuncioController(AlmacenService almacenService, RoleAccessService roleAccessService) {
        this.almacenService = almacenService;
        this.roleAccessService = roleAccessService;
    }

    @GetMapping("/nuevo")
    public String mostrarFormulario(Model model, HttpSession session) {
        roleAccessService.requireAdsManagement(session);
        Anuncio anuncio = new Anuncio();
        anuncio.setCreadoPor(new Empleado());
        model.addAttribute("anuncio", anuncio);
        model.addAttribute("gerentes", almacenService.getGerentes());
        model.addAttribute("tiposExtra", InformacionExtraTipo.values());
        return "anuncio-form";
    }

    @PostMapping("/guardar")
    public String guardarAnuncio(@ModelAttribute Anuncio anuncio, HttpSession session) {
        roleAccessService.requireAdsManagement(session);
        almacenService.guardarAnuncio(anuncio);
        return "redirect:/anuncios/lista";
    }

    @GetMapping("/lista")
    public String listarAnuncios(Model model) {
        model.addAttribute("anuncios", almacenService.getAnuncios());
        return "anuncio-lista";
    }
}