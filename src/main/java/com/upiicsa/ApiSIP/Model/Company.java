package com.upiicsa.ApiSIP.Model;

import jakarta.persistence.*;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "SIP_EMPRESA")
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_EMPRESA")
    public Integer id;

    @Column(name = "RAZON_SOCIAL", length = 100, nullable = false)
    private String name;

    @Column(name = "CORREO", length = 100, nullable = false,  unique = true)
    private String email;

    @Column(name = "SECTOR", length = 20, nullable = false)
    private String sector;

    @Column(name = "TELEFONO", length = 45, nullable = false, unique = true)
    private String phone;

    @Column(name = "EXTENSION", length = 10)
    private String extension;

    @Column(name = "FAX", length = 45)
    private String fax;

    @Column(name = "RESPONSABLE", length = 45, nullable = false)
    private String supervisor;

    @Column(name = "GRADO_ACA_RESPO", length = 10, nullable = false)
    private String supervisorGrade;

    @Column(name = "PUESTO_RESPO", length = 100, nullable = false)
    private String positionSupervisor;

    @Column(name = "PUESTO_ALUMNO", length = 100, nullable = false)
    private String positionStudent;

    @ManyToOne
    @JoinColumn(name = "ID_DIRECCION")
    private Address address;
}
