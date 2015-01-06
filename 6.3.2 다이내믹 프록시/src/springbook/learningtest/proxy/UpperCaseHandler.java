package springbook.learningtest.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class UpperCaseHandler implements InvocationHandler {

	Object target;
	
	// ���̳��� ���Ͻ÷κ��� ���޹��� ��û�� �ٽ� Ÿ�� ������Ʈ�� �����ϱ� ���� 
	// Ÿ�� ������Ʈ�� ���Թ޾� �д�.
	public UpperCaseHandler(Object target) {
		super();
		this.target = target;
	}


	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		// TODO Auto-generated method stub
		
		Object ret = (String)method.invoke(target, args);// Ÿ������ ����. �������̽��� �޼ҵ� ȣ�⿡ ��� ����ȴ�.
		if(ret instanceof String && method.getName().startsWith("say")){
			return ((String)ret).toUpperCase();// �빮�ڷ� ��ȯ�ϴ� �ΰ� ����۾� �� ����
		}else{
			return ret;
		}
	}

}
