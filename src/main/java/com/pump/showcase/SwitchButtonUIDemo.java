package com.pump.showcase;

import java.net.URL;

import javax.swing.JToggleButton;

import com.pump.plaf.SwitchButtonUI;

public class SwitchButtonUIDemo extends ShowcaseDemo {
	private static final long serialVersionUID = 1L;

	JToggleButton button = new JToggleButton("Hello");

	public SwitchButtonUIDemo() {
		button.setUI(new SwitchButtonUI());
		add(button);
	}

	@Override
	public String getTitle() {
		return "SwitchButtonUI";
	}

	@Override
	public String getSummary() {
		return "This is an alternative to checkboxes that is common on smartphones and tablets.";
	}

	@Override
	public URL getHelpURL() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getKeywords() {
		return new String[] { "button", "ux", "switch", "checkbox", "toggle" };
	}

	@Override
	public Class<?>[] getClasses() {
		return new Class[] { SwitchButtonUI.class };
	}

}
