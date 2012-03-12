package com.mymita.vaadlets.addon;

import static java.lang.String.format;

import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;

public class VaadletsBuilder {

  private static final Log LOG = LogFactory.getLog(VaadletsBuilder.class);

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
      ((com.vaadin.ui.Panel) vaadinParent).setContent((com.vaadin.ui.ComponentContainer) vaadinComponent);
    } else if (vaadinParent instanceof com.vaadin.ui.Form) {
      ((com.vaadin.ui.Form) vaadinParent).setLayout((com.vaadin.ui.Layout) vaadinComponent);
    } else if (vaadinParent instanceof com.vaadin.ui.GridLayout) {
      final Short column = vaadletComponent.getColumn();
      final Short row = vaadletComponent.getRow();
      final Short column2 = vaadletComponent.getColumn2();
      final Short row2 = vaadletComponent.getRow2();
      if (column != null && row != null && column2 != null && row2 != null) {
        ((com.vaadin.ui.GridLayout) vaadinParent).addComponent(vaadinComponent, column, row, column2, row2);
      } else if (column != null && row != null) {
        ((com.vaadin.ui.GridLayout) vaadinParent).addComponent(vaadinComponent, column, row);
      } else {
        ((com.vaadin.ui.GridLayout) vaadinParent).addComponent(vaadinComponent);
      }
    } else if (vaadinParent instanceof com.vaadin.ui.AbstractOrderedLayout) {
      final com.mymita.vaadlets.layout.Alignment alignment = vaadletComponent.getAlignment();
      final Short expandRatio = vaadletComponent.getExpandRatio();
      ((com.vaadin.ui.AbstractOrderedLayout) vaadinParent).addComponent(vaadinComponent);
      if (alignment != null) {
        ((com.vaadin.ui.AbstractOrderedLayout) vaadinParent).setComponentAlignment(vaadinComponent,
            alignmentOf(alignment));
      }
      if (expandRatio != null) {
        ((com.vaadin.ui.AbstractOrderedLayout) vaadinParent).setExpandRatio(vaadinComponent, expandRatio);
      }
    } else {
      ((ComponentContainer) vaadinParent).addComponent(vaadinComponent);
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

  private static void createChildComponents(final com.vaadin.ui.Component parent,
      final com.mymita.vaadlets.core.ComponentContainer r) {
    LOG.debug(format("Create child components for parent '%s'/'%s'", parent, r));
    for (final com.mymita.vaadlets.core.ComponentContainer cc : r.getComponents()) {
      final Component child = createVaadinComponent(cc);
      addComponent(parent, r, child, cc);
      createChildComponents(child, cc);
    }
  }

  private static com.vaadin.ui.Component createVaadinComponent(final com.mymita.vaadlets.core.Component c) {
    try {
      final String vaadinComponentClassName = "com.vaadin.ui." + c.getClass().getSimpleName();
      LOG.debug(format("Create vaadin component '%s'", vaadinComponentClassName));
      final Class<?> vaadinComponentClass = Class.forName(vaadinComponentClassName);
      final com.vaadin.ui.Component result = (com.vaadin.ui.Component) vaadinComponentClass.newInstance();
      setComponentAttributes(result, c);
      setPanelAttributes(result, c);
      setWindowAttributes(result, c);
      setLayoutAttributes(result, c);
      return result;
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
      throw new RuntimeException(format("Can't find vaadin class for '%s'", c), e);
    }
  }

  private static void setComponentAttributes(final com.vaadin.ui.Component result,
      final com.mymita.vaadlets.core.Component c) {
    result.setCaption(c.getCaption());
    result.setEnabled(c.isEnabled());
    result.setHeight(c.getHeight());
    result.setWidth(c.getWidth());
    result.setReadOnly(c.isReadonly());
    result.setVisible(c.isVisible());
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

  private com.vaadin.ui.Component root;

  public void build(final InputStream inputStream) {
    final com.mymita.vaadlets.core.ComponentContainer r = JAXBUtils.unmarshal(inputStream).getRootComponent();
    createChildComponents(root = createVaadinComponent(r), r);
  }

  public Component getRoot() {
    return root;
  }
}