package org.datamate.identity.adapter.out.persistence.authorization.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "resource")
public class ResourceJpaEntity {
    @Id
    private UUID id;

    @Column(name = "namespace_id")
    private UUID namespaceId;

    private String name;
    private String description;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getNamespaceId() {
        return namespaceId;
    }

    public void setNamespaceId(UUID namespaceId) {
        this.namespaceId = namespaceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
