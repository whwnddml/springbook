package springbook.user.service;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import springbook.user.domain.User;

public class UserServiceTx implements UserService {
	UserService userService;
	PlatformTransactionManager transactionManager;
	
	public void setTransactionManager(PlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	// UserService 를 구현한 다른 오브젝트를 DI받는다.
	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	//DI받은 UserService 오브젝트에 모든 기능을 위임한다.
	@Override
	public void add(User user) {
		// TODO Auto-generated method stub
		userService.add(user);
	}

	@Override
	public void upgradeLevels()  {
		// TODO Auto-generated method stub
		TransactionStatus status = this.transactionManager.getTransaction(new DefaultTransactionDefinition());
		try {
			userService.upgradeLevels();
			
			this.transactionManager.commit(status);
		} catch (RuntimeException e) {
			// TODO: handle exception
			this.transactionManager.rollback(status);
			throw e;
		}
		
	}

}
