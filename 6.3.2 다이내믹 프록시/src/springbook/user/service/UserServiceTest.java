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
				// �迭�� ����Ʈ�� ����� �ִ� ���� �޼ҵ�
				new User("bumjin", "�ڹ���", "p1", Level.BASIC,
						MIN_LOGCOUNT_FOR_SILVER - 1, 0, "bumjin@springbook.com"),
						new User("joytouch", "����", "p2", Level.BASIC,
								MIN_LOGCOUNT_FOR_SILVER, 0,
								"joytouch@springbook.com"), new User("erwins",
								"�Ž���", "p3", Level.SILVER, 60,
								MIN_RECOMMEND_FOR_GOLD - 1,
								"erwins@springbook.com"), new User("madnite1",
								"�̻�ȣ", "P4", Level.SILVER, 60,
								MIN_RECOMMEND_FOR_GOLD,
								"madnite1@springbook.com"), new User("green",
								"���α�", "p5", Level.GOLD, 100,
								Integer.MAX_VALUE, "green@springbook.com"));
	}

	@Test
	public void bean() {
		assertThat(this.userService, is(notNullValue()));

	}

	@Test
	@DirtiesContext // ���ؽ�Ʈ�� DI������ �����ϴ� �׽�Ʈ��� ���� �˷��ش�.
	public void upgradeLevels() throws Exception {
		// ���� �׽�Ʈ������ �׽�Ʈ ��� ������Ʈ�� ���� �����ϸ� �ȴ�.
		UserServiceImpl userServiceImpl = new UserServiceImpl();
		 
		//�� ������Ʈ�� ���� UserDao�� ���� DI ���ش�.
		MockUserDao mockUserDao = new MockUserDao(this.users);
		userServiceImpl.setUserDao(mockUserDao);
		
		//���� �߼� ����� �׽�Ʈ�� �� �ֵ��� �� ������Ʈ�� ����� userServiceImpl�� ���� ������Ʈ�� �����Ѵ�.
		MocMailSender mocMailSender = new MocMailSender();
		userServiceImpl.setMailSender(mocMailSender);
		
		// �׽�Ʈ ��� ����
		userServiceImpl.upgradeLevels();
		
		List<User> updated = mockUserDao.getUpdated(); // MockUserDao�� ���� ������Ʈ ����� �����´�.
	    assertThat(updated.size(), is(2));
	    checkUserAndLevel(updated.get(0), "joytouch",Level.SILVER);
	    checkUserAndLevel(updated.get(1), "madnite1",Level.GOLD);
	    
		
		//�� ������Ʈ�� ����� ���� ������ ����� ������
		//���׷��̵� ���� ��ġ�ϴ� �� Ȯ�� �Ѵ�.
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
			// ���׷��̵尡 �Ͼ���� Ȯ��
			assertThat(userUpdate.getLevel(), is(user.getLevel().nextLevel()));
		} else {
			// ���׷��̵尡 �Ͼ�� �ʾҴ��� Ȯ��
			assertThat(userUpdate.getLevel(), is(user.getLevel()));
		}
	}

	@Test
	public void add() {
		userDao.deleteAll();

		User userWithLevel = users.get(4); // �̹� ������ ������ ������ �ִ� ����
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

	// ������Ʈ�� ���� DI
	@Test
	public void upgradeAllorNothing() throws Exception {
		
		UserServiceImpl testUserService = new TestUserService(users.get(3).getId());
		testUserService.setUserDao(userDao); // �������� ������� UserDao������Ʈ�� DI���ش�.
		// testUserService ������Ʈ�� ������ ������ ���� ���ǵ� userService ��� �����ϰ� UserDao��
		// ����� ������ �׼��� ����� �̿��� �� �ִ�.
		testUserService.setMailSender(mailSender);
		// Ʈ����� ����� �и��� UserServiceTx�� ���� �߻������� ������ �ʿ䰡 ������ �״�� ����Ѵ�.
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

		// ���ӱ�� ����
		@Override
		public List<User> getAll() {
			// TODO Auto-generated method stub
			return this.users;
		}

		// �� ������Ʈ ��� ����
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
