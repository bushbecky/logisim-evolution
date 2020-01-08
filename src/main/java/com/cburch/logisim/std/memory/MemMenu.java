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

package com.cburch.logisim.std.memory;

import static com.cburch.logisim.std.Strings.S;

import com.cburch.logisim.circuit.CircuitState;
import com.cburch.logisim.file.Loader;
import com.cburch.logisim.file.LogisimFile;
import com.cburch.logisim.gui.hex.HexFile;
import com.cburch.logisim.gui.hex.HexFrame;
import com.cburch.logisim.instance.Instance;
import com.cburch.logisim.proj.Project;
import com.cburch.logisim.tools.MenuExtender;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

class MemMenu implements ActionListener, MenuExtender {
  private Mem factory;
  private Instance instance;
  private Project proj;
  private Frame frame;
  private CircuitState circState;
  private JMenuItem edit;
  private JMenuItem clear;
  private JMenuItem load;
  private JMenuItem save;

  MemMenu(Mem factory, Instance instance) {
    this.factory = factory;
    this.instance = instance;
  }

  public void actionPerformed(ActionEvent evt) {
    Object src = evt.getSource();
    if (src == edit) doEdit();
    else if (src == clear) doClear();
    else if (src == load) doLoad();
    else if (src == save) doSave();
  }

  public void configureMenu(JPopupMenu menu, Project proj) {
    this.proj = proj;
    this.frame = proj.getFrame();
    this.circState = proj.getCircuitState();

    Object attrs = instance.getAttributeSet();
    if (attrs instanceof RomAttributes) {
      ((RomAttributes) attrs).setProject(proj);
    }

    boolean enabled = circState != null;
    edit = createItem(enabled, S.get("ramEditMenuItem"));
    clear = createItem(enabled, S.get("ramClearMenuItem"));
    load = createItem(enabled, S.get("ramLoadMenuItem"));
    save = createItem(enabled, S.get("ramSaveMenuItem"));

    menu.addSeparator();
    menu.add(edit);
    menu.add(clear);
    menu.add(load);
    menu.add(save);
  }

  private JMenuItem createItem(boolean enabled, String label) {
    JMenuItem ret = new JMenuItem(label);
    ret.setEnabled(enabled);
    ret.addActionListener(this);
    return ret;
  }

  private void doClear() {
    MemState s = factory.getState(instance, circState);
    boolean isAllZero = s.getContents().isClear();
    if (isAllZero) return;

    int choice =
        JOptionPane.showConfirmDialog(
            frame,
            S.get("ramConfirmClearMsg"),
            S.get("ramConfirmClearTitle"),
            JOptionPane.YES_NO_OPTION);
    if (choice == JOptionPane.YES_OPTION) {
      s.getContents().clear();
    }
  }

  private void doEdit() {
    MemState s = factory.getState(instance, circState);
    if (s == null) return;
    HexFrame frame = factory.getHexFrame(proj, instance, circState);
    frame.setVisible(true);
    frame.toFront();
  }

  private void doLoad() {
    File oldSelected = factory.getCurrentImage(instance);
    JFileChooser chooser = HexFile.createFileOpenChooser(oldSelected);
    chooser.setDialogTitle(S.get("ramLoadDialogTitle"));
    int choice = chooser.showOpenDialog(frame);
    if (choice == JFileChooser.APPROVE_OPTION) {
      File f = chooser.getSelectedFile();
      try {
        factory.loadImage(circState.getInstanceState(instance), f);
      } catch (IOException e) {
        JOptionPane.showMessageDialog(
            frame, e.getMessage(), S.get("ramLoadErrorTitle"), JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  private void doSave() {
    MemState s = factory.getState(instance, circState);

    File oldSelected = factory.getCurrentImage(instance);
    if (oldSelected == null) {
      LogisimFile lf = proj.getLogisimFile();
      Loader ld = (lf == null ? null : lf.getLoader());
      oldSelected = (ld == null ? null : ld.getCurrentDirectory());
    }
    JFileChooser chooser = HexFile.createFileSaveChooser(oldSelected, (MemContents)s.getContents());   chooser.setDialogTitle(S.get("ramSaveDialogTitle"));
    int choice = chooser.showSaveDialog(frame);
    if (choice == JFileChooser.APPROVE_OPTION) {
      File f = chooser.getSelectedFile();
      try {
        HexFile.save(f, (MemContents)s.getContents(), chooser.getFileFilter());
        factory.setCurrentImage(instance, f);
      } catch (IOException e) {
        JOptionPane.showMessageDialog(
            frame, e.getMessage(), S.get("ramSaveErrorTitle"), JOptionPane.ERROR_MESSAGE);
      }
    }
  }
}
