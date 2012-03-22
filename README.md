Vaadlets
========

Vaadlets is a template engine for vaadin. 

Imagine you can use a library like Facelets (Java Server Faces) in your Vaadin application. 

The ui declaration in Vaadlets is xml/xsd based, so you have auto completion etc.

Please see the examples below.     

Building from Source
--------------------

### Prerequisites

* Java 7
* Maven 3
* Git
* Eclipse (m2eclipse, egit)
* A working internet connection

### Checking out the code

Clone the git repository using the URL on the Github home page:

    $ git clone git@github.com:ahoehma/vaadlets.git
    $ cd vaadlets

### Build on command line

    $ mvn install

Editing the code in an IDE
--------------------------

I'm using eclipse with the power of m2eclipse (maven integration). Simply import the projects. That's it ;)   

Trying out the demo
-------------------

  $ cd vaadlets-demo
  $ mvn jetty:run
  
Open browser http://localhost:8080/vaadlets-demo/

Using Vaadlets in your project
------------------------------

Simply add the following dependency to your Maven project:

    <dependency>
        <groupId>com.mymita</groupId>
        <artifactId>vaadlets-addon</artifactId>
        <version>0.0.1</version>
    </dependency>

Examples
--------

### From the demo ui

Vaadlet template:

    <?xml version="1.0" encoding="UTF-8"?>
    <vaadlets xmlns="http://www.mymita.com/vaadlets/1.0.0"
              xmlns:core="http://www.mymita.com/vaadlets/core/1.0.0"
              xmlns:layout="http://www.mymita.com/vaadlets/layout/1.0.0"
              xmlns:ui="http://www.mymita.com/vaadlets/ui/1.0.0"
              xmlns:input="http://www.mymita.com/vaadlets/input/1.0.0"
              xmlns:addon="http://www.mymita.com/vaadlets/addon/1.0.0"
              xmlns:xsd="http://www.w3.org/2001/XMLSchema-instance"
              xsd:schemaLocation="http://www.mymita.com/vaadlets/1.0.0 http://www.mymita.com/xsd/vaadlets-1.0.0.xsd
                                  http://www.mymita.com/vaadlets/core/1.0.0 http://www.mymita.com/xsd/vaadlets-core-1.0.0.xsd
                                  http://www.mymita.com/vaadlets/layout/1.0.0 http://www.mymita.com/xsd/vaadlets-layout-1.0.0.xsd
                                  http://www.mymita.com/vaadlets/ui/1.0.0 http://www.mymita.com/xsd/vaadlets-ui-1.0.0.xsd
                                  http://www.mymita.com/vaadlets/input/1.0.0 http://www.mymita.com/xsd/vaadlets-input-1.0.0.xsd
                                  http://www.mymita.com/vaadlets/addon/1.0.0 http://www.mymita.com/xsd/vaadlets-addon-1.0.0.xsd">
      <ui:window caption="Vaadlets Editor">
        <ui:window caption="Welcome" width="300px" height="200px" center="true" resizeable="false" modal="true">
          <ui:label caption="Welcome to the Vaadlets DEMO Application"/>
        </ui:window>
        <ui:horizontalSplitPanel>
          <layout:verticalLayout size-full="true" margin="true" spacing="true">
            <ui:panel size-full="true" expand-ratio="1.0">
              <layout:verticalLayout size-full="true" >
                <addon:codeMirror id="editor" size-full="true" expand-ratio="1.0" code-style="XML" style-name="foobar"/>
              </layout:verticalLayout>
            </ui:panel>
            <layout:horizontalLayout>
              <input:button id="test" caption="Test Vaadlet"/>
              <input:button id="reset" caption="Reset Vaadlet"/>
            </layout:horizontalLayout>
          </layout:verticalLayout>
          <ui:panel id="content" size-full="true">
            <layout:verticalLayout margin="true" size-full="true"/>
          </ui:panel>
        </ui:horizontalSplitPanel>
      </ui:window>
    </vaadlets>
    
Vaadin application:
        
    package com.mymita.vaadlets.demo;

    import java.io.ByteArrayInputStream;
    import java.io.IOException;
    import java.io.InputStreamReader;
    import java.io.PrintWriter;
    import java.io.StringWriter;
    
    import org.apache.commons.logging.Log;
    import org.apache.commons.logging.LogFactory;
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
        vaadlets.<Panel>getComponent("content").getContent().removeAllComponents();
        final TextField editor = vaadlets.getComponent("editor");
        try {
          editor.setValue(CharStreams.toString(new InputStreamReader(new ClassPathResource("startingPoint.xml",
              AddonDemoApplication.class).getInputStream(), "UTF-8")));
        } catch (final IOException e) {
        }
      }
    
      @Override
      public void init() {
        final VaadletsBuilder vaadlets = new VaadletsBuilder();
        try {
          vaadlets.build(new ClassPathResource("demo.xml", AddonDemoApplication.class).getInputStream());
        } catch (final IOException e) {
          LOG.error("error", e);
        }
        final Window root = (Window) vaadlets.getRoot();
        setMainWindow(root);
    
        final Button testButton = vaadlets.getComponent("test");
        testButton.addListener(new ClickListener() {
    
          @Override
          public void buttonClick(final ClickEvent event) {
            final Panel content = vaadlets.getComponent("content");
            final TextField editor = vaadlets.getComponent("editor");
            content.getContent().removeAllComponents();
            try {
              final VaadletsBuilder v = new VaadletsBuilder();
              v.build(new ByteArrayInputStream(editor.toString().getBytes()));
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
        final Button resetButton = vaadlets.getComponent("reset");
        resetButton.addListener(new ClickListener() {
    
          @Override
          public void buttonClick(final ClickEvent event) {
            fillEditorWithDefaultXML(vaadlets);
          }
        });
        fillEditorWithDefaultXML(vaadlets);
      }
    }

Release notes
-------------

### 0.1

* Initial release - in progress