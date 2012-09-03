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
package com.mymita.vaadlets;

import static com.google.common.base.Objects.firstNonNull;
import static java.lang.Boolean.FALSE;
import static java.lang.String.format;

import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.xml.bind.JAXBElement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.vaadin.codemirror.CodeMirror;

import com.google.common.base.Charsets;
import com.google.common.base.Objects;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import com.mymita.vaadlets.layout.GridLayoutColumExpandRatio;
import com.mymita.vaadlets.layout.GridLayoutRowExpandRatio;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Component.Listener;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.VerticalLayout;

/**
 * @author Andreas Höhmann
 * @since 0.0.1
 */
public class VaadletsBuilder {

  private static final Log LOG = LogFactory.getLog(VaadletsBuilder.class);

  /**
   * @param vaadinParent
   * @param vaadletParent
   * @param vaadinComponent
   * @param vaadletComponent
   * @throws IllegalArgumentException
   *           if the given <code>vaadinParent</code> is not a {@link ComponentContainer}
   */
  private static void addComponent(final com.vaadin.ui.Component vaadinParent,
      final com.mymita.vaadlets.core.Component vaadletParent, final com.vaadin.ui.Component vaadinComponent,
      final com.mymita.vaadlets.core.Component vaadletComponent) throws IllegalArgumentException {

    LOG.debug(format("Add vaadin component '%s' (%s) to vaadin parent '%s' (%s)", vaadinComponent, vaadletComponent,
        vaadinParent, vaadletParent));

    if (!(vaadinParent instanceof com.vaadin.ui.ComponentContainer)) {
      throw new IllegalArgumentException(format(
          "Given vaadin parent '%s' (%s) is no ComponentContainer - can't add other components", vaadinParent,
          vaadletParent));
    }

    if (vaadinParent instanceof com.vaadin.ui.Window && vaadinComponent instanceof com.vaadin.ui.Window) {
      ((com.vaadin.ui.Window) vaadinParent).addWindow((com.vaadin.ui.Window) vaadinComponent);
      return;
    }

    if (vaadinParent instanceof com.vaadin.ui.Panel) {
      if (vaadinComponent instanceof com.vaadin.ui.ComponentContainer) {
        ((com.vaadin.ui.Panel) vaadinParent).setContent((com.vaadin.ui.ComponentContainer) vaadinComponent);
      } else {
        ((com.vaadin.ui.Panel) vaadinParent).addComponent(vaadinComponent);
      }
      return;
    }

    if (vaadinParent instanceof com.vaadin.ui.Form) {
      ((com.vaadin.ui.Form) vaadinParent).setLayout((com.vaadin.ui.Layout) vaadinComponent);
      return;
    }

    if (vaadinParent instanceof com.vaadin.ui.GridLayout) {
      final Short column = vaadletComponent.getColumn();
      final Short row = vaadletComponent.getRow();
      final Short column2 = vaadletComponent.getColumn2();
      final Short row2 = vaadletComponent.getRow2();
      if (column != null && row != null && column2 != null && row2 != null) {
        LOG.debug(format("Add component to grid layout from column '%d', row '%d' to column '%d', row '%d'", column,
            row, column2, row2));
        ((com.vaadin.ui.GridLayout) vaadinParent).addComponent(vaadinComponent, column, row, column2, row2);
      } else if (column != null && row != null) {
        LOG.debug(format("Add component to grid layout at column '%d' and row '%d'", column, row));
        ((com.vaadin.ui.GridLayout) vaadinParent).addComponent(vaadinComponent, column, row);
      } else {
        ((com.vaadin.ui.GridLayout) vaadinParent).addComponent(vaadinComponent);
      }
      return;
    }

    if (vaadinParent instanceof com.vaadin.ui.AbstractOrderedLayout) {
      final com.mymita.vaadlets.layout.Alignment alignment = vaadletComponent.getAlignment();
      final Float expandRatio = vaadletComponent.getExpandRatio();
      ((com.vaadin.ui.AbstractOrderedLayout) vaadinParent).addComponent(vaadinComponent);
      if (alignment != null) {
        ((com.vaadin.ui.AbstractOrderedLayout) vaadinParent).setComponentAlignment(vaadinComponent,
            VaadinUtils.alignmentOf(alignment));
      }
      if (expandRatio != null) {
        ((com.vaadin.ui.AbstractOrderedLayout) vaadinParent).setExpandRatio(vaadinComponent, expandRatio);
      }
      return;
    }

    if (vaadinParent instanceof com.vaadin.ui.AbstractSplitPanel) {
      ((com.vaadin.ui.AbstractSplitPanel) vaadinParent).addComponent(vaadinComponent);
      return;
    }

    if (vaadinParent instanceof com.vaadin.ui.TabSheet) {
      final int position = vaadletComponent.getTabPosition();
      ((com.vaadin.ui.TabSheet) vaadinParent).addTab(vaadinComponent, position);
      return;
    }

    ((com.vaadin.ui.ComponentContainer) vaadinParent).addComponent(vaadinComponent);
  }

  public static VaadletsBuilder build(final com.mymita.vaadlets.core.Component aComponent) {
    final VaadletsBuilder builder = new VaadletsBuilder();
    builder.create(aComponent);
    return builder;
  }

  public static VaadletsBuilder build(final Reader aReader) {
    final VaadletsBuilder builder = new VaadletsBuilder();
    builder.create(JAXBUtils.unmarshal(aReader).getRootComponent());
    return builder;
  }

  private static void setComponentAttributes(final com.vaadin.ui.Component vaadinComponent,
      final com.mymita.vaadlets.core.Component c) {
    vaadinComponent.setCaption(c.getCaption());
    vaadinComponent.setEnabled(c.isEnabled());
    vaadinComponent.setReadOnly(c.isReadonly());
    vaadinComponent.setVisible(c.isVisible());
    if (c.getHeight() != null) {
      vaadinComponent.setHeight(c.getHeight());
    }
    if (c.getWidth() != null) {
      vaadinComponent.setWidth(c.getWidth());
    }
    if (c.isSizeUndefined() != null && c.isSizeUndefined().booleanValue()) {
      vaadinComponent.setSizeUndefined();
    }
    if (c.isSizeFull() != null && c.isSizeFull().booleanValue()) {
      vaadinComponent.setSizeFull();
    }
  }

  private static void setEmbeddedAttributes(final com.vaadin.ui.Component vaadinComponent,
      final com.mymita.vaadlets.core.Component vaadletsComponent) {
    if (vaadletsComponent instanceof com.mymita.vaadlets.ui.Embedded) {
      final String source = ((com.mymita.vaadlets.ui.Embedded) vaadletsComponent).getSource();
      if (source.startsWith("theme:")) {
        try {
          final URI uri = new URI(source);
          final String theme = uri.getHost();
          final String path = uri.getPath();
          final String resourceId = URLDecoder.decode(path.replaceFirst("/", ""), Charsets.UTF_8.toString());
          ((com.vaadin.ui.Embedded) vaadinComponent).setSource(new ThemeResource(resourceId));
        } catch (final URISyntaxException e) {
        } catch (final UnsupportedEncodingException e) {
        }
      }
    }
  }

  private static void setInputAttributes(final com.vaadin.ui.Component vaadinComponent,
      final com.mymita.vaadlets.core.Component vaadletsComponent) {
    if (vaadletsComponent instanceof com.mymita.vaadlets.input.AbstractField) {
      final com.mymita.vaadlets.input.AbstractField field = (com.mymita.vaadlets.input.AbstractField) vaadletsComponent;
      final String value = field.getValue();
      // TODO check for el expressions
    }
  }

  private static void setLayoutAttributes(final com.vaadin.ui.Component vaadinComponent,
      final com.mymita.vaadlets.core.Component vaadletsComponent) {
    if (vaadletsComponent instanceof com.mymita.vaadlets.layout.AbstractLayout) {
      final Boolean margin = ((com.mymita.vaadlets.layout.AbstractLayout) vaadletsComponent).isMargin();
      if (margin != null) {
        ((com.vaadin.ui.AbstractLayout) vaadinComponent).setMargin(margin);
      }
      final Boolean marginTop = ((com.mymita.vaadlets.layout.AbstractLayout) vaadletsComponent).isMarginTop();
      final Boolean marginRight = ((com.mymita.vaadlets.layout.AbstractLayout) vaadletsComponent).isMarginRight();
      final Boolean marginBottom = ((com.mymita.vaadlets.layout.AbstractLayout) vaadletsComponent).isMarginBottom();
      final Boolean marginLeft = ((com.mymita.vaadlets.layout.AbstractLayout) vaadletsComponent).isMarginLeft();
      if (marginTop != null || marginRight != null || marginBottom != null || marginLeft != null) {
        ((com.vaadin.ui.AbstractLayout) vaadinComponent).setMargin(
            firstNonNull(marginTop, firstNonNull(margin, FALSE)),
            firstNonNull(marginRight, firstNonNull(margin, FALSE)),
            firstNonNull(marginBottom, firstNonNull(margin, FALSE)),
            firstNonNull(marginLeft, firstNonNull(margin, FALSE)));
      }
    }
    if (vaadletsComponent instanceof com.mymita.vaadlets.layout.OrderedLayout) {
      final Boolean spacing = ((com.mymita.vaadlets.layout.OrderedLayout) vaadletsComponent).isSpacing();
      if (spacing != null) {
        ((com.vaadin.ui.AbstractOrderedLayout) vaadinComponent).setSpacing(spacing);
      }
    }
    if (vaadletsComponent instanceof com.mymita.vaadlets.layout.GridLayout) {
      final com.vaadin.ui.GridLayout vaadinGridLayout = (com.vaadin.ui.GridLayout) vaadinComponent;
      final com.mymita.vaadlets.layout.GridLayout vaadletsGridLayout = (com.mymita.vaadlets.layout.GridLayout) vaadletsComponent;
      vaadinGridLayout.setColumns(vaadletsGridLayout.getColumns());
      vaadinGridLayout.setRows(vaadletsGridLayout.getRows());
      for (final Object object : vaadletsGridLayout.getColumnExpandRatioOrRowExpandRatio()) {
        if (object instanceof GridLayoutColumExpandRatio) {
          final GridLayoutColumExpandRatio cr = (GridLayoutColumExpandRatio) object;
          vaadinGridLayout.setColumnExpandRatio(cr.getColumnIndex(), cr.getRatio());
        }
        if (object instanceof GridLayoutRowExpandRatio) {
          final GridLayoutRowExpandRatio rr = (GridLayoutRowExpandRatio) object;
          vaadinGridLayout.setRowExpandRatio(rr.getRowIndex(), rr.getRatio());
        }
      }
      final Boolean spacing = ((com.mymita.vaadlets.layout.GridLayout) vaadletsComponent).isSpacing();
      if (spacing != null) {
        ((com.vaadin.ui.GridLayout) vaadinComponent).setSpacing(spacing);
      }
    }
  }

  private static void setPanelAttributes(final Component vaadinComponent,
      final com.mymita.vaadlets.core.Component vaadletsComponent) {
    if (vaadletsComponent instanceof com.mymita.vaadlets.ui.Panel) {
      ((com.vaadin.ui.Panel) vaadinComponent).setScrollable(((com.mymita.vaadlets.ui.Panel) vaadletsComponent)
          .isScrollable());
    }
  }

  private static void setSplitPanelAttributes(final Component vaadinComponent,
      final com.mymita.vaadlets.core.Component c) {
    if (c instanceof com.mymita.vaadlets.ui.AbstractSplitPanel) {
      final String splitPosition = ((com.mymita.vaadlets.ui.AbstractSplitPanel) c).getSplitPosition();
      final int pos = (int) VaadinUtils.parseStringSize(splitPosition)[0];
      final int unit = (int) VaadinUtils.parseStringSize(splitPosition)[1];
      ((com.vaadin.ui.AbstractSplitPanel) vaadinComponent).setSplitPosition(pos, unit);
      ((com.vaadin.ui.AbstractSplitPanel) vaadinComponent).setLocked(((com.mymita.vaadlets.ui.AbstractSplitPanel) c)
          .isLocked());
    }
  }

  private static void setWindowAttributes(final com.vaadin.ui.Component vaadinComponent,
      final com.mymita.vaadlets.core.Component vaadletsComponent) {
    if (vaadletsComponent instanceof com.mymita.vaadlets.ui.Window) {
      final com.vaadin.ui.Window vaadinWindow = (com.vaadin.ui.Window) vaadinComponent;
      final com.mymita.vaadlets.ui.Window vaadletsWindow = (com.mymita.vaadlets.ui.Window) vaadletsComponent;
      if (vaadletsWindow.isCenter()) {
        vaadinWindow.center();
      }
      final Short positionX = vaadletsWindow.getPositionX();
      if (positionX != null) {
        vaadinWindow.setPositionX(positionX);
      }
      final Short positionY = vaadletsWindow.getPositionY();
      if (positionY != null) {
        vaadinWindow.setPositionY(positionY);
      }
      vaadinWindow.setBorder(vaadletsWindow.getBorder().ordinal());
      vaadinWindow.setModal(vaadletsWindow.isModal());
      vaadinWindow.setClosable(vaadletsWindow.isCloseable());
      vaadinWindow.setDraggable(vaadletsWindow.isDraggable());
      vaadinWindow.setResizable(vaadletsWindow.isResizeable());
      vaadinWindow.setResizeLazy(vaadletsWindow.isResizeLazy());
    }
  }

  private final BiMap<String, com.vaadin.ui.Component> components = HashBiMap.create();
  private final Map<String, Listener> componentListener = Maps.newHashMap();
  private final Map<String, ClickListener> clickListener = Maps.newHashMap();

  private com.vaadin.ui.Component root;

  public VaadletsBuilder addClickListener(final String id, final ClickListener aListener) {
    final Component component = getComponent(id);
    if (component instanceof Button) {
      ((Button) component).addListener(aListener);
    }
    return this;
  }

  private void create(final com.mymita.vaadlets.core.Component aComponent) {
    if (!(aComponent instanceof com.mymita.vaadlets.core.ComponentContainer)) {
      // XXX create a dummy component container
      final VerticalLayout dummy = new VerticalLayout();
      dummy.setSizeFull();
      root = new CustomComponent(dummy);
      dummy.addComponent(createVaadinComponent(aComponent));
    } else {
      root = createVaadinComponent(aComponent);
      createChildComponents(root, (com.mymita.vaadlets.core.ComponentContainer) aComponent);
    }
    for (final Entry<String, ClickListener> cl : clickListener.entrySet()) {
      final Component component = getComponent(cl.getKey());
      if (component instanceof Button) {
        ((Button) component).addListener(cl.getValue());
      }
    }
    for (final Entry<String, Listener> cl : componentListener.entrySet()) {
      final Component component = getComponent(cl.getKey());
      if (component != null) {
        component.addListener(cl.getValue());
      }
    }
  }

  private void createChildComponents(final com.vaadin.ui.Component vaadinParentComponent,
      final com.mymita.vaadlets.core.ComponentContainer vaadletsComponentContainer) {
    LOG.debug(format("Create child components for vaadin parent '%s' from vaadlets component container '%s'",
        vaadinParentComponent, vaadletsComponentContainer));
    for (final com.mymita.vaadlets.core.Component vaadletsComponent : vaadletsComponentContainer.getComponents()) {
      final com.vaadin.ui.Component vaadinComponent = createVaadinComponentWithChildren(vaadletsComponent);
      addComponent(vaadinParentComponent, vaadletsComponentContainer, vaadinComponent, vaadletsComponent);
    }
  }

  private com.vaadin.ui.Component createComponent(final com.mymita.vaadlets.core.Component vaadletComponent)
      throws ClassNotFoundException, InstantiationException, IllegalAccessException {
    if (vaadletComponent instanceof com.mymita.vaadlets.addon.CodeMirror) {
      // TODO build tag handler to adapt custom tags
      final com.mymita.vaadlets.addon.CodeMirror cm = (com.mymita.vaadlets.addon.CodeMirror) vaadletComponent;
      final Class<?> codeMirrorComponentClass = Class.forName("org.vaadin.codemirror.CodeMirror");
      try {
        final Constructor<?> constructor = codeMirrorComponentClass.getDeclaredConstructor(String.class);
        final CodeMirror result = (CodeMirror) constructor.newInstance(cm.getCaption());
        result.setCodeStyle(VaadinUtils.codeMirrorCodeStyleOf(cm.getCodeStyle()));
        result.setShowLineNumbers(cm.isShowLineNumbers());
        return result;
      } catch (NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException e) {
        throw new IllegalArgumentException(format("Can't create vaadin component for vaadlets component '%s'",
            vaadletComponent), e);
      }
    }
    String vaadinComponentClassName = null;
    if (vaadletComponent instanceof com.mymita.vaadlets.ui.PopupView) {
      // TODO build tag handler to adapt custom tags
      final com.mymita.vaadlets.ui.PopupView popupView = (com.mymita.vaadlets.ui.PopupView) vaadletComponent;
      final String small = popupView.getSmall();
      final com.mymita.vaadlets.core.Component large = ((JAXBElement<com.mymita.vaadlets.core.Component>) popupView
          .getLarge().getContent().get(0)).getValue();
      LOG.debug(format("Create vaadin component '%s' with small '%s' and large '%s'", vaadinComponentClassName, small,
          large));
      return new PopupView(small, createVaadinComponentWithChildren(large));
    }
    vaadinComponentClassName = "com.vaadin.ui." + vaadletComponent.getClass().getSimpleName();
    LOG.debug(format("Create vaadin component '%s'", vaadinComponentClassName));
    return (com.vaadin.ui.Component) Class.forName(vaadinComponentClassName).newInstance();
  }

  /**
   * Build a {@link com.vaadin.ui.Component} for the given {@link com.mymita.vaadlets.core.Component}.
   * 
   * @param vaadletsComponent
   * @return never <code>null</code>
   */
  private com.vaadin.ui.Component createVaadinComponent(final com.mymita.vaadlets.core.Component vaadletsComponent) {
    try {
      final String componentId = Objects.firstNonNull(vaadletsComponent.getId(), UUID.randomUUID().toString());
      final com.vaadin.ui.Component result = createComponent(vaadletsComponent);
      setComponentAttributes(result, vaadletsComponent);
      setPanelAttributes(result, vaadletsComponent);
      setSplitPanelAttributes(result, vaadletsComponent);
      setWindowAttributes(result, vaadletsComponent);
      setLayoutAttributes(result, vaadletsComponent);
      setInputAttributes(result, vaadletsComponent);
      setEmbeddedAttributes(result, vaadletsComponent);
      components.put(componentId, result);
      return result;
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
      throw new IllegalArgumentException(format("Can't create vaadin component for vaadlets component '%s'",
          vaadletsComponent), e);
    }
  }

  private com.vaadin.ui.Component createVaadinComponentWithChildren(
      final com.mymita.vaadlets.core.Component vaadletsComponent) {
    final com.vaadin.ui.Component vaadinComponent = createVaadinComponent(vaadletsComponent);
    if (vaadletsComponent instanceof com.mymita.vaadlets.core.ComponentContainer) {
      createChildComponents(vaadinComponent, (com.mymita.vaadlets.core.ComponentContainer) vaadletsComponent);
    }
    return vaadinComponent;
  }

  @SuppressWarnings("unchecked")
  public <T extends Component> T getComponent(final String id) {
    return (T) components.get(id);
  }

  public Component getRoot() {
    return root;
  }

  public VaadletsBuilder withClickListener(final String id, final ClickListener aListener) {
    clickListener.put(id, aListener);
    return this;
  }

  public VaadletsBuilder withComponentListener(final String id, final Listener aListener) {
    componentListener.put(id, aListener);
    return this;
  }
}