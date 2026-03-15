package com.upiicsa.ApiSIP.Model.Token_Restore;

import com.upiicsa.ApiSIP.Model.UserSIP;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "SIP_TOKEN_RESETEO")
public class TokenReset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_TOKEN_RESETEO")
    private Integer id;

    @Column(name = "TOKEN", length = 200, nullable = false,  unique = true)
    private String token;

    @Column(name = "FECHA_EXPIRACION", nullable = false)
    private LocalDateTime ExpirationDate;

    @Column(name = "FECHA_USO")
    private LocalDateTime UseDate;

    @ManyToOne
    @JoinColumn(name = "ID_USUARIO")
    private UserSIP user;
}
