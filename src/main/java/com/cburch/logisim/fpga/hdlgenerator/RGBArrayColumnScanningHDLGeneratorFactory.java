package com.cburch.logisim.fpga.hdlgenerator;

import com.cburch.logisim.util.LineBuffer;
import java.util.ArrayList;
import java.util.SortedMap;
import java.util.TreeMap;

import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.fpga.designrulecheck.Netlist;

public class RGBArrayColumnScanningHDLGeneratorFactory extends LedArrayColumnScanningHDLGeneratorFactory {

  public static String RGBArrayName = "RGBArrayColumnScanning";

  public static ArrayList<String> getPortMap(int id) {
    final var contents =
        (new LineBuffer())
            .addPair("addr", LedArrayGenericHDLGeneratorFactory.LedArrayColumnAddress)
            .addPair("clock", TickComponentHDLGeneratorFactory.FPGA_CLOCK)
            .addPair("insR", LedArrayGenericHDLGeneratorFactory.LedArrayRedInputs)
            .addPair("insG", LedArrayGenericHDLGeneratorFactory.LedArrayGreenInputs)
            .addPair("insB", LedArrayGenericHDLGeneratorFactory.LedArrayBlueInputs)
            .addPair("outsR", LedArrayGenericHDLGeneratorFactory.LedArrayRowRedOutputs)
            .addPair("outsG", LedArrayGenericHDLGeneratorFactory.LedArrayRowGreenOutputs)
            .addPair("outsB", LedArrayGenericHDLGeneratorFactory.LedArrayRowBlueOutputs)
            .addPair("id", id);

    if (HDL.isVHDL()) {
      contents.add(
          "PORT MAP ( {{addr}} => {{addr}}{{id}},",
          "           {{clock}} => {{clock}},",
          "           {{outsR}} => {{outsR}}{{id}},",
          "           {{outsG}} => {{outsG}}{{id}},",
          "           {{outsB}} => {{outsB}}{{id}},",
          "           {{insR}} => s_{{insR}}{{id}},",
          "           {{insG}} => s_{{insG}}{{id}},",
          "           {{insB}} => s_{{insB}}{{id}} );");
    } else {
      contents.add(
          "( .{{addr}}({{addr}}{{id}}),",
          "  .{{clock}({{clock}}),",
          "  .{{outsR}}({{outsR}}{{id}}),",
          "  .{{outsG}}({{outsG}}{{id}}),",
          "  .{{outsB}}({{outsB}}{{id}}),",
          "  .{{insR}}(s_{{insR}}{{id}}),",
          "  .{{insG}}(s_{{insG}}{{id}}),",
          "  .{{insB}}(s_{{insB}}{{id}}) );");
    }
    return contents.getWithIndent(6);
  }

  @Override
  public SortedMap<String, Integer> GetOutputList(Netlist TheNetlist, AttributeSet attrs) {
    final var outputs = new TreeMap<String, Integer>();
    outputs.put(LedArrayGenericHDLGeneratorFactory.LedArrayColumnAddress, nrOfColumnAddressBitsGeneric);
    outputs.put(LedArrayGenericHDLGeneratorFactory.LedArrayRowRedOutputs, nrOfRowsGeneric);
    outputs.put(LedArrayGenericHDLGeneratorFactory.LedArrayRowGreenOutputs, nrOfRowsGeneric);
    outputs.put(LedArrayGenericHDLGeneratorFactory.LedArrayRowBlueOutputs, nrOfRowsGeneric);
    return outputs;
  }

  @Override
  public SortedMap<String, Integer> GetInputList(Netlist TheNetlist, AttributeSet attrs) {
    final var inputs = new TreeMap<String, Integer>();
    inputs.put(TickComponentHDLGeneratorFactory.FPGA_CLOCK, 1);
    inputs.put(LedArrayGenericHDLGeneratorFactory.LedArrayRedInputs, nrOfLedsGeneric);
    inputs.put(LedArrayGenericHDLGeneratorFactory.LedArrayGreenInputs, nrOfLedsGeneric);
    inputs.put(LedArrayGenericHDLGeneratorFactory.LedArrayBlueInputs, nrOfLedsGeneric);
    return inputs;
  }

  @Override
  public SortedMap<String, Integer> GetWireList(AttributeSet attrs, Netlist Nets) {
    final var wires = new TreeMap<String, Integer>();
    wires.putAll(super.GetWireList(attrs, Nets));
    wires.put("s_maxRedLedInputs", maxNrLedsGeneric);
    wires.put("s_maxBlueLedInputs", maxNrLedsGeneric);
    wires.put("s_maxGreenLedInputs", maxNrLedsGeneric);
    return wires;
  }

  @Override
  public ArrayList<String> GetModuleFunctionality(Netlist netlist, AttributeSet attrs) {
    final var redIn = LedArrayGenericHDLGeneratorFactory.LedArrayRedInputs;
    final var greenIn = LedArrayGenericHDLGeneratorFactory.LedArrayGreenInputs;
    final var blueIn = LedArrayGenericHDLGeneratorFactory.LedArrayBlueInputs;

    final var redOut = LedArrayGenericHDLGeneratorFactory.LedArrayRedOutputs;
    final var greenOut = LedArrayGenericHDLGeneratorFactory.LedArrayGreenOutputs;
    final var blueOut = LedArrayGenericHDLGeneratorFactory.LedArrayBlueOutputs;

    final var contents =
        (new LineBuffer())
            .addPair("nrOfLeds", nrOfLedsString)
            .addPair("nrOfRows", nrOfRowsString)
            .addPair("activeLow", activeLowString)
            .addPair("insR", LedArrayGenericHDLGeneratorFactory.LedArrayRedInputs)
            .addPair("insG", LedArrayGenericHDLGeneratorFactory.LedArrayGreenInputs)
            .addPair("insB", LedArrayGenericHDLGeneratorFactory.LedArrayBlueInputs)
            .addPair("outsR", LedArrayGenericHDLGeneratorFactory.LedArrayRedOutputs)
            .addPair("outsG", LedArrayGenericHDLGeneratorFactory.LedArrayGreenOutputs)
            .addPair("outsB", LedArrayGenericHDLGeneratorFactory.LedArrayBlueOutputs);

    contents.add(getColumnCounterCode());
    if (HDL.isVHDL()) {
      contents.add(
          "makeVirtualInputs : PROCESS ( internalRedLeds, internalGreenLeds, internalBlueLeds ) IS",
          "BEGIN",
          "   s_maxRedLedInputs <= (OTHERS => '0');",
          "   s_maxGreenLedInputs <= (OTHERS => '0');",
          "   s_maxBlueLedInputs <= (OTHERS => '0');",
          "   IF ({{activeLow}} = 1) THEN",
          "      s_maxRedLedInputs({{nrOfLeds}}-1 DOWNTO 0)   <= NOT {{insR}};",
          "      s_maxGreenLedInputs({{nrOfLeds}}-1 DOWNTO 0) <= NOT {{insG}};",
          "      s_maxBlueLedInputs({{nrOfLeds}}-1 DOWNTO 0)  <= NOT {{insB}};",
          "   ELSE",
          "      s_maxRedLedInputs({{nrOfLeds}}-1 DOWNTO 0)   <= {{insR}};",
          "      s_maxGreenLedInputs({{nrOfLeds}}-1 DOWNTO 0) <= {{insG}};",
          "      s_maxBlueLedInputs({{nrOfLeds}}-1 DOWNTO 0)  <= {{insB}};",
          "   END IF;",
          "END PROCESS makeVirtualInputs;",
          "",
          "GenOutputs : FOR n IN {{nrOfRows}}-1 DOWNTO 0 GENERATE",
          "   {{insR}}(n) <= s_maxRedLedInputs(to_integer(unsigned(s_columnCounterReg)) + n*nrOfColumns);",
          "   {{insG}}(n) <= s_maxGreenLedInputs(to_integer(unsigned(s_columnCounterReg)) + n*nrOfColumns);",
          "   {{insB}}(n) <= s_maxBlueLedInputs(to_integer(unsigned(s_columnCounterReg)) + n*nrOfColumns);",
          "END GENERATE GenOutputs;");
    } else {
      contents.add(
          "",
          "genvar i;",
          "generate",
          "   for (i = 0; i < {{nrOfRows}}; i = i + 1) begin",
          "      assign {{outsR}}[i] = (activeLow == 1)",
          "         ? ~{{insR}}[i*nrOfColumns+s_columnCounterReg]",
          "         :  {{insR}}[i*nrOfColumns+s_columnCounterReg];",
          "      assign {{outsR}}[i] = (activeLow == 1)",
          "         ? ~{{insG}}[i*nrOfColumns+s_columnCounterReg]",
          "         :  {{insG}[i*nrOfColumns+s_columnCounterReg];",
          "      assign {{outsB}}[i] = (activeLow == 1)",
          "         ? ~{{insB}}[i*nrOfColumns+s_columnCounterReg]",
          "         :  [{insB}}[i*nrOfColumns+s_columnCounterReg];",
          "   end",
          "endgenerate");
    }
    return contents.getWithIndent();
  }

  @Override
  public String getComponentStringIdentifier() {
    return RGBArrayName;
  }

}
