package net.benjaminurquhart.tokenizer.commands;

import java.util.List;

import net.benjaminurquhart.jch.Command;
import net.benjaminurquhart.tokenizer.Tokenizer;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

public class Help extends Command<Tokenizer>{

	public Help(){
		super("help");
	}
	@Override
	public void handle(GuildMessageReceivedEvent event, Tokenizer self) {
		TextChannel channel = event.getChannel();
		List<Command<Tokenizer>> commands = this.getHandler().getRegisteredCommands();
		String out = "```";
		for(Command<?> command : commands){
			if(command.hide()){
				continue;
			}
			out += command.getHelpMenu().replace("Usage:", "").trim() + " - " + command.getDescription() + "\n";
		}
		out += "```";
		channel.sendMessage(out).queue();
	}
	@Override
	public String getDescription() {
		return "does all the work";
	}

}
