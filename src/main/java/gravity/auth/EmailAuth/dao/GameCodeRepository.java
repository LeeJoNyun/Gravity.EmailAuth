package gravity.auth.EmailAuth.dao;

import gravity.auth.EmailAuth.entity.GameCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GameCodeRepository extends JpaRepository<GameCode, String> {
    Optional<GameCode> findByGameCode(String gameCode);
}
