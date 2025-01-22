package gravity.auth.EmailAuth.entity;

import jakarta.persistence.*;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;

import java.util.UUID;

@Entity
@Data
@Table(name = "tb_verify_email_list")
public class EmailList {
    @Id
    @Column(name = "Id")
    public String id = UUID.randomUUID().toString();

    @Column(name = "Token", nullable = false)
    public String token;

    @Column(name = "Email", nullable = false)
    public String email;

}
