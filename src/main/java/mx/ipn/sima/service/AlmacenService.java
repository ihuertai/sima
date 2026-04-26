package mx.ipn.sima.service;

import jakarta.annotation.PostConstruct;
import mx.ipn.sima.model.*;
import mx.ipn.sima.repository.AnuncioRepository;
import mx.ipn.sima.repository.ClienteRepository;
import mx.ipn.sima.repository.EmpleadoRepository;
import mx.ipn.sima.repository.SucursalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class AlmacenService {

    private final ClienteRepository clienteRepository;
    private final AnuncioRepository anuncioRepository;
    private final SucursalRepository sucursalRepository;
    private final EmpleadoRepository empleadoRepository;

    public AlmacenService(ClienteRepository clienteRepository,
                          AnuncioRepository anuncioRepository,
                          SucursalRepository sucursalRepository,
                          EmpleadoRepository empleadoRepository) {
        this.clienteRepository = clienteRepository;
        this.anuncioRepository = anuncioRepository;
        this.sucursalRepository = sucursalRepository;
        this.empleadoRepository = empleadoRepository;
    }

    @PostConstruct
    @Transactional
    public void initData() {
        if (sucursalRepository.count() == 0) {
            Sucursal matriz = new Sucursal();
            matriz.setClave("MAT");
            matriz.setNombre("Matriz");
            matriz.setTelefono("6440000000");
            matriz.setCorreo("matriz@sima.local");
            sucursalRepository.save(matriz);

            Empleado gerente = new Empleado();
            gerente.setNombre("Gerente General");
            gerente.setCorreo("gerencia@sima.local");
            gerente.setTelefono("6441111111");
            gerente.setPuesto("Gerente de anuncios");
            gerente.setRolOperativo(RolOperativo.GERENTE);
            gerente.setSucursal(matriz);
            empleadoRepository.save(gerente);

            Empleado jefe = new Empleado();
            jefe.setNombre("Jefe de Sucursal Matriz");
            jefe.setCorreo("jefe.matriz@sima.local");
            jefe.setTelefono("6442222222");
            jefe.setPuesto("Jefe de sucursal");
            jefe.setRolOperativo(RolOperativo.JEFE_SUCURSAL);
            jefe.setSucursal(matriz);
            empleadoRepository.save(jefe);
        }

        if (anuncioRepository.count() == 0) {
            Empleado gerente = getGerentes().stream().findFirst().orElse(null);
            if (gerente != null) {
                Anuncio anuncio = new Anuncio();
                anuncio.setTitulo("Promocion de bienvenida");
                anuncio.setTexto("Conoce nuestros productos destacados de la temporada.");
                anuncio.setImagen("https://example.com/promocion.jpg");
                anuncio.setFechaPublicacion(LocalDate.now());
                anuncio.setInformacionExtraTipo(InformacionExtraTipo.URL);
                anuncio.setInformacionExtraValor("https://example.com/detalle-promocion");
                anuncio.setCreadoPor(gerente);
                anuncioRepository.save(anuncio);
            }
        }
    }

    @Transactional
    public void guardarCliente(Cliente cliente) {
        if (cliente.getFacturacionMensual() == null) {
            cliente.setFacturacionMensual(BigDecimal.ZERO);
        }
        cliente.setSucursal(resolveSucursal(cliente.getSucursal()));
        cliente.setJefeSucursal(resolveEmpleado(cliente.getJefeSucursal()));
        clienteRepository.save(cliente);
    }

    @Transactional
    public void guardarAnuncio(Anuncio anuncio) {
        if (anuncio.getFechaPublicacion() == null) {
            anuncio.setFechaPublicacion(LocalDate.now());
        }
        if (anuncio.getInformacionExtraTipo() == null) {
            anuncio.setInformacionExtraTipo(InformacionExtraTipo.TEXTO);
        }
        anuncio.setCreadoPor(resolveEmpleado(anuncio.getCreadoPor()));
        anuncioRepository.save(anuncio);
    }

    @Transactional(readOnly = true)
    public List<Cliente> getClientes() {
        return clienteRepository.findAllByActiveTrueOrderByNombreAsc();
    }

    @Transactional(readOnly = true)
    public List<Anuncio> getAnuncios() {
        return anuncioRepository.findAllByActiveTrueOrderByFechaPublicacionDescIdDesc();
    }

    @Transactional(readOnly = true)
    public Cliente getCliente(Long id) {
        return clienteRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));
    }

    @Transactional(readOnly = true)
    public Cliente findClienteByTelefono(String telefono) {
        String normalized = normalizePhone(telefono);
        return clienteRepository.findAllByActiveTrue().stream()
                .filter(cliente -> normalizePhone(cliente.getTelefono()).equals(normalized))
                .findFirst()
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public Anuncio getAnuncio(Long id) {
        return anuncioRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Anuncio no encontrado"));
    }

    @Transactional(readOnly = true)
    public Sucursal getSucursal(Long id) {
        return sucursalRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Sucursal no encontrada"));
    }

    @Transactional(readOnly = true)
    public Empleado getEmpleado(Long id) {
        return empleadoRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Empleado no encontrado"));
    }

    @Transactional(readOnly = true)
    public Empleado findEmpleadoResponsableDeCliente(Cliente cliente) {
        if (cliente == null) {
            return null;
        }
        if (cliente.getJefeSucursal() != null) {
            return cliente.getJefeSucursal();
        }
        return getJefesSucursal().stream()
                .filter(jefe -> jefe.getSucursal() != null && cliente.getSucursal() != null
                        && jefe.getSucursal().getId().equals(cliente.getSucursal().getId()))
                .findFirst()
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public Empleado findEmpleadoByLoginContext(Long userId, String email) {
        return empleadoRepository.findAllByActiveTrueAndRolOperativoOrderByNombreAsc(RolOperativo.JEFE_SUCURSAL).stream()
                .filter(empleado -> (userId != null && empleado.getLoginUserId() != null && empleado.getLoginUserId().equals(userId))
                        || (email != null && !email.isBlank() && email.equalsIgnoreCase(empleado.getCorreo())))
                .findFirst()
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public List<Sucursal> getSucursales() {
        return sucursalRepository.findAllByActiveTrueOrderByNombreAsc();
    }

    @Transactional(readOnly = true)
    public List<Empleado> getJefesSucursal() {
        return empleadoRepository.findAllByActiveTrueAndRolOperativoOrderByNombreAsc(RolOperativo.JEFE_SUCURSAL);
    }

    @Transactional(readOnly = true)
    public List<Empleado> getGerentes() {
        return empleadoRepository.findAllByActiveTrueAndRolOperativoOrderByNombreAsc(RolOperativo.GERENTE);
    }

    private Sucursal resolveSucursal(Sucursal sucursal) {
        if (sucursal == null || sucursal.getId() == null) {
            return null;
        }
        return sucursalRepository.findById(sucursal.getId())
                .orElseThrow(() -> new IllegalArgumentException("Sucursal no encontrada"));
    }

    private Empleado resolveEmpleado(Empleado empleado) {
        if (empleado == null || empleado.getId() == null) {
            return null;
        }
        return empleadoRepository.findById(empleado.getId())
                .orElseThrow(() -> new IllegalArgumentException("Empleado no encontrado"));
    }

    private String normalizePhone(String phone) {
        return phone == null ? "" : phone.replaceAll("\\D", "");
    }
}