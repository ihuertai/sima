package mx.ipn.sima.repository;

import mx.ipn.sima.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    List<Cliente> findAllByActiveTrueOrderByNombreAsc();
}