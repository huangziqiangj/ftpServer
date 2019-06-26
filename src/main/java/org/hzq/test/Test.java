package org.hzq.test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Test {
	public static void main(String[] args) {
		sendPost();
	             
	}

	public static void sendPost(){
		List<String> asList = new ArrayList<>();
		asList.add("ds");
		asList.add("sdd");
		asList.add("cc");
		Iterator<String> iterator = asList.iterator();
		String next = iterator.next();
		iterator.remove();
		System.out.println(asList);
		
	}
}
