package com.cburch.logisim.fpga.fpgagui;

import com.cburch.logisim.fpga.designrulecheck.SimpleDRCContainer;

public class FPGAReportGui extends FPGAReport {
	private FPGAReportTabbedPane myCommander = null;

	public FPGAReportGui(FPGACommanderGui parent) {
		myCommander = parent.getReporterGui();
	}

	@Override
	public void AddErrorIncrement(String Message) {
		myCommander.AddErrors(new SimpleDRCContainer(Message,SimpleDRCContainer.LEVEL_NORMAL,true));
	}

	@Override
	public void AddError(Object Message) {
		if (Message instanceof String)
			myCommander.AddErrors(new SimpleDRCContainer(Message,SimpleDRCContainer.LEVEL_NORMAL));
		else
			myCommander.AddErrors(Message);
	}

	@Override
	public void AddFatalError(String Message) {
		myCommander.AddErrors(new SimpleDRCContainer(Message,SimpleDRCContainer.LEVEL_FATAL));
	}

	@Override
	public void AddSevereError(String Message) {
		myCommander.AddErrors(new SimpleDRCContainer(Message,SimpleDRCContainer.LEVEL_SEVERE));
	}

	@Override
	public void AddInfo(String Message) {
		myCommander.AddInfo(Message);
	}

	@Override
	public void AddSevereWarning(String Message) {
		myCommander.AddWarning(new SimpleDRCContainer(Message,SimpleDRCContainer.LEVEL_SEVERE));
	}

	@Override
	public void AddWarningIncrement(String Message) {
		myCommander.AddWarning(new SimpleDRCContainer(Message,SimpleDRCContainer.LEVEL_NORMAL,true));
	}

	@Override
	public void AddWarning(Object Message) {
		if (Message instanceof String)
			myCommander.AddWarning(new SimpleDRCContainer(Message,SimpleDRCContainer.LEVEL_NORMAL));
		else
			myCommander.AddWarning(Message);
	}

	@Override
	public void ClsScr() {
		myCommander.ClearConsole();
	}

	@Override
	public void print(String Message) {
		myCommander.AddConsole(Message);
	}
}
