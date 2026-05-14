package mx.ipn.sima.service;

import mx.ipn.sima.dto.LoginGerenteOption;
import mx.ipn.sima.model.Empleado;
import mx.ipn.sima.model.RolOperativo;
import mx.ipn.sima.model.Sucursal;
import mx.ipn.sima.repository.EmpleadoRepository;
import mx.ipn.sima.repository.SucursalRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class GerenteSyncService {

    private static final Logger log = LoggerFactory.getLogger(GerenteSyncService.class);

    private final EmpleadoRepository empleadoRepository;
    private final SucursalRepository sucursalRepository;
    private final RestTemplate restTemplate;
    private final String loginGerentesUrl;

    public GerenteSyncService(EmpleadoRepository empleadoRepository,
                              SucursalRepository sucursalRepository,
                              @Value("${app.sso.login-gerentes-url:http://localhost:8080/auth/gerentes}") String loginGerentesUrl) {
        this.empleadoRepository = empleadoRepository;
        this.sucursalRepository = sucursalRepository;
        this.restTemplate = new RestTemplate();
        this.loginGerentesUrl = loginGerentesUrl;
    }

    public List<Empleado> getGerentesParaCampana(String authToken) {
        List<Empleado> fallback = empleadoRepository.findAllByActiveTrueAndRolOperativoOrderByNombreAsc(RolOperativo.GERENTE);
        if (authToken == null || authToken.isBlank()) {
            return fallback;
        }

        try {
            String url = UriComponentsBuilder.fromUriString(loginGerentesUrl)
                    .queryParam("token", authToken)
                    .toUriString();

            LoginGerenteOption[] response = restTemplate.getForObject(url, LoginGerenteOption[].class);
            List<LoginGerenteOption> gerentes = response != null ? Arrays.asList(response) : List.of();
            if (gerentes.isEmpty()) {
                return fallback;
            }

            Sucursal defaultSucursal = sucursalRepository.findAllByActiveTrueOrderByNombreAsc().stream().findFirst().orElse(null);
            for (LoginGerenteOption gerente : gerentes) {
                if (gerente == null || gerente.id() == null) {
                    continue;
                }
                Empleado empleado = resolveEmpleado(gerente);
                empleado.setLoginUserId(gerente.id());
                empleado.setNombre(gerente.username() != null && !gerente.username().isBlank() ? gerente.username() : gerente.email());
                empleado.setCorreo(gerente.email());
                empleado.setTelefono(gerente.telefono());
                empleado.setPuesto("Gerente del sistema");
                empleado.setRolOperativo(RolOperativo.GERENTE);
                if (empleado.getSucursal() == null) {
                    empleado.setSucursal(defaultSucursal);
                }
                empleado.setActive(true);
                empleadoRepository.save(empleado);
            }

            List<Empleado> synced = empleadoRepository.findAllByActiveTrueAndRolOperativoOrderByNombreAsc(RolOperativo.GERENTE).stream()
                    .filter(empleado -> empleado.getLoginUserId() != null)
                    .toList();
            return synced.isEmpty() ? fallback : synced;
        } catch (Exception ex) {
            log.warn("No fue posible sincronizar gerentes desde login: {}", ex.getMessage());
            return fallback;
        }
    }

    private Empleado resolveEmpleado(LoginGerenteOption gerente) {
        Optional<Empleado> byLoginUserId = empleadoRepository.findByLoginUserId(gerente.id());
        if (byLoginUserId.isPresent()) {
            return byLoginUserId.get();
        }
        if (gerente.email() != null && !gerente.email().isBlank()) {
            return empleadoRepository.findByCorreoIgnoreCase(gerente.email()).orElseGet(Empleado::new);
        }
        return new Empleado();
    }
}