/*******************************************************************************
 * This file is part of logisim-evolution.
 *
 *   logisim-evolution is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   logisim-evolution is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with logisim-evolution.  If not, see <http://www.gnu.org/licenses/>.
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
 *******************************************************************************/

package com.cburch.logisim.fpga.fpgaboardeditor;

import java.awt.Dimension;
import java.util.Hashtable;

import javax.swing.JLabel;
import javax.swing.JSlider;

import com.cburch.logisim.prefs.AppPreferences;

@SuppressWarnings("serial")
public class ZoomSlider extends JSlider {

	 public ZoomSlider() {
		 JLabel label;
		 super.setOrientation(JSlider.HORIZONTAL);
		 super.setMinimum(100);
		 super.setMaximum(200);
		 super.setValue(100);
		 Dimension orig=super.getSize();
		 orig.height=AppPreferences.getScaled(orig.height);
		 orig.width=AppPreferences.getScaled(orig.width);
		 super.setSize(orig);
		 setMajorTickSpacing(50);
		 setMinorTickSpacing(10);
		 setPaintTicks(true);
		 Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
		 label = new JLabel("1.0x");
		 label.setFont(AppPreferences.getScaledFont(label.getFont()));
		 labelTable.put(new Integer(100), label);
		 label = new JLabel("1.5x");
		 label.setFont(AppPreferences.getScaledFont(label.getFont()));
		 labelTable.put(new Integer(150), label);
		 label = new JLabel("2.0x");
		 label.setFont(AppPreferences.getScaledFont(label.getFont()));
		 labelTable.put(new Integer(200), label);
		 setLabelTable(labelTable);
		 setPaintLabels(true);
	 }
}
