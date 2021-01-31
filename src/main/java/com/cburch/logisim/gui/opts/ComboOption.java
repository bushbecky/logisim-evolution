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
 * Original code by Carl Burch (http://www.cburch.com), 2011.
 * Subsequent modifications by:
 *   + College of the Holy Cross
 *     http://www.holycross.edu
 *   + Haute École Spécialisée Bernoise/Berner Fachhochschule
 *     http://www.bfh.ch
 *   + Haute École du paysage, d'ingénierie et d'architecture de Genève
 *     http://hepia.hesge.ch/
 *   + Haute École d'Ingénierie et de Gestion du Canton de Vaud
 *     http://www.heig-vd.ch/
 */

package com.cburch.logisim.gui.opts;

import com.cburch.logisim.data.AttributeOption;
import com.cburch.logisim.util.StringGetter;
import javax.swing.JComboBox;

class ComboOption {
  @SuppressWarnings("rawtypes")
  static void setSelected(JComboBox combo, Object value) {
    for (int i = combo.getItemCount() - 1; i >= 0; i--) {
      ComboOption opt = (ComboOption) combo.getItemAt(i);
      if (opt.getValue().equals(value)) {
        combo.setSelectedItem(opt);
        return;
      }
    }
    combo.setSelectedItem(combo.getItemAt(0));
  }

  private final Object value;

  private final StringGetter getter;

  ComboOption(AttributeOption value) {
    this.value = value;
    this.getter = null;
  }

  ComboOption(String value, StringGetter getter) {
    this.value = value;
    this.getter = getter;
  }

  public Object getValue() {
    return value;
  }

  @Override
  public String toString() {
    if (getter != null) return getter.toString();
    if (value instanceof AttributeOption) return ((AttributeOption) value).toDisplayString();
    return "???";
  }
}
