package com.upiicsa.ApiSIP.Model.Document_Process;

import com.upiicsa.ApiSIP.Model.Catalogs.DocumentStatus;
import com.upiicsa.ApiSIP.Model.Catalogs.DocumentType;
import com.upiicsa.ApiSIP.Model.UserSIP;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "SIP_DOCUMENTOS")
public class Document {

    @Id
    @Column(name = "ID_DOCUMENTO")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "FECHA_CARGA", nullable = false)
    private LocalDateTime uploadDate;

    @Column(name = "URL", nullable = false  )
    private String URL;

    @Column(name = "FECHA_BAJA")
    private LocalDateTime cancellationDate;

    @ManyToOne
    @JoinColumn(name = "ID_TIPO_DOC", nullable = false)
    private DocumentType documentType;

    @ManyToOne
    @JoinColumn(name = "ID_ESTADO_DOC", nullable = false)
    private DocumentStatus documentStatus;

    @ManyToOne
    @JoinColumn(name = "ID_USUARIO_CARGA", referencedColumnName = "ID_USUARIO", nullable = false)
    private UserSIP user;

    @ManyToOne
    @JoinColumn(name = "ID_PROCESO")
    private StudentProcess studentProcess;
}
