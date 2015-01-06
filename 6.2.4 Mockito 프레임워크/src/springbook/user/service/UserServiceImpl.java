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
	// Ʈ����� �и��� ���� ���� �޼ҵ带 ����Ѵ�.
	public void upgradeLevels() {

		// ����Ͻ������� ���� �޼ҵ�� �и��ߴ�.
		upgradeLevelsInternal();

	}

	private void upgradeLevelsInternal() {
		// ����� ������ ��� �����´�.
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
		sendUpgradeEMail(user); // user�� �̿��� ������ �߼��ϴ� �޼ҵ带 ȣ��

	}

	// �������� �����ϴ� JavaMailSender ������ Ŭ������ ����ؼ� ������ �߼��ϴ� �޼ҵ带 �ۼ��Ѵ�.
	private void sendUpgradeEMail(User user) {
		SimpleMailMessage mailMessage = new SimpleMailMessage();
		mailMessage.setTo(user.getEmail());
		mailMessage.setFrom("useradmin@ksug.org");
		mailMessage.setSubject("Upgrade �ȳ�");
		mailMessage.setText("����ڴ��� ����� " + user.getLevel().name());

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
