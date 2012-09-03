/*******************************************************************************
 * Copyright 2012 Andreas Höhmann (mymita.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.mymita.vaadlets.demo;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.springframework.core.io.ClassPathResource;

import com.google.common.io.CharStreams;
import com.mymita.vaadlets.VaadletsBuilder;
import com.vaadin.Application;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class AddonDemoApplication extends Application {

  private static final Log LOG = LogFactory.getLog(AddonDemoApplication.class);

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

  private static void fillEditorWithDefaultXML(final VaadletsBuilder vaadlets) {
    vaadlets.<Panel> getComponent("content").getContent().removeAllComponents();
    final TextField editor = vaadlets.getComponent("editor");
    try {
      editor.setValue(CharStreams.toString(new InputStreamReader(new ClassPathResource("startingPoint.xml",
          AddonDemoApplication.class).getInputStream(), "UTF-8")));
    } catch (final IOException e) {
    }
  }

  @Override
  public void init() {
    try {
      final VaadletsBuilder vaadletsBuilder = VaadletsBuilder.build(new InputStreamReader(new ClassPathResource(
          "demo.xml", AddonDemoApplication.class).getInputStream()));
      final Window root = (Window) vaadletsBuilder.getRoot();
      setMainWindow(root);
      setTheme("vaadlets");
      final Button testButton = vaadletsBuilder.getComponent("test");
      testButton.addListener(new ClickListener() {

        @Override
        public void buttonClick(final ClickEvent event) {
          final Panel content = vaadletsBuilder.getComponent("content");
          final TextField editor = vaadletsBuilder.getComponent("editor");
          content.getContent().removeAllComponents();
          try {
            final VaadletsBuilder v = VaadletsBuilder.build(CharStreams.newReaderSupplier((String) editor.getValue())
                .getInput());
            if (v.getRoot() instanceof Window) {
              final Window w = (Window) v.getRoot();
              root.addWindow(w);
            } else {
              content.getContent().addComponent(v.getRoot());
            }
          } catch (final Exception e) {
            LOG.error("error", e);
            content.addComponent(createStackTraceLabel(e));
          }
        }
      });
      final Button resetButton = vaadletsBuilder.getComponent("reset");
      resetButton.addListener(new ClickListener() {

        @Override
        public void buttonClick(final ClickEvent event) {
          fillEditorWithDefaultXML(vaadletsBuilder);
        }
      });
      fillEditorWithDefaultXML(vaadletsBuilder);
    } catch (final IOException e) {
    }
  }
}