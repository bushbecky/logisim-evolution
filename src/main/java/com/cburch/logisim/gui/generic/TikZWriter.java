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

package com.cburch.logisim.gui.generic;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.RenderingHints.Key;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.io.FileWriter;
import java.text.AttributedCharacterIterator;
import java.util.Map;

public class TikZWriter extends Graphics2D {
  
  private TikZInfo MyInfo;
  
  public TikZWriter() {
    MyInfo = new TikZInfo(); 
  };
  
  public TikZWriter(TikZInfo info) {
    MyInfo = info;
  }
  
  @Override
  public void draw(Shape s) {
    MyInfo.addBezier(s,false);  
  }

  @Override
  public boolean drawImage(Image img, AffineTransform xform, ImageObserver obs) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void drawImage(BufferedImage img, BufferedImageOp op, int x, int y) {
    // TODO Auto-generated method stub
  }

  @Override
  public void drawRenderedImage(RenderedImage img, AffineTransform xform) {
    // TODO Auto-generated method stub
  }

  @Override
  public void drawRenderableImage(RenderableImage img, AffineTransform xform) {
    // TODO Auto-generated method stub
  }

  @Override
  public void drawString(String str, int x, int y) {
    MyInfo.addString(str, x, y);
  }

  @Override
  public void drawString(String str, float x, float y) {
    MyInfo.addString(str, (int)x,(int)y);
  }

  @Override
  public void drawString(AttributedCharacterIterator iterator, int x, int y) {
    // TODO Auto-generated method stub
  }

  @Override
  public void drawString(AttributedCharacterIterator iterator, float x, float y) {
    // TODO Auto-generated method stub
  }

  @Override
  public void drawGlyphVector(GlyphVector g, float x, float y) {
    // TODO Auto-generated method stub
  }

  @Override
  public void fill(Shape s) {
    MyInfo.addBezier(s,true);
  }

  @Override
  public boolean hit(Rectangle rect, Shape s, boolean onStroke) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public GraphicsConfiguration getDeviceConfiguration() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void setComposite(Composite comp) {
    // TODO Auto-generated method stub
  }

  @Override
  public void setPaint(Paint paint) {
    // TODO Auto-generated method stub
  }

  @Override
  public void setStroke(Stroke s) {
    MyInfo.setStroke(s);
  }

  @Override
  public void setRenderingHint(Key hintKey, Object hintValue) {
    // TODO Auto-generated method stub
  }

  @Override
  public Object getRenderingHint(Key hintKey) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void setRenderingHints(Map<?, ?> hints) {
    // TODO Auto-generated method stub
  }

  @Override
  public void addRenderingHints(Map<?, ?> hints) {
    // TODO Auto-generated method stub
  }

  @Override
  public RenderingHints getRenderingHints() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void translate(int x, int y) {
    MyInfo.getAffineTransform().translate(x, y);
  }

  @Override
  public void translate(double tx, double ty) {
    MyInfo.getAffineTransform().translate(tx, ty);
  }

  @Override
  public void rotate(double theta) {
    MyInfo.rotate(theta);
  }

  @Override
  public void rotate(double theta, double x, double y) {
    MyInfo.rotate(theta,x,y);
  }

  @Override
  public void scale(double sx, double sy) {
    MyInfo.getAffineTransform().scale(sx, sy);
  }

  @Override
  public void shear(double shx, double shy) {
    MyInfo.getAffineTransform().shear(shx, shy);
  }

  @Override
  public void transform(AffineTransform Tx) {
    MyInfo.getAffineTransform().concatenate(Tx);
  }

  @Override
  public void setTransform(AffineTransform Tx) {
    MyInfo.setAffineTransform(Tx);
  }

  @Override
  public AffineTransform getTransform() {
    return (AffineTransform) MyInfo.getAffineTransform().clone();
  }

  @Override
  public Paint getPaint() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Composite getComposite() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void setBackground(Color color) {
    MyInfo.setBackground(color);
  }

  @Override
  public Color getBackground() {
    return MyInfo.getBackground();
  }

  @Override
  public Stroke getStroke() {
    return MyInfo.getStroke();
  }

  @Override
  public void clip(Shape s) {
    // TODO Auto-generated method stub
  }

  @Override
  public FontRenderContext getFontRenderContext() {
    /* TODO: just stubs, not related to LaTeX */
    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    GraphicsDevice gd = ge.getDefaultScreenDevice();
    GraphicsConfiguration config = gd.getDefaultConfiguration();
    Canvas c = new Canvas(config);
    return c.getFontMetrics(MyInfo.getFont()).getFontRenderContext();
  }

  @Override
  public Graphics create() {
    return new TikZWriter(MyInfo.clone());
  }

  @Override
  public Color getColor() {
    return MyInfo.getColor();
  }
  
  @Override
  public void setColor(Color c) {
    MyInfo.setColor(c);
  }

  @Override
  public void setPaintMode() {
    // default mode
  }

  @Override
  public void setXORMode(Color c1) {
    System.out.println("TikZWriter not yet supported : setXORMode!");  
  }

  @Override
  public Font getFont() {
    return MyInfo.getFont();
  }

  @Override
  public void setFont(Font font) {
    MyInfo.setFont(font);  
  }

  @Override
  public FontMetrics getFontMetrics(Font f) {
    /* TODO: just stubs, not related to LaTeX */
    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    GraphicsDevice gd = ge.getDefaultScreenDevice();
    GraphicsConfiguration config = gd.getDefaultConfiguration();
    Canvas c = new Canvas(config);
    return c.getFontMetrics(f);
  }

  @Override
  public Rectangle getClipBounds() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void clipRect(int x, int y, int width, int height) {
    // TODO Auto-generated method stub
  }

  @Override
  public void setClip(int x, int y, int width, int height) {
    // TODO Auto-generated method stub
  }

  @Override
  public Shape getClip() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void setClip(Shape clip) {
    // TODO Auto-generated method stub
  }

  @Override
  public void copyArea(int x, int y, int width, int height, int dx, int dy) {
    MyInfo.copyArea(x, y, width, height, dx, dy);
  }

  @Override
  public void drawLine(int x1, int y1, int x2, int y2) {
    MyInfo.addMergeLine(x1,y1,x2,y2);
  }

  @Override
  public void fillRect(int x, int y, int width, int height) {
    MyInfo.addRectangle(x,y,x+width,y+height,true,false);
  }

  @Override
  public void clearRect(int x, int y, int width, int height) {
    MyInfo.addRectangle(x,y,x+width,y+height,true,true);
  }

  @Override
  public void drawRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
    MyInfo.addRoundedRectangle(x,y,x+width,y+height,arcWidth,arcHeight,false);
  }

  @Override
  public void fillRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
    MyInfo.addRoundedRectangle(x,y,x+width,y+height,arcWidth,arcHeight,true);
  }

  @Override
  public void drawOval(int x, int y, int width, int height) {
    MyInfo.addElipse(x,y,width,height,false);
  }

  @Override
  public void fillOval(int x, int y, int width, int height) {
    MyInfo.addElipse(x,y,width,height,true);
  }

  @Override
  public void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
    MyInfo.addArc(x,y,width,height,startAngle,arcAngle,false);
  }

  @Override
  public void fillArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
    MyInfo.addArc(x,y,width,height,startAngle,arcAngle,true);
  }

  @Override
  public void drawPolyline(int[] xPoints, int[] yPoints, int nPoints) {
    MyInfo.addPolyline(xPoints,yPoints,nPoints,false,false);
  }

  @Override
  public void drawPolygon(int[] xPoints, int[] yPoints, int nPoints) {
    MyInfo.addPolyline(xPoints,yPoints,nPoints,false,true);
  }

  @Override
  public void fillPolygon(int[] xPoints, int[] yPoints, int nPoints) {
    MyInfo.addPolyline(xPoints,yPoints,nPoints,true,true);
  }

  @Override
  public boolean drawImage(Image img, int x, int y, ImageObserver observer) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean drawImage(Image img, int x, int y, int width, int height, ImageObserver observer) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean drawImage(Image img, int x, int y, Color bgcolor, ImageObserver observer) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean drawImage(Image img, int x, int y, int width, int height, Color bgcolor, ImageObserver observer) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2,
      ImageObserver observer) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2,
      Color bgcolor, ImageObserver observer) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void dispose() {
  }
  
  public void WriteFile(FileWriter outfile) {
    MyInfo.WriteFile(outfile);
  }

}
