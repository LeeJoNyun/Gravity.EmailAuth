package gravity.auth.EmailAuth.service;

import gravity.auth.EmailAuth.dto.EmailInputModel;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.StoredProcedureQuery;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailAuthService {

    private final EntityManager _em;

    @Transactional
    public int authEmailAsync(String token) {
        // StoredProcedureQuery 생성
        StoredProcedureQuery query = _em.createStoredProcedureQuery("[dbo].[usp_auth_email_set]");

        // 매개변수 설정 (입력 매개변수)
        query.registerStoredProcedureParameter("@pToken", String.class, ParameterMode.IN);

        query.setParameter("@pToken", token);

        // 출력 매개변수 설정 (출력 값)
        query.registerStoredProcedureParameter("@oResult", Integer.class, ParameterMode.OUT);

        // 저장 프로시저 실행
        query.execute();

        // 출력 매개변수 값 가져오기
        return (Integer) query.getOutputParameterValue("@oResult");
    }

}
