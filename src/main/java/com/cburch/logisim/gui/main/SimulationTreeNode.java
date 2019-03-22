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
package com.cburch.logisim.gui.main;

import com.cburch.logisim.comp.ComponentFactory;
import java.util.Enumeration;
import javax.swing.tree.TreeNode;

public abstract class SimulationTreeNode implements TreeNode {
  public abstract Enumeration<? extends TreeNode> children();

  public abstract boolean getAllowsChildren();

  public abstract TreeNode getChildAt(int childIndex);

  public abstract int getChildCount();

  public abstract ComponentFactory getComponentFactory();

  public abstract int getIndex(TreeNode node);

  public abstract TreeNode getParent();

  public boolean isCurrentView(SimulationTreeModel model) {
    return false;
  }

  public abstract boolean isLeaf();
}
