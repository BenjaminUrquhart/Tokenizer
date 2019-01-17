package net.benjaminurquhart.tokenizer.commands;

import java.awt.Color;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.kohsuke.github.GHContent;
import org.kohsuke.github.GitHub;

import net.benjaminurquhart.jch.Command;

import net.benjaminurquhart.tokenizer.Tokenizer;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

public class Check extends Command<Tokenizer> {
	
	private GitHub github;
	
	private ScheduledExecutorService executor;
	
	protected List<QueuedAction> queue;
	
	public Check(){
		super("check","bot id");
		executor = Executors.newSingleThreadScheduledExecutor();
		queue = Collections.synchronizedList(new ArrayList<>());
		executor.scheduleWithFixedDelay(() -> {
			try {
				if(!queue.isEmpty()) {
					queue.remove(0).run();
				}
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}, 0, 5, TimeUnit.SECONDS);
	}
	@Override
	public void handle(GuildMessageReceivedEvent event, Tokenizer self) {
		TextChannel channel = event.getChannel();
		String[] args = event.getMessage().getContentRaw().toUpperCase().split(" ", 3);
		if(args.length < 3){
			channel.sendMessage(this.getHelpMenu()).queue();
			return;
		}
		try{
			if(github == null){
				try {
					github = self.getGHUsername() == null ? GitHub.connectAnonymously() : GitHub.connectUsingPassword(self.getGHUsername(), self.getGHPassword());
				}
				catch (IOException e) {
					throw new IllegalStateException("Failed to authenticate with Github! " + e);
				}
			}
			event.getJDA().retrieveUserById(args[2]).queue((user) -> {
				channel.sendMessage(String.format("Your request has been queued (Approximate wait time: %d seconds)", queue.size()*15)).queue((m) -> queue.add(new QueuedAction(event, user, github)));
			}, (e) -> channel.sendMessage(e.toString()).queue());
		}
		catch(Exception e){
			channel.sendMessage(e.toString()).queue();
			e.printStackTrace();
		}
	}
}
final class QueuedAction implements Runnable{
	
	private GuildMessageReceivedEvent event;
	private GitHub github;
	private User user;
	
	protected QueuedAction(GuildMessageReceivedEvent event, User user, GitHub github) {
		this.github = github;
		this.event = event;
		this.user = user;
	}
	@SuppressWarnings("deprecation")
	@Override
	public void run() {
		TextChannel channel = event.getChannel();
		try{
			channel.sendTyping().queue();
			User author = event.getAuthor();
			String b64ID = Base64.getEncoder().encodeToString(user.getId().getBytes());
			List<GHContent> results = github.searchContent().q(b64ID).list().asList();
			for(GHContent result : results){
				if(result.getContent().contains(b64ID)){
					EmbedBuilder eb = new EmbedBuilder();
					eb.setColor(Color.RED);
					eb.setTimestamp(Instant.now());
					eb.setTitle("Token Found", result.getGitUrl());
					eb.setDescription(user.getAsMention());
					eb.addField("Repository", String.format("[%s](%s)", result.getOwner().getName(), result.getOwner().getHtmlUrl()), true);
					eb.addField("File", String.format("[%s](%s)", result.getName(), result.getHtmlUrl()), true);
					eb.setFooter(String.format("Requested by %s#%s", author.getName(), author.getDiscriminator()), author.getEffectiveAvatarUrl());
					channel.sendMessage(author.getAsMention()).embed(eb.build()).queue();
					return;
				}
			}
			channel.sendMessage(author.getAsMention() + " No results found! Safe!").queue();
		}
		catch(Exception e){
			channel.sendMessage(e.toString()).queue();
			e.printStackTrace();
		}
	}
	
}
