/**
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

package com.cburch.logisim.soc.rv32im;

import static com.cburch.logisim.soc.Strings.S;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;

import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Direction;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.gui.icons.ArithmeticIcon;
import com.cburch.logisim.instance.Instance;
import com.cburch.logisim.instance.InstancePainter;
import com.cburch.logisim.instance.InstancePoker;
import com.cburch.logisim.instance.InstanceState;
import com.cburch.logisim.instance.Port;
import com.cburch.logisim.instance.StdAttr;
import com.cburch.logisim.soc.data.SocBusSlaveInterface;
import com.cburch.logisim.soc.data.SocBusSnifferInterface;
import com.cburch.logisim.soc.data.SocInstanceFactory;
import com.cburch.logisim.soc.data.SocProcessorInterface;
import com.cburch.logisim.soc.data.SocSimulationManager;
import com.cburch.logisim.soc.data.SocUpSimulationState;
import com.cburch.logisim.tools.MenuExtender;
import com.cburch.logisim.util.GraphicsUtil;

public class Rv32im_riscv extends SocInstanceFactory {
  
  public static class SimStatePoker extends InstancePoker {
    @Override
    public void mousePressed(InstanceState state, MouseEvent e) {
      Location loc = state.getInstance().getLocation();
      Bounds bloc = SocUpSimulationState.getButtonLocation(loc.getX(), loc.getY(), simStateBounds);
      if (bloc.contains(e.getX(), e.getY())) {
        state.getAttributeValue(RV32imAttributes.RV32IM_STATE).simButtonPressed();
      }
    }
  }
  
  private static Bounds busConBounds = Bounds.create(50, 600, 280 , 20);
  public static Bounds simStateBounds = Bounds.create(340, 600, 270, 20);
  public static Bounds upStateBounds = Bounds.create(50,10,590,590);

  public Rv32im_riscv() {
    super("Rv32im",S.getter("Rv32imComponent"),SocMaster);
    setIcon(new ArithmeticIcon("uP",2));
    setOffsetBounds(Bounds.create(0, 0, 640, 640));
    setInstancePoker(SimStatePoker.class);
  }

  @Override
  public AttributeSet createAttributeSet() {
    return new RV32imAttributes();
  }
  
  @Override
  protected Object getInstanceFeature(Instance instance, Object key) {
    if (key == MenuExtender.class) {
      return new RV32imMenu(instance);
    }
    return super.getInstanceFeature(instance, key);
  }

  private void updatePorts(Instance instance) {
    int NrOfIrqs = instance.getAttributeValue(RV32imAttributes.RV32IM_STATE).getNrOfIrqs();
    Port[] ps = new Port[NrOfIrqs+2];
    ps[0] = new Port(0,610,Port.INPUT,1);
    ps[0].setToolTip(S.getter("Rv32imResetInput"));
    ps[1] = new Port(0,630,Port.INPUT,1);
    ps[1].setToolTip(S.getter("Rv32imClockInput"));
    for (int i = 0 ; i < NrOfIrqs ; i++) {
      ps[i+2] = new Port(0,10+i*10,Port.INPUT,1);
      ps[i+2].setToolTip(S.getter("Rv32imIrqInput", Integer.toString(i)));
    }
    instance.setPorts(ps);
  }
	
  @Override
  protected void configureNewInstance(Instance instance) {
    instance.addAttributeListener();
    updatePorts(instance);
    Bounds bds = instance.getBounds();
    instance.setTextField(
            StdAttr.LABEL,
            StdAttr.LABEL_FONT,
            bds.getX() + bds.getWidth() / 2,
            bds.getY() - 3,
            GraphicsUtil.H_CENTER,
            GraphicsUtil.V_BASELINE);
  }

  @Override
  protected void instanceAttributeChanged(Instance instance, Attribute<?> attr) {
    if (attr == RV32imAttributes.NR_OF_IRQS) {
      updatePorts(instance);
    }
    if (attr == SocSimulationManager.SOC_BUS_SELECT) {
      instance.fireInvalidated();
    }
    super.instanceAttributeChanged(instance, attr);
  }

  @Override
  public void paintInstance(InstancePainter painter) {
    RV32im_state state = painter.getAttributeValue(RV32imAttributes.RV32IM_STATE);
    Location loc = painter.getLocation();
    Graphics2D g2 = (Graphics2D)painter.getGraphics();
    painter.drawBounds();
    painter.drawLabel();
    painter.drawClock(1, Direction.EAST);
    painter.drawPort(0, "Reset", Direction.EAST);
    for (int i = 0 ; i < painter.getAttributeValue(RV32imAttributes.RV32IM_STATE).getNrOfIrqs() ; i++) {
      painter.drawPort(i+2, "IRQ"+i, Direction.EAST);
    }
    Font f = g2.getFont();
    g2.setFont(StdAttr.DEFAULT_LABEL_FONT);
    GraphicsUtil.drawCenteredText(g2, "RISC V IM simulator", loc.getX()+320, loc.getY()+630);
    g2.setFont(f);
    painter.getAttributeValue(SocSimulationManager.SOC_BUS_SELECT).paint(g2, 
    		Bounds.create(loc.getX()+busConBounds.getX(), loc.getY()+busConBounds.getY(), 
    				busConBounds.getWidth(), busConBounds.getHeight()));
    state.paint(loc.getX(), loc.getY(), g2,painter.getInstance(),painter.getAttributeValue(RV32imAttributes.RV32IM_STATE_VISABLE));
  }

  @Override
  public void propagate(InstanceState state) {
    state.getAttributeValue(RV32imAttributes.RV32IM_STATE).updateClock(state.getPortValue(1), state.getPortValue(0));
  }

  @Override
  public SocBusSlaveInterface getSlaveInterface(AttributeSet attrs) { return null; }

  @Override
  public SocBusSnifferInterface getSnifferInterface(AttributeSet attrs) { return null; }

  @Override
  public SocProcessorInterface getProcessorInterface(AttributeSet attrs) {
    return attrs.getValue(RV32imAttributes.RV32IM_STATE);
  }
  
}
