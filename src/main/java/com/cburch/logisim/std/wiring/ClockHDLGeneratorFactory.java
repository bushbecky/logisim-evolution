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

package com.cburch.logisim.std.wiring;

import com.cburch.logisim.comp.Component;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.fpga.designrulecheck.Netlist;
import com.cburch.logisim.fpga.designrulecheck.NetlistComponent;
import com.cburch.logisim.fpga.hdlgenerator.AbstractHDLGeneratorFactory;
import com.cburch.logisim.fpga.hdlgenerator.HDL;
import com.cburch.logisim.fpga.hdlgenerator.TickComponentHDLGeneratorFactory;
import com.cburch.logisim.util.LineBuffer;
import java.util.ArrayList;
import java.util.SortedMap;
import java.util.TreeMap;

public class ClockHDLGeneratorFactory extends AbstractHDLGeneratorFactory {

  public static final int NrOfClockBits = 5;
  public static final int DerivedClockIndex = 0;
  public static final int InvertedDerivedClockIndex = 1;
  public static final int PositiveEdgeTickIndex = 2;
  public static final int NegativeEdgeTickIndex = 3;
  public static final int GlobalClockIndex = 4;
  private static final String HighTickStr = "HighTicks";
  private static final int HighTickId = -1;
  private static final String LowTickStr = "LowTicks";
  private static final int LowTickId = -2;
  private static final String PhaseStr = "Phase";
  private static final int PhaseId = -3;
  private static final String NrOfBitsStr = "NrOfBits";
  private static final int NrOfBitsId = -4;

  private String GetClockNetName(Component comp, Netlist TheNets) {
    StringBuilder Contents = new StringBuilder();
    int ClockNetId = TheNets.GetClockSourceId(comp);
    if (ClockNetId >= 0) {
      Contents.append(ClockTreeName).append(ClockNetId);
    }
    return Contents.toString();
  }

  @Override
  public String getComponentStringIdentifier() {
    return "CLOCKGEN";
  }

  @Override
  public SortedMap<String, Integer> GetInputList(Netlist TheNetlist, AttributeSet attrs) {
    final var map = new TreeMap<String, Integer>();
    map.put("GlobalClock", 1);
    map.put("ClockTick", 1);
    return map;
  }

  @Override
  public ArrayList<String> GetModuleFunctionality(Netlist TheNetlist, AttributeSet attrs) {
    final var Contents = (new LineBuffer())
            .addPair("phase", PhaseStr)
            .addPair("nrOfBits", NrOfBitsStr)
            .addPair("lowTick", LowTickStr)
            .addPair("highTick", HighTickStr)
    .addRemarkBlock("Here the output signals are defines; we synchronize them all on the main clock");
/*    if (TheNetlist.RawFPGAClock()) {
      if (HighTicks != LowTicks) {
        Reporter.AddFatalError("Clock component detected with " +HighTicks+":"+LowTicks+ " hi:lo duty cycle,"
            + " but maximum clock speed was selected. Only 1:1 duty cycle is supported with "
            + " maximum clock speed.");
      }
      if (HDLType.equals(VHDL)) {
        Contents.add("   ClockBus <= GlobalClock & '1' & '1' & NOT(GlobalClock) & GlobalClock;");
      } else {
        Contents.add("   assign ClockBus = {GlobalClock, 3'b1, 3'b1, ~GlobalClock, GlobalClock};");
      }
      Contents.add("");
      return Contents;
    }
*/    if (HDL.isVHDL()) {
      Contents.add(
          "   ClockBus <= GlobalClock&s_output_regs;",
          "   makeOutputs : PROCESS( GlobalClock )",
          "   BEGIN",
          "      IF (GlobalClock'event AND (GlobalClock = '1')) THEN",
          "         s_buf_regs(0)     <= s_derived_clock_reg({{phase}}-1);",
          "         s_buf_regs(1)     <= NOT(s_derived_clock_reg({{phase}}-1));",
          "         s_output_regs(0)  <= s_buf_regs(0);",
          "         s_output_regs(1)  <= s_buf_regs(1);",
          "         s_output_regs(2)  <= NOT(s_buf_regs(0)) AND s_derived_clock_reg({{phase}}-1);",
          "         s_output_regs(3)  <= s_buf_regs(0) AND NOT(s_derived_clock_reg({{phase}}-1));",
          "      END IF;",
          "   END PROCESS makeOutputs;");
    } else {
      Contents.add(
          "   assign ClockBus = {GlobalClock,s_output_regs};",
          "   always @(posedge GlobalClock)",
          "   begin",
          "      s_buf_regs[0]    <= s_derived_clock_reg[{{phase}}-1];",
          "      s_buf_regs[1]    <= ~s_derived_clock_reg[{{phase}}-1];",
          "      s_output_regs[0] <= s_buf_regs[0];",
          "      s_output_regs[1] <= s_output_regs[1];",
          "      s_output_regs[2] <= ~s_buf_regs[0] & s_derived_clock_reg[{{phase}}-1];",
          "      s_output_regs[3] <= ~s_derived_clock_reg[{{phase}}-1] & s_buf_regs[0];",
          "   end");
    }
    Contents.add("");
    Contents.addRemarkBlock("Here the control signals are defined");
    if (HDL.isVHDL()) {
      Contents.add(
          "   s_counter_is_zero <= '1' WHEN s_counter_reg = std_logic_vector(to_unsigned(0,{{nrOfBitsStr}})) ELSE '0';",
          "   s_counter_next    <= std_logic_vector(unsigned(s_counter_reg) - 1)",
          "                           WHEN s_counter_is_zero = '0' ELSE",
          "                        std_logic_vector(to_unsigned(({{lowTick}}-1), {{nrOfBitsStr}}))",
          "                           WHEN s_derived_clock_reg(0) = '1' ELSE",
          "                        std_logic_vector(to_unsigned(({{highTick}}-1), {{nrOfBitsStr}}));"
      );
    } else {
      Contents.add(
              "   assign s_counter_is_zero = (s_counter_reg == 0) ? 1'b1 : 1'b0;",
              "   assign s_counter_next = (s_counter_is_zero == 1'b0) ? s_counter_reg - 1 :",
              "                           (s_derived_clock_reg[0] == 1'b1) ? {{lowTick}} - 1 :",
              "                                                           {{highTick}} - 1;",
              "")
          .addRemarkBlock("Here the initial values are defined (for simulation only)")
          .add(
              "   initial",
              "   begin",
              "      s_output_regs = 0;",
              "      s_derived_clock_reg = 0;",
              "      s_counter_reg = 0;",
              "   end");
    }
    Contents.add("");
    Contents.addRemarkBlock("Here the state registers are defined");
    if (HDL.isVHDL()) {
      Contents.add(
          "makeDerivedClock : PROCESS( GlobalClock , ClockTick , s_counter_is_zero ,",
          "                            s_derived_clock_reg)",
          "BEGIN",
          "   IF (GlobalClock'event AND (GlobalClock = '1')) THEN",
          "      IF (s_derived_clock_reg(0) /= '0' AND s_derived_clock_reg(0) /= '1') THEN --For simulation only",
          "         s_derived_clock_reg <= (OTHERS => '1');",
          "      ELSIF (ClockTick = '1') THEN",
          "         FOR n IN {{phase}}-1 DOWNTO 1 LOOP",
          "           s_derived_clock_reg(n) <= s_derived_clock_reg(n-1);",
          "         END LOOP;",
          "         s_derived_clock_reg(0) <= s_derived_clock_reg(0) XOR s_counter_is_zero;",
          "      END IF;",
          "   END IF;",
          "END PROCESS makeDerivedClock;",
          "",
          "makeCounter : PROCESS( GlobalClock , ClockTick , s_counter_next ,",
          "                       s_derived_clock_reg )",
          "BEGIN",
          "   IF (GlobalClock'event AND (GlobalClock = '1')) THEN",
          "      IF (s_derived_clock_reg(0) /= '0' AND s_derived_clock_reg(0) /= '1') THEN --For simulation only",
          "         s_counter_reg <= (OTHERS => '0');",
          "      ELSIF (ClockTick = '1') THEN",
          "         s_counter_reg <= s_counter_next;",
          "      END IF;",
          "   END IF;",
          "END PROCESS makeCounter;");
    } else {
      Contents.add(
          "integer n;",
          "always @(posedge GlobalClock)",
          "begin",
          "   if (ClockTick)",
          "   begin",
          "      s_derived_clock_reg[0] <= s_derived_clock_reg[0] ^ s_counter_is_zero;",
          "      for (n = 1; n < {{phase}}; n = n+1) begin",
          "         s_derived_clock_reg[n] <= s_derived_clock_reg[n-1];",
          "      end",
          "   end",
          "end",
          "",
          "always @(posedge GlobalClock)",
          "begin",
          "   if (ClockTick)",
          "   begin",
          "      s_counter_reg <= s_counter_next;",
          "   end",
          "end");
    }
    Contents.add("");
    return Contents.getWithIndent();
  }

  @Override
  public SortedMap<String, Integer> GetOutputList(Netlist TheNetlist, AttributeSet attrs) {
    final var map = new TreeMap<String, Integer>();
    map.put("ClockBus", NrOfClockBits);
    return map;
  }

  @Override
  public SortedMap<Integer, String> GetParameterList(AttributeSet attrs) {
    final var map = new TreeMap<Integer, String>();
    map.put(HighTickId, HighTickStr);
    map.put(LowTickId, LowTickStr);
    map.put(PhaseId, PhaseStr);
    map.put(NrOfBitsId, NrOfBitsStr);
    return map;
  }

  @Override
  public SortedMap<String, Integer> GetParameterMap(Netlist Nets, NetlistComponent ComponentInfo) {
    final var map = new TreeMap<String, Integer>();
    int HighTicks = ComponentInfo.GetComponent().getAttributeSet().getValue(Clock.ATTR_HIGH);
    int LowTicks = ComponentInfo.GetComponent().getAttributeSet().getValue(Clock.ATTR_LOW);
    int Phase = ComponentInfo.GetComponent().getAttributeSet().getValue(Clock.ATTR_PHASE);
    Phase = Phase % (HighTicks + LowTicks);
    int MaxValue = Math.max(HighTicks, LowTicks);
    int nr_of_bits = 0;
    while (MaxValue != 0) {
      nr_of_bits++;
      MaxValue /= 2;
    }
    map.put(HighTickStr, HighTicks);
    map.put(LowTickStr, LowTicks);
    map.put(PhaseStr, (HighTicks + LowTicks) - Phase);
    map.put(NrOfBitsStr, nr_of_bits);
    return map;
  }

  @Override
  public SortedMap<String, String> GetPortMap(Netlist Nets, Object MapInfo) {
    final var map = new TreeMap<String, String>();
    if (!(MapInfo instanceof NetlistComponent)) return map;
    NetlistComponent ComponentInfo = (NetlistComponent) MapInfo;
    map.put("GlobalClock", TickComponentHDLGeneratorFactory.FPGAClock);
    map.put("ClockTick", TickComponentHDLGeneratorFactory.FPGATick);
    map.put("ClockBus", "s_" + GetClockNetName(ComponentInfo.GetComponent(), Nets));
    return map;
  }

  @Override
  public SortedMap<String, Integer> GetRegList(AttributeSet attrs) {
    final var map = new TreeMap<String, Integer>();
    map.put("s_output_regs", NrOfClockBits - 1);
    map.put("s_buf_regs", 2);
    map.put("s_counter_reg", NrOfBitsId);
    map.put("s_derived_clock_reg", PhaseId);
    return map;
  }

  @Override
  public String GetSubDir() {
    /*
     * this method returns the module directory where the HDL code needs to
     * be placed
     */
    return "base";
  }

  @Override
  public SortedMap<String, Integer> GetWireList(AttributeSet attrs, Netlist Nets) {
    final var map = new TreeMap<String, Integer>();
    map.put("s_counter_next", NrOfBitsId);
    map.put("s_counter_is_zero", 1);
    return map;
  }

  @Override
  public boolean HDLTargetSupported(AttributeSet attrs) {
    return true;
  }
}
