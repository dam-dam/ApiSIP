package com.upiicsa.ApiSIP.Model.Document_Process;

import com.upiicsa.ApiSIP.Model.Catalogs.DocumentType;
import com.upiicsa.ApiSIP.Model.Catalogs.ProcessStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Entity
@Table(name = "SIP_CPROCESODOC")
public class DocumentProcess {

    @EmbeddedId
    private DocumentProcessId id = new DocumentProcessId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idTypeDocument")
    @JoinColumn(name = "ID_TIPO_DOC")
    private DocumentType documentType;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idStateProcess")
    @JoinColumn(name = "ID_EST_PROCESO")
    private ProcessStatus processStatus;

    @Column(name = "REQUERIDO")
    private Boolean requery;

    public DocumentProcess(DocumentType documentType, ProcessStatus processStatus, Boolean requery) {
        this.documentType = documentType;
        this.processStatus = processStatus;
        this.requery = requery;
    }
    public DocumentProcess(){

    }
}
