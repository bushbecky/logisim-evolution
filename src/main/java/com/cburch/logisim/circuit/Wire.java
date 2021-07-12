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

package com.cburch.logisim.circuit;

import static com.cburch.logisim.circuit.Strings.S;

import com.cburch.logisim.comp.Component;
import com.cburch.logisim.comp.ComponentDrawContext;
import com.cburch.logisim.comp.ComponentFactory;
import com.cburch.logisim.comp.EndData;
import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeOption;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.data.Attributes;
import com.cburch.logisim.data.BitWidth;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.fpga.designrulecheck.Netlist;
import com.cburch.logisim.tools.CustomHandles;
import com.cburch.logisim.util.Cache;
import com.cburch.logisim.util.GraphicsUtil;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Stroke;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public final class Wire implements Component, AttributeSet, CustomHandles, Iterable<Location> {
  private class EndList extends AbstractList<EndData> {
    @Override
    public EndData get(int i) {
      return getEnd(i);
    }

    @Override
    public int size() {
      return 2;
    }
  }

  public static Wire create(Location e0, Location e1) {
    return (Wire) cache.get(new Wire(e0, e1));
  }

  /** Stroke width when drawing wires. */
  public static final int WIDTH = 3;

  public static final int WIDTH_BUS = 4;
  public static final int HIGHLIGHTED_WIDTH = 4;
  public static final int HIGHLIGHTED_WIDTH_BUS = 5;
  public static final Stroke HIGHLIGHTED_STROKE = new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] {7}, 0);
  public static final double DOT_MULTIPLY_FACTOR = 1.35; // multiply factor for the intersection points
  public static final AttributeOption VALUE_HORZ = new AttributeOption("horz", S.getter("wireDirectionHorzOption"));
  public static final AttributeOption VALUE_VERT = new AttributeOption("vert", S.getter("wireDirectionVertOption"));

  public static final Attribute<AttributeOption> dir_attr = Attributes.forOption("direction", S.getter("wireDirectionAttr"), new AttributeOption[] {VALUE_HORZ, VALUE_VERT});
  public static final Attribute<Integer> len_attr = Attributes.forInteger("length", S.getter("wireLengthAttr"));

  private static final List<Attribute<?>> ATTRIBUTES = Arrays.asList(dir_attr, len_attr);

  private static final Cache cache = new Cache();

  final Location e0;
  final Location e1;
  final boolean is_x_equal;
  private boolean DRCHighlighted = false;
  private Color DRCHighlightColor = Netlist.DRC_WIRE_MARK_COLOR;

  private Wire(Location e0, Location e1) {
    this.is_x_equal = e0.getX() == e1.getX();
    if (is_x_equal) {
      if (e0.getY() > e1.getY()) {
        this.e0 = e1;
        this.e1 = e0;
      } else {
        this.e0 = e0;
        this.e1 = e1;
      }
    } else {
      if (e0.getX() > e1.getX()) {
        this.e0 = e1;
        this.e1 = e0;
      } else {
        this.e0 = e0;
        this.e1 = e1;
      }
    }
  }

  // Component methods
  //
  // (Wire never issues ComponentEvents, so we don't need ComponentListener to track listeners,
  // hence no addComponentListener() implementation.

  //
  // AttributeSet methods
  //
  // It makes some sense for a wire to be its own attribute, since
  // after all it is immutable.
  //
  @Override
  public Object clone() {
    return this;
  }

  @Override
  public boolean contains(Location q) {
    int qx = q.getX();
    int qy = q.getY();
    if (is_x_equal) {
      int wx = e0.getX();
      return qx >= wx - 2 && qx <= wx + 2 && e0.getY() <= qy && qy <= e1.getY();
    } else {
      int wy = e0.getY();
      return qy >= wy - 2 && qy <= wy + 2 && e0.getX() <= qx && qx <= e1.getX();
    }
  }

  @Override
  public boolean contains(Location pt, Graphics g) {
    return contains(pt);
  }

  @Override
  public boolean containsAttribute(Attribute<?> attr) {
    return ATTRIBUTES.contains(attr);
  }

  @Override
  public void draw(ComponentDrawContext context) {
    final var state = context.getCircuitState();
    final var g = context.getGraphics();
    GraphicsUtil.switchToWidth(g, WIDTH);
    g.setColor(state.getValue(e0).getColor());
    g.drawLine(e0.getX(), e0.getY(), e1.getX(), e1.getY());
  }

  @Override
  public void drawHandles(ComponentDrawContext context) {
    context.drawHandle(e0);
    context.drawHandle(e1);
  }

  @Override
  public boolean endsAt(Location pt) {
    return e0.equals(pt) || e1.equals(pt);
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof Wire)) return false;
    final var w = (Wire) other;
    return w.e0.equals(this.e0) && w.e1.equals(this.e1);
  }

  //
  // user interface methods
  //
  @Override
  public void expose(ComponentDrawContext context) {
    java.awt.Component dest = context.getDestination();
    final var x0 = e0.getX();
    final var y0 = e0.getY();
    dest.repaint(x0 - 5, y0 - 5, e1.getX() - x0 + 10, e1.getY() - y0 + 10);
  }

  @Override
  public Attribute<?> getAttribute(String name) {
    for (Attribute<?> attr : ATTRIBUTES) {
      if (name.equals(attr.getName())) return attr;
    }
    return null;
  }

  @Override
  public List<Attribute<?>> getAttributes() {
    return ATTRIBUTES;
  }

  @Override
  public AttributeSet getAttributeSet() {
    return this;
  }

  @Override
  public Bounds getBounds() {
    final var x0 = e0.getX();
    final var y0 = e0.getY();
    return Bounds.create(x0 - 2, y0 - 2, e1.getX() - x0 + 5, e1.getY() - y0 + 5);
  }

  @Override
  public Bounds getBounds(Graphics g) {
    return getBounds();
  }

  @Override
  public EndData getEnd(int index) {
    return new EndData(getEndLocation(index), BitWidth.UNKNOWN, EndData.INPUT_OUTPUT);
  }

  public Location getEnd0() {
    return e0;
  }

  public Location getEnd1() {
    return e1;
  }

  public Location getEndLocation(int index) {
    return index == 0 ? e0 : e1;
  }

  //
  // propagation methods
  //
  @Override
  public List<EndData> getEnds() {
    return new EndList();
  }

  @Override
  public ComponentFactory getFactory() {
    return WireFactory.instance;
  }

  @Override
  public Object getFeature(Object key) {
    if (key == CustomHandles.class) return this;
    return null;
  }

  public int getLength() {
    return (e1.getY() - e0.getY()) + (e1.getX() - e0.getX());
  }

  // location/extent methods
  @Override
  public Location getLocation() {
    return e0;
  }

  public Location getOtherEnd(Location loc) {
    return (loc.equals(e0) ? e1 : e0);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <V> V getValue(Attribute<V> attr) {
    if (attr == dir_attr) {
      return (V) (is_x_equal ? VALUE_VERT : VALUE_HORZ);
    } else if (attr == len_attr) {
      return (V) Integer.valueOf(getLength());
    } else {
      return null;
    }
  }

  @Override
  public int hashCode() {
    return e0.hashCode() * 31 + e1.hashCode();
  }

  public boolean isParallel(Wire other) {
    return this.is_x_equal == other.is_x_equal;
  }

  @Override
  public boolean isReadOnly(Attribute<?> attr) {
    return true;
  }

  @Override
  public boolean isToSave(Attribute<?> attr) {
    return false;
  }

  //
  // other methods
  //
  public boolean isVertical() {
    return is_x_equal;
  }

  @Override
  public Iterator<Location> iterator() {
    return new WireIterator(e0, e1);
  }

  private boolean overlaps(Location q0, Location q1, boolean includeEnds) {
    if (is_x_equal) {
      int x0 = q0.getX();
      if (x0 != q1.getX() || x0 != e0.getX()) return false;
      if (includeEnds) {
        return e1.getY() >= q0.getY() && e0.getY() <= q1.getY();
      } else {
        return e1.getY() > q0.getY() && e0.getY() < q1.getY();
      }
    } else {
      int y0 = q0.getY();
      if (y0 != q1.getY() || y0 != e0.getY()) return false;
      if (includeEnds) {
        return e1.getX() >= q0.getX() && e0.getX() <= q1.getX();
      } else {
        return e1.getX() > q0.getX() && e0.getX() < q1.getX();
      }
    }
  }

  public boolean overlaps(Wire other, boolean includeEnds) {
    return overlaps(other.e0, other.e1, includeEnds);
  }

  @Override
  public void propagate(CircuitState state) {
    // Normally this is handled by CircuitWires, and so it won't get
    // called. The exception is when a wire is added or removed
    state.markPointAsDirty(e0);
    state.markPointAsDirty(e1);
  }

  @Override
  public void setReadOnly(Attribute<?> attr, boolean value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <V> void setValue(Attribute<V> attr, V value) {
    throw new IllegalArgumentException("read only attribute");
  }

  public boolean sharesEnd(Wire other) {
    return this.e0.equals(other.e0)
        || this.e1.equals(other.e0)
        || this.e0.equals(other.e1)
        || this.e1.equals(other.e1);
  }

  @Override
  public String toString() {
    return "Wire[" + e0 + "-" + e1 + "]";
  }

  public void SetDRCHighlight(boolean Highlight) {
    DRCHighlighted = Highlight;
  }

  public boolean IsDRCHighlighted() {
    return DRCHighlighted;
  }

  public void SetDRCHighlightColor(Color col) {
    DRCHighlightColor = col;
  }

  public Color GetDRCHighlightColor() {
    return DRCHighlightColor;
  }
}
