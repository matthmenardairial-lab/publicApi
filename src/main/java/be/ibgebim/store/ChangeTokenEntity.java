package be.ibgebim.store;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.Instant;

@Entity
public class ChangeTokenEntity {
    @Id
    private String listId;
    private String lastChangeToken;
    private Instant updatedAt;

    public ChangeTokenEntity() {}
    public ChangeTokenEntity(String listId, String lastChangeToken, Instant updatedAt) {
        this.listId = listId;
        this.lastChangeToken = lastChangeToken;
        this.updatedAt = updatedAt;
    }

    public String getListId() {
        return listId;
    }

    public void setListId(String listId) {
        this.listId = listId;
    }

    public String getLastChangeToken() {
        return lastChangeToken;
    }

    public void setLastChangeToken(String lastChangeToken) {
        this.lastChangeToken = lastChangeToken;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}

