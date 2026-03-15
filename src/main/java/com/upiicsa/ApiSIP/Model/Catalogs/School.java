package com.upiicsa.ApiSIP.Model.Catalogs;

import jakarta.persistence.*;
import lombok.*;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "SIP_ESCUELAS")
public class School {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_ESCUELA")
    private Integer id;

    @Column(name = "NOMBRE", length = 100, nullable = false, unique = true)
    private String name;

    @Column(name = "SIGLAS", length = 30, nullable = false, unique = true)
    private String acronym;
}
