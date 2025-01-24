package gravity.auth.EmailAuth.dao;

import gravity.auth.EmailAuth.entity.EmailList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmailListRepository extends JpaRepository<EmailList, String> {
    List<EmailList> findByToken(String token);
}
