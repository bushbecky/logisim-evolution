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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cburch.logisim.gui.menu;

/*import javax.swing.event.CaretEvent;
 import javax.swing.event.CaretListener;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.Highlighter;
 import javax.swing.text.JTextComponent;*/
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.Document;

import com.cburch.logisim.circuit.Circuit;
import com.cburch.logisim.circuit.CircuitState;
import com.cburch.logisim.circuit.SimulatorEvent;
import com.cburch.logisim.circuit.SimulatorListener;
import com.cburch.logisim.comp.Component;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.file.Options;
import com.cburch.logisim.gui.generic.LFrame;
import com.cburch.logisim.instance.StdAttr;
import com.cburch.logisim.proj.Project;
import com.cburch.logisim.std.memory.Register;
import com.cburch.logisim.std.wiring.Clock;

public class AssemblyWindow implements ActionListener, WindowListener,
		SimulatorListener, KeyListener {

	private Preferences prefs;
	private LFrame windows;
	private JMenuBar winMenuBar;
	private JCheckBoxMenuItem ontopItem;
	private JMenuItem openFileItem;
	private JMenuItem reloadFileItem;
	private JMenuItem close;
	private JButton refresh = new JButton("Get Registers");
	private JLabel status = new JLabel();
	private final JEditorPane document = new JEditorPane();
	@SuppressWarnings("rawtypes")
	private JComboBox combo = new JComboBox<>();
	private HashMap<String, Component> entry = new HashMap<String, Component>();
	private Component selReg = null;
	private Project proj;
	private static Circuit curCircuit;
	private static CircuitState curCircuitState;
	private File file;

	public AssemblyWindow(Project proj) {

		this.proj = proj;
		curCircuit = proj.getCurrentCircuit();
		curCircuitState = proj.getCircuitState();
		winMenuBar = new JMenuBar();
		JMenu windowMenu = new JMenu("Window");
		JMenu fileMenu = new JMenu("File");
		JPanel main = new JPanel(new BorderLayout());
		JPanel north = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
		/* LinePainter painter = new LinePainter(document); */

		windowMenu.setMnemonic('W');
		fileMenu.setMnemonic('F');
		combo.addActionListener(this);
		combo.setFocusable(false);
		refresh.addActionListener(this);
		refresh.setFocusable(false);
		refresh.setToolTipText("Get register list of current displayed circuit.");

		ontopItem = new JCheckBoxMenuItem("Set on top", true);
		ontopItem.addActionListener(this);
		openFileItem = new JMenuItem("Open lss file");
		openFileItem.addActionListener(this);
		reloadFileItem = new JMenuItem("Reload lss file");
		reloadFileItem.addActionListener(this);
		close = new JMenuItem("Close");
		close.addActionListener(this);
		winMenuBar.add(fileMenu);
		winMenuBar.add(windowMenu);
		winMenuBar.setFocusable(false);
		windowMenu.add(ontopItem);
		fileMenu.add(openFileItem);
		fileMenu.add(reloadFileItem);
		fileMenu.addSeparator();
		fileMenu.add(close);

		windows = new LFrame();
		windows.setTitle("Assembly: " + proj.getLogisimFile().getDisplayName());
		windows.setJMenuBar(winMenuBar);
		windows.toFront();
		windows.setAlwaysOnTop(true);
		windows.setVisible(false);
		windows.addWindowListener(this);
		windows.addKeyListener(this);

		north.add(new JLabel("Register: "));
		north.add(combo);
		north.add(refresh);

		document.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		document.setEditable(false);
		document.setPreferredSize(new Dimension(document.getWidth() * 4 / 5,
				Math.max(200, document.getHeight() * 2 / 3)));
		document.addKeyListener(this);
		main.add(new JScrollPane(document), BorderLayout.CENTER);
		main.add(north, BorderLayout.NORTH);
		main.add(status, BorderLayout.SOUTH);
		windows.setContentPane(main);
		proj.getSimulator().addSimulatorListener(this);

		windows.pack();
		prefs = Preferences.userRoot().node(this.getClass().getName());
		windows.setLocation(prefs.getInt("X", 0), prefs.getInt("Y", 0));
		windows.setSize(prefs.getInt("W", windows.getSize().width),
				prefs.getInt("H", windows.getSize().height));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
		if (src == ontopItem) {
			e.paramString();
			windows.setAlwaysOnTop(ontopItem.getState());
		} else if (src == openFileItem) {
			final JFileChooser fileChooser = proj.createChooser();
			FileFilter ff = new FileFilter() {
				@Override
				public boolean accept(File f) {
					return f.isDirectory()
							|| f.getName().toLowerCase().endsWith(".lss");
				}

				@Override
				public String getDescription() {
					return ".lss disassembly file";
				}
			};
			fileChooser.setFileFilter(ff);
			fileChooser.setAcceptAllFileFilterUsed(false);
			int result = fileChooser.showOpenDialog(windows);
			if (result == JFileChooser.APPROVE_OPTION) {
				file = fileChooser.getSelectedFile();
				try {
					if (file.getName().toLowerCase().endsWith(".lss")) {
						status.setText("");
						document.setPage(file.toURI().toURL());
					} else {
						status.setText("Wrong file selected !");
						file = null;
					}
				} catch (Exception ex) {
					status.setText("Cannot open file !");
					file = null;
				}
			}
			// Allow reload of same file
			document.getDocument().putProperty(
					Document.StreamDescriptionProperty, null);
			windows.invalidate();
		} else if (src == reloadFileItem) {
			if (file != null) {
				try {
					document.setPage(file.toURI().toURL());
					status.setText("File reloaded.");
				} catch (Exception ex) {
					status.setText("Cannot open file !");
					file = null;
				}
			}
			windows.invalidate();
		} else if (src == refresh) {
			curCircuit = proj.getCurrentCircuit();
			curCircuitState = proj.getCircuitState();
			fillCombo();
			updateHighlightLine();
		} else if (src == combo) {
			updateHighlightLine();
		} else if (src == close) {
			setVisible(false);
		}
	}

	@SuppressWarnings("unchecked")
	private void fillCombo() {
		Set<Component> comps = curCircuit.getNonWires();
		Iterator<Component> iter = comps.iterator();
		entry.clear();
		while (iter.hasNext()) {
			Component comp = iter.next();
			if (comp.getFactory().getName().equals("Register")) {
				if (!comp.getAttributeSet().getValue(StdAttr.LABEL).equals("")) {
					entry.put(comp.getAttributeSet().getValue(StdAttr.LABEL),
							comp);
				}
			}
		}

		combo.removeAllItems();

		if (entry.isEmpty()) {
			status.setText("No labeled registers found.");
			combo.setEnabled(false);
		} else {
			status.setText("");
			combo.setEnabled(true);
			Object[] objArr = entry.keySet().toArray();
			Arrays.sort(objArr);
			for (int i = 0; i < objArr.length; i++) {
				combo.addItem(objArr[i]);
			}
		}
	}

	public boolean isVisible() {
		if (windows != null) {
			return windows.isVisible();
		} else {
			return false;
		}
	}

	@Override
	public void keyPressed(KeyEvent ke) {
		// throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void keyReleased(KeyEvent ke) {
		int keyCode = ke.getKeyCode();
		if (keyCode == KeyEvent.VK_F2) {
			int ticks = 0;
			for (com.cburch.logisim.comp.Component clock : proj
					.getLogisimFile().getMainCircuit().getClocks()) {
				if (clock.getAttributeSet().getValue(StdAttr.LABEL)
						.contentEquals("clk")) {
					if (proj.getOptions().getAttributeSet()
							.getValue(Options.ATTR_TICK_MAIN)
							.equals(Options.TICK_MAIN_HALF_PERIOD)) {
						if (proj.getCircuitState()
								.getValue(clock.getLocation()).toIntValue() == 0) {
							ticks = clock.getAttributeSet().getValue(
									Clock.ATTR_LOW);
						} else {
							ticks = clock.getAttributeSet().getValue(
									Clock.ATTR_HIGH);
						}
					} else {
						ticks = clock.getAttributeSet()
								.getValue(Clock.ATTR_LOW)
								+ clock.getAttributeSet().getValue(
										Clock.ATTR_HIGH);
					}
					break;
				}
			}
			proj.getSimulator().tickMain(ticks);
		}
	}

	@Override
	public void keyTyped(KeyEvent ke) {
		// throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void propagationCompleted(SimulatorEvent e) {
		if (e.getSource().isRunning()) {
			updateHighlightLine();
		}
	}

	public void setTitle(String title) {
		windows.setTitle(title);
	}

	public void setVisible(boolean bool) {
		fillCombo();
		windows.setVisible(bool);
	}

	@Override
	public void simulatorStateChanged(SimulatorEvent e) {
	}

	/*
	 * Track the movement of the Caret by painting a background line at the
	 * current caret position.
	 */

	@Override
	public void tickCompleted(SimulatorEvent e) {
	}

	public void toFront() {
		if (windows != null) {
			windows.toFront();
		}
	}

	private void updateHighlightLine() {
		String where;
		if (combo.getSelectedItem() != null) {
			selReg = entry.get(combo.getSelectedItem().toString());
			Value val = curCircuitState.getInstanceState(selReg).getPortValue(
					Register.OUT);
			if (val.isFullyDefined()) {
				where = val.toHexString().replaceAll("^0*", "");
				if (where.isEmpty()) {
					where = "0";
				}
				Pattern pattern = Pattern.compile("^[ ]+" + where + ":",
						Pattern.MULTILINE + Pattern.CASE_INSENSITIVE);
				Matcher m = pattern.matcher(document.getText().replaceAll("\r",
						""));
				if (m.find()) {
					document.setCaretPosition(m.start());
					status.setText("");
				} else {
					status.setText("Line (" + where + ") not found!");
				}
			}
		}
	}

	@Override
	public void windowActivated(WindowEvent e) {
	}

	@Override
	public void windowClosed(WindowEvent e) {
	}

	@Override
	public void windowClosing(WindowEvent e) {
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		prefs.putInt("X", windows.getX());
		prefs.putInt("Y", windows.getY());
		prefs.putInt("W", windows.getWidth());
		prefs.putInt("H", windows.getHeight());
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
	}

	@Override
	public void windowIconified(WindowEvent e) {
	}

	@Override
	public void windowOpened(WindowEvent e) {
	}

	/*
	 * private class LinePainter implements Highlighter.HighlightPainter,
	 * CaretListener, MouseListener, MouseMotionListener {
	 * 
	 * private JTextComponent component; private Color color; private Rectangle
	 * lastView;
	 * 
	 * 
	 * The line color will be calculated automatically by attempting to make the
	 * current selection lighter by a factor of 1.2.
	 * 
	 * @param component text component that requires background line painting
	 * 
	 * public LinePainter(JTextComponent component) { this(component, null);
	 * setLighter(component.getSelectionColor()); }
	 * 
	 * 
	 * Manually control the line color
	 * 
	 * @param component text component that requires background line painting
	 * 
	 * @param color the color of the background line
	 * 
	 * public LinePainter(JTextComponent component, Color color) {
	 * this.component = component; setColor(color);
	 * 
	 * // Add listeners so we know when to change highlighting
	 * 
	 * component.addCaretListener(this); component.addMouseListener(this);
	 * component.addMouseMotionListener(this);
	 * 
	 * // Turn highlighting on by adding a dummy highlight
	 * 
	 * try { component.getHighlighter().addHighlight(0, 0, this); } catch
	 * (BadLocationException ble) { } }
	 * 
	 * 
	 * You can reset the line color at any time
	 * 
	 * @param color the color of the background line
	 * 
	 * private void setColor(Color color) { this.color = color; }
	 * 
	 * 
	 * Calculate the line color by making the selection color lighter
	 * 
	 * @return the color of the background line
	 * 
	 * private void setLighter(Color color) { int red = Math.min(255, (int)
	 * (color.getRed() * 1.2)); int green = Math.min(255, (int)
	 * (color.getGreen() * 1.2)); int blue = Math.min(255, (int)
	 * (color.getBlue() * 1.2)); setColor(new Color(red, green, blue)); }
	 * 
	 * // Paint the background highlight
	 * 
	 * @Override public void paint(Graphics g, int p0, int p1, Shape bounds,
	 * JTextComponent c) { try { Rectangle r =
	 * c.modelToView(c.getCaretPosition()); g.setColor(color); g.fillRect(0,
	 * r.y, c.getWidth(), r.height);
	 * 
	 * if (lastView == null) { lastView = r; } } catch (BadLocationException
	 * ble) { System.out.println(ble); } }
	 * 
	 * 
	 * Caret position has changed, remove the highlight
	 * 
	 * private void resetHighlight() { // Use invokeLater to make sure updates
	 * to the Document are completed, // otherwise Undo processing causes the
	 * modelToView method to loop.
	 * 
	 * SwingUtilities.invokeLater(new Runnable() {
	 * 
	 * @Override public void run() { try { int offset =
	 * component.getCaretPosition(); Rectangle currentView =
	 * component.modelToView(offset);
	 * 
	 * // Remove the highlighting from the previously highlighted line
	 * 
	 * if (lastView.y != currentView.y) { component.repaint(0, lastView.y,
	 * component.getWidth(), lastView.height); lastView = currentView; } } catch
	 * (BadLocationException ble) { } } }); }
	 * 
	 * // Implement CaretListener
	 * 
	 * @Override public void caretUpdate(CaretEvent e) { resetHighlight(); }
	 * 
	 * // Implement MouseListener
	 * 
	 * @Override public void mousePressed(MouseEvent e) { resetHighlight(); }
	 * 
	 * @Override public void mouseClicked(MouseEvent e) { }
	 * 
	 * @Override public void mouseEntered(MouseEvent e) { }
	 * 
	 * @Override public void mouseExited(MouseEvent e) { }
	 * 
	 * @Override public void mouseReleased(MouseEvent e) { }
	 * 
	 * // Implement MouseMotionListener
	 * 
	 * @Override public void mouseDragged(MouseEvent e) { resetHighlight(); }
	 * 
	 * @Override public void mouseMoved(MouseEvent e) { } }
	 */
}
