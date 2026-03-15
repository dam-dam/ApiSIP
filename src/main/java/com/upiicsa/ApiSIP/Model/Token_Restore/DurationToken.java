package com.upiicsa.ApiSIP.Model.Token_Restore;

import com.upiicsa.ApiSIP.Model.UserType;
import jakarta.persistence.*;
import lombok.*;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "SIP_CDURACION_TOKEN")
public class DurationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_DURACION")
    private Integer id;

    @Column(name = "DURACION_HORAS", nullable = false)
    private Integer hours;

    @ManyToOne
    @JoinColumn(name = "ID_TIPO_USUARIO",  nullable = false)
    private UserType user;
}
