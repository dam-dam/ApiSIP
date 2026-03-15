package com.upiicsa.ApiSIP.Model;

import jakarta.persistence.*;
import lombok.*;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "SIP_PERMISOS")
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_PERMISO")
    private Integer id;

    @Column(name = "DESCRIPCION", length = 80, nullable = false, unique = true)
    private String description;
}
