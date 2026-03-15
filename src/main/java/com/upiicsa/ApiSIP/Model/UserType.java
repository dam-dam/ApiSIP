package com.upiicsa.ApiSIP.Model;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "SIP_CTIPO_USUARIO")
public class UserType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_TIPO_USUARIO")
    private Integer id;

    @Column(name = "DESCRIPCION", length = 80,  nullable = false, unique = true)
    private String description;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "SIP_TIPO_PERMISOS",
            joinColumns = @JoinColumn(name = "ID_TIPO_USUARIO"),
            inverseJoinColumns = @JoinColumn(name = "ID_PERMISO")
    )
    @ToString.Exclude
    private Set<Permission> permissions = new HashSet<>();
}
