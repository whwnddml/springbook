package springbook.user.service;

import springbook.user.dao.UserDao;
import springbook.user.domain.Level;
import springbook.user.domain.User;

public class DefaultUserServiceUpgrade implements UserServiceUpgradePolicy {
	public static final int MIN_LOGCOUNT_FOR_SILVER = 40;
	public static final int MIN_RECOMMEND_FOR_GOLD = 20;
	
	UserDao userDao;
	
	@Override
	public boolean canUpgradeLevel(User user) {
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

	@Override
	public void upgradeLevel(User user) {
		// TODO Auto-generated method stub
		user.upgradeLevel();
		userDao.update(user);
	}

}
