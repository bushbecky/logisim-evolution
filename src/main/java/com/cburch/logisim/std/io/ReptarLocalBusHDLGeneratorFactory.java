/*
 * Logisim-evolution - digital logic design tool and simulator
 * Copyright by the Logisim-evolution developers
 *
 * https://github.com/logisim-evolution/
 *
 * This is free software released under GNU GPLv3 license
 */

package com.cburch.logisim.std.io;

import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.fpga.designrulecheck.Netlist;
import com.cburch.logisim.fpga.designrulecheck.netlistComponent;
import com.cburch.logisim.fpga.file.FileWriter;
import com.cburch.logisim.fpga.hdlgenerator.AbstractHDLGeneratorFactory;
import com.cburch.logisim.fpga.hdlgenerator.HDL;
import com.cburch.logisim.instance.Port;
import com.cburch.logisim.util.LineBuffer;
import java.util.ArrayList;
import java.util.SortedMap;
import java.util.TreeMap;

public class ReptarLocalBusHDLGeneratorFactory extends AbstractHDLGeneratorFactory {

  public ReptarLocalBusHDLGeneratorFactory() {
    super();
    myPorts
        .add(Port.INOUT, "Addr_Data_LB_io", 16, 0)
        .add(Port.INPUT, "SP6_LB_WAIT3_i", 1, ReptarLocalBus.SP6_LB_WAIT3_i)
        .add(Port.INPUT, "IRQ_i", 1, ReptarLocalBus.IRQ_i)
        .add(Port.OUTPUT, "SP6_LB_nCS3_o", 1, ReptarLocalBus.SP6_LB_nCS3_o)
        .add(Port.OUTPUT, "SP6_LB_nADV_ALE_o", 1, ReptarLocalBus.SP6_LB_nADV_ALE_o)
        .add(Port.OUTPUT, "SP6_LB_RE_nOE_o", 1, ReptarLocalBus.SP6_LB_RE_nOE_o)
        .add(Port.OUTPUT, "SP6_LB_nWE_o", 1, ReptarLocalBus.SP6_LB_nWE_o)
        .add(Port.OUTPUT, "Addr_LB_o", 9, ReptarLocalBus.Addr_LB_o);
  }

  @Override
  public ArrayList<String> getArchitecture(Netlist nets, AttributeSet attrs, String componentName) {
    final var contents = LineBuffer.getBuffer();
    if (HDL.isVhdl()) {
      contents
          .pair("compName", componentName)
          .add(FileWriter.getGenerateRemark(componentName, nets.projName()))
          .add("""
              
              ARCHITECTURE PlatformIndependent OF {{compName}} IS 
              
              BEGIN
              
              FPGA_out(0) <= NOT SP6_LB_WAIT3_i;
              FPGA_out(1) <= NOT IRQ_i;
              SP6_LB_nCS3_o       <= FPGA_in(0);
              SP6_LB_nADV_ALE_o   <= FPGA_in(1);
              SP6_LB_RE_nOE_o     <= FPGA_in(2);
              SP6_LB_nWE_o        <= FPGA_in(3);
              Addr_LB_o           <= FPGA_in(11 DOWNTO 4);
              
              IOBUF_Addresses_Datas : for i in 0 to Addr_Data_LB_io'length-1 generate
                IOBUF_Addresse_Data : IOBUF
                generic map (
                  DRIVE => 12,
                  IOSTANDARD => "LVCMOS18"
                  SLEW => "FAST"
                )
                port map (
                  O => Addr_Data_LB_o(i), -- Buffer output
                  IO => Addr_Data_LB_io(i), -- Buffer inout port (connect directly to top-level port)
                  I => Addr_Data_LB_i(i), -- Buffer input
                  T => Addr_Data_LB_tris_i -- 3-state enable input, high=input, low=output
                );
              end generate;
              
              END PlatformIndependent;
              """);
    }
    return contents.get();
  }

  @Override
  public ArrayList<String> getComponentInstantiation(Netlist theNetlist, AttributeSet attrs, String componentName) {
    return LineBuffer.getBuffer()
        .add("""
            COMPONENT LocalBus
               PORT ( SP6_LB_WAIT3_i     : IN  std_logic;
                      IRQ_i              : IN  std_logic;
                      Addr_Data_LB_io    : INOUT  std_logic_vector( 15 DOWNTO 0 );
                      Addr_LB_o          : OUT std_logic_vector( 8 DOWNTO 0 );
                      SP6_LB_RE_nOE_o    : OUT std_logic;
                      SP6_LB_nADV_ALE_o  : OUT std_logic;
                      SP6_LB_nCS3_o      : OUT std_logic;
                      SP6_LB_nWE_o       : OUT std_logic;
                      FPGA_in            : IN std_logic_vector(12 downto 0);
                      FPGA_out           : OUT std_logic_vector(1 downto 0);
                     Addr_Data_LB_i      : IN std_logic_vector(15 downto 0);
                     Addr_Data_LB_o      : OUT std_logic_vector(15 downto 0);
                     Addr_Data_LB_tris_i : IN std_logic);
            END COMPONENT;
            """)
        .getWithIndent();
  }

  @Override
  public ArrayList<String> getEntity(Netlist nets, AttributeSet attrs, String componentName) {
    return LineBuffer.getBuffer()
        .pair("compName", componentName)
        .add(FileWriter.getGenerateRemark(componentName, nets.projName()))
        .add(FileWriter.getExtendedLibrary())
        .add("""
            Library UNISIM;
            use UNISIM.vcomponents.all;
            
            ENTITY {{compName}} IS
               PORT ( Addr_Data_LB_io     : INOUT std_logic_vector(15 downto 0);
                      SP6_LB_nCS3_o       : OUT std_logic;
                      SP6_LB_nADV_ALE_o   : OUT std_logic;
                      SP6_LB_RE_nOE_o     : OUT std_logic;
                      SP6_LB_nWE_o        : OUT std_logic;
                      SP6_LB_WAIT3_i      : IN std_logic;
                      IRQ_i               : IN std_logic;
                      FPGA_in             : IN std_logic_vector(12 downto 0);
                      FPGA_out            : OUT std_logic_vector(1 downto 0);
                      Addr_LB_o           : OUT std_logic_vector(8 downto 0);
                      Addr_Data_LB_o      : OUT std_logic_vector(15 downto 0);
                      Addr_Data_LB_i      : IN std_logic_vector(15 downto 0);
                      Addr_Data_LB_tris_i : IN std_logic);
            END {{compName}};
            """)
        .get();
  }

  @Override
  public ArrayList<String> getModuleFunctionality(Netlist theNetlist, AttributeSet attrs) {
    final var contents = new ArrayList<String>();
    if (HDL.isVhdl()) {
      contents.add(" ");
    } else {
      // FIXME: hardcoded string
      throw new UnsupportedOperationException("Reptar Local Bus doesn't support Verilog yet.");
    }
    return contents;
  }

  @Override
  public SortedMap<String, String> getPortMap(Netlist nets, Object mapInfo) {
    final var map = new TreeMap<String, String>();
    if (!(mapInfo instanceof netlistComponent)) return map;
    final var ComponentInfo = (netlistComponent) mapInfo;
    map.putAll(super.getPortMap(nets, mapInfo));
    map.put(
        "Addr_Data_LB_io",
        String.format(
            "%s(%d DOWNTO %d)",
            LOCAL_INOUT_BUBBLE_BUS_NAME,
            ComponentInfo.getLocalBubbleInOutEndId(),
            ComponentInfo.getLocalBubbleInOutStartId()));
    map.put(
        "FPGA_in",
        String.format(
            "%s(%d DOWNTO %d)",
            LOCAL_INPUT_BUBBLE_BUS_NAME,
            ComponentInfo.getLocalBubbleInputEndId(),
            ComponentInfo.getLocalBubbleInputStartId()));
    map.put(
        "FPGA_out",
        String.format(
            "%s(%d DOWNTO %d)",
            LOCAL_OUTPUT_BUBBLE_BUS_NAME
                + ComponentInfo.getLocalBubbleOutputEndId()
                + ComponentInfo.getLocalBubbleOutputStartId()));
    map.putAll(
        GetNetMap(
            "Addr_Data_LB_o",
            true,
            ComponentInfo,
            ReptarLocalBus.Addr_Data_LB_o,
            nets));
    map.putAll(
        GetNetMap(
            "Addr_Data_LB_i",
            true,
            ComponentInfo,
            ReptarLocalBus.Addr_Data_LB_i,
            nets));
    map.putAll(
        GetNetMap(
            "Addr_Data_LB_tris_i",
            true,
            ComponentInfo,
            ReptarLocalBus.Addr_Data_LB_tris_i,
            nets));
    return map;
  }

  @Override
  public boolean isHdlSupportedTarget(AttributeSet attrs) {
    return HDL.isVhdl();
  }
}
