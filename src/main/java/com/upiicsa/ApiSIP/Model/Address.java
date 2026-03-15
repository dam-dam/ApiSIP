package com.upiicsa.ApiSIP.Model;

import com.upiicsa.ApiSIP.Model.Catalogs.State;
import jakarta.persistence.*;
import lombok.*;

@Getter @Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "SIP_DIRECCION")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_DIRECCION")
    private Integer id;

    @Column(name = "CALLE", length = 100, nullable = false)
    private String street;

    @Column(name = "NUMERO", length = 5)
    private String number;

    @Column(name = "COLONIA", length = 100)
    private String neighborhood;

    @Column(name = "CODIGOP", length = 10)
    private String zipCode;

    @ManyToOne
    @JoinColumn(name = "ID_EDO")
    private State state;
}
