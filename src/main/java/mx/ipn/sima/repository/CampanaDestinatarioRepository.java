package mx.ipn.sima.repository;

import mx.ipn.sima.model.CampanaDestinatario;
import mx.ipn.sima.model.CampanaEnvio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CampanaDestinatarioRepository extends JpaRepository<CampanaDestinatario, Long> {
    List<CampanaDestinatario> findAllByCampanaOrderByClienteNombreAsc(CampanaEnvio campana);
    void deleteAllByCampana(CampanaEnvio campana);
}