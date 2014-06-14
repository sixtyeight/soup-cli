package at.metalab.m68k.soupcli;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.apache.commons.cli.CommandLine;

import at.metalab.m68k.soup.SoupClient;
import at.metalab.m68k.soup.SoupHelper;
import at.metalab.m68k.soup.resource.Blog;
import at.metalab.m68k.soup.resource.Group;
import at.metalab.m68k.soup.resource.PostResult;
import at.metalab.m68k.soup.resource.User;
import at.metalab.m68k.soup.resource.posts.Event;
import at.metalab.m68k.soup.resource.posts.FileUpload;
import at.metalab.m68k.soup.resource.posts.Link;
import at.metalab.m68k.soup.resource.posts.Postable;
import at.metalab.m68k.soup.resource.posts.Quote;
import at.metalab.m68k.soup.resource.posts.Regular;
import at.metalab.m68k.soup.resource.posts.Review;
import at.metalab.m68k.soup.resource.posts.Video;
import at.metalab.m68k.soupcli.property.CommandLinePropertySource;
import at.metalab.m68k.soupcli.property.FilePropertySource;
import at.metalab.m68k.soupcli.property.PropertySource;

public class SoupCli {
	private SoupClient soupClient;

	public SoupCli(SoupClient soupClient) {
		this.soupClient = soupClient;
	}

	public void process(CommandLine commandLine) {
		String postDirectory = commandLine.getOptionValue("postDirectory");

		PropertySource propertySource = new CommandLinePropertySource(
				commandLine);
		if (postDirectory != null) {
			propertySource = new FilePropertySource(new File(postDirectory));
		}

		String command = commandLine.getOptionValue("command");

		if (command == null) {
			System.out.println("command must be provided (-commmand cmd)");
		} else if ("post".equals(command)) {
			post(commandLine, propertySource);
		} else if ("listGroups".equals(command)) {
			listGroups(commandLine);
		} else if ("searchGroups".equals(command)) {
			searchGroups(commandLine);
		} else {
			System.out.println("unknown command: " + command);
		}
	}

	private void listGroups(CommandLine commandLine) {
		User user = soupClient.getUser();
		List<Blog> groups = SoupHelper.getGroups(user);

		Collections.sort(groups, SoupHelper.Comparators.Group.BY_NAME);

		for (Blog blog : groups) {
			System.out.println(blog.getName() + "\t" + blog.getResource()
					+ "\t" + blog.getPermissions().get(0));
		}
	}

	private void searchGroups(CommandLine commandLine) {
		List<Group> groups = soupClient.groupSearch((String) commandLine
				.getArgList().get(0));

		for (Group group : groups) {
			System.out.println(group.getName() + "\t" + group.getId() + "\t"
					+ group.getUrl());
		}
	}

	private void createGroup(CommandLine commandLine) {
	}

	private void joinGroup(CommandLine commandLine) {
	}

	private void leaveGroup(CommandLine commandLine) {
	}

	private void post(CommandLine commandLine, PropertySource propertySource) {
		Postable post = null;
		String type = commandLine.getOptionValue("type");

		if ("regular".equals(type)) {
			Regular regular = new Regular();
			regular.setBody(propertySource.getValue("body"));
			regular.setTags(propertySource.getValue("tags"));
			regular.setTitle(propertySource.getValue("title"));
			post = regular;
		} else if ("video".equals(type)) {
			Video video = new Video();
			video.setDescription(propertySource.getValue("description"));
			video.setEmbedCode(propertySource.getValue("embed_code"));
			video.setTags(propertySource.getValue("tags"));
			video.setUrl(propertySource.getValue("url"));
			post = video;
		} else if ("review".equals(type)) {
			Review review = new Review();
			review.setRating(Integer.valueOf(propertySource.getValue("rating")));
			review.setReview(propertySource.getValue("review"));
			review.setTags(propertySource.getValue("tags"));
			review.setTitle(propertySource.getValue("title"));
			review.setUrl(propertySource.getValue("url"));
			post = review;
		} else if ("event".equals(type)) {
			Event event = new Event();
			event.setDescription(propertySource.getValue("description"));
		} else if ("quote".equals(type)) {
			Quote quote = new Quote();
			quote.setQuote(propertySource.getValue("quote"));
			quote.setSource(propertySource.getValue("source"));
			quote.setTags(propertySource.getValue("tags"));
			post = quote;
		} else if ("link".equals(type)) {
			Link link = new Link();
			link.setCaption(propertySource.getValue("caption"));
			link.setDescription(propertySource.getValue("description"));
			link.setTags(propertySource.getValue("tags"));
			link.setUrl(propertySource.getValue("url"));
			post = link;
		} else if ("file".equals(type)) {
			FileUpload fileUpload = new FileUpload();
			fileUpload.setDescription(propertySource.getValue("description"));
			fileUpload.setFilename(propertySource.getValue("filename"));
			fileUpload.setTags(propertySource.getValue("tags"));
			fileUpload.setUrl(propertySource.getValue("url"));
			post = fileUpload;
		}

		User user = soupClient.getUser();
		Blog blog = SoupHelper.findByUrl(user,
				commandLine.getOptionValue("blog"));

		if (blog == null) {
			System.out.println("Could not find blog");
			return;
		}

		PostResult postResult = soupClient.post(blog, post);

		System.out.println("created post: " + postResult.getId());
	}
}
