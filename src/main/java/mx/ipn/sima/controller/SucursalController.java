package mx.ipn.sima.controller;

import jakarta.servlet.http.HttpSession;
import mx.ipn.sima.model.Sucursal;
import mx.ipn.sima.service.RoleAccessService;
import mx.ipn.sima.service.SucursalService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/sucursales")
public class SucursalController {

    private final SucursalService sucursalService;
    private final RoleAccessService roleAccessService;

    public SucursalController(SucursalService sucursalService, RoleAccessService roleAccessService) {
        this.sucursalService = sucursalService;
        this.roleAccessService = roleAccessService;
    }

    @GetMapping
    public String mostrarPantalla(Model model, HttpSession session) {
        roleAccessService.requireCampaignManagement(session);
        model.addAttribute("sucursal", new Sucursal());
        model.addAttribute("sucursales", sucursalService.listarSucursales());
        return "sucursales";
    }

    @GetMapping("/{id}")
    public String editarSucursal(@PathVariable Long id, Model model, HttpSession session) {
        roleAccessService.requireCampaignManagement(session);
        model.addAttribute("sucursal", sucursalService.obtenerSucursal(id));
        model.addAttribute("sucursales", sucursalService.listarSucursales());
        return "sucursales";
    }

    @PostMapping("/guardar")
    public String guardarSucursal(@ModelAttribute Sucursal sucursal, HttpSession session, Model model) {
        roleAccessService.requireCampaignManagement(session);
        try {
            sucursalService.guardarSucursal(sucursal);
            return "redirect:/sucursales";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("sucursal", sucursal);
            model.addAttribute("sucursales", sucursalService.listarSucursales());
            return "sucursales";
        }
    }

    @PostMapping("/eliminar/{id}")
    public String eliminarSucursal(@PathVariable Long id, HttpSession session) {
        roleAccessService.requireCampaignManagement(session);
        sucursalService.eliminarSucursal(id);
        return "redirect:/sucursales";
    }
}