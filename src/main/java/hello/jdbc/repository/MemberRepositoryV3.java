package hello.jdbc.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.NoSuchElementException;

import javax.sql.DataSource;

import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;

import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;

/**
 * 트랜잭션 - 트랜잭션 매니저
 * DataSourceUtils.getConnection()
 * DataSourceUtils.releaseConnection()
 */
@Slf4j
public class MemberRepositoryV3 {

	private final DataSource dataSource;

	public MemberRepositoryV3(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public Member save(Member member) throws SQLException {
		// ? 바인딩 방식을 통해 SQL Injection 을 방지할 수 있음
		String sql = "insert into member(member_id, money) values (?, ?)";

		Connection con = null;
		PreparedStatement pstmt = null; // DB에 Query를 날리는 객체

		try {
			con = getConnection();
			pstmt = con.prepareStatement(sql);

			// parameter bainding
			pstmt.setString(1, member.getMemberId());
			pstmt.setInt(2, member.getMoney());

			// data update -> executeUpdate();
			pstmt.executeUpdate();

			return member;
		} catch (SQLException e) {
			log.error("db error", e);
			throw e;
		} finally {
			// 역순 종료(커낵션 TCP 연결 종료)
			// pstmt.close();
			// con.close();
			close(con, pstmt, null);
		}
	}

	public Member findById(String memberId) throws SQLException {
		String sql = "select * from member where member_id = ?";

		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {
			con = getConnection();
			pstmt = con.prepareStatement(sql);

			pstmt.setString(1, memberId);
			// select -> executeQuery();
			rs = pstmt.executeQuery();

			// rs.next()를 해야 실제 있는 데이터부터 시작
			if (rs.next()) {
				Member member = new Member();
				member.setMemberId(rs.getString("member_id"));
				member.setMoney(rs.getInt("money"));
				return member;
			} else {
				throw new NoSuchElementException("member not found memberId = " + memberId);
			}

		} catch (SQLException e) {
			log.error("db error", e);
			throw e;
		} finally {
			close(con, pstmt, rs);
		}
	}

	public void update(String memberId, int money) throws SQLException {
		String sql = "update member set money = ? where member_id=?";

		Connection con = null;
		PreparedStatement pstmt = null;

		try {
			con = getConnection();
			pstmt = con.prepareStatement(sql);

			// parameter bainding
			pstmt.setInt(1, money);
			pstmt.setString(2, memberId);

			// data update -> executeUpdate();
			int resultSize = pstmt.executeUpdate();
			log.info("resultSize={}", resultSize);
		} catch (SQLException e) {
			log.error("db error", e);
			throw e;
		} finally {
			close(con, pstmt, null);
		}
	}

	public void delete(String memberId) throws SQLException {
		String sql = "delete from member where member_id = ?";

		Connection con = null;
		PreparedStatement pstmt = null;

		try {
			con = getConnection();
			pstmt = con.prepareStatement(sql);

			// parameter bainding
			pstmt.setString(1, memberId);

			// data update -> executeUpdate();
			pstmt.executeUpdate();
		} catch (SQLException e) {
			log.error("db error", e);
			throw e;
		} finally {
			close(con, pstmt, null);
		}
	}

	private void close(Connection con, Statement stmt, ResultSet rs) {
		JdbcUtils.closeResultSet(rs);
		JdbcUtils.closeStatement(stmt);
		//주의! 트랜잭션 동기화를 사용하려면 DataSourceUtils를 사용해야 한다.
		DataSourceUtils.releaseConnection(con, dataSource);
		// JdbcUtils.closeConnection(con);
	}

	private Connection getConnection() throws SQLException {
		//주의! 트랜잭션 동기화를 사용하라면 DataSourceUtils를 사용해야 한다.
		Connection con = DataSourceUtils.getConnection(dataSource);
		log.info("get connection={}, class={}", con, con.getClass());
		return con;
	}
}
