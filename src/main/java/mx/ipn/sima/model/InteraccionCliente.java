package mx.ipn.sima.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "interacciones_cliente")
public class InteraccionCliente extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "campana_id")
    private CampanaEnvio campana;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "anuncio_id")
    private Anuncio anuncio;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "jefe_responsable_id")
    private Empleado jefeResponsable;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoInteraccionCliente tipo;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_seguimiento", nullable = false, length = 20)
    private EstadoSeguimiento estadoSeguimiento = EstadoSeguimiento.PENDIENTE;

    @Column(name = "telefono_cliente", nullable = false, length = 30)
    private String telefonoCliente;

    @Column(name = "producto_nombre", length = 140)
    private String productoNombre;

    @Column(name = "mensaje_cliente", length = 1000)
    private String mensajeCliente;

    @Column(name = "notificacion_interna", length = 1000)
    private String notificacionInterna;

    @Column(name = "fecha_interaccion", nullable = false)
    private LocalDateTime fechaInteraccion;

    public Long getId() { return id; }
    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
    public CampanaEnvio getCampana() { return campana; }
    public void setCampana(CampanaEnvio campana) { this.campana = campana; }
    public Anuncio getAnuncio() { return anuncio; }
    public void setAnuncio(Anuncio anuncio) { this.anuncio = anuncio; }
    public Empleado getJefeResponsable() { return jefeResponsable; }
    public void setJefeResponsable(Empleado jefeResponsable) { this.jefeResponsable = jefeResponsable; }
    public TipoInteraccionCliente getTipo() { return tipo; }
    public void setTipo(TipoInteraccionCliente tipo) { this.tipo = tipo; }
    public EstadoSeguimiento getEstadoSeguimiento() { return estadoSeguimiento; }
    public void setEstadoSeguimiento(EstadoSeguimiento estadoSeguimiento) { this.estadoSeguimiento = estadoSeguimiento; }
    public String getTelefonoCliente() { return telefonoCliente; }
    public void setTelefonoCliente(String telefonoCliente) { this.telefonoCliente = telefonoCliente; }
    public String getProductoNombre() { return productoNombre; }
    public void setProductoNombre(String productoNombre) { this.productoNombre = productoNombre; }
    public String getMensajeCliente() { return mensajeCliente; }
    public void setMensajeCliente(String mensajeCliente) { this.mensajeCliente = mensajeCliente; }
    public String getNotificacionInterna() { return notificacionInterna; }
    public void setNotificacionInterna(String notificacionInterna) { this.notificacionInterna = notificacionInterna; }
    public LocalDateTime getFechaInteraccion() { return fechaInteraccion; }
    public void setFechaInteraccion(LocalDateTime fechaInteraccion) { this.fechaInteraccion = fechaInteraccion; }
}