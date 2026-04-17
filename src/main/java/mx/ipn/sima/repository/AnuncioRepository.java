package mx.ipn.sima.repository;

import mx.ipn.sima.model.Anuncio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnuncioRepository extends JpaRepository<Anuncio, Long> {
    List<Anuncio> findAllByActiveTrueOrderByFechaPublicacionDescIdDesc();
}