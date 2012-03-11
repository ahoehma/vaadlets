package com.mymita.vaadlets.demo;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;

import com.mymita.vaadlets.addon.Addon;
import com.vaadin.Application;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class AddonDemoApplication extends Application implements ClickListener {

	private static final long serialVersionUID = 4745768698981212574L;
	private final TextField editor = new TextField();
	private final HorizontalSplitPanel splitter = new HorizontalSplitPanel();

	@Override
	public void buttonClick(final ClickEvent event) {
		try {
			final Addon xmlui = new Addon();
			xmlui.build(new ByteArrayInputStream(editor.toString().getBytes()));
			splitter.setSecondComponent(xmlui.getRoot());
		} catch (final Exception e) {
			final VerticalLayout vl = new VerticalLayout();
			vl.setMargin(true);
			vl.addComponent(new Label("<h1>Error</h1>", Label.CONTENT_XHTML));
			final StringWriter buf = new StringWriter();
			e.printStackTrace(new PrintWriter(buf));
			vl.addComponent(new Label(buf.toString(),
					Label.CONTENT_PREFORMATTED));
			splitter.setSecondComponent(vl);
		}
	}

	@Override
	public void init() {
		final VerticalLayout left = new VerticalLayout();
		final Window main = new Window("XMLUI Editor", splitter);
		setMainWindow(main);
		splitter.addComponent(left);
		editor.setSizeFull();
		left.setSizeFull();
		left.addComponent(editor);
		final Button testButton = new Button("Test XMLUI", this);
		left.addComponent(testButton);
		left.setExpandRatio(editor, 1.0f);
		fillEditorWithDefaultXML();
	}

	private void fillEditorWithDefaultXML() {
		try {
			final URL startingPoint = getClass().getResource(
					"startingPoint.xml");
			if (startingPoint != null) {
				final InputStreamReader isr = new InputStreamReader(
						startingPoint.openStream());
				final StringBuffer sb = new StringBuffer();
				final char[] buf = new char[1024 * 8];
				int len;
				while ((len = isr.read(buf)) > 0) {
					sb.append(buf, 0, len);
				}
				isr.close();
				editor.setValue(sb.toString());
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}
}