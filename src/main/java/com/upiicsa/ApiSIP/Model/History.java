package com.upiicsa.ApiSIP.Model;

import com.upiicsa.ApiSIP.Model.Catalogs.ProcessStatus;
import com.upiicsa.ApiSIP.Model.Document_Process.StudentProcess;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
@Builder
@Entity
@Table(name = "SIP_HISTORIAL")
public class History {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_HISTORIAL")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "ID_PROCESO", nullable = false)
    private StudentProcess process;

    @ManyToOne
    @JoinColumn(name = "ID_USUARIO", nullable = false)
    private UserSIP user;

    @Column(name = "FECHA_ACT", nullable = false)
    private LocalDateTime updateDate;

    @ManyToOne
    @JoinColumn(name = "ESTADO_NUEVO", referencedColumnName = "ID_EST_PROCESO", nullable = false)
    private ProcessStatus newState;

    @ManyToOne
    @JoinColumn(name = "ESTADO_ANTERIOR", referencedColumnName = "ID_EST_PROCESO", nullable = false)
    private ProcessStatus oldState;
}
