package mx.ipn.sima.repository;

import mx.ipn.sima.model.CampanaEnvio;
import mx.ipn.sima.model.EstadoCampana;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface CampanaEnvioRepository extends JpaRepository<CampanaEnvio, Long> {
    List<CampanaEnvio> findAllByActiveTrueOrderByCreatedAtDesc();
    List<CampanaEnvio> findAllByActiveTrueAndEstadoAndProgramadaParaLessThanEqual(EstadoCampana estado, LocalDateTime dateTime);
}