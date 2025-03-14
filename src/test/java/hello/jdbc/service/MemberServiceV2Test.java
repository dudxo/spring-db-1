package hello.jdbc.service;

import static hello.jdbc.connection.ConnectionConst.*;

import java.sql.SQLException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV2;

/**
 * 트랜잭션 - 커낵션 파라미터 전달 방식 동기화
 */
public class MemberServiceV2Test {

	public static final String MEMBER_A = "memberA";
	public static final String MEMBER_B = "memberB";
	public static final String MEMBER_EX = "ex";

	private MemberRepositoryV2 memberRepository;
	private MemberServiceV2 memberService;

	@BeforeEach
	void before() {
		DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
		memberRepository = new MemberRepositoryV2(dataSource);
		memberService = new MemberServiceV2(memberRepository, dataSource);
	}

	@AfterEach
	void after() throws SQLException {
		memberRepository.delete(MEMBER_A);
		memberRepository.delete(MEMBER_B);
		memberRepository.delete(MEMBER_EX);
	}

	@DisplayName("정상 이체")
	@Test
	void accountTransfer() throws SQLException {
		// given
		Member memberA = new Member(MEMBER_A, 10000);
		Member memberB = new Member(MEMBER_B, 10000);
		memberRepository.save(memberA);
		memberRepository.save(memberB);

		// when
		memberService.accountTransfer(memberA.getMemberId(), memberB.getMemberId(), 2000);

		// then
		Member findMemberA = memberRepository.findById(memberA.getMemberId());
		Member findMemberB = memberRepository.findById(memberB.getMemberId());
		Assertions.assertThat(findMemberA.getMoney()).isEqualTo(8000);
		Assertions.assertThat(findMemberB.getMoney()).isEqualTo(12000);
	}

	@DisplayName("이체 중 예외 발생")
	@Test
	void accountTransferEx() throws SQLException {
		// given
		Member memberA = new Member(MEMBER_A, 10000);
		Member memberEx = new Member(MEMBER_EX, 10000);
		memberRepository.save(memberA);
		memberRepository.save(memberEx);

		// when
		Assertions.assertThatThrownBy(
			() -> memberService.accountTransfer(memberA.getMemberId(), memberEx.getMemberId(), 2000)
		).isInstanceOf(IllegalStateException.class);

		// then
		Member findMemberA = memberRepository.findById(memberA.getMemberId());
		Member findMemberEx = memberRepository.findById(memberEx.getMemberId());
		Assertions.assertThat(findMemberA.getMoney()).isEqualTo(10000);
		Assertions.assertThat(findMemberEx.getMoney()).isEqualTo(10000);
	}

}
