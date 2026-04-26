package mx.ipn.sima.service;

import mx.ipn.sima.dto.DashboardMetrics;
import mx.ipn.sima.model.CampanaEnvio;
import mx.ipn.sima.model.EstadoCampana;
import mx.ipn.sima.model.InteraccionCliente;
import mx.ipn.sima.model.TipoInteraccionCliente;
import mx.ipn.sima.repository.AnuncioRepository;
import mx.ipn.sima.repository.CampanaEnvioRepository;
import mx.ipn.sima.repository.ClienteRepository;
import mx.ipn.sima.repository.InteraccionClienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DashboardService {

    private final ClienteRepository clienteRepository;
    private final AnuncioRepository anuncioRepository;
    private final CampanaEnvioRepository campanaEnvioRepository;
    private final InteraccionClienteRepository interaccionClienteRepository;

    public DashboardService(ClienteRepository clienteRepository,
                            AnuncioRepository anuncioRepository,
                            CampanaEnvioRepository campanaEnvioRepository,
                            InteraccionClienteRepository interaccionClienteRepository) {
        this.clienteRepository = clienteRepository;
        this.anuncioRepository = anuncioRepository;
        this.campanaEnvioRepository = campanaEnvioRepository;
        this.interaccionClienteRepository = interaccionClienteRepository;
    }

    @Transactional(readOnly = true)
    public DashboardMetrics getMetrics() {
        List<CampanaEnvio> campanas = campanaEnvioRepository.findAllByActiveTrueOrderByCreatedAtDesc();
        List<InteraccionCliente> interacciones = interaccionClienteRepository.findAllByActiveTrueOrderByFechaInteraccionDesc();

        long campanasProgramadas = campanas.stream()
                .filter(campana -> campana.getEstado() == EstadoCampana.PROGRAMADA)
                .count();
        long campanasEjecutadas = campanas.stream()
                .filter(campana -> campana.getEstado() == EstadoCampana.EJECUTADA || campana.getEstado() == EstadoCampana.EJECUTADA_CON_ERRORES)
                .count();
        long totalEnviosExitosos = campanas.stream()
                .map(CampanaEnvio::getEnviosExitosos)
                .filter(value -> value != null)
                .mapToLong(Integer::longValue)
                .sum();
        long totalEnviosError = campanas.stream()
                .map(CampanaEnvio::getEnviosError)
                .filter(value -> value != null)
                .mapToLong(Integer::longValue)
                .sum();
        long solicitudesContacto = interacciones.stream()
                .filter(interaccion -> interaccion.getTipo() == TipoInteraccionCliente.QUIERE_CONTACTO)
                .count();
        long solicitudesMasInfo = interacciones.stream()
                .filter(interaccion -> interaccion.getTipo() == TipoInteraccionCliente.MAS_INFO)
                .count();

        return new DashboardMetrics(
                clienteRepository.findAllByActiveTrue().size(),
                anuncioRepository.findAllByActiveTrueOrderByFechaPublicacionDescIdDesc().size(),
                campanas.size(),
                campanasProgramadas,
                campanasEjecutadas,
                totalEnviosExitosos,
                totalEnviosError,
                interacciones.size(),
                solicitudesContacto,
                solicitudesMasInfo,
                campanas.stream().limit(5).toList(),
                interacciones.stream().limit(5).toList()
        );
    }
}