package springbook.learningtest.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class UpperCaseHandler implements InvocationHandler {

	Object target;
	
	// 다이나믹 프록시로부터 전달받은 요청을 다시 타깃 오브젝트에 위임하기 위해 
	// 타깃 오브젝트를 주입받아 둔다.
	public UpperCaseHandler(Object target) {
		super();
		this.target = target;
	}


	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		// TODO Auto-generated method stub
		
		Object ret = (String)method.invoke(target, args);// 타깃으로 위임. 인터페이스의 메소드 호출에 모두 적용된다.
		if(ret instanceof String && method.getName().startsWith("say")){
			return ((String)ret).toUpperCase();// 대문자로 전환하는 부가 기능작업 후 리턴
		}else{
			return ret;
		}
	}

}
