package org.fogbowcloud.app.core.models.task;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Sets meta information about Jobs.
 */
@Entity
@Table(name = "specification")
public class Specification implements Serializable {

    private static final String LN = System.lineSeparator();
    private static final String DOCKER_IMAGE_COLUMN_NAME = "docker_image";
    private static final String USER_ID_COLUMN_NAME = "user_id";
    private static final String REQUIREMENTS_KEY_COLUMN_NAME = "requirements_key";
    private static final String REQUIREMENTS_COLUMN_NAME = "requirements";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = DOCKER_IMAGE_COLUMN_NAME)
    private String dockerImage;

    @Column(name = USER_ID_COLUMN_NAME)
    private String userId;

    @ElementCollection
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @MapKeyColumn(name = REQUIREMENTS_KEY_COLUMN_NAME)
    @Column(name = REQUIREMENTS_COLUMN_NAME)
    private Map<String, String> requirements;

    public Specification(String dockerImage, String userId) {
        this.dockerImage = dockerImage;
        this.userId = userId;
        this.requirements = new HashMap<>();
    }

    public void addRequirement(String key, String value) {
        this.requirements.put(key, value);
    }

    public String getRequirementValue(String key) {
        return this.requirements.get(key);
    }

    private void putAllRequirements(Map<String, String> requirements) {
        for (Map.Entry<String, String> e : requirements.entrySet()) {
            this.requirements.put(e.getKey(), e.getValue());
        }
    }

    private Map<String, String> getAllRequirements() {
        return this.requirements;
    }

    private String getDockerImage() {
        return this.dockerImage;
    }

    public String getUserId() {
        return this.userId;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(" Image: ").append(this.dockerImage);

        if ((this.requirements != null) && !this.requirements.isEmpty()) {
            sb.append(LN).append("Requirements:{");
            for (Map.Entry<String, String> entry : this.requirements.entrySet()) {
                sb.append(LN)
                        .append("\t")
                        .append(entry.getKey())
                        .append(": ")
                        .append(entry.getValue());
            }
            sb.append(LN).append("}");
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((dockerImage == null) ? 0 : dockerImage.hashCode());
        result = prime * result + ((requirements == null) ? 0 : requirements.hashCode());
        result = prime * result + ((userId == null) ? 0 : userId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Specification other = (Specification) obj;
        if (dockerImage == null) {
            if (other.dockerImage != null) return false;
        } else if (!dockerImage.equals(other.dockerImage)) return false;
        if (requirements == null) {
            if (other.requirements != null) return false;
        } else if (!requirements.equals(other.requirements)) return false;
        if (userId == null) {
            return other.userId == null;
        } else return userId.equals(other.userId);
    }

    public long getId() {
        return id;
    }
}
