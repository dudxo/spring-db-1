package hello.jdbc.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.SQLException;
import java.util.NoSuchElementException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class MemberRepositoryV0Test {

	MemberRepositoryV0 repository = new MemberRepositoryV0();

	@Test
	void crud() throws SQLException {
		// save
		Member member = new Member("memberV7", 10000);
		repository.save(member);

		//findById
		Member findMember = repository.findById(member.getMemberId());
		log.info("findMember={}", findMember);
		Assertions.assertThat(findMember).isEqualTo(member);

		//updateById
		repository.updateById(member.getMemberId(), 20000);
		Member updateMember = repository.findById(member.getMemberId());
		Assertions.assertThat(updateMember.getMoney()).isEqualTo(20000);

		//deleteById
		repository.deleteById(member.getMemberId());
		Assertions.assertThatThrownBy(() -> repository.findById(member.getMemberId()))
			.isInstanceOf(NoSuchElementException.class);
	}
}