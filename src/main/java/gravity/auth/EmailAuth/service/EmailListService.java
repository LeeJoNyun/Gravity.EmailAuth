package gravity.auth.EmailAuth.service;

import gravity.auth.EmailAuth.dao.EmailListRepository;
import gravity.auth.EmailAuth.dto.EmailInputModel;
import gravity.auth.EmailAuth.entity.EmailList;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.StoredProcedureQuery;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmailListService {
    private final EmailListRepository _repo;
    private final EntityManager _em;

    public List<EmailList> getEmailList(String token){
        return _repo.findByToken(token);
    }

    @Transactional
    public int sendEmail(EmailInputModel input) {
        // StoredProcedureQuery 생성
        StoredProcedureQuery query = _em.createStoredProcedureQuery("[dbo].[usp_send_email]");

        // 매개변수 설정 (입력 매개변수)
        query.registerStoredProcedureParameter("@pEmail", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("@pGameCode", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("@pToken", String.class, ParameterMode.IN);

        query.setParameter("@pEmail", input.email);
        query.setParameter("@pGameCode", input.gameCode);
        query.setParameter("@pToken", input.token);

        // 출력 매개변수 설정 (출력 값)
        query.registerStoredProcedureParameter("@oResult", Integer.class, ParameterMode.OUT);

        // 저장 프로시저 실행
        query.execute();

        // 출력 매개변수 값 가져오기
        Integer returnValue = (Integer) query.getOutputParameterValue("@oResult");

        return returnValue != null ? returnValue : 0;
    }

}
