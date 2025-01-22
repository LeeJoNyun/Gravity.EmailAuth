package gravity.auth.EmailAuth.dao;

import gravity.auth.EmailAuth.entity.EmailList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmailListRepository extends JpaRepository<EmailList, String> {
    List<EmailList> findByToken(String token);
}
