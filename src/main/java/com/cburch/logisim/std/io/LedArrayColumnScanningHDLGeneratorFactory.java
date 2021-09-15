/*
 * Logisim-evolution - digital logic design tool and simulator
 * Copyright by the Logisim-evolution developers
 *
 * https://github.com/logisim-evolution/
 *
 * This is free software released under GNU GPLv3 license
 */

package com.cburch.logisim.std.io;

import com.cburch.logisim.util.LineBuffer;
import java.util.ArrayList;
import java.util.SortedMap;
import java.util.TreeMap;

import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.fpga.designrulecheck.Netlist;
import com.cburch.logisim.fpga.hdlgenerator.AbstractHDLGeneratorFactory;
import com.cburch.logisim.fpga.hdlgenerator.HDL;
import com.cburch.logisim.fpga.hdlgenerator.TickComponentHDLGeneratorFactory;

public class LedArrayColumnScanningHDLGeneratorFactory extends AbstractHDLGeneratorFactory {

  public static final int NR_OF_LEDS_ID = -1;
  public static final int NR_OF_ROWS_ID = -2;
  public static final int NR_OF_COLUMNS_ID = -3;
  public static final int NR_OF_COLUMN_ADDRESS_BITS_ID = -4;
  public static final int ACTIVE_LOW_ID = -5;
  public static final int SCANNING_COUNTER_BITS_ID = -6;
  public static final int MAX_NR_LEDS_ID = -7;
  public static final int SCANNING_COUNTER_VALUE_ID = -8;
  public static final String NR_OF_ROWS_STRING = "nrOfRows";
  public static final String NR_OF_COLUMNS_STRING = "nrOfColumns";
  public static final String NR_OF_LEDS_STRING = "nrOfLeds";
  public static final String NR_OF_COLUMN_ADDRESS_BITS_STRING = "nrOfColumnAddressBits";
  public static final String SCANNING_COUNTER_BITS_STRING = "nrOfScanningCounterBits";
  public static final String SCANNING_COUNTER_VALUE_STRING = "scanningCounterReloadValue";
  public static final String MAX_NR_LEDS_STRING = "maxNrLedsAddrColumns";
  public static final String ACTIVE_LOW_STRING = "activeLow";
  public static final String HDL_IDENTIFIER = "LedArrayColumnScanning";

  public LedArrayColumnScanningHDLGeneratorFactory() {
    super();
    myParametersList
        .add(ACTIVE_LOW_STRING, ACTIVE_LOW_ID)
        .add(MAX_NR_LEDS_STRING, MAX_NR_LEDS_ID)
        .add(NR_OF_COLUMNS_STRING, NR_OF_COLUMNS_ID)
        .add(NR_OF_COLUMN_ADDRESS_BITS_STRING, NR_OF_COLUMN_ADDRESS_BITS_ID)
        .add(NR_OF_LEDS_STRING, NR_OF_LEDS_ID)
        .add(NR_OF_ROWS_STRING, NR_OF_ROWS_ID)
        .add(SCANNING_COUNTER_BITS_STRING, SCANNING_COUNTER_BITS_ID)
        .add(SCANNING_COUNTER_VALUE_STRING, SCANNING_COUNTER_VALUE_ID);
  }

  public static ArrayList<String> getGenericMap(int nrOfRows, int nrOfColumns, long fpgaClockFrequency, boolean activeLow) {
    final var nrColAddrBits = LedArrayGenericHDLGeneratorFactory.getNrOfBitsRequired(nrOfColumns);
    final var scanningReload = (int) (fpgaClockFrequency / (long) 1000);
    final var nrOfScanningBitsCount = LedArrayGenericHDLGeneratorFactory.getNrOfBitsRequired(scanningReload);
    final var maxNrLeds = ((int) Math.pow(2.0, (double) nrColAddrBits)) * nrOfRows;

    final var contents =
        (new LineBuffer())
            .pair("nrOfLeds", NR_OF_LEDS_STRING)
            .pair("ledsCount", nrOfRows * nrOfColumns)
            .pair("maxNrLeds", MAX_NR_LEDS_STRING)
            .pair("maxNrLedsCount", maxNrLeds)
            .pair("nrOfRows", NR_OF_ROWS_STRING)
            .pair("nrOfRowsCount", nrOfRows)
            .pair("nrOfColumns", NR_OF_COLUMNS_STRING)
            .pair("nrOfColumnsCount", nrOfColumns)
            .pair("activeLow", ACTIVE_LOW_STRING)
            .pair("activeLowValue", activeLow ? "1" : "0")
            .pair("nrColAddrBits", NR_OF_COLUMN_ADDRESS_BITS_STRING)
            .pair("nrColAddrBitsCount", nrColAddrBits)
            .pair("scanningCounterBits", SCANNING_COUNTER_BITS_STRING)
            .pair("nrOfScanningBitsCount", nrOfScanningBitsCount)
            .pair("scanningCounter", SCANNING_COUNTER_VALUE_STRING)
            .pair("scanningValue", (scanningReload - 1));

    if (HDL.isVHDL()) {
      contents.add("""
          GENERIC MAP ( {{nrOfLeds}} => {{ledsCount}},
                        {{nrOfRows}} => {{nrOfRowsCount}},
                        {{nrOfColumns}} => {{nrOfColumnsCount}},
                        {{nrColAddrBits}} => {{nrColAddrBitsCount}},
                        {{scanningCounterBits}} => {{nrOfScanningBitsCount}},
                        {{scanningCounter}} => {{scanningValue}},
                        {{maxNrLeds}} => {{maxNrLedsCount}},
                        {{activeLow}} => {{activeLowValue}} )
          """);
    } else {
      contents.add("""
          #( .{{nrOfLeds}}({{ledsCount}}),
             .{{nrOfRows}}({{nrOfRowsCount}}),
             .{{nrOfColumns}}({{nrOfColumnsCount}}),
             .{{nrColAddrBits}}({{nrColAddrBitsCount}}),
             .{{scanningCounterBits}}({{nrOfScanningBitsCount}}),
             .{{scanningCounter}}({{scanningValue}}),
             .{{maxNrLeds}}({{maxNrLedsCount}}),
             .{{activeLow}}({{activeLowValue}}) )
             """);
    }
    return contents.getWithIndent(6);
  }

  public static ArrayList<String> getPortMap(int id) {
    final var contents =
        (new LineBuffer())
            .pair("columnAddress", LedArrayGenericHDLGeneratorFactory.LedArrayColumnAddress)
            .pair("outs", LedArrayGenericHDLGeneratorFactory.LedArrayRowOutputs)
            .pair("clock", TickComponentHDLGeneratorFactory.FPGA_CLOCK)
            .pair("ins", LedArrayGenericHDLGeneratorFactory.LedArrayInputs)
            .pair("id", id);

    if (HDL.isVHDL()) {
      contents.add("""
          PORT MAP ( {{columnAddress}} => {{columnAddress}}{{id}},
                     {{outs}} => {{outs}}{{id}},
                     {{clock}} => {{clock}},
                     {{ins}} => s_{{ins}}{{id}} );
          """);
    } else {
      contents.add("""
          ( .{{columnAddress}}({{columnAddress}}{{id}}),
            .{{outs}}({{outs}}{{id}}),
            .{{clock}}({{clock}}),
            .{{ins}}({{s_{{ins}}{{id}}) );
          """);
    }
    return contents.getWithIndent(6);
  }

  @Override
  public SortedMap<String, Integer> GetOutputList(Netlist TheNetlist, AttributeSet attrs) {
    final var outputs = new TreeMap<String, Integer>();
    outputs.put(LedArrayGenericHDLGeneratorFactory.LedArrayColumnAddress, NR_OF_COLUMN_ADDRESS_BITS_ID);
    outputs.put(LedArrayGenericHDLGeneratorFactory.LedArrayRowOutputs, NR_OF_ROWS_ID);
    return outputs;
  }

  @Override
  public SortedMap<String, Integer> GetInputList(Netlist TheNetlist, AttributeSet attrs) {
    final var inputs = new TreeMap<String, Integer>();
    inputs.put(TickComponentHDLGeneratorFactory.FPGA_CLOCK, 1);
    inputs.put(LedArrayGenericHDLGeneratorFactory.LedArrayInputs, NR_OF_LEDS_ID);
    return inputs;
  }

  @Override
  public SortedMap<String, Integer> GetWireList(AttributeSet attrs, Netlist Nets) {
    final var wires = new TreeMap<String, Integer>();
    wires.put("s_columnCounterNext", NR_OF_COLUMN_ADDRESS_BITS_ID);
    wires.put("s_scanningCounterNext", SCANNING_COUNTER_BITS_ID);
    wires.put("s_tickNext", 1);
    wires.put("s_maxLedInputs", MAX_NR_LEDS_ID);
    return wires;
  }

  @Override
  public SortedMap<String, Integer> GetRegList(AttributeSet attrs) {
    final var regs = new TreeMap<String, Integer>();
    regs.put("s_columnCounterReg", NR_OF_COLUMN_ADDRESS_BITS_ID);
    regs.put("s_scanningCounterReg", SCANNING_COUNTER_BITS_ID);
    regs.put("s_tickReg", 1);
    return regs;
  }

  public ArrayList<String> getColumnCounterCode() {
    final var contents =
        (new LineBuffer())
            .pair("columnAddress", LedArrayGenericHDLGeneratorFactory.LedArrayColumnAddress)
            .pair("clock", TickComponentHDLGeneratorFactory.FPGA_CLOCK)
            .pair("counterBits", SCANNING_COUNTER_BITS_STRING)
            .pair("counterValue", SCANNING_COUNTER_VALUE_STRING);

    if (HDL.isVHDL()) {
      contents.add(
          """

          {{columnAddress}} <= s_columnCounterReg;

          s_tickNext <= '1' WHEN s_scanningCounterReg = std_logic_vector(to_unsigned(0, {{counterBits}})) ELSE '0';

          s_scanningCounterNext <= (OTHERS => '0') WHEN s_tickReg /= '0' AND s_tickReg /= '1' ELSE -- for simulation
                                   std_logic_vector(to_unsigned({{counterValue}}-1, {{counterBits}}))
                                      WHEN s_scanningCounterReg = std_logic_vector(to_unsigned(0, {{counterBits}})) ELSE
                                   std_logic_vector(unsigned(s_scanningCounterReg)-1);

          s_columnCounterNext <= (OTHERS => '0') WHEN s_tickReg /= '0' AND s_tickReg /= '1' ELSE -- for simulation
                                 s_columnCounterReg WHEN s_tickReg = '0' ELSE
                                 std_logic_vector(to_unsigned(nrOfColumns-1,nrOfcolumnAddressBits))
                                    WHEN s_columnCounterReg = std_logic_vector(to_unsigned(0,nrOfColumnAddressBits)) ELSE
                                 std_logic_vector(unsigned(s_columnCounterReg)-1);

          makeFlops : PROCESS ({{clock}}) IS
          BEGIN
             IF (rising_edge({{clock}})) THEN
                s_columnCounterReg   <= s_columnCounterNext;
                s_scanningCounterReg <= s_scanningCounterNext;
                s_tickReg            <= s_tickNext;
             END IF;
          END PROCESS makeFlops;
          """);
    } else {
      contents
          .add("""

              assign columnAddress = s_columnCounterReg;

              assign s_tickNext = (s_scanningCounterReg == 0) ? 1'b1 : 1'b0;
              assign s_scanningCounterNext = (s_scanningCounterReg == 0) ? {{counterValue}} : s_scanningCounterReg - 1;
              assign s_columnCounterNext = (s_tickReg == 1'b0) ? s_columnCounterReg :
                                           (s_columnCounterReg == 0) ? nrOfColumns-1 : s_columnCounterReg-1;
              """)
          .addRemarkBlock("Here the simulation only initial is defined")
          .add("""
              initial
              begin
                 s_columnCounterReg   = 0;
                 s_scanningCounterReg = 0;
                 s_tickReg            = 1'b0;
              end

              always @(posedge {{clock}})
              begin
                  s_columnCounterReg   = s_columnCounterNext;
                  s_scanningCounterReg = s_scanningCounterNext;
                  s_tickReg            = s_tickNext;
              end
              """);
    }
    return contents.getWithIndent();
  }

  @Override
  public ArrayList<String> GetModuleFunctionality(Netlist TheNetlist, AttributeSet attrs) {
    final var contents =
        (new LineBuffer())
            .pair("ins", LedArrayGenericHDLGeneratorFactory.LedArrayInputs)
            .pair("outs", LedArrayGenericHDLGeneratorFactory.LedArrayRowOutputs)
            .pair("nrOfLeds", NR_OF_LEDS_STRING)
            .pair("nrOfRows", NR_OF_ROWS_STRING)
            .pair("activeLow", ACTIVE_LOW_STRING)
            .add(getColumnCounterCode());

    if (HDL.isVHDL()) {
      contents.add("""
          makeVirtualInputs : PROCESS ( internalLeds ) IS
          BEGIN
             s_maxLedInputs <= (OTHERS => '0');
             IF ({{activeLow}} = 1) THEN
                s_maxLedInputs( {{nrOfLeds}}-1 DOWNTO 0) <= NOT {{ins}};
             ELSE
                s_maxLedInputs( {{nrOfLeds}}-1 DOWNTO 0) <= {{ins}};
             END IF;
          END PROCESS makeVirtualInputs;

          GenOutputs : FOR n IN {{nrOfRows}}-1 DOWNTO 0 GENERATE
             {{outs}}(n) <= s_maxLedInputs(to_integer(unsigned(s_columnCounterReg)) + n*nrOfColumns);
          END GENERATE GenOutputs;
          """);
    } else {
      contents.add("""

          genvar i;
          generate
             for (i = 0; i < {{nrOfRows}}; i = i + 1)
             begin: outputs
                assign {{outs}}[i] = (activeLow == 1)
                    ? ~{{ins}}[i * nrOfColumns + s_columnCounterReg]
                    :  {{ins}}[i * nrOfColumns + s_columnCounterReg];
             end
          endgenerate
          """);
    }
    return contents.getWithIndent();
  }
}
