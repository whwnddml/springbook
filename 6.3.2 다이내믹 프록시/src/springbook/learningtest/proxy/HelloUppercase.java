package springbook.learningtest.proxy;

public class HelloUppercase implements Hello {
	Hello hello; // ������ Ÿ�� ������Ʈ, ���⼭�� Ÿ�� Ŭ������ ������Ʈ�� ���� ������
	//�ٸ� ���Ͻø� �߰��� ���� �����Ƿ� �������̽��� �����Ѵ�.
	
	
	public HelloUppercase(Hello hello) {
		super();
		this.hello = hello;
	}

	@Override
	public String sayHello(String name) {
		// TODO Auto-generated method stub
		return hello.sayHello(name).toUpperCase(); // ���Ӱ� �ΰ���� ����
	}

	@Override
	public String sayHi(String name) {
		// TODO Auto-generated method stub
		return hello.sayHi(name).toUpperCase();
	}

	@Override
	public String sayThankYou(String name) {
		// TODO Auto-generated method stub
		return hello.sayThankYou(name).toUpperCase();
	}

}
