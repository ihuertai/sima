package mx.ipn.sima.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "campanas_envio")
public class CampanaEnvio extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 140)
    private String nombre;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "anuncio_id", nullable = false)
    private Anuncio anuncio;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "creado_por_empleado_id")
    private Empleado creadoPor;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sucursal_id")
    private Sucursal sucursal;

    @Column(name = "facturacion_min", precision = 14, scale = 2)
    private BigDecimal facturacionMin;

    @Column(name = "facturacion_max", precision = 14, scale = 2)
    private BigDecimal facturacionMax;

    @Enumerated(EnumType.STRING)
    @Column(name = "tamano_empresa", length = 20)
    private TamanoEmpresa tamanoEmpresa;

    @Column(name = "categoria_producto", length = 120)
    private String categoriaProducto;

    @Column(length = 120)
    private String giro;

    @Column(name = "programada_para")
    private LocalDateTime programadaPara;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoCampana estado = EstadoCampana.BORRADOR;

    @Column(name = "enviar_ahora", nullable = false)
    private Boolean enviarAhora = Boolean.FALSE;

    @Column(name = "total_destinatarios", nullable = false)
    private Integer totalDestinatarios = 0;

    @Column(name = "envios_exitosos", nullable = false)
    private Integer enviosExitosos = 0;

    @Column(name = "envios_error", nullable = false)
    private Integer enviosError = 0;

    @Column(name = "ultimo_envio_at")
    private LocalDateTime ultimoEnvioAt;

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public Anuncio getAnuncio() { return anuncio; }
    public void setAnuncio(Anuncio anuncio) { this.anuncio = anuncio; }
    public Empleado getCreadoPor() { return creadoPor; }
    public void setCreadoPor(Empleado creadoPor) { this.creadoPor = creadoPor; }
    public Sucursal getSucursal() { return sucursal; }
    public void setSucursal(Sucursal sucursal) { this.sucursal = sucursal; }
    public BigDecimal getFacturacionMin() { return facturacionMin; }
    public void setFacturacionMin(BigDecimal facturacionMin) { this.facturacionMin = facturacionMin; }
    public BigDecimal getFacturacionMax() { return facturacionMax; }
    public void setFacturacionMax(BigDecimal facturacionMax) { this.facturacionMax = facturacionMax; }
    public TamanoEmpresa getTamanoEmpresa() { return tamanoEmpresa; }
    public void setTamanoEmpresa(TamanoEmpresa tamanoEmpresa) { this.tamanoEmpresa = tamanoEmpresa; }
    public String getCategoriaProducto() { return categoriaProducto; }
    public void setCategoriaProducto(String categoriaProducto) { this.categoriaProducto = categoriaProducto; }
    public String getGiro() { return giro; }
    public void setGiro(String giro) { this.giro = giro; }
    public LocalDateTime getProgramadaPara() { return programadaPara; }
    public void setProgramadaPara(LocalDateTime programadaPara) { this.programadaPara = programadaPara; }
    public EstadoCampana getEstado() { return estado; }
    public void setEstado(EstadoCampana estado) { this.estado = estado; }
    public Boolean getEnviarAhora() { return enviarAhora; }
    public void setEnviarAhora(Boolean enviarAhora) { this.enviarAhora = enviarAhora; }
    public Integer getTotalDestinatarios() { return totalDestinatarios; }
    public void setTotalDestinatarios(Integer totalDestinatarios) { this.totalDestinatarios = totalDestinatarios; }
    public Integer getEnviosExitosos() { return enviosExitosos; }
    public void setEnviosExitosos(Integer enviosExitosos) { this.enviosExitosos = enviosExitosos; }
    public Integer getEnviosError() { return enviosError; }
    public void setEnviosError(Integer enviosError) { this.enviosError = enviosError; }
    public LocalDateTime getUltimoEnvioAt() { return ultimoEnvioAt; }
    public void setUltimoEnvioAt(LocalDateTime ultimoEnvioAt) { this.ultimoEnvioAt = ultimoEnvioAt; }
}