package mx.ipn.sima.service;

import mx.ipn.sima.model.Sucursal;
import mx.ipn.sima.repository.SucursalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SucursalService {

    private final SucursalRepository sucursalRepository;

    public SucursalService(SucursalRepository sucursalRepository) {
        this.sucursalRepository = sucursalRepository;
    }

    @Transactional(readOnly = true)
    public List<Sucursal> listarSucursales() {
        return sucursalRepository.findAllByActiveTrueOrderByNombreAsc();
    }

    @Transactional(readOnly = true)
    public Sucursal obtenerSucursal(Long id) {
        return sucursalRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sucursal no encontrada"));
    }

    @Transactional
    public Sucursal guardarSucursal(Sucursal sucursal) {
        validateUnique(sucursal);
        return sucursalRepository.save(sucursal);
    }

    @Transactional
    public void eliminarSucursal(Long id) {
        Sucursal sucursal = obtenerSucursal(id);
        sucursal.setActive(false);
        sucursalRepository.save(sucursal);
    }

    private void validateUnique(Sucursal sucursal) {
        String clave = sucursal.getClave() != null ? sucursal.getClave().trim().toUpperCase() : "";
        String nombre = sucursal.getNombre() != null ? sucursal.getNombre().trim() : "";

        sucursalRepository.findAllByActiveTrueOrderByNombreAsc().forEach(existing -> {
            if (sucursal.getId() != null && existing.getId().equals(sucursal.getId())) {
                return;
            }
            if (existing.getClave() != null && existing.getClave().equalsIgnoreCase(clave)) {
                throw new IllegalArgumentException("Ya existe una sucursal con esa clave");
            }
            if (existing.getNombre() != null && existing.getNombre().equalsIgnoreCase(nombre)) {
                throw new IllegalArgumentException("Ya existe una sucursal con ese nombre");
            }
        });

        sucursal.setClave(clave);
        sucursal.setNombre(nombre);
        sucursal.setTelefono(sucursal.getTelefono() != null ? sucursal.getTelefono().trim() : null);
        sucursal.setCorreo(sucursal.getCorreo() != null ? sucursal.getCorreo().trim() : null);
        sucursal.setActive(true);
    }
}