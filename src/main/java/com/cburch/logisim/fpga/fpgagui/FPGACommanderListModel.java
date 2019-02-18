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

package com.cburch.logisim.fpga.fpgagui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.swing.AbstractListModel;
import javax.swing.ListCellRenderer;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import com.cburch.logisim.fpga.designrulecheck.SimpleDRCContainer;
import com.cburch.logisim.fpga.gui.ListModelCellRenderer;

@SuppressWarnings("serial")
public class FPGACommanderListModel extends  AbstractListModel<Object> {
	
	private ArrayList<Object> myData;
	private Set<ListDataListener> myListeners;
	private int count = 0;
	private ListModelCellRenderer MyRender;
	
	public FPGACommanderListModel(boolean CountLines) {
		myData = new ArrayList<Object>();
		myListeners = new HashSet<ListDataListener>();
		MyRender = new ListModelCellRenderer(CountLines);
	}
	
	public ListCellRenderer<Object> getMyRenderer() {
		return MyRender;
	}
	
	public void clear() {
		myData.clear();
		count = 0;
		FireEvent(null);
	}
	
	public void add(Object toAdd) {
		count++;
		if (toAdd instanceof SimpleDRCContainer) {
			SimpleDRCContainer add = (SimpleDRCContainer) toAdd;
			if (add.SupressCount())
				count--;
			else
				add.SetListNumber(count);
		}
		myData.add(toAdd);
		ListDataEvent e = new ListDataEvent(this,ListDataEvent.CONTENTS_CHANGED,0,myData.size());
		FireEvent(e);
	}
	
	public int getCountNr() {
		return count;
	}
	
	@Override
	public int getSize() {
		return myData.size();
	}

	@Override
	public Object getElementAt(int index) {
		if (index < myData.size())
			return myData.get(index);
		return null;
	}

	@Override
	public void addListDataListener(ListDataListener l) {
		myListeners.add(l);
	}

	@Override
	public void removeListDataListener(ListDataListener l) {
		myListeners.remove(l);
	}
	
	private void FireEvent(ListDataEvent e) {
		for (ListDataListener l :myListeners)
			l.contentsChanged(e);
	}

}
