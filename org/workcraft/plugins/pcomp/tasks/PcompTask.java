package org.workcraft.plugins.pcomp.tasks;

import java.io.File;
import java.util.ArrayList;

import org.workcraft.plugins.shared.PcompUtilitySettings;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.plugins.verification.tasks.ExternalProcessTask;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Task;
import org.workcraft.tasks.Result.Outcome;

public class PcompTask implements Task<ExternalProcessResult> {
	private File[] inputs;

	public PcompTask(File[] inputs)
	{
		this.inputs = inputs;
	}

	@Override
	public Result<ExternalProcessResult> run(ProgressMonitor<ExternalProcessResult> monitor)
	{
		ArrayList<String> command = new ArrayList<String>();
		command.add(PcompUtilitySettings.getPcompCommand());

		for (String arg : PcompUtilitySettings.getPcompArgs().split(" "))
			if (!arg.isEmpty())
				command.add(arg);

		for (File f : inputs)
			command.add(f.getAbsolutePath());

		Result<ExternalProcessResult> res = new ExternalProcessTask(command, new File(".")).run(monitor);

		if (res.getOutcome() != Outcome.FINISHED)
			return res;

		ExternalProcessResult retVal = res.getReturnValue();
		if (retVal.getReturnCode() < 2)
			return Result.finished(retVal);
		else
			return Result.failed(retVal);
	}
}