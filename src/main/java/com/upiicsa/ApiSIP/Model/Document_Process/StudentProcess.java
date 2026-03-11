package com.upiicsa.ApiSIP.Model.Document_Process;

import com.upiicsa.ApiSIP.Model.Catalogs.ProcessState;
import com.upiicsa.ApiSIP.Model.Company;
import com.upiicsa.ApiSIP.Model.Student;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "SIP_PROCESO")
public class StudentProcess {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_PROCESO")
    private Integer id;

    @Column(name = "FECHA_INICIO")
    private LocalDateTime startDate;

    @Column(name = "FECHA_FIN")
    private LocalDateTime endDate;

    @Column(name = "ACTIVO")
    private Boolean active;

    @ManyToOne
    @JoinColumn(name = "ID_USUARIO")
    private Student student;

    @ManyToOne
    @JoinColumn(name = "ESTADO_ACTUAL", referencedColumnName = "ID_ESTPROCESO")
    private ProcessState processState;

    @Column(name = "OBSERVACIONES", length = 150)
    private String observations;

    @ManyToOne
    @JoinColumn(name = "ID_EMPRESA")
    private Company company;
}
