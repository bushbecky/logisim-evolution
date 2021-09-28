/*
 * Logisim-evolution - digital logic design tool and simulator
 * Copyright by the Logisim-evolution developers
 *
 * https://github.com/logisim-evolution/
 *
 * This is free software released under GNU GPLv3 license
 */

package com.cburch.logisim.std.ttl;

import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.fpga.designrulecheck.Netlist;
import com.cburch.logisim.fpga.hdlgenerator.AbstractHdlGeneratorFactory;
import com.cburch.logisim.fpga.hdlgenerator.Hdl;
import com.cburch.logisim.instance.Port;

import java.util.ArrayList;

public class Ttl7451HDLGenerator extends AbstractHdlGeneratorFactory {

  public Ttl7451HDLGenerator() {
    super();
    myPorts
        .add(Port.INPUT, "A1", 1, 0)
        .add(Port.INPUT, "B1", 1, 9)
        .add(Port.INPUT, "C1", 1, 7)
        .add(Port.INPUT, "D1", 1, 8)
        .add(Port.INPUT, "A2", 1, 1)
        .add(Port.INPUT, "B2", 1, 2)
        .add(Port.INPUT, "C2", 1, 3)
        .add(Port.INPUT, "D2", 1, 4)
        .add(Port.OUTPUT, "Y1", 1, 6)
        .add(Port.OUTPUT, "Y2", 1, 5);
  }

  @Override
  public ArrayList<String> getModuleFunctionality(Netlist nets, AttributeSet attrs) {
    final var contents = new ArrayList<String>();
    contents.add("   " + Hdl.assignPreamble() + "Y1" + Hdl.assignOperator() + Hdl.notOperator()
            + "((A1" + Hdl.andOperator() + "B1)" + Hdl.orOperator() + "(C1" + Hdl.andOperator() + "D1));");
    contents.add("   " + Hdl.assignPreamble() + "Y2" + Hdl.assignOperator() + Hdl.notOperator()
            + "((A2" + Hdl.andOperator() + "B2)" + Hdl.orOperator() + "(C2" + Hdl.andOperator() + "D2));");
    return contents;
  }

  @Override
  public boolean isHdlSupportedTarget(AttributeSet attrs) {
    /* TODO: Add support for the ones with VCC and Ground Pin */
    if (attrs == null) return false;
    return (!attrs.getValue(TtlLibrary.VCC_GND));
  }
}
