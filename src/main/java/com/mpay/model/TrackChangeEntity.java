package com.mpay.model;

import com.mpay.initialization.UserContext;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.io.Serializable;
import java.sql.Timestamp;

@MappedSuperclass
@Getter
@Setter
@Slf4j
public abstract class TrackChangeEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Column(name = "CREATED_BY", updatable = false)
    protected Long createdBy;

    @Column(name = "DATE_CREATED", updatable = false)
    protected Timestamp dateCreated;

    @Column(name = "MODIFIED_BY")
    protected Long modifiedBy;

    @Column(name = "DATE_MODIFIED")
    protected Timestamp dateModified;

    @Column(name = "IS_DELETED")
    private Boolean isDeleted = false;

    @PrePersist
    public void prePersist() {
        if (this.dateCreated == null) {
            this.dateCreated = new Timestamp(System.currentTimeMillis());
        }

        if (this.dateModified == null) {
            this.dateModified = this.dateCreated;
        }

        try {
            if (this.createdBy == null && UserContext.getUserContext() != null) {
                this.createdBy = UserContext.getUserId();
            }

            if (this.modifiedBy == null && UserContext.getUserContext() != null) {
                this.modifiedBy = this.createdBy = UserContext.getUserId();
            }
        } catch (Exception e) {
            log.error("Exception in TrackChangeEntity - {}", e.getMessage(), e);
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.dateModified = new Timestamp(System.currentTimeMillis());
        if (this.modifiedBy == null) {
            this.modifiedBy = UserContext.getUserId();
        }
    }
}
