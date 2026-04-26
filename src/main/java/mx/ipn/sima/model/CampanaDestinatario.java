package mx.ipn.sima.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "campana_destinatarios")
public class CampanaDestinatario extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campana_id", nullable = false)
    private CampanaEnvio campana;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoDestinatario estado = EstadoDestinatario.PENDIENTE;

    @Column(name = "fecha_intento")
    private LocalDateTime fechaIntento;

    @Column(name = "detalle_error", length = 500)
    private String detalleError;

    public Long getId() { return id; }
    public CampanaEnvio getCampana() { return campana; }
    public void setCampana(CampanaEnvio campana) { this.campana = campana; }
    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
    public EstadoDestinatario getEstado() { return estado; }
    public void setEstado(EstadoDestinatario estado) { this.estado = estado; }
    public LocalDateTime getFechaIntento() { return fechaIntento; }
    public void setFechaIntento(LocalDateTime fechaIntento) { this.fechaIntento = fechaIntento; }
    public String getDetalleError() { return detalleError; }
    public void setDetalleError(String detalleError) { this.detalleError = detalleError; }
}