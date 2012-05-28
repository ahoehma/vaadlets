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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.vaadin.codemirror.client.ui.CodeStyle;

import com.mymita.vaadlets.addon.CodeMirrorCodeStyle;
import com.vaadin.terminal.Sizeable;

/**
 * @author Andreas Höhmann
 * @since 0.0.1
 */
public final class VaadinUtils {

  public static com.vaadin.ui.Alignment alignmentOf(final com.mymita.vaadlets.layout.Alignment aAlignment) {
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
    throw new IllegalArgumentException(String.format("Unkown vaadin aligment '%s'", aAlignment));
  }

  public static CodeStyle codeMirrorCodeStyleOf(final CodeMirrorCodeStyle codeStyle) {
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

  public static float[] parseStringSize(String s) {
    final Pattern sizePattern = Pattern.compile("^(-?\\d+(\\.\\d+)?)(%|px|em|ex|in|cm|mm|pt|pc)?$");
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
}
