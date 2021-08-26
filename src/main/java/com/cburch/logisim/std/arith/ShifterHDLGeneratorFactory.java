/*
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

package com.cburch.logisim.std.arith;

import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.fpga.designrulecheck.Netlist;
import com.cburch.logisim.fpga.designrulecheck.NetlistComponent;
import com.cburch.logisim.fpga.hdlgenerator.AbstractHDLGeneratorFactory;
import com.cburch.logisim.fpga.hdlgenerator.HDL;
import com.cburch.logisim.instance.StdAttr;
import com.cburch.logisim.util.LineBuffer;
import java.util.ArrayList;
import java.util.SortedMap;
import java.util.TreeMap;

public class ShifterHDLGeneratorFactory extends AbstractHDLGeneratorFactory {

  private static final String shiftModeStr = "ShifterMode";
  private static final int ShiftModeId = -1;

  @Override
  public String getComponentStringIdentifier() {
    return "Shifter";
  }

  @Override
  public SortedMap<String, Integer> GetInputList(Netlist TheNetlist, AttributeSet attrs) {
    final var inputs = new TreeMap<String, Integer>();
    inputs.put("DataA", attrs.getValue(StdAttr.WIDTH).getWidth());
    inputs.put("ShiftAmount", getNrofShiftBits(attrs));
    return inputs;
  }

  @Override
  public ArrayList<String> GetModuleFunctionality(Netlist TheNetlist, AttributeSet attrs) {
    final var contents = new LineBuffer();
    final var nrOfBits = attrs.getValue(StdAttr.WIDTH).getWidth();
    if (HDL.isVHDL()) {
      contents
          .add(
              "   -----------------------------------------------------------------------------",
              "   --- ShifterMode represents when:                                          ---",
              "   --- 0 : Logical Shift Left                                                ---",
              "   --- 1 : Rotate Left                                                       ---",
              "   --- 2 : Logical Shift Right                                               ---",
              "   --- 3 : Arithmetic Shift Right                                            ---",
              "   --- 4 : Rotate Right                                                      ---",
              "   -----------------------------------------------------------------------------")
          .empty(2);

      if (nrOfBits == 1) {
        contents
            .add("   Result <= DataA WHEN %s = 1 OR", shiftModeStr)
            .add("                        %s = 3 OR", shiftModeStr)
            .add("                        %s = 4 ELSE DataA AND NOT(ShiftAmount);", shiftModeStr);
      } else {
        for (var stage = 0; stage < getNrofShiftBits(attrs); stage++) {
          contents.add(GetStageFunctionalityVHDL(stage, nrOfBits));
        }
        contents
            .add(
                "   -----------------------------------------------------------------------------",
                "   --- Here we assign the result                                             ---",
                "   -----------------------------------------------------------------------------")
            .empty()
            .add("   Result <= s_stage_" + (getNrofShiftBits(attrs) - 1) + "_result;")
            .empty();
      }
    } else {
      contents
          .add(
              "   /***************************************************************************",
              "    ** ShifterMode represents when:                                          **",
              "    ** 0 : Logical Shift Left                                                **",
              "    ** 1 : Rotate Left                                                       **",
              "    ** 2 : Logical Shift Right                                               **",
              "    ** 3 : Arithmetic Shift Right                                            **",
              "    ** 4 : Rotate Right                                                      **",
              "    ***************************************************************************/")
          .empty(2);

      if (nrOfBits == 1) {
        contents
            .add("   assign Result = ((%s == 1)||", shiftModeStr)
            .add("                    (%s == 3)||", shiftModeStr)
            .add("                    (%s == 4)) ? DataA : DataA&(~ShiftAmount);", shiftModeStr);
      } else {
        for (var stage = 0; stage < getNrofShiftBits(attrs); stage++) {
          contents.add(GetStageFunctionalityVerilog(stage, nrOfBits));
        }
        contents
            .add(
                "   /***************************************************************************",
                "    ** Here we assign the result                                             **",
                "    ***************************************************************************/")
            .empty()
            .add("   assign Result = s_stage_%d_result;", (getNrofShiftBits(attrs) - 1))
            .empty();
      }
    }
    return contents.get();
  }

  private int getNrofShiftBits(AttributeSet attrs) {
    final var inputBits = attrs.getValue(StdAttr.WIDTH).getWidth();
    var shift = 1;
    while ((1 << shift) < inputBits) shift++;
    return shift;
  }

  @Override
  public SortedMap<String, Integer> GetOutputList(Netlist TheNetlist, AttributeSet attrs) {
    final var outputs = new TreeMap<String, Integer>();
    final var inputbits = attrs.getValue(StdAttr.WIDTH).getWidth();
    outputs.put("Result", inputbits);
    return outputs;
  }

  @Override
  public SortedMap<Integer, String> GetParameterList(AttributeSet attrs) {
    final var parameters = new TreeMap<Integer, String>();
    parameters.put(ShiftModeId, shiftModeStr);
    return parameters;
  }

  @Override
  public SortedMap<String, Integer> GetParameterMap(Netlist Nets, NetlistComponent ComponentInfo) {
    final var parameterMap = new TreeMap<String, Integer>();
    Object shift = ComponentInfo.GetComponent().getAttributeSet().getValue(Shifter.ATTR_SHIFT);
    if (shift == Shifter.SHIFT_LOGICAL_LEFT) parameterMap.put(shiftModeStr, 0);
    else if (shift == Shifter.SHIFT_ROLL_LEFT) parameterMap.put(shiftModeStr, 1);
    else if (shift == Shifter.SHIFT_LOGICAL_RIGHT) parameterMap.put(shiftModeStr, 2);
    else if (shift == Shifter.SHIFT_ARITHMETIC_RIGHT) parameterMap.put(shiftModeStr, 3);
    else parameterMap.put(shiftModeStr, 4);
    return parameterMap;
  }

  @Override
  public SortedMap<String, String> GetPortMap(Netlist Nets, Object MapInfo) {
    final var portMap = new TreeMap<String, String>();
    if (!(MapInfo instanceof NetlistComponent)) return portMap;
    final var componentInfo = (NetlistComponent) MapInfo;
    portMap.putAll(GetNetMap("DataA", true, componentInfo, Shifter.IN0, Nets));
    portMap.putAll(GetNetMap("ShiftAmount", true, componentInfo, Shifter.IN1, Nets));
    portMap.putAll(GetNetMap("Result", true, componentInfo, Shifter.OUT, Nets));
    return portMap;
  }

  private ArrayList<String> GetStageFunctionalityVerilog(int stageNumber, int nrOfBits) {
    final var contents = new LineBuffer();
    final var nrOfBitsToShift = (1 << stageNumber);
    contents
        .add("   /***************************************************************************")
        .add("    ** Here stage %s of the binary shift tree is defined                     **", stageNumber)
        .add("    ***************************************************************************/")
        .empty();
    if (stageNumber == 0) {
      contents
          .add("   assign s_stage_0_shiftin = ((%s == 1) || (%s == 3))", shiftModeStr, shiftModeStr)
          .add("        ? DataA[%s] : (%s == 4) ? DataA[0] : 0;", (nrOfBits - 1), shiftModeStr)
          .empty()
          .add("   assign s_stage_0_result  = (ShiftAmount == 0)")
          .add("        ? DataA")
          .add("        : ((%s == 0) || (%s == 1))", shiftModeStr, shiftModeStr)
          .add("           ? {DataA[%d:0],s_stage_0_shiftin}", (nrOfBits - 2))
          .add("           : {s_stage_0_shiftin,DataA[%d:1]};", (nrOfBits - 1))
          .empty();
    } else {
      contents
          .add("   assign s_stage_%s_shiftin = (%s == 1) ?", stageNumber, shiftModeStr)
          .add("                               s_stage_%d_result[%d:%d] : ", (stageNumber - 1), (nrOfBits - 1), (nrOfBits - nrOfBitsToShift))
          .add("                               (%s == 3) ?", shiftModeStr)
          .add("                               { %s{s_stage_%d_result[%d]} } :", nrOfBitsToShift, (stageNumber - 1), (nrOfBits - 1))
          .add("                               (%s == 4) ?", shiftModeStr)
          .add("                               s_stage_%d_result[%d:0] : 0;", (stageNumber - 1), (nrOfBitsToShift - 1))
          .empty()
          .add("   assign s_stage_%d_result  = (ShiftAmount[%d]==0) ?", stageNumber, stageNumber)
          .add("                              s_stage_%d_result : ", (stageNumber - 1))
          .add("                              ((%s == 0)||(%s == 1)) ?", shiftModeStr, shiftModeStr)
          .add("                              {s_stage_%d_result[%d:0],s_stage_%d_shiftin} :", (stageNumber - 1), (nrOfBits - nrOfBitsToShift - 1), stageNumber)
          .add("                              {s_stage_%d_shiftin,s_stage_%d_result[%d:%d]};", stageNumber, (stageNumber - 1), (nrOfBits - 1), nrOfBitsToShift)
          .empty();
    }
    return contents.get();
  }

  private ArrayList<String> GetStageFunctionalityVHDL(int stageNumber, int nrOfBits) {
    final var contents = new LineBuffer();
    final var nrOfBitsToShift = (1 << stageNumber);
    contents
        .add("   -----------------------------------------------------------------------------")
        .add("   --- Here stage %s of the binary shift tree is defined                     ---", stageNumber)
        .add("   -----------------------------------------------------------------------------")
        .empty();

    if (stageNumber == 0) {
      contents
          .add("   s_stage_0_shiftin <= DataA(%d) WHEN %s = 1 OR %s = 3 ELSE",
              (nrOfBits - 1), shiftModeStr, shiftModeStr)
          .add("                        DataA(0) WHEN %s = 4 ELSE '0';", shiftModeStr)
          .empty()
          .add("   s_stage_0_result  <= DataA")
          .add((nrOfBits == 2)
                  ? "                           WHEN ShiftAmount = '0' ELSE"
                  : "                           WHEN ShiftAmount(0) = '0' ELSE")
          .add("                        DataA(%d DOWNTO 0)&s_stage_0_shiftin", (nrOfBits - 2))
          .add("                           WHEN %s = 0 OR %s = 1 ELSE", shiftModeStr, shiftModeStr)
          .add("                        s_stage_0_shiftin&DataA(%d DOWNTO 1);", (nrOfBits - 1));
    } else {
      contents
          .add("   s_stage_%d_shiftin <= s_stage_%d_result(%d DOWNTO %d ) WHEN %s = 1 ELSE", stageNumber, (stageNumber - 1), (nrOfBits - 1), (nrOfBits - nrOfBitsToShift), shiftModeStr)
          .add("                        (OTHERS => s_stage_%d_result(%d)) WHEN %s = 3 ELSE", (stageNumber - 1), (nrOfBits - 1),  shiftModeStr)
          .add("                        s_stage_%d_result(%d DOWNTO 0 ) WHEN %s = 4 ELSE", (stageNumber - 1), (nrOfBitsToShift - 1), shiftModeStr)
          .add("                        (OTHERS => '0');")
          .empty()
          .add("   s_stage_%d_result  <= s_stage_%d_result", stageNumber, (stageNumber - 1))
          .add("                           WHEN ShiftAmount(%d) = '0' ELSE", stageNumber)
          .add("                        s_stage_%d_result(%d DOWNTO 0 )&s_stage_%d_shiftin", (stageNumber - 1), (nrOfBits - nrOfBitsToShift - 1), stageNumber)
          .add("                           WHEN %s = 0 OR %s = 1 ELSE", shiftModeStr, shiftModeStr)
          .add("                        s_stage_%d_shiftin&s_stage_%d_result(%d DOWNTO %d);", stageNumber, (stageNumber - 1), (nrOfBits - 1), nrOfBitsToShift);
    }
    contents.empty();
    return contents.get();
  }

  @Override
  public String GetSubDir() {
    return "arithmetic";
  }

  @Override
  public SortedMap<String, Integer> GetWireList(AttributeSet attrs, Netlist Nets) {
    final var wires = new TreeMap<String, Integer>();
    int shift = getNrofShiftBits(attrs);
    int loop;
    for (loop = 0; loop < shift; loop++) {
      wires.put("s_stage_" + loop + "_result", attrs.getValue(StdAttr.WIDTH).getWidth());
      wires.put("s_stage_" + loop + "_shiftin", 1 << loop);
    }
    return wires;
  }

  @Override
  public boolean HDLTargetSupported(AttributeSet attrs) {
    return true;
  }
}
