package springbook.user.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import springbook.user.domain.Level;
import springbook.user.domain.User;

public class UserDaoJdbc implements UserDao {

	// 스프링에서 기본으로 제공하는 jdbcTemplate 를 사용한다.
	private JdbcTemplate jdbcTemplate;

	private RowMapper<User> userMapper = new RowMapper<User>() {
		public User mapRow(ResultSet rs, int rowNum) throws SQLException {
			User user = new User();
			user.setId(rs.getString("id"));
			user.setName(rs.getString("name"));
			user.setPassword(rs.getString("password"));
			user.setLevel(Level.valueOf(rs.getInt("level")));
			user.setLogin(rs.getInt("login"));
			user.setRecommend(rs.getInt("recommend"));
			user.setEmail(rs.getString("email"));
			

			return user;
		}
	};

	// 수정자 메소드 이면서 JdbcContext에 대한 생성, 주입작업을 동시에 수행한다.
	public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);// jdbcTemplate 생성(Ioc)
	}

	public void add(final User user) {

		this.jdbcTemplate.update(
				"insert into users(id,name,password,level,login,recommend,email) values(?,?,?,?,?,?,?)",
				user.getId(), user.getName(), user.getPassword(), user.getLevel().initValue(), user.getLogin(), user.getRecommend(), user.getEmail() );

	}

	public User get(String id) {
		return this.jdbcTemplate.queryForObject(
				"select * from users where id=?", new Object[] { id },
				this.userMapper);
	}

	public List<User> getAll() {
		return this.jdbcTemplate.query("select * from users order by id",this.userMapper);
	}

	// 추가된 정보를 모두 지우기 위함.
	// 클라이언트 책임을 담당할 deleteAll() 메소드
	public void deleteAll() {
		this.jdbcTemplate.update("delete from users");

	}

	public int getCount() {
		return this.jdbcTemplate.queryForInt("select count(*) from users");
	}

	@Override
	public void update(User user) {
		// TODO Auto-generated method stub
		this.jdbcTemplate.update("update users set name = ? , password=?,level=?,login=?,recommend=?, email=? "
				+"where id=?", user.getName(), user.getPassword(), user.getLevel().initValue(), user.getLogin(), user.getRecommend(), user.getEmail(), user.getId());
		
		
	}
	
	

}
