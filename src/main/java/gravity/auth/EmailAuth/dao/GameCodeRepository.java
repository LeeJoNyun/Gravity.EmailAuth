package gravity.auth.EmailAuth.dao;

import gravity.auth.EmailAuth.entity.GameCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GameCodeRepository extends JpaRepository<GameCode, String> {
    Optional<GameCode> findByGameCode(String gameCode);
}
