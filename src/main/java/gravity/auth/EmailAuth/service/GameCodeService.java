package gravity.auth.EmailAuth.service;

import gravity.auth.EmailAuth.entity.GameCode;
import gravity.auth.EmailAuth.dao.GameCodeRepository;
import jakarta.persistence.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GameCodeService {

    private final GameCodeRepository _repo;
//    @PersistenceContext
    private final EntityManager _em;

    public String getSercretKey(String gameCode){
        return _repo.findByGameCode(gameCode)
                .map(GameCode::getSecretKey)
                .orElseThrow( () -> new RuntimeException("GameCode not found"));
    }

    @Transactional
    public int setEmailAuth(String token) {
        // StoredProcedureQuery 생성
        StoredProcedureQuery query = _em.createStoredProcedureQuery("dbo.usp_auth_email_set");

        // 매개변수 설정 (입력 매개변수)
        query.registerStoredProcedureParameter("pToken", String.class, ParameterMode.IN);
        query.setParameter("pToken", token);

        // 출력 매개변수 설정 (출력 값)
        query.registerStoredProcedureParameter("oResult", Integer.class, ParameterMode.OUT);

        // 저장 프로시저 실행
        query.execute();

        // 출력 매개변수 값 가져오기
        Integer returnValue = (Integer) query.getOutputParameterValue("oResult");

        return returnValue != null ? returnValue : 0;
    }
}
