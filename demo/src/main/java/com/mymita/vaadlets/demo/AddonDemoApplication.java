package com.mymita.vaadlets.demo;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.springframework.core.io.ClassPathResource;

import com.google.common.io.CharStreams;
import com.mymita.vaadlets.addon.VaadletsBuilder;
import com.vaadin.Application;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class AddonDemoApplication extends Application {

  private static final long serialVersionUID = 4745768698981212574L;

  private static VerticalLayout createStackTraceLabel(final Exception e) {
    final VerticalLayout vl = new VerticalLayout();
    vl.setMargin(true);
    vl.addComponent(new Label("<h1>Error</h1>", Label.CONTENT_XHTML));
    final StringWriter buf = new StringWriter();
    e.printStackTrace(new PrintWriter(buf));
    vl.addComponent(new Label(buf.toString(), Label.CONTENT_PREFORMATTED));
    return vl;
  }

  private VaadletsBuilder vaadlets;

  private void fillEditorWithDefaultXML() {
    final Panel content = vaadlets.getComponent("content");
    content.removeAllComponents();
    final TextField editor = vaadlets.getComponent("editor");
    try {
      editor.setValue(CharStreams.toString(new InputStreamReader(new ClassPathResource("startingPoint.xml",
          AddonDemoApplication.class).getInputStream(), "UTF-8")));
    } catch (final IOException e) {
    }
  }

  @Override
  public void init() {
    vaadlets = new VaadletsBuilder();
    try {
      vaadlets.build(new ClassPathResource("demo.xml", AddonDemoApplication.class).getInputStream());
    } catch (final IOException e1) {
    }
    final Window root = (Window) vaadlets.getRoot();
    setMainWindow(root);

    final Button testButton = vaadlets.getComponent("test");
    testButton.addListener(new ClickListener() {

      @Override
      public void buttonClick(final ClickEvent event) {
        final Panel content = vaadlets.getComponent("content");
        final TextField editor = vaadlets.getComponent("editor");
        content.removeAllComponents();
        try {
          final VaadletsBuilder v = new VaadletsBuilder();
          v.build(new ByteArrayInputStream(editor.toString().getBytes()));
          final Component root = v.getRoot();
          if (root instanceof Window) {
            getMainWindow().addWindow((Window) root);
          } else {
            content.addComponent(root);
          }
        } catch (final Exception e) {
          content.addComponent(createStackTraceLabel(e));
        }
      }
    });
    final Button resetButton = vaadlets.getComponent("reset");
    resetButton.addListener(new ClickListener() {

      @Override
      public void buttonClick(final ClickEvent event) {
        fillEditorWithDefaultXML();
      }
    });

    fillEditorWithDefaultXML();
  }
}