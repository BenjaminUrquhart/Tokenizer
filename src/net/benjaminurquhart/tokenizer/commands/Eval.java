package net.benjaminurquhart.tokenizer.commands;

import net.benjaminurquhart.jch.Command;
import net.benjaminurquhart.tokenizer.Tokenizer;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

public class Eval extends Command<Tokenizer>{

	private ScriptEngine se = new ScriptEngineManager().getEngineByName("Nashorn");
	
	
	private String eval(GuildMessageReceivedEvent event, Tokenizer self) throws ScriptException{
		se.put("channel", event.getChannel());
		se.put("guild", event.getGuild());
		se.put("msg", event.getMessage());
		se.put("user", event.getAuthor());
		se.put("jda", event.getJDA());
		se.put("event", event);
		se.put("self", self);
		String toEval = event.getMessage().getContentRaw().split(" ", 3)[2];
		return String.valueOf(se.eval(toEval));
	}
	@SuppressWarnings("deprecation")
	@Override
	public void handle(GuildMessageReceivedEvent event, Tokenizer self) {
		TextChannel channel = event.getChannel();
		if(event.getAuthor().getId().equals("273216249021071360")){
			try{
				String out = eval(event, self);
				if(out.length() > 1990){
					File file = new File("outputtokenizer.txt");
					if(file.exists()){
						file.delete();
					}
					file.createNewFile();
					FileOutputStream stream = new FileOutputStream(file);
					stream.write(out.getBytes());
					stream.close();
					channel.sendFile(file).queue();
				}
				else{
					channel.sendMessage("```" + out + "```").queue();
				}
			}
			catch(Exception e){
				channel.sendMessage(e.toString()).queue();
			}
		}
		else {
			try {
				URLConnection conn = new URL("https://aws.random.cat/meow").openConnection();
				conn.setRequestProperty("User-Agent", "Someone tried to eval");
				JSONObject json = new JSONObject(IOUtils.readLines(conn.getInputStream()).stream().reduce("",(x,y)->x+y));
				channel.sendMessage(json.getString("file")).queue();
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	@Override
	public boolean hide(){
		return true;
	}
}
