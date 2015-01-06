package springbook.learningtest.proxy;

import java.lang.reflect.Proxy;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

public class SimpleProxy {
	@Test
	public void simpleProxy(){
		Hello hello = new HelloTarget(); // 타깃은 인터페이스를 통해서 접근하는 습관을 들이자.
		assertThat(hello.sayHello("Toby"),is("Hello Toby"));
		assertThat(hello.sayHi("Toby"),is("Hi Toby"));
		assertThat(hello.sayThankYou("Toby"),is("Thank You Toby"));
	}
	
	@Test
	public void helloUppercase(){
		Hello proxiedHello = new HelloUppercase(new HelloTarget()); // 프록시를 통해 타깃 오브젝트에 접근하도록 구성한다.
		assertThat(proxiedHello.sayHello("Toby"),is("HELLO TOBY"));
		assertThat(proxiedHello.sayHi("Toby"),is("HI TOBY"));
		assertThat(proxiedHello.sayThankYou("Toby"),is("THANK YOU TOBY"));
	
	}
	
	@Test
	public void helloUppercaseWithInvoke(){
		//다이내믹 프록시를 생성하는 방법
		Hello proxiedHello = (Hello)Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{Hello.class}, new UpperCaseHandler(new HelloTarget()));
	
		assertThat(proxiedHello.sayHello("Toby"),is("HELLO TOBY"));
		assertThat(proxiedHello.sayHi("Toby"),is("HI TOBY"));
		assertThat(proxiedHello.sayThankYou("Toby"),is("THANK YOU TOBY"));
	}
}
