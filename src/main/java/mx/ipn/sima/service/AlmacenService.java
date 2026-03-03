package mx.ipn.sima.service;

import mx.ipn.sima.model.Cliente;
import mx.ipn.sima.model.Anuncio;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AlmacenService {

    private final List<Cliente> clientes = new ArrayList<>();
    private final List<Anuncio> anuncios = new ArrayList<>();

    public void guardarCliente(Cliente cliente) {
        clientes.add(cliente);
    }

    public void guardarAnuncio(Anuncio anuncio) {
        anuncios.add(anuncio);
    }

    public List<Cliente> getClientes() {
        return clientes;
    }

    public List<Anuncio> getAnuncios() {
        return anuncios;
    }
}