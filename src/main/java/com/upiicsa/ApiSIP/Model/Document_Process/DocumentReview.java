package com.upiicsa.ApiSIP.Model.Document_Process;

import com.upiicsa.ApiSIP.Model.UserSIP;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "SIP_REVISIONDOC")
public class DocumentReview {

    @Id
    @Column(name = "ID_DOCUMENTO")
    private Integer idDocument;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "ID_DOCUMENTO", nullable = false)
    private Document document;

    @ManyToOne
    @JoinColumn(name = "ID_USUARIO", nullable = false)
    private UserSIP user;

    @Column(name = "FECHA_REVISION", nullable = false)
    private LocalDateTime reviewDate;

    @Column(name = "APROBADO", nullable = false)
    private Boolean approved;

    @Column(name = "COMENTARIO", length = 300)
    private String comment;
}