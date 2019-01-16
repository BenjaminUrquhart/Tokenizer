package net.benjaminurquhart.tokenizer.commands;

import java.awt.Color;
import java.io.IOException;
import java.util.Base64;
import java.util.List;

import org.kohsuke.github.GHContent;
import org.kohsuke.github.GitHub;

import net.benjaminurquhart.jch.Command;

import net.benjaminurquhart.tokenizer.Tokenizer;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

public class Check extends Command<Tokenizer> {

	private GitHub github;
	
	public Check(){
		super("check","bot id");
		try {
			github = GitHub.connectAnonymously();
		}
		catch (IOException e) {
			throw new IllegalStateException("Failed to authenticate with Github! " + e);
		}
	}
	@SuppressWarnings("deprecation")
	@Override
	public void handle(GuildMessageReceivedEvent event, Tokenizer self) {
		TextChannel channel = event.getChannel();
		String[] args = event.getMessage().getContentRaw().toUpperCase().split(" ", 3);
		if(args.length < 3){
			channel.sendMessage(this.getHelpMenu()).queue();
			return;
		}
		try{
			String b64ID = Base64.getEncoder().encodeToString(args[2].getBytes());
			List<GHContent> results = github.searchContent().q(b64ID).list().asList();
			for(GHContent result : results){
				if(result.getContent().split("/[MN][A-Za-z\\d]{23}\\.[\\w-]{6}\\.[\\w-]{27}/g", 2).length > 1){
					EmbedBuilder eb = new EmbedBuilder();
					eb.setColor(Color.RED);
					eb.setTitle("Token Found", result.getGitUrl());
					eb.addField("Repository", String.format("[%s](%s)", result.getOwner().getName(), result.getOwner().getHttpTransportUrl()), true);
					eb.addField("File", String.format("[%s](%s)", result.getName(), result.getGitUrl()), true);
					channel.sendMessage(eb.build()).queue();
					return;
				}
			}
			channel.sendMessage("No results found! Safe!").queue();
		}
		catch(Exception e){
			channel.sendMessage(e.toString()).queue();
			e.printStackTrace();
		}
	}

}
