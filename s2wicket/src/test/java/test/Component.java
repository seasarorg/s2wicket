package test;

import java.util.ArrayList;
import java.util.List;

public abstract class Component {
	
	private List<String> hoge;
	
	public Component() {
		hoge = new ArrayList<String>();
		hoge.add("S2Wicket");
	}
	
	public abstract void onClick();
	
	protected void addHoge(String str) {
		hoge.add(str);
	}
	
	public void foo() {
		for (String str : hoge) {
			System.out.println(str);
		}
	}
	
}
