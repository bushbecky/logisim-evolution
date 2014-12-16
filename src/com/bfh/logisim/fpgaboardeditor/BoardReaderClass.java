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

package com.bfh.logisim.fpgaboardeditor;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.cburch.logisim.proj.Projects;

public class BoardReaderClass {
	
	final static Logger logger = LoggerFactory.getLogger(BoardReaderClass.class);
	
	private String myfilename;
	private DocumentBuilderFactory factory;
	private DocumentBuilder parser;
	private Document BoardDoc;
	private String pictureError = "/resources/logisim/error.png";
	private String pictureWarning = "/resources/logisim/warning.png";

	public BoardReaderClass(String filename) {
		myfilename = new String(filename);
	}

	private BufferedImage CreateImage(int width, int height,
			String[] CodeTable, String PixelData) {
		ImageXmlFactory reader = new ImageXmlFactory();
		reader.SetCodeTable(CodeTable);
		reader.SetCompressedString(PixelData);
		BufferedImage result = reader.GetPicture(width, height);
		return result;
	}

	public BoardInformation GetBoardInformation() {
		try {
			// Create instance of DocumentBuilderFactory
			factory = DocumentBuilderFactory.newInstance();
			// Get the DocumentBuilder
			parser = factory.newDocumentBuilder();
			// Create blank DOM Document
			if (myfilename.startsWith("url:")) {
				InputStream xml = getClass().getResourceAsStream(
						"/" + myfilename.substring("url:".length()));
				BoardDoc = parser.parse(xml);
			} else if (myfilename.startsWith("file:")) {
				File xml = new File(myfilename.substring("file:".length()));
				BoardDoc = parser.parse(xml);
			} else {
				File xml = new File(myfilename);
				BoardDoc = parser.parse(xml);
			}

			NodeList ImageList = BoardDoc
					.getElementsByTagName(BoardWriterClass.ImageInformationString);
			if (ImageList.getLength() != 1)
				return null;
			Node ThisImage = ImageList.item(0);
			NodeList ImageParameters = ThisImage.getChildNodes();

			String[] CodeTable = null;
			String PixelData = null;
			int PictureWidth = 0;
			int PictureHeight = 0;
			for (int i = 0; i < ImageParameters.getLength(); i++) {
				if (ImageParameters.item(i).getNodeName()
						.equals("CompressionCodeTable")) {
					NamedNodeMap TableAttrs = ImageParameters.item(i)
							.getAttributes();
					for (int j = 0; j < TableAttrs.getLength(); j++) {
						if (TableAttrs.item(j).getNodeName()
								.equals("TableData")) {
							String CodeTableStr = TableAttrs.item(j)
									.getNodeValue();
							CodeTable = CodeTableStr.split(" ");
						}
					}
				}
				if (ImageParameters.item(i).getNodeName()
						.equals("PictureDimension")) {
					NamedNodeMap SizeAttrs = ImageParameters.item(i)
							.getAttributes();
					for (int j = 0; j < SizeAttrs.getLength(); j++) {
						if (SizeAttrs.item(j).getNodeName().equals("Width"))
							PictureWidth = Integer.parseInt(SizeAttrs.item(j)
									.getNodeValue());
						if (SizeAttrs.item(j).getNodeName().equals("Height"))
							PictureHeight = Integer.parseInt(SizeAttrs.item(j)
									.getNodeValue());
					}
				}
				if (ImageParameters.item(i).getNodeName().equals("PixelData")) {
					NamedNodeMap PixelAttrs = ImageParameters.item(i)
							.getAttributes();
					for (int j = 0; j < PixelAttrs.getLength(); j++)
						if (PixelAttrs.item(j).getNodeName().equals("PixelRGB"))
							PixelData = PixelAttrs.item(j).getNodeValue();
				}
			}
			if (CodeTable == null) {
				showDialogNotification("Error",
						"The selected xml file does not contain a compression code table");
				return null;
			}
			if ((PictureWidth == 0) || (PictureHeight == 0)) {
				showDialogNotification("Error",
						"The selected xml file does not contain the picture dimensions");
				return null;
			}
			if (PixelData == null) {
				showDialogNotification("Error",
						"The selected xml file does not contain the picture data");
				return null;
			}

			BoardInformation result = new BoardInformation();
			BufferedImage Picture = CreateImage(PictureWidth, PictureHeight,
					CodeTable, PixelData);
			if (Picture == null)
				return null;
			result.SetImage(Picture);
			FPGAClass FPGA = GetFPGAInfo();
			if (FPGA == null)
				return null;
			result.fpga = FPGA;
			NodeList CompList = BoardDoc
					.getElementsByTagName("PinsInformation"); // for backward
																// compatibility
			ProcessComponentList(CompList, result);
			CompList = BoardDoc.getElementsByTagName("ButtonsInformation"); // for
																			// backward
																			// compatibility
			ProcessComponentList(CompList, result);
			CompList = BoardDoc.getElementsByTagName("LEDsInformation"); // for
																			// backward
																			// compatibility
			ProcessComponentList(CompList, result);
			CompList = BoardDoc
					.getElementsByTagName(BoardWriterClass.ComponentsSectionString); // new
																						// format
			ProcessComponentList(CompList, result);
			return result;
		} catch (Exception e) {
			logger.error("Exceptions not handled yet in GetBoardInformation(), but got an exception: {}", e.getMessage());
			/* TODO: handle exceptions */
			return null;
		}
	}

	private FPGAClass GetFPGAInfo() {
		NodeList FPGAList = BoardDoc
				.getElementsByTagName(BoardWriterClass.BoardInformationSectionString);
		long frequency = -1;
		String clockpin = null;
		String clockpull = null;
		String clockstand = null;
		String Unusedpull = null;
		String vendor = null;
		String Part = null;
		String family = null;
		String Package = null;
		String Speed = null;
		String UsbTmc = null;
		String JTAGPos = null;
		if (FPGAList.getLength() != 1)
			return null;
		Node ThisFPGA = FPGAList.item(0);
		NodeList FPGAParameters = ThisFPGA.getChildNodes();
		for (int i = 0; i < FPGAParameters.getLength(); i++) {
			if (FPGAParameters.item(i).getNodeName()
					.equals(BoardWriterClass.ClockInformationSectionString)) {
				NamedNodeMap ClockAttrs = FPGAParameters.item(i)
						.getAttributes();
				for (int j = 0; j < ClockAttrs.getLength(); j++) {
					if (ClockAttrs.item(j).getNodeName()
							.equals(BoardWriterClass.ClockSectionStrings[0]))
						frequency = Long.parseLong(ClockAttrs.item(j)
								.getNodeValue());
					if (ClockAttrs.item(j).getNodeName()
							.equals(BoardWriterClass.ClockSectionStrings[1]))
						clockpin = ClockAttrs.item(j).getNodeValue();
					if (ClockAttrs.item(j).getNodeName()
							.equals(BoardWriterClass.ClockSectionStrings[2]))
						clockpull = ClockAttrs.item(j).getNodeValue();
					if (ClockAttrs.item(j).getNodeName()
							.equals(BoardWriterClass.ClockSectionStrings[3]))
						clockstand = ClockAttrs.item(j).getNodeValue();
				}
			}
			if (FPGAParameters.item(i).getNodeName()
					.equals(BoardWriterClass.UnusedPinsString)) {
				NamedNodeMap UnusedAttrs = FPGAParameters.item(i)
						.getAttributes();
				for (int j = 0; j < UnusedAttrs.getLength(); j++)
					if (UnusedAttrs.item(j).getNodeName()
							.equals("PullBehavior"))
						Unusedpull = UnusedAttrs.item(j).getNodeValue();
			}
			if (FPGAParameters.item(i).getNodeName()
					.equals(BoardWriterClass.FPGAInformationSectionString)) {
				NamedNodeMap FPGAAttrs = FPGAParameters.item(i).getAttributes();
				for (int j = 0; j < FPGAAttrs.getLength(); j++) {
					if (FPGAAttrs.item(j).getNodeName()
							.equals(BoardWriterClass.FPGASectionStrings[0]))
						vendor = FPGAAttrs.item(j).getNodeValue();
					if (FPGAAttrs.item(j).getNodeName()
							.equals(BoardWriterClass.FPGASectionStrings[1]))
						Part = FPGAAttrs.item(j).getNodeValue();
					if (FPGAAttrs.item(j).getNodeName()
							.equals(BoardWriterClass.FPGASectionStrings[2]))
						family = FPGAAttrs.item(j).getNodeValue();
					if (FPGAAttrs.item(j).getNodeName()
							.equals(BoardWriterClass.FPGASectionStrings[3]))
						Package = FPGAAttrs.item(j).getNodeValue();
					if (FPGAAttrs.item(j).getNodeName()
							.equals(BoardWriterClass.FPGASectionStrings[4]))
						Speed = FPGAAttrs.item(j).getNodeValue();
					if (FPGAAttrs.item(j).getNodeName()
							.equals(BoardWriterClass.FPGASectionStrings[5]))
						UsbTmc = FPGAAttrs.item(j).getNodeValue();
					if (FPGAAttrs.item(j).getNodeName()
							.equals(BoardWriterClass.FPGASectionStrings[6]))
						JTAGPos = FPGAAttrs.item(j).getNodeValue();
				}
			}
		}
		if ((frequency < 0) || (clockpin == null) || (clockpull == null)
				|| (clockstand == null) || (Unusedpull == null)
				|| (vendor == null) || (Part == null) || (family == null)
				|| (Package == null) || (Speed == null)) {
			showDialogNotification("Error",
					"The selected xml file does not contain the required FPGA parameters");
			return null;
		}
		if (UsbTmc == null)
			UsbTmc = Boolean.toString(false);
		if (JTAGPos == null)
			JTAGPos = "1";
		FPGAClass result = new FPGAClass();
		result.Set(frequency, clockpin, clockpull, clockstand, family, Part,
				Package, Speed, vendor, Unusedpull,
				UsbTmc.equals(Boolean.toString(true)), JTAGPos);
		return result;
	}

	private void ProcessComponentList(NodeList CompList, BoardInformation board) {
		Node tempNode = null;
		if (CompList.getLength() == 1) {
			tempNode = CompList.item(0);
			CompList = tempNode.getChildNodes();
			for (int i = 0; i < CompList.getLength(); i++) {
				FPGAIOInformationContainer NewComp = new FPGAIOInformationContainer(
						CompList.item(i));
				if (NewComp.IsKnownComponent()) {
					board.AddComponent(NewComp);
				}
			}
		}
	}

	private void showDialogNotification(String type, String string) {
		final JFrame dialog = new JFrame(type);
		JLabel pic = new JLabel();
		if (type.equals("Warning")) {
			pic.setIcon(new ImageIcon(getClass().getResource(pictureWarning)));
		} else {
			pic.setIcon(new ImageIcon(getClass().getResource(pictureError)));
		}
		GridBagLayout dialogLayout = new GridBagLayout();
		dialog.setLayout(dialogLayout);
		GridBagConstraints c = new GridBagConstraints();
		JLabel message = new JLabel(string);
		JButton close = new JButton("close");
		ActionListener actionListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// panel.setAlwaysOnTop(true);
				dialog.dispose();
			}
		};
		close.addActionListener(actionListener);

		c.gridx = 0;
		c.gridy = 0;
		c.ipadx = 20;
		dialog.add(pic, c);

		c.gridx = 1;
		c.gridy = 0;
		dialog.add(message, c);

		c.gridx = 1;
		c.gridy = 1;
		dialog.add(close, c);
		dialog.pack();
		// dialog.setLocation(100, 100);
		dialog.setLocation(Projects.getCenteredLoc(dialog.getWidth(),
				dialog.getHeight()));
		dialog.setAlwaysOnTop(true);
		dialog.setVisible(true);

	}

}