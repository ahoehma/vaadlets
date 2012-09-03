/**
 * 
 */
package com.mymita.vaadlets.demo;

import com.bsb.common.vaadin.embed.support.EmbedVaadin;
import com.mymita.vaadlets.VaadletsBuilder;
import com.mymita.vaadlets.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.themes.BaseTheme;

/**
 * @author Andreas Höhmann
 * @since 0.0.1
 */
public class TestFluentAPI {

  public static void main(final String[] args) {
    EmbedVaadin
        .forComponent(
            VaadletsBuilder
                .build(
                    new Button().withId("foobar").withCaption("Foobar").withStyleName(BaseTheme.BUTTON_LINK)
                        .withWidth("100px")).addClickListener("foobar", new ClickListener() {

                  @Override
                  public void buttonClick(final ClickEvent aEvent) {
                    aEvent.getComponent().getApplication().getMainWindow().showNotification("Button clicked");
                  }
                }).getRoot()).openBrowser(true).wait(true).start();
  }
}