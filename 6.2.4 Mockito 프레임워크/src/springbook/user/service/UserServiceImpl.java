package springbook.user.service;

import java.util.List;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.sql.DataSource;

import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import springbook.user.dao.UserDao;
import springbook.user.domain.Level;
import springbook.user.domain.User;

public class UserServiceImpl implements UserService {
	public static final int MIN_LOGCOUNT_FOR_SILVER = 50;
	public static final int MIN_RECOMMEND_FOR_GOLD = 30;

	private DataSource dataSource;
	MailSender mailSender;
	UserDao userDao;
	
	public void setMailSender(MailSender mailSender) {
		this.mailSender = mailSender;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public UserDao getUserDao() {
		return userDao;
	}

	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	//
	// 트랜잭션 분리를 위해 내부 메소드를 사용한다.
	public void upgradeLevels() {

		// 비즈니스로직을 내부 메소드로 분리했다.
		upgradeLevelsInternal();

	}

	private void upgradeLevelsInternal() {
		// 사용자 정보를 모두 가져온다.
		List<User> users = userDao.getAll();

		for (User user : users) {
			if (canUpgradeLevel(user))
				upgradeLevel(user);
		}

	}

	protected void upgradeLevel(User user) {
		// TODO Auto-generated method stub
		user.upgradeLevel();
		userDao.update(user);
		sendUpgradeEMail(user); // user를 이용해 메일을 발송하는 메소드를 호출

	}

	// 스프링이 제공하는 JavaMailSender 구현한 클래스를 사용해서 메일을 발송하는 메소드를 작성한다.
	private void sendUpgradeEMail(User user) {
		SimpleMailMessage mailMessage = new SimpleMailMessage();
		mailMessage.setTo(user.getEmail());
		mailMessage.setFrom("useradmin@ksug.org");
		mailMessage.setSubject("Upgrade 안내");
		mailMessage.setText("사용자님의 등깁이 " + user.getLevel().name());

		this.mailSender.send(mailMessage);

	}

	private boolean canUpgradeLevel(User user) {
		// TODO Auto-generated method stub
		Level currentLevel = user.getLevel();

		switch (currentLevel) {
		case BASIC:
			return (user.getLogin() >= 50);
		case SILVER:
			return (user.getRecommend() >= 30);
		case GOLD:
			return false;
		default:
			throw new IllegalArgumentException("Unknown Level:" + currentLevel);
		}
	}

	public void add(User user) {
		// TODO Auto-generated method stub
		if (user.getLevel() == null)
			user.setLevel(Level.BASIC);
		userDao.add(user);

	}


}
