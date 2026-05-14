package mx.ipn.sima.repository;

import mx.ipn.sima.model.Empleado;
import mx.ipn.sima.model.RolOperativo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmpleadoRepository extends JpaRepository<Empleado, Long> {
    List<Empleado> findAllByActiveTrueAndRolOperativoOrderByNombreAsc(RolOperativo rolOperativo);
    Optional<Empleado> findByLoginUserId(Long loginUserId);
    Optional<Empleado> findByCorreoIgnoreCase(String correo);
}