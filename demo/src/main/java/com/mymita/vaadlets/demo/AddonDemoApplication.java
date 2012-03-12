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
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class AddonDemoApplication extends Application {

  private static final long serialVersionUID = 4745768698981212574L;
  private final TextField editor = new TextField();
  private final Panel vaadletsContent = new Panel();

  private static VerticalLayout createStackTraceLabel(final Exception e) {
    final VerticalLayout vl = new VerticalLayout();
    vl.setMargin(true);
    vl.addComponent(new Label("<h1>Error</h1>", Label.CONTENT_XHTML));
    final StringWriter buf = new StringWriter();
    e.printStackTrace(new PrintWriter(buf));
    vl.addComponent(new Label(buf.toString(), Label.CONTENT_PREFORMATTED));
    return vl;
  }

  @Override
  public void init() {
    final HorizontalSplitPanel splitter = new HorizontalSplitPanel();
    final Window main = new Window("Vaadlets Editor", splitter);
    setMainWindow(main);
    final VerticalLayout left = new VerticalLayout();
    splitter.setFirstComponent(left);
    splitter.setSecondComponent(vaadletsContent);
    editor.setSizeFull();
    left.setSizeFull();
    left.addComponent(editor);
    left.setExpandRatio(editor, 1.0f);
    final HorizontalLayout buttons = new HorizontalLayout();
    left.addComponent(buttons);
    final Button testButton = new Button("Test Vaadlet", new ClickListener() {

      @Override
      public void buttonClick(final ClickEvent event) {
        vaadletsContent.removeAllComponents();
        try {
          final VaadletsBuilder v = new VaadletsBuilder();
          v.build(new ByteArrayInputStream(editor.toString().getBytes()));
          final Component root = v.getRoot();
          if (root instanceof Window) {
            getMainWindow().addWindow((Window) root);
          } else {
            vaadletsContent.addComponent(root);
          }
        } catch (final Exception e) {
          vaadletsContent.addComponent(createStackTraceLabel(e));
        }
      }
    });
    final Button resetButton = new Button("Reset Vaadlet", new ClickListener() {

      @Override
      public void buttonClick(final ClickEvent event) {
        fillEditorWithDefaultXML();
      }
    });
    buttons.addComponent(resetButton);
    buttons.addComponent(testButton);
    fillEditorWithDefaultXML();
  }

  private void fillEditorWithDefaultXML() {
    vaadletsContent.removeAllComponents();
    try {
      editor.setValue(CharStreams.toString(new InputStreamReader(new ClassPathResource("startingPoint.xml",
          AddonDemoApplication.class).getInputStream(), "UTF-8")));
    } catch (final IOException e) {

    }
  }
}