package com.upiicsa.ApiSIP.Model.Catalogs;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@NoArgsConstructor
@Getter @Setter
@Entity
@Table(name = "SIP_CEST_PROCESO")
public class ProcessStatus extends BaseCatalog{

    @Id
    @Column(name = "ID_EST_PROCESO")
    private Integer id;
}
