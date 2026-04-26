package mx.ipn.sima.repository;

import mx.ipn.sima.model.Empleado;
import mx.ipn.sima.model.InteraccionCliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InteraccionClienteRepository extends JpaRepository<InteraccionCliente, Long> {
    List<InteraccionCliente> findAllByActiveTrueOrderByFechaInteraccionDesc();
    List<InteraccionCliente> findAllByActiveTrueAndJefeResponsableOrderByFechaInteraccionDesc(Empleado jefeResponsable);
}