package hello.jdbc.repository;

import static hello.jdbc.connection.ConnectionConst.*;

import java.sql.SQLException;
import java.util.NoSuchElementException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.zaxxer.hikari.HikariDataSource;

import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class MemberRepositoryV1Test {

	MemberRepositoryV1 repository;

	@BeforeEach
	void beforeEach() {
		//기본 DriverManager - 항상 새로운 커넥션을 획득
		// DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);

		//커낵션 풀링
		HikariDataSource dataSource = new HikariDataSource();
		dataSource.setJdbcUrl(URL);
		dataSource.setUsername(USERNAME);
		dataSource.setPassword(PASSWORD);

		repository = new MemberRepositoryV1(dataSource);
	}

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
		repository.update(member.getMemberId(), 20000);
		Member updateMember = repository.findById(member.getMemberId());
		Assertions.assertThat(updateMember.getMoney()).isEqualTo(20000);

		//deleteById
		repository.delete(member.getMemberId());
		Assertions.assertThatThrownBy(() -> repository.findById(member.getMemberId()))
			.isInstanceOf(NoSuchElementException.class);

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}