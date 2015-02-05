package at.metalab.m68k.soupcli;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import at.metalab.m68k.soup.NotAuthorizedException;
import at.metalab.m68k.soup.OAuthHelper;
import at.metalab.m68k.soup.SoupClient;
import at.metalab.m68k.soup.SoupClientImpl;
import at.metalab.m68k.soup.http.exceptions.InternalServerErrorException;
import at.metalab.m68k.soup.resource.User;

public class SoupMain {

	public static void main(String[] args) {
		CommandLine line = null;
		Options options = getOptions();

		try {
			CommandLineParser parser = new GnuParser();
			line = parser.parse(options, args);
		} catch (ParseException parseException) {
			printUsage(options, parseException);
			return;
		}

		if (line.hasOption("help")) {
			printUsage(options);
			return;
		}

		if (!line.hasOption("accesstokenfile")) {
			printUsage(options, "AccessToken File is mandatory");
			return;
		}

		SoupClient soupClient = null;
		Properties accessTokenProperties = new Properties();
		final String accesstokenFile = line.getOptionValue("accesstokenfile");

		try {
			accessTokenProperties = OAuthHelper
					.loadAccessTokenProperties(new FileInputStream(new File(
							accesstokenFile)));
		} catch (IOException loadAccessTokenFailed) {
			if (!line.hasOption("authenticate")) {
				printUsage(options,
						"AccessToken file not found, maybe you want to -authenticate first?");
				return;
			}
		}

		try {
			Properties soupApiProperties = OAuthHelper.loadApiProperties();
			soupClient = new SoupClientImpl(soupApiProperties,
					accessTokenProperties, 1337);
		} catch (IOException loadApiPropertiesFailed) {
			printUsage(options, "Could not load Soup OAuth API properties");
			return;
		}

		User user = null;

		try {
			try {
				user = soupClient.getUser();
			} catch (InternalServerErrorException nope) {
				throw new NotAuthorizedException();
			}
		} catch (NotAuthorizedException getUserFailed) {
			try {
				accessTokenProperties = soupClient.authenticate();
			} catch (NotAuthorizedException authenticateFailed) {
				printUsage(options, "Could not authenticate");
				return;
			}

			try {
				OAuthHelper.storeAccessTokenProperties(accessTokenProperties,
						user, new FileOutputStream(new File(accesstokenFile)));
			} catch (IOException storeAccessTokenFailed) {
				printUsage(options, "Could not write AccessToken file");
				return;
			}
		}

		try {
			new SoupCli(soupClient).process(line);
		} catch (FileNotFoundException fileNotFoundException) {
			printUsage(options, "Could not read input file: "
					+ fileNotFoundException.getMessage());
		}
	}

	private static Option createPropertyOption(String propertyName,
			String argName) {
		@SuppressWarnings("static-access")
		Option option = OptionBuilder.withArgName(argName).hasArg()
				.withDescription(String.format("Property '%s'", propertyName))
				.create(String.format("p_%s", propertyName));
		return option;
	}

	/**
	 * @return
	 */
	private static Options getOptions() {
		Options options = new Options();

		@SuppressWarnings("static-access")
		Option accessTokenFile = OptionBuilder.withArgName("file").hasArg()
				.withDescription("use given file for the access token")
				.create("accesstokenfile");
		options.addOption(accessTokenFile);

		@SuppressWarnings("static-access")
		Option help = OptionBuilder.withDescription("display this help")
				.create("help");
		options.addOption(help);

		@SuppressWarnings("static-access")
		Option type = OptionBuilder
				.withArgName(
						"'text'|'link'|'quote'|'image'|'video'|'file'|'review'|'event'")
				.hasArg().withDescription("type of the post").create("type");
		options.addOption(type);

		@SuppressWarnings("static-access")
		Option blogId = OptionBuilder.withArgName("url").hasArg()
				.withDescription("resource url of the blog in which to post")
				.create("blog");
		options.addOption(blogId);

		@SuppressWarnings("static-access")
		Option command = OptionBuilder.withArgName("cmd").hasArg()
				.withDescription("command to execute").create("command");
		options.addOption(command);

		@SuppressWarnings("static-access")
		Option authenticate = OptionBuilder.withDescription(
				"authenticate and store the accesstoken")
				.create("authenticate");
		options.addOption(authenticate);

		options.addOption(createPropertyOption("tags", "text"));
		options.addOption(createPropertyOption("title", "text"));
		options.addOption(createPropertyOption("body", "text"));
		options.addOption(createPropertyOption("source", "url"));
		options.addOption(createPropertyOption("url", "url"));
		options.addOption(createPropertyOption("description", "text"));
		options.addOption(createPropertyOption("caption", "text"));
		options.addOption(createPropertyOption("quote", "text"));
		options.addOption(createPropertyOption("file", "file"));
		options.addOption(createPropertyOption("embed_code", "text"));
		options.addOption(createPropertyOption("filename", "text"));
		options.addOption(createPropertyOption("review", "text"));
		options.addOption(createPropertyOption("rating", "text"));
		options.addOption(createPropertyOption("location", "text"));
		options.addOption(createPropertyOption("start_date", "text"));
		options.addOption(createPropertyOption("end_date", "text"));
		options.addOption(createPropertyOption("inputfile", "filename"));
		
		return options;
	}

	private static void printUsage(Options options) {
		printUsage(options, null, null);
	}

	private static void printUsage(Options options,
			ParseException parseException) {
		printUsage(options, parseException, null);
	}

	private static void printUsage(Options options, String message) {
		printUsage(options, null, message);
	}

	private static void printUsage(Options options,
			ParseException parseException, String message) {
		if (message != null) {
			System.out.println(String.format("Error: %s!", message));
		}
		if (parseException != null) {
			System.out.println(parseException.getMessage());
		}
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp(120, "soup", "Soup.io command line client",
				options, "");
	}

}
