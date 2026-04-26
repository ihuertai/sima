package mx.ipn.sima.model;

import jakarta.persistence.*;

@Entity
@Table(name = "whatsapp_conversation_context")
public class WhatsappConversationContext extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "phone_number", nullable = false, unique = true, length = 30)
    private String phoneNumber;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "campana_id")
    private CampanaEnvio campana;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "anuncio_id")
    private Anuncio anuncio;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "jefe_responsable_id")
    private Empleado jefeResponsable;

    @Column(name = "producto_nombre", length = 140)
    private String productoNombre;

    public Long getId() { return id; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public CampanaEnvio getCampana() { return campana; }
    public void setCampana(CampanaEnvio campana) { this.campana = campana; }
    public Anuncio getAnuncio() { return anuncio; }
    public void setAnuncio(Anuncio anuncio) { this.anuncio = anuncio; }
    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
    public Empleado getJefeResponsable() { return jefeResponsable; }
    public void setJefeResponsable(Empleado jefeResponsable) { this.jefeResponsable = jefeResponsable; }
    public String getProductoNombre() { return productoNombre; }
    public void setProductoNombre(String productoNombre) { this.productoNombre = productoNombre; }
}