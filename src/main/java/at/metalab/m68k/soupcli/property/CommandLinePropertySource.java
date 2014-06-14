package at.metalab.m68k.soupcli.property;

import org.apache.commons.cli.CommandLine;

public class CommandLinePropertySource extends PropertySource {

	private CommandLine commandLine;

	public CommandLinePropertySource(CommandLine commandLine) {
		this.commandLine = commandLine;
	}

	@Override
	public String getValue(String propertyName) {
		return commandLine.getOptionValue(String.format("p_%s", propertyName));
	}

}
