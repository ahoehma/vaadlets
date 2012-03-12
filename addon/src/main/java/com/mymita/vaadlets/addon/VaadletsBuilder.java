package com.mymita.vaadlets.addon;

import static java.lang.String.format;

import java.io.InputStream;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Component;

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

  private static void setComponentAttributes(final com.vaadin.ui.Component result,
      final com.mymita.vaadlets.core.Component c) {
    result.setCaption(c.getCaption());
    result.setEnabled(c.isEnabled());
    result.setReadOnly(c.isReadonly());
    result.setVisible(c.isVisible());
    if (c.getHeight() != null) {
      result.setHeight(c.getHeight());
    }
    if (c.getWidth() != null) {
      result.setWidth(c.getWidth());
    }
    if (c.isSizeUndefined() != null && c.isSizeUndefined().booleanValue()) {
      result.setSizeUndefined();
    }
    if (c.isSizeFull() != null && c.isSizeFull().booleanValue()) {
      result.setSizeFull();
    }
  }

  private static void setLayoutAttributes(final com.vaadin.ui.Component result,
      final com.mymita.vaadlets.core.Component c) {
    if (c instanceof com.mymita.vaadlets.layout.GridLayout) {
      ((com.vaadin.ui.GridLayout) result).setColumns(((com.mymita.vaadlets.layout.GridLayout) c).getColumns());
      ((com.vaadin.ui.GridLayout) result).setRows(((com.mymita.vaadlets.layout.GridLayout) c).getRows());
    }
  }

  private static void setPanelAttributes(final Component result, final com.mymita.vaadlets.core.Component c) {
    if (c instanceof com.mymita.vaadlets.ui.Panel) {
      ((com.vaadin.ui.Panel) result).setScrollable(((com.mymita.vaadlets.ui.Panel) c).isScrollable());
    }
  }

  private static void setSplitPanelAttributes(final Component result, final com.mymita.vaadlets.core.Component c) {
    if (c instanceof com.mymita.vaadlets.ui.AbstractSplitPanel) {
      final String splitPosition = ((com.mymita.vaadlets.ui.AbstractSplitPanel) c).getSplitPosition();
      final int pos = (int) parseStringSize(splitPosition)[0];
      final int unit = (int) parseStringSize(splitPosition)[1];
      ((com.vaadin.ui.AbstractSplitPanel) result).setSplitPosition(pos, unit);
    }
  }

  private static void setWindowAttributes(final Component result, final com.mymita.vaadlets.core.Component c) {
    if (c instanceof com.mymita.vaadlets.ui.Window) {
      if (((com.mymita.vaadlets.ui.Window) c).isCenter()) {
        ((com.vaadin.ui.Window) result).center();
      }
      ((com.vaadin.ui.Window) result).setModal(((com.mymita.vaadlets.ui.Window) c).isModal());
      ((com.vaadin.ui.Window) result).setClosable(((com.mymita.vaadlets.ui.Window) c).isCloseable());
      ((com.vaadin.ui.Window) result).setDraggable(((com.mymita.vaadlets.ui.Window) c).isDraggable());
      ((com.vaadin.ui.Window) result).setResizable(((com.mymita.vaadlets.ui.Window) c).isResizeable());
      ((com.vaadin.ui.Window) result).setResizeLazy(((com.mymita.vaadlets.ui.Window) c).isResizeLazy());
    }
  }

  private final Map<String, com.vaadin.ui.Component> components = Maps.newHashMap();
  private com.vaadin.ui.Component root;

  public void build(final InputStream inputStream) {
    final com.mymita.vaadlets.core.Component r = JAXBUtils.unmarshal(inputStream).getRootComponent();
    createChildComponents(root = createVaadinComponent(r), (com.mymita.vaadlets.core.ComponentContainer) r);
  }

  private void createChildComponents(final com.vaadin.ui.Component parent,
      final com.mymita.vaadlets.core.ComponentContainer r) {
    LOG.debug(format("Create child components for parent '%s'/'%s'", parent, r));
    for (final com.mymita.vaadlets.core.Component vaadletsComponent : r.getComponents()) {
      final Component vaadinComponent = createVaadinComponent(vaadletsComponent);
      addComponent(parent, r, vaadinComponent, vaadletsComponent);
      if (vaadletsComponent instanceof com.mymita.vaadlets.core.ComponentContainer) {
        createChildComponents(vaadinComponent, (com.mymita.vaadlets.core.ComponentContainer) vaadletsComponent);
      }
    }
  }

  private com.vaadin.ui.Component createVaadinComponent(final com.mymita.vaadlets.core.Component c) {
    try {
      final String vaadinComponentClassName = "com.vaadin.ui." + c.getClass().getSimpleName();
      LOG.debug(format("Create vaadin component '%s'", vaadinComponentClassName));
      final Class<?> vaadinComponentClass = Class.forName(vaadinComponentClassName);
      final com.vaadin.ui.Component result = (com.vaadin.ui.Component) vaadinComponentClass.newInstance();
      setComponentAttributes(result, c);
      setPanelAttributes(result, c);
      setSplitPanelAttributes(result, c);
      setWindowAttributes(result, c);
      setLayoutAttributes(result, c);
      components.put(Objects.firstNonNull(c.getId(), UUID.randomUUID().toString()), result);
      return result;
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
      throw new RuntimeException(format("Can't find vaadin class for '%s'", c), e);
    }
  }

  public <T extends Component> T getComponent(final String id) {
    return (T) components.get(id);
  }

  public Component getRoot() {
    return root;
  }
}