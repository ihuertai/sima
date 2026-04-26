package mx.ipn.sima.controller;

import jakarta.servlet.http.HttpSession;
import mx.ipn.sima.model.*;
import mx.ipn.sima.service.AlmacenService;
import mx.ipn.sima.service.CampanaService;
import mx.ipn.sima.service.RoleAccessService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/envio")
public class EnvioController {

    private final AlmacenService almacenService;
    private final CampanaService campanaService;
    private final RoleAccessService roleAccessService;

    public EnvioController(AlmacenService almacenService,
                           CampanaService campanaService,
                           RoleAccessService roleAccessService) {
        this.almacenService = almacenService;
        this.campanaService = campanaService;
        this.roleAccessService = roleAccessService;
    }

    @GetMapping
    public String mostrarPantalla(Model model, HttpSession session) {
        roleAccessService.requireCampaignManagement(session);
        CampanaEnvio campana = new CampanaEnvio();
        campana.setAnuncio(new Anuncio());
        campana.setCreadoPor(new Empleado());
        campana.setSucursal(new Sucursal());
        model.addAttribute("campana", campana);
        model.addAttribute("anuncios", almacenService.getAnuncios());
        model.addAttribute("gerentes", almacenService.getGerentes());
        model.addAttribute("sucursales", almacenService.getSucursales());
        model.addAttribute("tamanos", TamanoEmpresa.values());
        return "envio-form";
    }

    @PostMapping("/guardar")
    public String guardarCampana(@ModelAttribute CampanaEnvio campana, HttpSession session) {
        roleAccessService.requireCampaignManagement(session);
        CampanaEnvio saved = campanaService.crearCampana(campana);
        return "redirect:/envio/resultado/" + saved.getId();
    }

    @GetMapping("/lista")
    public String listarCampanas(Model model, HttpSession session) {
        roleAccessService.requireCampaignManagement(session);
        model.addAttribute("campanas", campanaService.getCampanas());
        return "campana-lista";
    }

    @PostMapping("/ejecutar/{id}")
    public String ejecutarCampana(@PathVariable Long id, HttpSession session) {
        roleAccessService.requireCampaignManagement(session);
        campanaService.executeCampaign(id);
        return "redirect:/envio/resultado/" + id;
    }

    @GetMapping("/resultado/{id}")
    public String resultado(@PathVariable Long id, Model model, HttpSession session) {
        roleAccessService.requireCampaignManagement(session);
        CampanaEnvio campana = campanaService.getCampana(id);
        model.addAttribute("campana", campana);
        model.addAttribute("destinatarios", campanaService.getDestinatarios(id));
        model.addAttribute("resultado", buildResultMessage(campana));
        return "envio-resultado";
    }

    private String buildResultMessage(CampanaEnvio campana) {
        return switch (campana.getEstado()) {
            case PROGRAMADA -> "Campana programada correctamente.";
            case EJECUTADA -> "Campana ejecutada correctamente.";
            case EJECUTADA_CON_ERRORES -> "Campana ejecutada con incidencias. Revisa el detalle por destinatario.";
            case BORRADOR -> "Campana guardada en borrador.";
            case EN_PROCESO -> "Campana en proceso de envio.";
        };
    }
}