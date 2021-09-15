/*
 * Logisim-evolution - digital logic design tool and simulator
 * Copyright by the Logisim-evolution developers
 *
 * https://github.com/logisim-evolution/
 *
 * This is free software released under GNU GPLv3 license
 */

package com.cburch.logisim.fpga.hdlgenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeOption;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.data.BitWidth;
import com.cburch.logisim.fpga.designrulecheck.Netlist;
import com.cburch.logisim.fpga.designrulecheck.NetlistComponent;
import com.cburch.logisim.fpga.file.FileWriter;
import com.cburch.logisim.fpga.gui.Reporter;
import com.cburch.logisim.instance.StdAttr;
import com.cburch.logisim.prefs.AppPreferences;
import com.cburch.logisim.util.LineBuffer;

public abstract class HDL {

  public static final String NET_NAME = "s_LOGISIM_NET_";
  public static final String BUS_NAME = "s_LOGISIM_BUS_";
  
  public static class Parameters {

    private class ParameterInfo {
      private final boolean isOnlyUsedForBusses;
      private final String parameterName;
      private final int parameterId;
      private boolean useParameterValue = false;
      private int parameterValue = 0;
      private final Attribute<BitWidth> attributeToCheckForBus;
      private Attribute<?> attributeToGetValueFrom = null;
      private Map<AttributeOption, Integer> attributeOptionMap = null;

      public ParameterInfo(String name, int id) {
        this(false, StdAttr.WIDTH, name, id);
      }

      public ParameterInfo(String name, int id, int value) {
        this(false, StdAttr.WIDTH, name, id);
        parameterValue = value;
        useParameterValue = parameterValue >= 0;
      }

      public ParameterInfo(String name, int id, Attribute<Integer> attrMap) {
        this(false, StdAttr.WIDTH, name, id);
        attributeToGetValueFrom = attrMap;
      }

      public ParameterInfo(String name, int id, Attribute<AttributeOption> attrMap, Map<AttributeOption, Integer> valueMap) {
        this(false, StdAttr.WIDTH, name, id);
        attributeToGetValueFrom = attrMap;
        attributeOptionMap = valueMap;
      }

      public ParameterInfo(boolean forBusOnly, String name, int id) {
        this(forBusOnly, StdAttr.WIDTH, name, id);
      }

      public ParameterInfo(boolean forBusOnly, Attribute<BitWidth> checkAttr, String name, int id) {
        isOnlyUsedForBusses = forBusOnly;
        parameterName = name;
        parameterId = id;
        attributeToCheckForBus = checkAttr;
      }

      public boolean isUsed(AttributeSet attrs) {
        final var nrOfBits = (attrs != null) && attrs.containsAttribute(attributeToCheckForBus) ? attrs.getValue(attributeToCheckForBus).getWidth() : 0;
        return (!isOnlyUsedForBusses || (nrOfBits > 1));
      }

      public int getParameterId(AttributeSet attrs) {
        return isUsed(attrs) ? parameterId : 0;
      }

      public String getParameterString(AttributeSet attrs) {
        return isUsed(attrs) ? parameterName : null;
      }

      public int getParameterValue(AttributeSet attrs) {
        /* direct use of parameter value */
        if (useParameterValue) return parameterValue;
        /* most used case : */
        if (attrs != null) {
          if (attributeToGetValueFrom != null && attrs.containsAttribute(attributeToGetValueFrom)) {
            final var attrValue = attrs.getValue(attributeToGetValueFrom);
            if (attrValue instanceof Integer) return ((Integer) attrValue).intValue();
            if (attrValue instanceof AttributeOption && attributeOptionMap != null 
                && attributeOptionMap.containsKey(attrValue))
              return attributeOptionMap.get(attrValue);
          } else {
            final var offset = (parameterValue < 0) ? (-parameterValue) % 100 : 0;
            final var multiply = (parameterValue < 0) ? (-parameterValue) / 100 : 0; 
            return attrs.getValue(attributeToCheckForBus).getWidth() * multiply + offset;
          }
        }
        throw new IllegalArgumentException("Cannot determine parameter map");
      }
    }

    private final List<ParameterInfo> myParameters = new ArrayList<>();

    public Parameters add(String name, int id) {
      myParameters.add(new ParameterInfo(name, id));
      return this;
    }

    public Parameters add(String name, int id, int value) {
      myParameters.add(new ParameterInfo(name, id, value));
      return this;
    }

    public Parameters add(String name, int id, Attribute<Integer> attrMap) {
      myParameters.add(new ParameterInfo(name, id, attrMap));
      return this;
    }

    public Parameters add(String name, int id, Attribute<AttributeOption> attrMap, Map<AttributeOption, Integer> valueMap) {
      myParameters.add(new ParameterInfo(name, id, attrMap, valueMap));
      return this;
    }

    public Parameters addBusOnly(String name, int id) {
      myParameters.add(new ParameterInfo(true, name, id));
      return this;
    }

    public Parameters addBusOnly(Attribute<BitWidth> checkAttr, String name, int id) {
      myParameters.add(new ParameterInfo(true, checkAttr, name, id));
      return this;
    }

    public boolean containsKey(int id, AttributeSet attrs) {
      for (var parameter : myParameters) 
        if (id == parameter.getParameterId(attrs)) return true;
      return false;
    }

    public String get(int id, AttributeSet attrs) {
      for (var parameter : myParameters) 
        if (id == parameter.getParameterId(attrs)) return parameter.getParameterString(attrs);
      return null;
    }

    public Map<String, Integer> getMaps(AttributeSet attrs) {
      final var contents = new TreeMap<String, Integer>();
      for (var parameter : myParameters) {
        if (parameter.isUsed(attrs)) {
          final var value = parameter.getParameterValue(attrs);
          if (value >= 0)
            contents.put(parameter.getParameterString(attrs), value);
        }
      }
      return contents;
    }

    public boolean isEmpty(AttributeSet attrs) {
      var count = 0;
      for (var parameter : myParameters)
        if (parameter.isUsed(attrs)) count++;
      return count == 0;
    }

    public List<Integer> keySet(AttributeSet attrs) {
      final var keySet = new ArrayList<Integer>();
      for (var parameter : myParameters) {
        if (parameter.isUsed(attrs)) keySet.add(parameter.getParameterId(attrs));
      }
      return keySet;
    }
  }
  
  public static Parameters createParameters() {
    return new Parameters();
  }

  public static boolean isVHDL() {
    return AppPreferences.HDL_Type.get().equals(HDLGeneratorFactory.VHDL);
  }

  public static boolean isVerilog() {
    return AppPreferences.HDL_Type.get().equals(HDLGeneratorFactory.VERILOG);
  }

  public static String BracketOpen() {
    return isVHDL() ? "(" : "[";
  }

  public static String BracketClose() {
    return isVHDL() ? ")" : "]";
  }

  public static int remarkOverhead() {
    return isVHDL() ? 3 : 4;
  }

  public static String getRemakrChar(boolean first, boolean last) {
    if (isVHDL()) return "-";
    if (first) return "/";
    if (last) return " ";
    return "*";
  }

  public static String getRemarkStart() {
    if (isVHDL()) return "-- ";
    return " ** ";
  }

  public static String assignPreamble() {
    return isVHDL() ? "" : "assign ";
  }

  public static String assignOperator() {
    return isVHDL() ? " <= " : " = ";
  }

  public static String notOperator() {
    return isVHDL() ? " NOT " : "~";
  }

  public static String andOperator() {
    return isVHDL() ? " AND " : "&";
  }

  public static String orOperator() {
    return isVHDL() ? " OR " : "|";
  }

  public static String xorOperator() {
    return isVHDL() ? " XOR " : "^";
  }

  public static String zeroBit() {
    return isVHDL() ? "'0'" : "1'b0";
  }

  public static String oneBit() {
    return isVHDL() ? "'1'" : "1'b1";
  }

  public static String unconnected(boolean empty) {
    return isVHDL() ? "OPEN" : empty ? "" : "'bz";
  }

  public static String vectorLoopId() {
    return isVHDL() ? " DOWNTO " : ":";
  }

  public static String GetZeroVector(int nrOfBits, boolean floatingPinTiedToGround) {
    var contents = new StringBuilder();
    if (isVHDL()) {
      var fillValue = (floatingPinTiedToGround) ? "0" : "1";
      var hexFillValue = (floatingPinTiedToGround) ? "0" : "F";
      if (nrOfBits == 1) {
        contents.append("'").append(fillValue).append("'");
      } else {
        if ((nrOfBits % 4) > 0) {
          contents.append("\"");
          contents.append(fillValue.repeat((nrOfBits % 4)));
          contents.append("\"");
          if (nrOfBits > 3) {
            contents.append("&");
          }
        }
        if ((nrOfBits / 4) > 0) {
          contents.append("X\"");
          contents.append(hexFillValue.repeat(Math.max(0, (nrOfBits / 4))));
          contents.append("\"");
        }
      }
    } else {
      contents.append(nrOfBits).append("'d");
      contents.append(floatingPinTiedToGround ? "0" : "-1");
    }
    return contents.toString();
  }

  public static String getConstantVector(long value, int nrOfBits) {
    final var bitString = new StringBuffer();
    var mask = 1L << (nrOfBits - 1);
    if (HDL.isVHDL())
      bitString.append(nrOfBits == 1 ? '\'' : '"');
    else
      bitString.append(LineBuffer.format("{{1}}'b", nrOfBits));
    while (mask != 0) {
      bitString.append(((value & mask) == 0) ? "0" : "1");
      mask >>= 1L;
      // fix in case of a 64-bit vector
      if (mask < 0) mask &= Long.MAX_VALUE;
    }
    if (HDL.isVHDL()) bitString.append(nrOfBits == 1 ? '\'' : '"');
    return bitString.toString();
  }

  public static String getNetName(NetlistComponent comp, int endIndex, boolean floatingNetTiedToGround, Netlist myNetlist) {
    var netName = "";
    if ((endIndex >= 0) && (endIndex < comp.nrOfEnds())) {
      final var floatingValue = floatingNetTiedToGround ? zeroBit() : oneBit();
      final var thisEnd = comp.getEnd(endIndex);
      final var isOutput = thisEnd.isOutputEnd();

      if (thisEnd.getNrOfBits() == 1) {
        final var solderPoint = thisEnd.get((byte) 0);
        if (solderPoint.getParentNet() == null) {
          // The net is not connected
          netName = LineBuffer.formatHdl(isOutput ? unconnected(true) : floatingValue);
        } else {
          // The net is connected, we have to find out if the connection
          // is to a bus or to a normal net.
          netName = (solderPoint.getParentNet().getBitWidth() == 1)
                  ? LineBuffer.formatHdl("{{1}}{{2}}", NET_NAME, myNetlist.getNetId(solderPoint.getParentNet()))
                  : LineBuffer.formatHdl("{{1}}{{2}}{{<}}{{3}}{{>}}", BUS_NAME,
                      myNetlist.getNetId(solderPoint.getParentNet()), solderPoint.getParentNetBitIndex());
        }
      }
    }
    return netName;
  }

  public static String getBusEntryName(NetlistComponent comp, int endIndex, boolean floatingNetTiedToGround, int bitindex, Netlist theNets) {
    var busName = "";
    if ((endIndex >= 0) && (endIndex < comp.nrOfEnds())) {
      final var thisEnd = comp.getEnd(endIndex);
      final var isOutput = thisEnd.isOutputEnd();
      final var nrOfBits = thisEnd.getNrOfBits();
      if ((nrOfBits > 1) && (bitindex >= 0) && (bitindex < nrOfBits)) {
        if (thisEnd.get((byte) bitindex).getParentNet() == null) {
          // The net is not connected
          busName = LineBuffer.formatHdl(isOutput ? unconnected(false) : GetZeroVector(1, floatingNetTiedToGround));
        } else {
          final var connectedNet = thisEnd.get((byte) bitindex).getParentNet();
          final var connectedNetBitIndex = thisEnd.get((byte) bitindex).getParentNetBitIndex();
          // The net is connected, we have to find out if the connection
          // is to a bus or to a normal net.
          busName =
              !connectedNet.isBus()
                  ? LineBuffer.formatHdl("{{1}}{{2}}", NET_NAME, theNets.getNetId(connectedNet))
                  : LineBuffer.formatHdl("{{1}}{{2}}{{<}}{{3}}{{>}}", BUS_NAME, theNets.getNetId(connectedNet), connectedNetBitIndex);
        }
      }
    }
    return busName;
  }

  public static String getBusNameContinues(NetlistComponent comp, int endIndex, Netlist theNets) {
    if ((endIndex < 0) || (endIndex >= comp.nrOfEnds())) return null;
    final var connectionInformation = comp.getEnd(endIndex);
    final var nrOfBits = connectionInformation.getNrOfBits();
    if (nrOfBits == 1) return getNetName(comp, endIndex, true, theNets);
    if (!theNets.isContinuesBus(comp, endIndex)) return null;
    final var connectedNet = connectionInformation.get((byte) 0).getParentNet();
    return LineBuffer.format("{{1}}{{2}}{{<}}{{3}}{{4}}{{5}}{{>}}",
        BUS_NAME,
        theNets.getNetId(connectedNet),
        connectionInformation.get((byte) (connectionInformation.getNrOfBits() - 1)).getParentNetBitIndex(),
        HDL.vectorLoopId(),
        connectionInformation.get((byte) (0)).getParentNetBitIndex());
  }

  public static String getBusName(NetlistComponent comp, int endIndex, Netlist theNets) {
    if ((endIndex < 0) || (endIndex >= comp.nrOfEnds())) return null;
    final var connectionInformation = comp.getEnd(endIndex);
    final var nrOfBits = connectionInformation.getNrOfBits();
    if (nrOfBits == 1)  return getNetName(comp, endIndex, true, theNets);
    if (!theNets.isContinuesBus(comp, endIndex)) return null;
    final var ConnectedNet = connectionInformation.get((byte) 0).getParentNet();
    if (ConnectedNet.getBitWidth() != nrOfBits) return getBusNameContinues(comp, endIndex, theNets);
    return LineBuffer.format("{{1}}{{2}}", BUS_NAME, theNets.getNetId(ConnectedNet));
  }

  public static String getClockNetName(NetlistComponent comp, int endIndex, Netlist theNets) {
    var contents = new StringBuilder();
    if ((theNets.getCurrentHierarchyLevel() != null) && (endIndex >= 0) && (endIndex < comp.nrOfEnds())) {
      final var endData = comp.getEnd(endIndex);
      if (endData.getNrOfBits() == 1) {
        final var ConnectedNet = endData.get((byte) 0).getParentNet();
        final var ConnectedNetBitIndex = endData.get((byte) 0).getParentNetBitIndex();
        /* Here we search for a clock net Match */
        final var clocksourceid = theNets.getClockSourceId(
            theNets.getCurrentHierarchyLevel(), ConnectedNet, ConnectedNetBitIndex);
        if (clocksourceid >= 0) {
          contents.append(HDLGeneratorFactory.CLOCK_TREE_NAME).append(clocksourceid);
        }
      }
    }
    return contents.toString();
  }

  public static boolean writeEntity(String targetDirectory, ArrayList<String> contents, String componentName) {
    if (!HDL.isVHDL()) return true;
    if (contents.isEmpty()) {
      // FIXME: hardcoded string
      Reporter.Report.AddFatalError("INTERNAL ERROR: Empty entity description received!");
      return false;
    }
    final var outFile = FileWriter.getFilePointer(targetDirectory, componentName, true);
    if (outFile == null) return false;
    return FileWriter.writeContents(outFile, contents);
  }

  public static boolean writeArchitecture(String targetDirectory, ArrayList<String> contents, String componentName) {
    if (contents == null || contents.isEmpty()) {
      // FIXME: hardcoded string
      Reporter.Report.AddFatalError(
          "INTERNAL ERROR: Empty behavior description for Component '"
              + componentName
              + "' received!");
      return false;
    }
    final var outFile = FileWriter.getFilePointer(targetDirectory, componentName, false);
    if (outFile == null)  return false;
    return FileWriter.writeContents(outFile, contents);
  }

}
