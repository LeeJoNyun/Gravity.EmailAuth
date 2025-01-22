package gravity.auth.EmailAuth.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;
import java.util.UUID;

@Entity
@Data
@Table(name = "tb_game_code_list")
public class GameCode {
    @Id
    @Column(name = "Id")
    private String id = UUID.randomUUID().toString();

    @Column(name = "GameCode", nullable = false)
    private String gameCode;

    @Column(name = "SecretKey", nullable = false)
    private String secretKey;

    @Column(name = "Regdate")
    private Date regDate;
}
