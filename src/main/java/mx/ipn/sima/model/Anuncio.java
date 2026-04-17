package mx.ipn.sima.model;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "anuncios")
public class Anuncio extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String titulo;

    @Column(nullable = false, length = 1000)
    private String texto;

    @Column(name = "imagen_url", nullable = false, length = 500)
    private String imagen;

    @Column(name = "fecha_publicacion")
    private LocalDate fechaPublicacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "informacion_extra_tipo", length = 20)
    private InformacionExtraTipo informacionExtraTipo;

    @Column(name = "informacion_extra_valor", length = 500)
    private String informacionExtraValor;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "creado_por_empleado_id")
    private Empleado creadoPor;

    public Long getId() { return id; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }
    public String getImagen() { return imagen; }
    public void setImagen(String imagen) { this.imagen = imagen; }
    public LocalDate getFechaPublicacion() { return fechaPublicacion; }
    public void setFechaPublicacion(LocalDate fechaPublicacion) { this.fechaPublicacion = fechaPublicacion; }
    public InformacionExtraTipo getInformacionExtraTipo() { return informacionExtraTipo; }
    public void setInformacionExtraTipo(InformacionExtraTipo informacionExtraTipo) { this.informacionExtraTipo = informacionExtraTipo; }
    public String getInformacionExtraValor() { return informacionExtraValor; }
    public void setInformacionExtraValor(String informacionExtraValor) { this.informacionExtraValor = informacionExtraValor; }
    public Empleado getCreadoPor() { return creadoPor; }
    public void setCreadoPor(Empleado creadoPor) { this.creadoPor = creadoPor; }
}