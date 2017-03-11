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
 *   Original code by Carl Burch (http://www.cburch.com), 2011.
 *   Subsequent modifications by :
 *     + Haute École Spécialisée Bernoise
 *       http://www.bfh.ch
 *     + Haute École du paysage, d'ingénierie et d'architecture de Genève
 *       http://hepia.hesge.ch/
 *     + Haute École d'Ingénierie et de Gestion du Canton de Vaud
 *       http://www.heig-vd.ch/
 *   The project is currently maintained by :
 *     + REDS Institute - HEIG-VD
 *       Yverdon-les-Bains, Switzerland
 *       http://reds.heig-vd.ch
 *******************************************************************************/

package com.cburch.logisim.gui.prefs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.cburch.logisim.gui.scale.ScaledCheckBox;
import com.cburch.logisim.prefs.PrefMonitor;
import com.cburch.logisim.util.StringGetter;

class PrefBoolean extends ScaledCheckBox implements ActionListener,
		PropertyChangeListener {
	private static final long serialVersionUID = 1L;
	private PrefMonitor<Boolean> pref;
	private StringGetter title;

	PrefBoolean(PrefMonitor<Boolean> pref, StringGetter title) {
		super(title.toString());
		this.pref = pref;
		this.title = title;

		addActionListener(this);
		pref.addPropertyChangeListener(this);
		setSelected(pref.getBoolean());
	}

	public void actionPerformed(ActionEvent e) {
		pref.setBoolean(this.isSelected());
	}

	void localeChanged() {
		setText(title.toString());
	}

	public void propertyChange(PropertyChangeEvent event) {
		if (pref.isSource(event)) {
			setSelected(pref.getBoolean());
		}
	}
}
