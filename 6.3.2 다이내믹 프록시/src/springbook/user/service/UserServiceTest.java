package springbook.user.service;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Matchers.*;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;

import springbook.user.domain.Level;
import springbook.user.domain.User;
import springbook.user.dao.UserDao;
import static springbook.user.service.UserServiceImpl.MIN_LOGCOUNT_FOR_SILVER;
import static springbook.user.service.UserServiceImpl.MIN_RECOMMEND_FOR_GOLD;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/test-applicationContext.xml")
public class UserServiceTest {

	@Autowired
	UserService userService;
	@Autowired
	UserServiceImpl userServiceImpl;
	@Autowired
	UserDao userDao;
	@Autowired
	DataSource dataSource;
	@Autowired
	PlatformTransactionManager transactionManager;
	@Autowired
	MailSender mailSender;

	List<User> users;

	@Before
	public void setUp() {
		users = Arrays
				.asList(
				// 배열을 리스트로 만들어 주는 편리한 메소드
				new User("bumjin", "박범진", "p1", Level.BASIC,
						MIN_LOGCOUNT_FOR_SILVER - 1, 0, "bumjin@springbook.com"),
						new User("joytouch", "강명성", "p2", Level.BASIC,
								MIN_LOGCOUNT_FOR_SILVER, 0,
								"joytouch@springbook.com"), new User("erwins",
								"신승한", "p3", Level.SILVER, 60,
								MIN_RECOMMEND_FOR_GOLD - 1,
								"erwins@springbook.com"), new User("madnite1",
								"이상호", "P4", Level.SILVER, 60,
								MIN_RECOMMEND_FOR_GOLD,
								"madnite1@springbook.com"), new User("green",
								"오민규", "p5", Level.GOLD, 100,
								Integer.MAX_VALUE, "green@springbook.com"));
	}

	@Test
	public void bean() {
		assertThat(this.userService, is(notNullValue()));

	}

	@Test
	@DirtiesContext // 컨텍스트의 DI설정을 변경하는 테스트라는 것을 알려준다.
	public void upgradeLevels() throws Exception {
		// 고립된 테스트에서는 테스트 대상 오브젝트를 직접 생성하면 된다.
		UserServiceImpl userServiceImpl = new UserServiceImpl();
		 
		//목 오브젝트로 만든 UserDao를 직접 DI 해준다.
		MockUserDao mockUserDao = new MockUserDao(this.users);
		userServiceImpl.setUserDao(mockUserDao);
		
		//메일 발송 결과를 테스트할 수 있도록 목 오브젝트를 만들어 userServiceImpl의 의존 오브젝트로 주입한다.
		MocMailSender mocMailSender = new MocMailSender();
		userServiceImpl.setMailSender(mocMailSender);
		
		// 테스트 대상 실행
		userServiceImpl.upgradeLevels();
		
		List<User> updated = mockUserDao.getUpdated(); // MockUserDao로 부터 업데이트 결과를 가져온다.
	    assertThat(updated.size(), is(2));
	    checkUserAndLevel(updated.get(0), "joytouch",Level.SILVER);
	    checkUserAndLevel(updated.get(1), "madnite1",Level.GOLD);
	    
		
		//목 오브젝트에 저장된 메일 수신자 목록을 가져와
		//업그레이드 대상과 일치하는 지 확인 한다.
		List<String> request = mocMailSender.getRequests();
		assertThat(request.size(), is(2));
		assertThat(request.get(0),is(users.get(1).getEmail()));
		assertThat(request.get(1),is(users.get(3).getEmail()));

	}

	@Test
	public void mockUpgradLevels() throws Exception {
		UserServiceImpl userServiceImpl = new UserServiceImpl();
		
		UserDao mockUserDao = mock(UserDao.class);
		when(mockUserDao.getAll()).thenReturn(this.users);
		userServiceImpl.setUserDao(mockUserDao);
		
		MailSender mockMailSender = mock(MailSender.class);
		userServiceImpl.setMailSender(mockMailSender);
		
		userServiceImpl.upgradeLevels();
		
		verify(mockUserDao, times(2)).update(any(User.class));
		verify(mockUserDao, times(2)).update(any(User.class));
		verify(mockUserDao).update(users.get(1));
		assertThat(users.get(1).getLevel(), is(Level.SILVER));
		verify(mockUserDao).update(users.get(3));
		assertThat(users.get(3).getLevel(), is(Level.GOLD));
		
		ArgumentCaptor<SimpleMailMessage> mailMessageArg =
				ArgumentCaptor.forClass(SimpleMailMessage.class);
		
		verify(mockMailSender, times(2)).send(mailMessageArg.capture());
		List<SimpleMailMessage> mailMessages = mailMessageArg.getAllValues();
		assertThat(mailMessages.get(0).getTo()[0], is(users.get(1).getEmail()));
		assertThat(mailMessages.get(1).getTo()[0], is(users.get(3).getEmail()));
	}
	
	
	private void checkUserAndLevel(User updated, String expectedId, Level expectedLevel) {
		// TODO Auto-generated method stub
		assertThat(updated.getId(),is(expectedId));
		assertThat(updated.getLevel(), is(expectedLevel));
	}

	private void checkLevelUpgraded(User user, boolean upgraded) {
		// TODO Auto-generated method stub
		User userUpdate = userDao.get(user.getId());

		if (upgraded) {
			// 업그레이드가 일어났는지 확인
			assertThat(userUpdate.getLevel(), is(user.getLevel().nextLevel()));
		} else {
			// 업그레이드가 일어나지 않았는지 확인
			assertThat(userUpdate.getLevel(), is(user.getLevel()));
		}
	}

	@Test
	public void add() {
		userDao.deleteAll();

		User userWithLevel = users.get(4); // 이미 지정된 레벨을 가지고 있는 유저
		User userWithoutLevel = users.get(0);
		userWithoutLevel.setLevel(null);

		userService.add(userWithLevel);
		userService.add(userWithoutLevel);

		User userWithLevelRead = userDao.get(userWithLevel.getId());
		User userWithoutLevelRead = userDao.get(userWithoutLevel.getId());

		assertThat(userWithLevelRead.getLevel(), is(userWithLevel.getLevel()));
		assertThat(userWithoutLevelRead.getLevel(), is(Level.BASIC));

	}

	private void checkLevel(User user, Level expectedLevel) {
		User userUpdate = userDao.get(user.getId());
		assertThat(userUpdate.getLevel(), is(expectedLevel));

	}

	// 오브젝트의 수동 DI
	@Test
	public void upgradeAllorNothing() throws Exception {
		
		UserServiceImpl testUserService = new TestUserService(users.get(3).getId());
		testUserService.setUserDao(userDao); // 스프링이 만들어준 UserDao오브젝트를 DI해준다.
		// testUserService 오브젝트는 스프링 설정에 의해 정의된 userService 빈과 동일하게 UserDao를
		// 사용해 데이터 액세스 기능을 이용할 수 있다.
		testUserService.setMailSender(mailSender);
		// 트랜잭션 기능을 분리한 UserServiceTx는 예외 발생용으로 수정할 필요가 없으니 그대로 사용한다.
		UserServiceTx txUserService = new UserServiceTx();
		txUserService.setTransactionManager(transactionManager);
		txUserService.setUserService(testUserService);
		

		userDao.deleteAll();
		for (User user : users) {
			userDao.add(user);
		}
		try {
			txUserService.upgradeLevels();
			fail("TestUserServiceException expected");

		} catch (TestUserServiceException e) {
			// TODO: handle exception

		}
		checkLevelUpgraded(users.get(1), false);
	}

	static class TestUserService extends UserServiceImpl {
		private String id;

		private TestUserService(String id) {
			this.id = id;
		}

		protected void upgradeLevel(User user) {
			if (user.getId().equals(this.id))
				throw new TestUserServiceException();
			super.upgradeLevel(user);
		}
	}

	static class TestUserServiceException extends RuntimeException {

	}
	
	static class MocMailSender implements MailSender{
		private List<String> requests = new ArrayList<String>();

		public List<String> getRequests() {
			return requests;
		}
		

		@Override
		public void send(SimpleMailMessage mailMessage) throws MailException {
			// TODO Auto-generated method stub
			requests.add(mailMessage.getTo()[0]);
		}

		@Override
		public void send(SimpleMailMessage[] mailMessage) throws MailException {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	static class MockUserDao implements UserDao {
		private List<User> users;
		private List<User> updated = new ArrayList();

		private MockUserDao(List<User> users) {
			this.users = users;
		}

		public List<User> getUpdated() {
			return this.updated;
		}

		// 스텁기능 제공
		@Override
		public List<User> getAll() {
			// TODO Auto-generated method stub
			return this.users;
		}

		// 목 오브젝트 기능 제공
		@Override
		public void update(User user) {
			// TODO Auto-generated method stub
			updated.add(user);
		}

		@Override
		public void add(User user) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException();
		}

		@Override
		public User get(String id) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException();
		}

		@Override
		public void deleteAll() {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException();
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException();
		}

	}
}
