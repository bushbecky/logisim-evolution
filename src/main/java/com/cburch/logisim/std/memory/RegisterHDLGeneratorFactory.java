/*
 * Logisim-evolution - digital logic design tool and simulator
 * Copyright by the Logisim-evolution developers
 *
 * https://github.com/logisim-evolution/
 *
 * This is free software released under GNU GPLv3 license
 */

package com.cburch.logisim.std.memory;

import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.fpga.designrulecheck.Netlist;
import com.cburch.logisim.fpga.designrulecheck.netlistComponent;
import com.cburch.logisim.fpga.hdlgenerator.AbstractHDLGeneratorFactory;
import com.cburch.logisim.fpga.hdlgenerator.HDL;
import com.cburch.logisim.fpga.hdlgenerator.HDLParameters;
import com.cburch.logisim.fpga.hdlgenerator.HDLPorts;
import com.cburch.logisim.instance.Port;
import com.cburch.logisim.instance.StdAttr;
import com.cburch.logisim.util.LineBuffer;
import java.util.ArrayList;
import java.util.SortedMap;
import java.util.TreeMap;

public class RegisterHDLGeneratorFactory extends AbstractHDLGeneratorFactory {

  private static final String NR_OF_BITS_STRING = "NrOfBits";
  private static final int NR_OF_BITS_ID = -1;
  private static final String INVERT_CLOCK_STRING = "InvertClock";
  private static final int INVERT_CLOCK_ID = -2;

  public RegisterHDLGeneratorFactory() {
    super();
    myParametersList
        .add(NR_OF_BITS_STRING, NR_OF_BITS_ID)
        .add(INVERT_CLOCK_STRING, INVERT_CLOCK_ID, HDLParameters.MAP_ATTRIBUTE_OPTION, StdAttr.TRIGGER, AbstractFlipFlopHDLGeneratorFactory.TRIGGER_MAP);
    myWires
        .addWire("s_clock", 1)
        .addRegister("s_state_reg", NR_OF_BITS_ID);
    myPorts
        .add(Port.CLOCK, HDLPorts.getClockName(1), 1, Register.CK)
        .add(Port.INPUT, "Reset", 1, Register.CLR)
        .add(Port.INPUT, "ClockEnable", 1, Register.EN, false)
        .add(Port.INPUT, "D", NR_OF_BITS_ID, Register.IN)
        .add(Port.OUTPUT, "Q", NR_OF_BITS_ID, Register.OUT);
  }

  @Override
  public SortedMap<String, String> getPortMap(Netlist Nets, Object MapInfo) {
    final var map = new TreeMap<String, String>();
    map.putAll(super.getPortMap(Nets, MapInfo));
    if (MapInfo instanceof netlistComponent && HDL.isVhdl()) {
      final var comp = (netlistComponent) MapInfo;
      final var nrOfBits = comp.getComponent().getAttributeSet().getValue(StdAttr.WIDTH).getWidth();
      if (nrOfBits == 1) {
        final var inMap = map.get("D");
        final var outMap = map.get("Q");
        map.remove("D");
        map.remove("Q");
        map.put("D(0)", inMap);
        map.put("Q(0)", outMap);
      }
    }
    return map;
  }

  @Override
  public ArrayList<String> getModuleFunctionality(Netlist nets, AttributeSet attrs) {
    final var contents = LineBuffer.getBuffer()
            .pair("invertClock", INVERT_CLOCK_STRING)
            .pair("clock", HDLPorts.getClockName(1))
            .pair("Tick", HDLPorts.getTickName(1));
    if (HDL.isVhdl()) {
      contents.add("""
          Q       <= s_state_reg;
          s_clock <= {{clock}} WHEN {{invertClock}} = 0 ELSE NOT({{clock}});
          
          make_memory : PROCESS( s_clock , Reset , ClockEnable , {{Tick}} , D )
          BEGIN
             IF (Reset = '1') THEN s_state_reg <= (OTHERS => '0');
          """);
      if (Netlist.isFlipFlop(attrs)) {
        contents.add("""
               ELSIF (rising_Edge(s_clock)) THEN
                  IF (ClockEnable = '1' AND {{Tick}} = '1') THEN
                     s_state_reg <= D;
                  END IF;
               """);
      } else {
        contents.add("""
              ELSIF (s_clock = '1') THEN
                 IF (ClockEnable = '1' AND {{Tick}} = '1') THEN
                    s_state_reg <= D;
                 END IF;
              """);
      }
      contents.add("""
                 END IF;
              END PROCESS make_memory;
              """);
    } else {
      if (!Netlist.isFlipFlop(attrs)) {
        contents.add("""
            assign Q = s_state_reg;
            assign s_clock = {{invertClock}} == 0 ? {{clock}} : ~{{clock}};
            
            always @(*)
            begin
               if (Reset) s_state_reg <= 0;
               else if (s_Clock&ClockEnable&{{Tick}}) s_state_reg <= D;
            end
            """);
      } else {
        contents.add("""
            assign Q = s_state_reg;
            assign s_clock = {{invertClock}} == 0 ? {{clock}} : ~{{clock}};
            
            always @(posedge s_clock or posedge Reset)
            begin
               if (Reset) s_state_reg <= 0;
               else if (ClockEnable&{{Tick}}) s_state_reg <= D;
            end
            """);
      }
    }
    return contents.getWithIndent();
  }
}
