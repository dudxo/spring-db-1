package hello.jdbc.service;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV2;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 트랜잭션 - 파라미터 연동, 풀을 고려한 종료
 */
@RequiredArgsConstructor
@Slf4j
public class MemberServiceV2 {

	private final MemberRepositoryV2 memberRepository;
	private final DataSource dataSource;

	public void accountTransfer(String fromId, String toId, int money) throws SQLException {

		Connection con = dataSource.getConnection();
		try {
			con.setAutoCommit(false); // 트랜잭션 시작
			//비즈니스 로직
			bizLogic(con, fromId, toId, money);
			con.commit();
		} catch (Exception e) {
			log.info("rollback ST");
			con.rollback();
			log.info("rollback ED");
			throw new IllegalStateException(e);
		} finally {
			if (con != null) {
				try {
					con.setAutoCommit(true);    // 커넥션 풀은 항상 autocommit == true이기 때문에 반환 전 다시 원복
					con.close();
				} catch (Exception e) {
					log.info("error", e);
				}
			}
		}

	}

	private void bizLogic(Connection con, String fromId, String toId, int money) throws SQLException {
		//시작
		Member fromMember = memberRepository.findById(con, fromId);
		Member toMember = memberRepository.findById(con, toId);

		memberRepository.update(con, fromId, fromMember.getMoney() - money);
		validation(toMember);
		memberRepository.update(con, toId, toMember.getMoney() + money);
	}

	private void validation(Member toMember) {
		if (toMember.getMemberId().equals("ex")) {
			throw new IllegalStateException("이체중 예외 발생");
		}
	}
}
