package mx.ipn.sima.controller;

import jakarta.servlet.http.HttpSession;
import mx.ipn.sima.model.Cliente;
import mx.ipn.sima.model.Empleado;
import mx.ipn.sima.model.Sucursal;
import mx.ipn.sima.model.TamanoEmpresa;
import mx.ipn.sima.service.AlmacenService;
import mx.ipn.sima.service.RoleAccessService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/clientes")
public class ClienteController {

    private final AlmacenService almacenService;
    private final RoleAccessService roleAccessService;

    public ClienteController(AlmacenService almacenService, RoleAccessService roleAccessService) {
        this.almacenService = almacenService;
        this.roleAccessService = roleAccessService;
    }

    @GetMapping("/nuevo")
    public String mostrarFormulario(Model model, HttpSession session) {
        roleAccessService.requireClientManagement(session);
        Cliente cliente = new Cliente();
        cliente.setSucursal(new Sucursal());
        cliente.setJefeSucursal(new Empleado());
        model.addAttribute("cliente", cliente);
        model.addAttribute("sucursales", almacenService.getSucursales());
        model.addAttribute("jefesSucursal", almacenService.getJefesSucursal());
        model.addAttribute("tamanos", TamanoEmpresa.values());
        return "cliente-form";
    }

    @GetMapping("/lista")
    public String listarClientes(Model model) {
        model.addAttribute("clientes", almacenService.getClientes());
        return "cliente-lista";
    }

    @PostMapping("/guardar")
    public String guardarCliente(@ModelAttribute Cliente cliente, HttpSession session) {
        roleAccessService.requireClientManagement(session);
        almacenService.guardarCliente(cliente);
        return "redirect:/clientes/lista";
    }
}