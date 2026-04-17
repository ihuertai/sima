package mx.ipn.sima.repository;

import mx.ipn.sima.model.Sucursal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SucursalRepository extends JpaRepository<Sucursal, Long> {
    List<Sucursal> findAllByActiveTrueOrderByNombreAsc();
}