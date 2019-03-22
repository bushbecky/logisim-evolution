/**
 * ***************************************************************************** This file is part
 * of logisim-evolution.
 *
 * <p>logisim-evolution is free software: you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * <p>logisim-evolution is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * <p>You should have received a copy of the GNU General Public License along with
 * logisim-evolution. If not, see <http://www.gnu.org/licenses/>.
 *
 * <p>Original code by Carl Burch (http://www.cburch.com), 2011. Subsequent modifications by: +
 * College of the Holy Cross http://www.holycross.edu + Haute École Spécialisée Bernoise/Berner
 * Fachhochschule http://www.bfh.ch + Haute École du paysage, d'ingénierie et d'architecture de
 * Genève http://hepia.hesge.ch/ + Haute École d'Ingénierie et de Gestion du Canton de Vaud
 * http://www.heig-vd.ch/
 * *****************************************************************************
 */
package com.cburch.logisim.std.wiring;

import com.cburch.logisim.circuit.RadixOption;
import com.cburch.logisim.comp.EndData;
import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeOption;
import com.cburch.logisim.data.BitWidth;
import com.cburch.logisim.instance.StdAttr;
import java.util.Arrays;
import java.util.List;

class PinAttributes extends ProbeAttributes {
  public static PinAttributes instance = new PinAttributes();

  private static final List<Attribute<?>> ATTRIBUTES =
      Arrays.asList(
          new Attribute<?>[] {
            StdAttr.FACING,
            Pin.ATTR_TYPE,
            StdAttr.WIDTH,
            Pin.ATTR_TRISTATE,
            Pin.ATTR_PULL,
            StdAttr.LABEL,
            StdAttr.LABEL_FONT,
            RadixOption.ATTRIBUTE,
            PROBEAPPEARANCE
          });

  BitWidth width = BitWidth.ONE;
  boolean threeState = false; // true;
  int type = EndData.INPUT_ONLY;
  Object pull = Pin.PULL_NONE;
  AttributeOption Appearance = ProbeAttributes.APPEAR_EVOLUTION_NEW;

  public PinAttributes() {}

  @Override
  public List<Attribute<?>> getAttributes() {
    return ATTRIBUTES;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <V> V getValue(Attribute<V> attr) {
    if (attr == StdAttr.WIDTH) return (V) width;
    if (attr == Pin.ATTR_TRISTATE) return (V) Boolean.valueOf(threeState);
    if (attr == Pin.ATTR_TYPE) return (V) Boolean.valueOf(type == EndData.OUTPUT_ONLY);
    if (attr == Pin.ATTR_PULL) return (V) pull;
    if (attr == PROBEAPPEARANCE) return (V) Appearance;
    return super.getValue(attr);
  }

  boolean isInput() {
    return type != EndData.OUTPUT_ONLY;
  }

  boolean isOutput() {
    return type != EndData.INPUT_ONLY;
  }

  @Override
  public <V> void setValue(Attribute<V> attr, V value) {
    if (attr == StdAttr.WIDTH) {
      BitWidth NewWidth = (BitWidth) value;
      if (width == NewWidth) return;
      width = (BitWidth) value;
      if (width.getWidth() > 8 && Appearance == ProbeAttributes.APPEAR_EVOLUTION_NEW)
        super.setValue(RadixOption.ATTRIBUTE, RadixOption.RADIX_16);
    } else if (attr == Pin.ATTR_TRISTATE) {
      boolean NewThree = ((Boolean) value).booleanValue();
      if (threeState == NewThree) return;
      threeState = NewThree;
    } else if (attr == Pin.ATTR_TYPE) {
      int Newtype = ((Boolean) value).booleanValue() ? EndData.OUTPUT_ONLY : EndData.INPUT_ONLY;
      if (type == Newtype) return;
      type = Newtype;
    } else if (attr == Pin.ATTR_PULL) {
      Object newPull = value;
      if (pull.equals(newPull)) return;
      pull = newPull;
    } else if (attr == PROBEAPPEARANCE) {
      AttributeOption NewAppearance = (AttributeOption) value;
      if (Appearance.equals(NewAppearance)) return;
      Appearance = NewAppearance;
    } else if (attr == RadixOption.ATTRIBUTE) {
      if (width.getWidth() == 1) {
        super.setValue(RadixOption.ATTRIBUTE, RadixOption.RADIX_2);
      } else super.setValue(attr, value);
      return;
    } else {
      super.setValue(attr, value);
      return;
    }
    fireAttributeValueChanged(attr, value, null);
  }
}
