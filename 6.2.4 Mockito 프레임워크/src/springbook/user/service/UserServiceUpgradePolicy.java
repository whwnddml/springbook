package springbook.user.service;

import springbook.user.domain.User;

public interface UserServiceUpgradePolicy {
	boolean canUpgradeLevel(User user);
	void upgradeLevel(User user);
}
