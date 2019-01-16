package net.benjaminurquhart.tokenizer;

import java.io.File;
import java.nio.file.Files;

import org.json.JSONObject;

import net.benjaminurquhart.jch.CommandHandler;

import net.dv8tion.jda.core.JDABuilder;

public class Tokenizer {

	private static String token = "";
	private String username, password;
	
	private Tokenizer(){
		try{
			JSONObject json = new JSONObject(Files.lines(new File("tokenizer.json").toPath()).reduce("",(x,y)->x+y));
			token = json.getString("token");
			if(json.has("github")){
				json = json.getJSONObject("github");
				username = json.getString("username");
				password = json.getString("password");
			}
		}
		catch(Exception e){
			e.printStackTrace();
			System.exit(1);
		}
	}
	public String getGHUsername(){
		return username;
	}
	public String getGHPassword(){
		return password;
	}
	public static void main(String[] args) throws Exception{
		new JDABuilder()
		.addEventListener(
				new CommandHandler<Tokenizer>(
				new Tokenizer(), 
				"token ", 
				"273216249021071360", 
				"net.benjaminurquhart.tokenizer.commands"))
		.setToken(token)
		.build();
	}
}
