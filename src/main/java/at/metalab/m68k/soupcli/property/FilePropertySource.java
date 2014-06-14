package at.metalab.m68k.soupcli.property;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

public class FilePropertySource extends PropertySource {

	private File directory;

	public FilePropertySource(File directory) {
		this.directory = directory;
	}

	@Override
	public String getValue(String propertyName) {
		try {
			return FileUtils.readFileToString(new File(directory, propertyName));
		}
		catch(IOException ioException) {
			return null;
		}
	}
}
