package com.upiicsa.ApiSIP.Model.Token_Restore;

import com.upiicsa.ApiSIP.Model.UserSIP;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "SIP_CODIGO_CONFIRM")
public class ConfirmationCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_CODIGO_CONFIRM")
    private Integer id;

    @Column(name = "CODIGO", length = 10, nullable = false, unique = true)
    private String code;

    @Column(name = "FECHA_EXPIRACION", nullable = false)
    private LocalDateTime expirationDate;

    @Column(name = "FECHA_USO")
    private LocalDateTime useDate;

    @ManyToOne
    @JoinColumn(name = "ID_USUARIO")
    private UserSIP user;
}
