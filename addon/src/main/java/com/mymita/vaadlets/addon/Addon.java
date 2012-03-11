package com.mymita.vaadlets.addon;

import java.io.InputStream;

import com.mymita.vaadlets.Vaadlets;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.Form;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Window;

public class Addon {
	
	public void build(final InputStream inputStream) {
		final Vaadlets v = JAXBUtils.unmarschal(inputStream);
		com.mymita.vaadlets.core.ComponentContainer root = v.getRootComponent();
		root.getComponents();
	}

	public Component createVaadinComponent(com.mymita.vaadlets.core.Component c)  {
		try {
			return (Component) Class.forName("com.vaadin.ui."+c.getClass().getSimpleName()).newInstance();
		} catch (ClassNotFoundException|InstantiationException|IllegalAccessException e) {
			throw new RuntimeException(String.format("Can't find vaadin class for '%s'", c), e);
		}
	}

	public Component getRoot() {
		return null;
	}

	private void addComponent(ComponentContainer parent, Component c) {
		if (parent instanceof Window && c instanceof Window) {
			((Window) parent).addWindow((Window) c);
		} else if (parent instanceof Panel) {
			((Panel) parent).setContent((ComponentContainer) c);
		} else if (parent instanceof Form) {
			((Form) parent).setLayout((Layout) c);
		} else {
			parent.addComponent(c);
		}
	}
}