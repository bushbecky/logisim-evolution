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

package com.cburch.logisim.soc.pio;

import static com.cburch.logisim.soc.Strings.S;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import com.cburch.logisim.instance.Instance;
import com.cburch.logisim.proj.Project;
import com.cburch.logisim.tools.MenuExtender;

public class PioMenu implements ActionListener, MenuExtender {

  private Instance instance;
  private Frame frame;
  private JMenuItem exportC;
  
  public PioMenu(Instance inst) {
    instance = inst;
  }
  
  @Override
  public void configureMenu(JPopupMenu menu, Project proj) {
    this.frame = proj.getFrame();
    exportC = createItem(true,S.get("PioMenuExportC"));
    menu.addSeparator();
    menu.add(exportC);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    Object src = e.getSource();
    if (src == exportC)
      exportC();
  }

  private JMenuItem createItem(boolean enabled, String label) {
    JMenuItem ret = new JMenuItem(label);
    ret.setEnabled(enabled);
    ret.addActionListener(this);
    return ret;
  }
  
  private void exportC() {
    JFileChooser fc = new JFileChooser();
    fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    int result = fc.showDialog(frame, S.get("PioMenuSelectDirectoryToStoreC"));
    if (result == JFileChooser.APPROVE_OPTION) {
      PioState myState = instance.getAttributeValue(PioAttributes.PIO_STATE);
      if (myState == null)
        throw new NullPointerException("BUG in PioMenu.java");
      String compName = myState.getName().replace(" ", "_").replace("@", "_").toUpperCase();
      String headerFileName = fc.getSelectedFile().getAbsolutePath()+File.separator+compName+".h";
      String cFileName = fc.getSelectedFile().getAbsolutePath()+File.separator+compName+".c";
      FileWriter headerFile = null;
      FileWriter cFile = null;
      try {
        headerFile = new FileWriter(headerFileName,false);
      } catch (IOException e) {
    	headerFile = null;
      }
      try {
        cFile = new FileWriter(cFileName,false);
      } catch (IOException e) {
        cFile = null;
      }
      if (headerFile == null || cFile == null) {
        JOptionPane.showMessageDialog(frame, S.get("PioMenuErrorCreatingHeaderAndOrCFile"), S.get("PioMenuExportC"), JOptionPane.ERROR_MESSAGE);
        return;
      }
      PrintWriter headerWriter = new PrintWriter(headerFile);
      PrintWriter cWriter = new PrintWriter(cFile);
      headerWriter.println("/* Logisim automatically generated file for a PIO-component */\n");
      cWriter.println("/* Logisim automatically generated file for a PIO-component */\n");
      headerWriter.println("#ifndef __"+compName+"_H__");
      headerWriter.println("#define __"+compName+"_H__");
      headerWriter.println();
      int base = myState.getStartAddress();
      int nrBits = myState.getNrOfIOs().getWidth();
      String functName;
      if (myState.getPortDirection() != PioAttributes.PORT_INPUT) {
    	  functName = "OutputValue";
          headerWriter.println(S.fmt("PioMenuOutputDataFunctionRemark",Integer.toString(nrBits)));
          addSetterFunction(headerWriter,compName,functName,base,0,true);
          headerWriter.println();
          addSetterFunction(cWriter,compName,functName,base,0,false);
      }
      if (myState.getPortDirection() != PioAttributes.PORT_OUTPUT) {
        functName = "InputValue";
        headerWriter.println(S.fmt("PioMenuInputDataFunctionRemark",Integer.toString(nrBits)));
        addGetterFunction(headerWriter,compName,functName,base,0,true);
        headerWriter.println();
        addGetterFunction(cWriter,compName,functName,base,0,false);
        if (myState.getPortDirection() == PioAttributes.PORT_BIDIR) {
          functName = "DirectionReg";
          headerWriter.println(S.fmt("PioMenuBidirFunctionsRemark", Integer.toString(nrBits)));
          addAllFunctions(headerWriter,cWriter,compName,functName,base,2);
        }
        if (myState.inputGeneratesIrq()) {
          functName = "IrqMaskReg";
          String reactName = myState.getIrqType() == PioAttributes.IRQ_EDGE ? S.get("PioMenuIrqEdge") : S.get("PioMenuIrqLevel");
          headerWriter.println(S.fmt("PioMenuMaskFunctionsRemark", reactName, Integer.toString(nrBits)));          
          addAllFunctions(headerWriter,cWriter,compName,functName,base,2);
        }
        if (myState.inputIsCapturedSynchronisely()) {
          functName = "EdgeCapturReg";
          String EdgeName = S.get("PioMenuCaptureAny");
          if (myState.getInputCaptureEdge() == PioAttributes.CAPT_RISING)
            EdgeName = S.get("PioMenuCaptureRising");
          if (myState.getInputCaptureEdge() == PioAttributes.CAPT_FALLING)
            EdgeName = S.get("PioMenuCaptureFalling");
          String ClearName = myState.inputCaptureSupportsBitClearing() ? S.get("PioMenuCaptureBit") : S.get("PioMenuCaptureAll");
          headerWriter.println(S.fmt("PioMenuEdgeCaptureRemark", EdgeName, ClearName, Integer.toString(nrBits)));
          addAllFunctions(headerWriter,cWriter,compName,functName,base,3);
        }
        if (myState.outputSupportsBitManipulations()) {
          functName = "OutsetReg";
          headerWriter.println(S.fmt("PioMenuOutSetRemark", Integer.toString(nrBits)));
          addSetterFunction(headerWriter,compName,functName,base,4,true);
          headerWriter.println();
          addSetterFunction(cWriter,compName,functName,base,4,false);
          functName = "OutclearReg";
          headerWriter.println(S.fmt("PioMenuOutClearRemark", Integer.toString(nrBits)));
          addSetterFunction(headerWriter,compName,functName,base,5,true);
          headerWriter.println();
          addSetterFunction(cWriter,compName,functName,base,5,false);
        }
      }
      headerWriter.println("#endif");
      headerWriter.close();
      cWriter.close();
      JOptionPane.showMessageDialog(frame, S.fmt("PioMenuSuccesCreatingHeaderAndCFile", headerFileName, cFileName));
    }
  }
  
  private void addAllFunctions(PrintWriter h , PrintWriter c , String compName, String functName, int base, int index) {
    addSetterFunction(h,compName,functName,base,index,true);
    addGetterFunction(h,compName,functName,base,index,true);
    h.println();
    addSetterFunction(c,compName,functName,base,index,false);
    addGetterFunction(c,compName,functName,base,index,false);
  }
  
  private void addGetterFunction(PrintWriter w , String compName, String functName, int base, int index, boolean header) {
    w.print("unsigned int get"+compName+functName+"()");
    if (header) {
      w.println(";");
      return;
    } else w.println(" {");
    w.println("  volatile unsigned int* base;");
    w.println("  base = (unsigned int *) "+String.format("0x%X", base)+";");
    w.println("  return base["+index+"];");
    w.println("}\n");
  }
  
  private void addSetterFunction(PrintWriter w , String compName, String functName, int base, int index, boolean header) {
    w.print("void set"+compName+functName+"(unsigned int value)");
    if (header) {
      w.println(";");
      return;
    } else w.println(" {");
    w.println("  volatile unsigned int* base;");
    w.println("  base = (unsigned int *) "+String.format("0x%X", base)+";");
    w.println("  base["+index+"] = value;");
    w.println("}\n");
  }

}
