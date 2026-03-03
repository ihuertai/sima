package mx.ipn.sima.model;

import java.time.LocalDate;

public class Anuncio {

    private String texto;
    private String imagen;
    private LocalDate fechaPublicacion;

    public Anuncio() {
    }

    public Anuncio(String texto, String imagen, LocalDate fechaPublicacion) {
        this.texto = texto;
        this.imagen = imagen;
        this.fechaPublicacion = fechaPublicacion;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public LocalDate getFechaPublicacion() {
        return fechaPublicacion;
    }

    public void setFechaPublicacion(LocalDate fechaPublicacion) {
        this.fechaPublicacion = fechaPublicacion;
    }
}