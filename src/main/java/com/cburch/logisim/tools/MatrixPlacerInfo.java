/*
 * Logisim-evolution - digital logic design tool and simulator
 * Copyright by the Logisim-evolution developers
 *
 * https://github.com/logisim-evolution/
 *
 * This is free software released under GNU GPLv3 license
 */

package com.cburch.logisim.tools;

import com.cburch.logisim.data.Bounds;

public class MatrixPlacerInfo {

  private final String oldLabel;
  private String sharedLabel;
  private int xCopiesCount = 1;
  private int yCopiesCount = 1;
  private int displacementX = 1;
  private int displacementY = 1;
  private int xDispMin = 1;
  private int yDispMin = 1;

  public MatrixPlacerInfo(String label) {
    sharedLabel = label;
    oldLabel = label;
  }

  void setBounds(Bounds bds) {
    displacementX = xDispMin = (bds.getWidth() + 9) / 10;
    displacementY = yDispMin = (bds.getHeight() + 9) / 10;
  }

  int getMinimalXDisplacement() {
    return xDispMin;
  }

  int getMinimalYDisplacement() {
    return yDispMin;
  }

  String getLabel() {
    return sharedLabel;
  }

  void undoLabel() {
    sharedLabel = oldLabel;
  }

  void setLabel(String lab) {
    sharedLabel = lab;
  }

  int getxCopiesCount() {
    return xCopiesCount;
  }

  void setxCopiesCount(int val) {
    xCopiesCount = val;
  }

  int getyCopiesCount() {
    return yCopiesCount;
  }

  void setyCopiesCount(int val) {
    yCopiesCount = val;
  }

  int getDeltaX() {
    return displacementX * 10;
  }

  void setDeltaX(int value) {
    if (value > 0) displacementX = (value + 9) / 10;
  }

  void setDisplacementX(int value) {
    if (value > 0) displacementX = value;
  }

  int getDisplacementX() {
    return displacementX;
  }

  int getDeltaY() {
    return displacementY * 10;
  }

  void setDeltaY(int value) {
    if (value > 0) displacementY = (value + 9) / 10;
  }

  void setDisplacementY(int value) {
    if (value > 0) displacementY = value;
  }

  int getDisplacementY() {
    return displacementY;
  }
}
