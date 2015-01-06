package springbook.learningtest.proxy;

public class HelloUppercase implements Hello {
	Hello hello; // 위임할 타겟 오브젝트, 여기서는 타깃 클래스의 오브젝트인 것은 알지만
	//다른 프록시를 추가할 수도 있으므로 인터페이스로 접근한다.
	
	
	public HelloUppercase(Hello hello) {
		super();
		this.hello = hello;
	}

	@Override
	public String sayHello(String name) {
		// TODO Auto-generated method stub
		return hello.sayHello(name).toUpperCase(); // 위임과 부가기능 적용
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
