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
			if(github == null){
				try {
					github = self.getGHUsername() == null ? GitHub.connectAnonymously() : GitHub.connectUsingPassword(self.getGHUsername(), self.getGHPassword());
				}
				catch (IOException e) {
					throw new IllegalStateException("Failed to authenticate with Github! " + e);
				}
			}
			event.getJDA().retrieveUserById(args[2]).queue((user) -> {
				try{
					channel.sendTyping().queue();
					String b64ID = Base64.getEncoder().encodeToString(args[2].getBytes());
					List<GHContent> results = github.searchContent().q(b64ID).list().asList();
					for(GHContent result : results){
						if(result.getContent().contains(b64ID)){
							EmbedBuilder eb = new EmbedBuilder();
							eb.setColor(Color.RED);
							eb.setTitle("Token Found", result.getGitUrl());
							eb.setDescription(user.getAsMention());
							eb.addField("Repository", String.format("[%s](%s)", result.getOwner().getName(), result.getOwner().getHttpTransportUrl()), true);
							eb.addField("File", String.format("[%s](%s)", result.getName(), result.getHtmlUrl()), true);
							channel.sendMessage(eb.build()).queue();
							return;
						}
					}
					channel.sendMessage("No results found! Safe!").queue();
				}
				catch(IOException e){
					channel.sendMessage(e.toString()).queue();
					e.printStackTrace();
				}
			}, (e) -> channel.sendMessage(e.toString()).queue());
		}
		catch(Exception e){
			channel.sendMessage(e.toString()).queue();
			e.printStackTrace();
		}
	}

}
