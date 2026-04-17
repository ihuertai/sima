package mx.ipn.sima.controller;

import mx.ipn.sima.model.Cliente;
import mx.ipn.sima.model.Empleado;
import mx.ipn.sima.model.Sucursal;
import mx.ipn.sima.model.TamanoEmpresa;
import mx.ipn.sima.service.AlmacenService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/clientes")
public class ClienteController {

    private final AlmacenService almacenService;

    public ClienteController(AlmacenService almacenService) {
        this.almacenService = almacenService;
    }

    @GetMapping("/nuevo")
    public String mostrarFormulario(Model model) {
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
    public String guardarCliente(@ModelAttribute Cliente cliente) {
        almacenService.guardarCliente(cliente);
        return "redirect:/clientes/lista";
    }
}