package com.upiicsa.ApiSIP.Model.Document_Process;

import com.upiicsa.ApiSIP.Model.Catalogs.ProcessStatus;
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
@Table(name = "SIP_PROCESOS")
public class StudentProcess {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_PROCESO")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "ID_ALUMNO")
    private Student student;

    @Column(name = "FECHA_INICIO", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "FECHA_FIN")
    private LocalDateTime endDate;

    @ManyToOne
    @JoinColumn(name = "ESTADO_ACTUAL", referencedColumnName = "ID_EST_PROCESO", nullable = false)
    private ProcessStatus processStatus;

    @Column(name = "MOTIVO_BAJA", length = 200)
    private String reasonLeaving;

    @ManyToOne
    @JoinColumn(name = "ID_EMPRESA")
    private Company company;
}
