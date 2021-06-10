/*
 * This file is part of logisim-evolution.
 *
 * Logisim-evolution is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Logisim-evolution is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with logisim-evolution. If not, see <http://www.gnu.org/licenses/>.
 *
 * Original code by Marcin Orlowski (http://MarcinOrlowski.com), 2021
 */

package com.cburch.logisim.gui.icons;

import java.awt.*;
import java.awt.font.TextLayout;

public class LedClusterIcon extends AnimatedIcon {

  private int state = 0;

  @Override
  public void animationUpdate() {
    state = (state + 1) % 4;
  }

  @Override
  public void resetToStatic() {
    state = 0;
  }

  @Override
  protected void paintIcon(Graphics2D g2) {
    g2.setColor(Color.DARK_GRAY);
    g2.fillRect(0, 0, getIconWidth(), getIconHeight());

    Color col1, col2;
    switch (state) {
      case 0:
        col1 = Color.gray;
        col2 = Color.gray;
        break;
      case 1:
        col1 = Color.green;
        col2 = Color.gray;
        break;
      case 3:
        col1 = Color.gray;
        col2 = Color.green;
        break;
      case 4:
      default:
        col1 = Color.green;
        col2 = Color.green;
        break;
    }

    int y = scale(2);
    int h = scale(12);
    int w = scale(5);
    g2.setColor(col1);
    g2.fillRect(scale(2), y, w, h);
    g2.setColor(col2);
    g2.fillRect(scale(9), y, w, h);
  }
}
