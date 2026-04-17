package mx.ipn.sima.model;

import jakarta.persistence.*;

@Entity
@Table(name = "sucursales")
public class Sucursal extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String clave;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(length = 30)
    private String telefono;

    @Column(length = 160)
    private String correo;

    public Long getId() { return id; }
    public String getClave() { return clave; }
    public void setClave(String clave) { this.clave = clave; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
}