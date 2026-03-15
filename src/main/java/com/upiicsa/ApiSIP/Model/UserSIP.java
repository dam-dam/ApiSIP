package com.upiicsa.ApiSIP.Model;

import com.upiicsa.ApiSIP.Model.Catalogs.Status;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "SIP_USUARIOS")
@Inheritance(strategy = InheritanceType.JOINED)
public class UserSIP implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_USUARIO")
    private Integer id;

    @Column(name = "NOMBRE", length = 60, nullable = false)
    private String name;

    @Column(name = "PATERNO", length = 60, nullable = false)
    private String fLastName;

    @Column(name = "MATERNO", length = 60, nullable = false)
    private String mLastName;

    @Column(name = "CORREO", length = 100, nullable = false, unique = true)
    private String email;

    @Column(name = "CONTRASENA", nullable = false)
    private String password;

    @Column(name = "HABILITADO", nullable = false)
    private Boolean enabled;

    @Column(name = "FECHA_ALTA", nullable = false)
    private LocalDateTime registrationDate;

    @Column(name = "FECHA_BAJA")
    private LocalDateTime cancellationDate;

    @ManyToOne
    @JoinColumn(name = "ID_TIPO_USUARIO")
    private UserType userType;

    @ManyToOne
    @JoinColumn(name = "ID_ESTATUS")
    private Status status;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> authorities = new HashSet<>();
        this.userType.getPermissions()
                .stream()
                .map(permiso ->  new SimpleGrantedAuthority(permiso.getDescription()))
                .forEach(authorities::add);
        authorities.add(new SimpleGrantedAuthority("ROLE_" + this.userType.getDescription()));

        return authorities;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }
}
