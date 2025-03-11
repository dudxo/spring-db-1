package hello.jdbc.service;

import java.sql.SQLException;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV3;
import lombok.extern.slf4j.Slf4j;

/**
 * 트랜잭션 - 트랜잭션 템플
 */
@Slf4j
public class MemberServiceV3_2 {

	private final MemberRepositoryV3 memberRepository;
	private final TransactionTemplate txTemplate;

	public MemberServiceV3_2(MemberRepositoryV3 memberRepository, PlatformTransactionManager transactionManager) {
		this.memberRepository = memberRepository;
		this.txTemplate = new TransactionTemplate(transactionManager);
	}

	public void accountTransfer(String fromId, String toId, int money) throws SQLException {
		txTemplate.executeWithoutResult((status) -> {
			try {
				bizLogic(fromId, toId, money);
				// 성공하면 커밋
			} catch (SQLException e) {
				// 언체크(런타임) 예외 터지면 txTemplate가 rollback시킨다.(체크 예외의 경우 커밋)
				throw new IllegalStateException(e);
			}
		});
	}

	private void bizLogic(String fromId, String toId, int money) throws SQLException {
		//시작
		Member fromMember = memberRepository.findById(fromId);
		Member toMember = memberRepository.findById(toId);

		memberRepository.update(fromId, fromMember.getMoney() - money);
		validation(toMember);
		memberRepository.update(toId, toMember.getMoney() + money);
	}

	private void validation(Member toMember) {
		if (toMember.getMemberId().equals("ex")) {
			throw new IllegalStateException("이체중 예외 발생");
		}
	}
}
