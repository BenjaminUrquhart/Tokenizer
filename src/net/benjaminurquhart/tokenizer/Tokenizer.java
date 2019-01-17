package net.benjaminurquhart.tokenizer;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;

import org.json.JSONObject;

import com.github.scribejava.apis.GitHubApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.oauth.OAuth20Service;

import net.benjaminurquhart.jch.CommandHandler;

import net.dv8tion.jda.core.JDABuilder;

public class Tokenizer {

	private static String token = "";
	private String username, password;
	
	private HashMap<String, OAuth2AccessToken> oauth;
	private OAuth20Service oauthService;
	
	private Tokenizer(){
		try{
			JSONObject json = new JSONObject(Files.lines(new File("tokenizer.json").toPath()).reduce("",(x,y)->x+y));
			token = json.getString("token");
			oauth = new HashMap<>();
			if(json.has("github")){
				json = json.getJSONObject("github");
				username = json.getString("username");
				password = json.getString("password");
				if(json.has("oauth")){
					json = json.getJSONObject("oauth");
					oauthService = new ServiceBuilder(json.getString("oauth_id"))
							.apiSecret(json.getString("oauth_secret"))
							.callback(json.getString("callback"))
							.userAgent(json.getString("user_agent"))
							.build(GitHubApi.instance());
					json.getJSONArray("tokens").forEach((obj) -> {
						JSONObject j = (JSONObject)obj;
						oauth.put(j.getString("id"), new OAuth2AccessToken(j.getString("token")));
					});
				}
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
	public String getOAuthTokenString(String id){
		return oauth.containsKey(id) ? oauth.get(id).getAccessToken() : null;
	}
	public OAuth2AccessToken getOAuthToken(String id){
		return oauth.containsKey(id) ? oauth.get(id) : null;
	}
	public void putOAuthToken(String id, OAuth2AccessToken token){
		oauth.put(id, token);
	}
	public OAuth20Service getOAuthService(){
		return oauthService;
	}
	public String getOAuthURL(){
		return oauthService == null ? null : oauthService.getAuthorizationUrl();
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
