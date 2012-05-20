package net.gps.tracker.shared;

import java.io.Serializable;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.Unique;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class User implements Serializable {

    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Long ID;
    @Persistent
    @Unique
    private String name;
    @Persistent
    private String phone;
    @Persistent
    private String email;
    @Persistent
    private String status;

    public User() {
    }

    public User(String name, String phone, String email) {
        super();
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.status = "N/A";
    }

    public void setId(Long ID) {
        this.ID = ID;
    }

    public void setName(String value) {
        name = value;
    }

    public void setPhone(String value) {
        phone = value;
    }

    public void setEmail(String value) {
        email = value;
    }

    public void setStatus(String value) {
        status = value;
    }

    public Long getId() {
        return this.ID;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getStatus() {
        return status;
    }
}
