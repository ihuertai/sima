package mx.ipn.sima.model;

import jakarta.persistence.*;

@Entity
@Table(name = "empleados")
public class Empleado extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(length = 160)
    private String correo;

    @Column(length = 30)
    private String telefono;

    @Column(length = 120)
    private String puesto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RolOperativo rolOperativo;

    @Column(name = "login_user_id")
    private Long loginUserId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sucursal_id")
    private Sucursal sucursal;

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getPuesto() { return puesto; }
    public void setPuesto(String puesto) { this.puesto = puesto; }
    public RolOperativo getRolOperativo() { return rolOperativo; }
    public void setRolOperativo(RolOperativo rolOperativo) { this.rolOperativo = rolOperativo; }
    public Long getLoginUserId() { return loginUserId; }
    public void setLoginUserId(Long loginUserId) { this.loginUserId = loginUserId; }
    public Sucursal getSucursal() { return sucursal; }
    public void setSucursal(Sucursal sucursal) { this.sucursal = sucursal; }
}