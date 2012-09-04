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