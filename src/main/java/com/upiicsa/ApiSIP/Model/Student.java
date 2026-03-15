package com.upiicsa.ApiSIP.Model;

import com.upiicsa.ApiSIP.Model.Catalogs.Semester;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "SIP_ALUMNOS")
@PrimaryKeyJoinColumn(name = "ID_USUARIO")
public class Student extends UserSIP {

    @Column(name = "MATRICULA", length = 20, nullable = false, unique = true)
    private String enrollment;

    @Column(name = "TELEFONO", length = 20, nullable = false, unique = true)
    private String phone;

    @ManyToOne
    @JoinColumn(name = "ID_SEMESTRE")
    private Semester semester;

    @Column(name = "EGRESADO", nullable = false)
    private boolean graduate;

    @ManyToOne
    @JoinColumn(name = "ID_DIRECCION")
    private Address address;

    @ManyToOne
    @JoinColumn(name = "ID_OFERTA", nullable = false)
    private Offer offer;
}
