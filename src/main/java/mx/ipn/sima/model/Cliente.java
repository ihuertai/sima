package mx.ipn.sima.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "clientes")
public class Cliente extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(length = 160)
    private String correo;

    @Column(nullable = false, length = 30)
    private String telefono;

    @Column(name = "razon_social", length = 180)
    private String razonSocial;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sucursal_id")
    private Sucursal sucursal;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "jefe_sucursal_id")
    private Empleado jefeSucursal;

    @Column(name = "facturacion_mensual", precision = 14, scale = 2)
    private BigDecimal facturacionMensual;

    @Enumerated(EnumType.STRING)
    @Column(name = "tamano_empresa", length = 20)
    private TamanoEmpresa tamanoEmpresa;

    @Column(name = "categoria_producto", length = 120)
    private String categoriaProducto;

    @Column(length = 120)
    private String giro;

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getRazonSocial() { return razonSocial; }
    public void setRazonSocial(String razonSocial) { this.razonSocial = razonSocial; }
    public Sucursal getSucursal() { return sucursal; }
    public void setSucursal(Sucursal sucursal) { this.sucursal = sucursal; }
    public Empleado getJefeSucursal() { return jefeSucursal; }
    public void setJefeSucursal(Empleado jefeSucursal) { this.jefeSucursal = jefeSucursal; }
    public BigDecimal getFacturacionMensual() { return facturacionMensual; }
    public void setFacturacionMensual(BigDecimal facturacionMensual) { this.facturacionMensual = facturacionMensual; }
    public TamanoEmpresa getTamanoEmpresa() { return tamanoEmpresa; }
    public void setTamanoEmpresa(TamanoEmpresa tamanoEmpresa) { this.tamanoEmpresa = tamanoEmpresa; }
    public String getCategoriaProducto() { return categoriaProducto; }
    public void setCategoriaProducto(String categoriaProducto) { this.categoriaProducto = categoriaProducto; }
    public String getGiro() { return giro; }
    public void setGiro(String giro) { this.giro = giro; }
}