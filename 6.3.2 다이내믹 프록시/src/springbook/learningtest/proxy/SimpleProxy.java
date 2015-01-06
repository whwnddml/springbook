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
		Hello hello = new HelloTarget(); // Ÿ���� �������̽��� ���ؼ� �����ϴ� ������ ������.
		assertThat(hello.sayHello("Toby"),is("Hello Toby"));
		assertThat(hello.sayHi("Toby"),is("Hi Toby"));
		assertThat(hello.sayThankYou("Toby"),is("Thank You Toby"));
	}
	
	@Test
	public void helloUppercase(){
		Hello proxiedHello = new HelloUppercase(new HelloTarget()); // ���Ͻø� ���� Ÿ�� ������Ʈ�� �����ϵ��� �����Ѵ�.
		assertThat(proxiedHello.sayHello("Toby"),is("HELLO TOBY"));
		assertThat(proxiedHello.sayHi("Toby"),is("HI TOBY"));
		assertThat(proxiedHello.sayThankYou("Toby"),is("THANK YOU TOBY"));
	
	}
	
	@Test
	public void helloUppercaseWithInvoke(){
		//���̳��� ���Ͻø� �����ϴ� ���
		Hello proxiedHello = (Hello)Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{Hello.class}, new UpperCaseHandler(new HelloTarget()));
	
		assertThat(proxiedHello.sayHello("Toby"),is("HELLO TOBY"));
		assertThat(proxiedHello.sayHi("Toby"),is("HI TOBY"));
		assertThat(proxiedHello.sayThankYou("Toby"),is("THANK YOU TOBY"));
	}
}
