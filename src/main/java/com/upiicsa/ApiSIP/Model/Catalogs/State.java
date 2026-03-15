package com.upiicsa.ApiSIP.Model.Catalogs;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Getter @Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "SIP_CEDOS")
public class State {

    @Id
    @Column(name = "ID_EDO")
    private Integer id;

    @Column(name = "NOMBRE", nullable = false, unique = true)
    private String name;
}
