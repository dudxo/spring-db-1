package hello.jdbc.repository;

import java.sql.SQLException;

import hello.jdbc.domain.Member;

public interface MemberRepositoryEx {

	Member save(Member member) throws SQLException;  // Interface 에서 JDBC(특정 기술)에 종속됨..!!!

	Member findById(String memberId) throws SQLException;

	void update(String memberId, int money) throws SQLException;

	void delete(String memberId) throws SQLException;
}
