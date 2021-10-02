/*
 * Logisim-evolution - digital logic design tool and simulator
 * Copyright by the Logisim-evolution developers
 *
 * https://github.com/logisim-evolution/
 *
 * This is free software released under GNU GPLv3 license
 */

package com.cburch.logisim.std.bfh;

import static com.cburch.logisim.std.Strings.S;

import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.BitWidth;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Direction;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.instance.InstanceFactory;
import com.cburch.logisim.instance.InstancePainter;
import com.cburch.logisim.instance.InstanceState;
import com.cburch.logisim.instance.Port;
import com.cburch.logisim.instance.StdAttr;
import java.awt.Color;
import java.awt.Graphics;

public class bcd2sevenseg extends InstanceFactory {
  /**
   * Unique identifier of the tool, used as reference in project files.
   * Do NOT change as it will prevent project files from loading.
   *
   * Identifier value must MUST be unique string among all tools.
   */
  public static final String _ID = "BCD_to_7_Segment_decoder";

  static final int PER_DELAY = 1;
  public static final int Segment_A = 0;
  public static final int Segment_B = 1;
  public static final int Segment_C = 2;
  public static final int Segment_D = 3;
  public static final int Segment_E = 4;
  public static final int Segment_F = 5;
  public static final int Segment_G = 6;
  public static final int BCDin = 7;

  public bcd2sevenseg() {
    super(_ID, S.getter("BCD2SevenSegment"), new bcd2sevensegHDLGeneratorFactory());
    setAttributes(new Attribute[] {StdAttr.DUMMY}, new Object[] {""});
    setOffsetBounds(Bounds.create(-10, -20, 50, 100));
    Port[] ps = new Port[8];
    ps[Segment_A] = new Port(20, 0, Port.OUTPUT, 1);
    ps[Segment_B] = new Port(30, 0, Port.OUTPUT, 1);
    ps[Segment_C] = new Port(20, 60, Port.OUTPUT, 1);
    ps[Segment_D] = new Port(10, 60, Port.OUTPUT, 1);
    ps[Segment_E] = new Port(0, 60, Port.OUTPUT, 1);
    ps[Segment_F] = new Port(10, 0, Port.OUTPUT, 1);
    ps[Segment_G] = new Port(0, 0, Port.OUTPUT, 1);
    ps[BCDin] = new Port(10, 80, Port.INPUT, 4);
    ps[Segment_A].setToolTip(S.getter("Segment_A"));
    ps[Segment_B].setToolTip(S.getter("Segment_B"));
    ps[Segment_C].setToolTip(S.getter("Segment_C"));
    ps[Segment_D].setToolTip(S.getter("Segment_D"));
    ps[Segment_E].setToolTip(S.getter("Segment_E"));
    ps[Segment_F].setToolTip(S.getter("Segment_F"));
    ps[Segment_G].setToolTip(S.getter("Segment_G"));
    ps[BCDin].setToolTip(S.getter("BCDValue"));
    setPorts(ps);
  }

  @Override
  public void paintInstance(InstancePainter painter) {
    Graphics g = painter.getGraphics();
    Bounds MyBounds = painter.getBounds();
    if (!painter.isPrintView())
      g.setColor(Color.BLUE);
    painter.drawRectangle(MyBounds, "");
    painter.drawPort(BCDin, "BCD", Direction.SOUTH);
    for (int i = 0; i < 7; i++) painter.drawPort(i);
    g.setColor(Color.BLACK);
    painter.drawRectangle(
        MyBounds.getX() + 5,
        MyBounds.getY() + 20,
        MyBounds.getWidth() - 10,
        MyBounds.getHeight() - 40,
        "");
  }

  /**
   * Sets all segments' ports to known value.
   *
   * @param state Instance state.
   * @param portValues Binary encoded port values (0/1). Segment A is bit 0, segment G is bit 6.
   */
  private void setKnown(InstanceState state, int portValues) {
    final var mask = 0b00000001;
    for (var idx = Segment_A; idx <= Segment_G; idx++) {
      final var value = (portValues & mask) == 0 ? 0 : 1;
      state.setPort(idx, Value.createKnown(BitWidth.create(1), value), PER_DELAY);
      portValues >>= 1;
    }
  }

  /**
   * Sets all segments' ports to unknown.
   *
   * @param state Instance state.
   */
  private void setUnknown(InstanceState state) {
    for (var idx = Segment_A; idx <= Segment_G; idx++) {
      state.setPort(idx, Value.createUnknown(BitWidth.create(1)), PER_DELAY);
    }
  }

  @Override
  public void propagate(InstanceState state) {
    if (state.getPortValue(BCDin).isFullyDefined()
        & !state.getPortValue(BCDin).isErrorValue()
        & !state.getPortValue(BCDin).isUnknown()) {

      switch ((int) state.getPortValue(BCDin).toLongValue()) {
        case 0 -> setKnown(state, 0b1111110);
        case 1 -> setKnown(state, 0b0110000);
        case 2 -> setKnown(state, 0b1101101);
        case 3 -> setKnown(state, 0b1111001);
        case 4 -> setKnown(state, 0b0110011);
        case 5 -> setKnown(state, 0b1011011);
        case 6 -> setKnown(state, 0b1011111);
        case 7 -> setKnown(state, 0b1110000);
        case 8 -> setKnown(state, 0b1111111);
        case 9 -> setKnown(state, 0b1111011);
        default -> setUnknown(state);
      }
    } else {
      setUnknown(state);
    }
  }
}
