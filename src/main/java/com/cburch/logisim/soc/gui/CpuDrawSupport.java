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

package com.cburch.logisim.soc.gui;

import static com.cburch.logisim.soc.Strings.S;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;

import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.instance.InstancePoker;
import com.cburch.logisim.instance.InstanceState;
import com.cburch.logisim.prefs.AppPreferences;
import com.cburch.logisim.soc.data.SocUpSimulationState;
import com.cburch.logisim.soc.data.SocUpStateInterface;
import com.cburch.logisim.soc.data.TraceInfo;
import com.cburch.logisim.util.GraphicsUtil;

public class CpuDrawSupport {
  
  public static final int NrOfTraces = 21;
  public static final int TRACEHEIGHT = 20;
  
  public static Bounds busConBounds = Bounds.create(50, 600, 280 , 20);
  public static Bounds simStateBounds = Bounds.create(340, 600, 270, 20);
  public static Bounds upStateBounds = Bounds.create(50,10,590,590);

  public static class SimStatePoker extends InstancePoker {
    @Override
    public void mousePressed(InstanceState state, MouseEvent e) {
      Location loc = state.getInstance().getLocation();
      Bounds bloc = SocUpSimulationState.getButtonLocation(loc.getX(), loc.getY(), simStateBounds);
      if (bloc.contains(e.getX(), e.getY())) { ((SocUpStateInterface)state.getData()).SimButtonPressed();
      }
    }
  }
  
  public static Bounds getBounds(int x , int y , int width , int height, boolean scale) {
    if (scale)
    return Bounds.create(AppPreferences.getScaled(x), AppPreferences.getScaled(y), 
    AppPreferences.getScaled(width), AppPreferences.getScaled(height));
    return Bounds.create(x, y, width, height);
  }

  public static int getBlockWidth(Graphics2D g2,boolean scale) {
    FontMetrics f =g2.getFontMetrics();
    int StrWidth = f.stringWidth("0x00000000")+(scale ? AppPreferences.getScaled(2) : 2);
    int blkPrefWidth = scale ? AppPreferences.getScaled(80) : 80;
    int blockWidth = StrWidth < blkPrefWidth ? blkPrefWidth : StrWidth;
    return blockWidth;
  }

  public static void drawRegisters(Graphics2D g, int x , int y, boolean scale, SocUpStateInterface cpu) {
    Graphics2D g2 = (Graphics2D) g.create();
    Bounds bds;
    if (scale) g2.setFont(AppPreferences.getScaledFont(g.getFont()));
    g2.translate(x, y);
    int blockWidth = getBlockWidth(g2,scale);
    int blockX = ((scale ? AppPreferences.getScaled(160):160)-blockWidth)/2;
    if (scale) {
      blockWidth = AppPreferences.getDownScaled(blockWidth);
      blockX = AppPreferences.getDownScaled(blockX);
    }
    g2.setColor(Color.YELLOW);
    bds = getBounds(0,0,160,495,scale);
    g2.fillRect(bds.getX(), bds.getY(), bds.getWidth(), bds.getHeight());
    g2.setColor(Color.BLUE);
    bds = getBounds(0,0,160,15,scale);
    g2.fillRect(bds.getX(), bds.getY(), bds.getWidth(), bds.getHeight());
    g2.setColor(Color.YELLOW);
    bds = getBounds(80,6,0,0,scale);
    GraphicsUtil.drawCenteredText(g2, S.get("Rv32imRegisterFile"), bds.getX(), bds.getY());
    g2.setColor(Color.BLACK);
    bds = getBounds(0,0,160,495,scale);
    g2.drawRect(bds.getX(), bds.getY(), bds.getWidth(), bds.getHeight());
    for (int i = 0 ; i < 32 ; i++) {
      bds = getBounds(20,21+i*15,0,0,scale);
      GraphicsUtil.drawCenteredText(g2, cpu.getRegisterNormalName(i), bds.getX(), bds.getY());
      g2.setColor(i==cpu.getLastRegisterWritten() ? Color.BLUE : Color.WHITE);
      bds = getBounds(blockX, 16+i*15, blockWidth, 13,scale);
      g2.fillRect(bds.getX(), bds.getY(), bds.getWidth(), bds.getHeight());
      g2.setColor(Color.BLACK);
      g2.drawRect(bds.getX(), bds.getY(), bds.getWidth(), bds.getHeight());
      g2.setColor(i==cpu.getLastRegisterWritten() ? Color.WHITE : Color.BLUE);
      bds = getBounds(blockX+blockWidth/2, 21+i*15,0,0,scale);
      GraphicsUtil.drawCenteredText(g2, cpu.getRegisterValueHex(i), bds.getX(), bds.getY());
      g2.setColor(Color.darkGray);
      bds = getBounds(140, 21+i*15,0,0,scale);
      GraphicsUtil.drawCenteredText(g2, cpu.getRegisterAbiName(i) , bds.getX(), bds.getY());
      g2.setColor(Color.BLACK);
    }
    g2.dispose();
  }

  public static void drawProgramCounter(Graphics2D g, int x , int y, boolean scale, SocUpStateInterface cpu) {
    Graphics2D g2 = (Graphics2D) g.create();
    Bounds bds;
    if (scale) g2.setFont(AppPreferences.getScaledFont(g.getFont()));
    bds = getBounds(x,y,0,0,scale);
    g2.translate(bds.getX(), bds.getY());
    int blockWidth = getBlockWidth(g2,scale);
    if (scale) blockWidth = AppPreferences.getDownScaled(blockWidth);
    g2.setColor(Color.YELLOW);
    bds = getBounds(0,0,blockWidth,30,scale);
    g2.fillRect(bds.getX(), bds.getY(), bds.getWidth(), bds.getHeight());
    g2.setColor(Color.BLUE);
    bds = getBounds(0,0,blockWidth,15,scale);
    g2.fillRect(bds.getX(), bds.getY(), bds.getWidth(), bds.getHeight());
    g2.setColor(Color.YELLOW);
    bds = getBounds(blockWidth/2,6,0,0,scale);
    GraphicsUtil.drawCenteredText(g2, S.get("Rv32imProgramCounter"), bds.getX(), bds.getY());
    g2.setColor(Color.BLACK);
    bds = getBounds(0,0,blockWidth,30,scale);
    g2.drawRect(bds.getX(), bds.getY(), bds.getWidth(), bds.getHeight());
    g2.setColor(Color.WHITE);
    bds = getBounds(1,16,blockWidth-2,13,scale);
    g2.fillRect(bds.getX(), bds.getY(), bds.getWidth(), bds.getHeight());
    g2.setColor(Color.BLACK);
    g2.drawRect(bds.getX(), bds.getY(), bds.getWidth(), bds.getHeight());
    g2.setColor(Color.RED);
    bds = getBounds(blockWidth/2,21,0,0,scale);
    GraphicsUtil.drawCenteredText(g2, String.format("0x%08X", cpu.getProgramCounter()), bds.getX(), bds.getY());
    g2.dispose();
  }

  public static void drawTrace(Graphics2D g, int x , int y, boolean scale, SocUpStateInterface cpu) {
    Graphics2D g2 = (Graphics2D) g.create();
    Bounds bds;
    if (scale) g2.setFont(AppPreferences.getScaledFont(g.getFont()));
    int blockWidth = getBlockWidth(g2,scale);
    if (scale) blockWidth = AppPreferences.getDownScaled(blockWidth);
    bds = getBounds(x,y,0,0,scale);
    g2.translate(bds.getX(), bds.getY());
    g2.setColor(Color.YELLOW);
    bds = getBounds(0,0,415,455,scale);
    g2.fillRect(bds.getX(), bds.getY(), bds.getWidth(), bds.getHeight());
    g2.setColor (Color.BLUE);
    bds = getBounds(0,0,415,15,scale);
    g2.fillRect(bds.getX(), bds.getY(), bds.getWidth(), bds.getHeight());
    g2.setColor(Color.YELLOW);
    bds = getBounds(207,6,0,0,scale);
    GraphicsUtil.drawCenteredText(g2, S.get("Rv32imExecutionTrace"), bds.getX(), bds.getY());
    g2.setColor(Color.BLACK);
    bds = getBounds(0,0,415,455,scale);
    g2.drawRect(bds.getX(), bds.getY(), bds.getWidth(), bds.getHeight());
    g2.setColor(Color.WHITE);
    bds = getBounds(5,15,blockWidth,15,scale);
    g2.fillRect(bds.getX(), bds.getY(), bds.getWidth(), bds.getHeight());
    bds = getBounds(10+blockWidth,15,blockWidth,15,scale);
    g2.fillRect(bds.getX(), bds.getY(), bds.getWidth(), bds.getHeight());
    bds = getBounds(15+2*blockWidth,15,395-2*blockWidth,15,scale);
    g2.fillRect(bds.getX(), bds.getY(), bds.getWidth(), bds.getHeight());
    g2.setColor(Color.BLACK);
    bds = getBounds(5+blockWidth/2,21,0,0,scale);
    GraphicsUtil.drawCenteredText(g2, S.get("Rv32imProgramCounter"), bds.getX(), bds.getY());
    bds = getBounds(10+blockWidth+blockWidth/2,21,0,0,scale);
    GraphicsUtil.drawCenteredText(g2, S.get("Rv32imBinInstruction"), bds.getX(), bds.getY());
    bds = getBounds(215+blockWidth,21,0,0,scale);
    GraphicsUtil.drawCenteredText(g2, S.get("Rv32imAsmInstruction"), bds.getX(), bds.getY());
    if (cpu.getTraces().isEmpty()) {
      bds = getBounds(207,250,0,0,scale);
      GraphicsUtil.drawCenteredText(g2, S.get("Rv32imEmptyTrace"), bds.getX(), bds.getY());
    } else {
      int yOff = 30;
      for (TraceInfo t : cpu.getTraces()) {
        t.paint(g2, yOff,scale);
        yOff += TRACEHEIGHT;
      }
    }
    g2.dispose();
  }

}
