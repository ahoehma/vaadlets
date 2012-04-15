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
/**
 * Copyright 2012 Andreas Höhmann (mymita.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package com.mymita.vaadlets;

import static com.google.common.base.Objects.firstNonNull;
import static java.lang.Boolean.FALSE;
import static java.lang.String.format;

import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.vaadin.codemirror.CodeMirror;
import org.vaadin.codemirror.client.ui.CodeStyle;

import com.google.common.base.Objects;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mymita.vaadlets.addon.CodeMirrorCodeStyle;
import com.mymita.vaadlets.layout.GridLayoutColumExpandRatio;
import com.mymita.vaadlets.layout.GridLayoutRowExpandRatio;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.VerticalLayout;

public class VaadletsBuilder {

  private static final Log LOG = LogFactory.getLog(VaadletsBuilder.class);

  private static final Pattern sizePattern = Pattern.compile("^(-?\\d+(\\.\\d+)?)(%|px|em|ex|in|cm|mm|pt|pc)?$");

  private static void addComponent(final com.vaadin.ui.Component vaadinParent,
      final com.mymita.vaadlets.core.Component vaadletParent, final com.vaadin.ui.Component vaadinComponent,
      final com.mymita.vaadlets.core.Component vaadletComponent) {
    LOG.debug(format("Add vaadin component '%s' (%s) to vaadin parent '%s' (%s)", vaadinComponent, vaadletComponent,
        vaadinParent, vaadletParent));
    if (!(vaadinParent instanceof com.vaadin.ui.ComponentContainer)) {
      throw new RuntimeException(format(
          "Given vaadin parent '%s' (%s) is no ComponentContainer - can't add other components", vaadinParent,
          vaadletParent));
    }
    if (vaadinParent instanceof com.vaadin.ui.Window && vaadinComponent instanceof com.vaadin.ui.Window) {
      ((com.vaadin.ui.Window) vaadinParent).addWindow((com.vaadin.ui.Window) vaadinComponent);
    } else if (vaadinParent instanceof com.vaadin.ui.Panel) {
      if (vaadinComponent instanceof com.vaadin.ui.ComponentContainer) {
        ((com.vaadin.ui.Panel) vaadinParent).setContent((com.vaadin.ui.ComponentContainer) vaadinComponent);
      } else {
        ((com.vaadin.ui.Panel) vaadinParent).addComponent(vaadinComponent);
      }
    } else if (vaadinParent instanceof com.vaadin.ui.Form) {
      ((com.vaadin.ui.Form) vaadinParent).setLayout((com.vaadin.ui.Layout) vaadinComponent);
    } else if (vaadinParent instanceof com.vaadin.ui.GridLayout) {
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
    } else if (vaadinParent instanceof com.vaadin.ui.AbstractOrderedLayout) {
      final com.mymita.vaadlets.layout.Alignment alignment = vaadletComponent.getAlignment();
      final Float expandRatio = vaadletComponent.getExpandRatio();
      ((com.vaadin.ui.AbstractOrderedLayout) vaadinParent).addComponent(vaadinComponent);
      if (alignment != null) {
        ((com.vaadin.ui.AbstractOrderedLayout) vaadinParent).setComponentAlignment(vaadinComponent,
            alignmentOf(alignment));
      }
      if (expandRatio != null) {
        ((com.vaadin.ui.AbstractOrderedLayout) vaadinParent).setExpandRatio(vaadinComponent, expandRatio);
      }
    } else if (vaadinParent instanceof com.vaadin.ui.AbstractSplitPanel) {
      ((com.vaadin.ui.AbstractSplitPanel) vaadinParent).addComponent(vaadinComponent);
    } else {
      ((com.vaadin.ui.ComponentContainer) vaadinParent).addComponent(vaadinComponent);
    }
  }

  private static com.vaadin.ui.Alignment alignmentOf(final com.mymita.vaadlets.layout.Alignment aAlignment) {
    switch (aAlignment) {
      case BOTTOM_LEFT:
        return com.vaadin.ui.Alignment.BOTTOM_LEFT;
      case BOTTOM_CENTER:
        return com.vaadin.ui.Alignment.BOTTOM_CENTER;
      case BOTTOM_RIGHT:
        return com.vaadin.ui.Alignment.BOTTOM_RIGHT;
      case MIDDLE_LEFT:
        return com.vaadin.ui.Alignment.MIDDLE_LEFT;
      case MIDDLE_CENTER:
        return com.vaadin.ui.Alignment.MIDDLE_CENTER;
      case MIDDLE_RIGHT:
        return com.vaadin.ui.Alignment.MIDDLE_RIGHT;
      case TOP_LEFT:
        return com.vaadin.ui.Alignment.TOP_LEFT;
      case TOP_CENTER:
        return com.vaadin.ui.Alignment.TOP_CENTER;
      case TOP_RIGHT:
        return com.vaadin.ui.Alignment.TOP_RIGHT;
    }
    throw new IllegalArgumentException(String.format("Unkown aligment '%s'", aAlignment));
  }

  private static CodeStyle codeMirrorCodeStyleOf(final CodeMirrorCodeStyle codeStyle) {
    switch (codeStyle) {
      case CSS:
        return org.vaadin.codemirror.client.ui.CodeStyle.CSS;
      case JAVA:
        return org.vaadin.codemirror.client.ui.CodeStyle.JAVA;
      case JAVASCRIPT:
        return org.vaadin.codemirror.client.ui.CodeStyle.JAVASCRIPT;
      case LUA:
        return org.vaadin.codemirror.client.ui.CodeStyle.LUA;
      case PHP:
        return org.vaadin.codemirror.client.ui.CodeStyle.PHP;
      case PYTHON:
        return org.vaadin.codemirror.client.ui.CodeStyle.PYTHON;
      case SQL:
        return org.vaadin.codemirror.client.ui.CodeStyle.SQL;
      case TEXT:
        return org.vaadin.codemirror.client.ui.CodeStyle.TEXT;
      case XML:
        return org.vaadin.codemirror.client.ui.CodeStyle.XML;
    }
    throw new IllegalArgumentException(String.format("Unkown code mirror code style '%s'", codeStyle));
  }

  private static float[] parseStringSize(String s) {
    final float[] values = {
      -1, Sizeable.UNITS_PIXELS
    };
    if (s == null) {
      return values;
    }
    s = s.trim();
    if ("".equals(s)) {
      return values;
    }
    final Matcher matcher = sizePattern.matcher(s);
    if (matcher.find()) {
      values[0] = Float.parseFloat(matcher.group(1));
      if (values[0] < 0) {
        values[0] = -1;
      } else {
        final String unit = matcher.group(3);
        if (unit == null) {
          values[1] = Sizeable.UNITS_PIXELS;
        } else if (unit.equals("px")) {
          values[1] = Sizeable.UNITS_PIXELS;
        } else if (unit.equals("%")) {
          values[1] = Sizeable.UNITS_PERCENTAGE;
        } else if (unit.equals("em")) {
          values[1] = Sizeable.UNITS_EM;
        } else if (unit.equals("ex")) {
          values[1] = Sizeable.UNITS_EX;
        } else if (unit.equals("in")) {
          values[1] = Sizeable.UNITS_INCH;
        } else if (unit.equals("cm")) {
          values[1] = Sizeable.UNITS_CM;
        } else if (unit.equals("mm")) {
          values[1] = Sizeable.UNITS_MM;
        } else if (unit.equals("pt")) {
          values[1] = Sizeable.UNITS_POINTS;
        } else if (unit.equals("pc")) {
          values[1] = Sizeable.UNITS_PICAS;
        }
      }
    } else {
      throw new IllegalArgumentException("Invalid size argument: \"" + s + "\" (should match " + sizePattern.pattern()
          + ")");
    }
    return values;
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
      final int pos = (int) parseStringSize(splitPosition)[0];
      final int unit = (int) parseStringSize(splitPosition)[1];
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
  private com.vaadin.ui.Component root;

  public void build(final Reader aReader) {
    final com.mymita.vaadlets.core.Component r = JAXBUtils.unmarshal(aReader).getRootComponent();
    if (!(r instanceof com.mymita.vaadlets.core.ComponentContainer)) {
      // XXX create a dummy component container
      final VerticalLayout dummy = new VerticalLayout();
      dummy.setSizeFull();
      root = new CustomComponent(dummy);
      dummy.addComponent(createVaadinComponent(r));
    } else {
      root = createVaadinComponent(r);
      createChildComponents(root, (com.mymita.vaadlets.core.ComponentContainer) r);
    }
  }

  private void createChildComponents(final com.vaadin.ui.Component vaadinParentComponent,
      final com.mymita.vaadlets.core.ComponentContainer r) {
    LOG.debug(format("Create child components to parent '%s'/'%s'", vaadinParentComponent, r));
    for (final com.mymita.vaadlets.core.Component vaadletsComponent : r.getComponents()) {
      final Component vaadinComponent = createVaadinComponent(vaadletsComponent);
      addComponent(vaadinParentComponent, r, vaadinComponent, vaadletsComponent);
      if (vaadletsComponent instanceof com.mymita.vaadlets.core.ComponentContainer) {
        createChildComponents(vaadinComponent, (com.mymita.vaadlets.core.ComponentContainer) vaadletsComponent);
      }
    }
  }

  private com.vaadin.ui.Component createComponent(final com.mymita.vaadlets.core.Component vaadletComponent)
      throws ClassNotFoundException, InstantiationException, IllegalAccessException {
    String vaadinComponentClassName = null;
    if (vaadletComponent instanceof com.mymita.vaadlets.addon.CodeMirror) {
      // TODO build tag handler to adapt custom tags
      final com.mymita.vaadlets.addon.CodeMirror cm = (com.mymita.vaadlets.addon.CodeMirror) vaadletComponent;
      final Class<?> codeMirrorComponentClass = Class.forName("org.vaadin.codemirror.CodeMirror");
      try {
        final Constructor<?> constructor = codeMirrorComponentClass.getDeclaredConstructor(String.class);
        final CodeMirror result = (CodeMirror) constructor.newInstance(cm.getCaption());
        result.setCodeStyle(codeMirrorCodeStyleOf(cm.getCodeStyle()));
        result.setShowLineNumbers(cm.isShowLineNumbers());
        return result;
      } catch (NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException e) {
      }
      return null;
    }
    vaadinComponentClassName = "com.vaadin.ui." + vaadletComponent.getClass().getSimpleName();
    LOG.debug(format("Create vaadin component '%s'", vaadinComponentClassName));
    final Class<?> vaadinComponentClass = Class.forName(vaadinComponentClassName);
    return (com.vaadin.ui.Component) vaadinComponentClass.newInstance();
  }

  private com.vaadin.ui.Component createVaadinComponent(final com.mymita.vaadlets.core.Component c) {
    try {
      final String componentId = Objects.firstNonNull(c.getId(), UUID.randomUUID().toString());
      final com.vaadin.ui.Component result = createComponent(c);
      setComponentAttributes(result, c);
      setPanelAttributes(result, c);
      setSplitPanelAttributes(result, c);
      setWindowAttributes(result, c);
      setLayoutAttributes(result, c);
      setInputAttributes(result, c);
      components.put(componentId, result);
      return result;
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
      throw new RuntimeException(format("Can't find vaadin class for '%s'", c), e);
    }
  }

  @SuppressWarnings("unchecked")
  public <T extends Component> T getComponent(final String id) {
    return (T) components.get(id);
  }

  public Component getRoot() {
    return root;
  }

  private void setInputAttributes(final Component vaadinComponent,
      final com.mymita.vaadlets.core.Component vaadletsComponent) {
    if (vaadletsComponent instanceof com.mymita.vaadlets.input.AbstractField) {
      final com.mymita.vaadlets.input.AbstractField field = (com.mymita.vaadlets.input.AbstractField) vaadletsComponent;
      final String value = field.getValue();
      // TODO check for el expressions
    }
  }
}
