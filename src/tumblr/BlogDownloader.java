package tumblr;

import java.io.FileNotFoundException;
import java.net.URL;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.PreparedStatement;

import org.json.simple.JsonArray;
import org.json.simple.JsonObject;
import org.json.simple.Jsoner;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.ilmtest.lib.io.DBUtils;
import com.ilmtest.lib.io.IOUtils;
import com.ilmtest.searchengine.model.Entry;
import com.ilmtest.util.text.TextUtils;

import jdk.nashorn.api.scripting.URLReader;

public class BlogDownloader {

	private String m_user;

	public BlogDownloader()
	{
	}

	private static String extractBody(String value) {
		Document d = Jsoup.parse(value);
		return TextUtils.htmlToPlainText(d);
	}

	public void collect(Connection c, String... blogs) throws Exception
	{
		int requests = 0;
		
		for (String blog: blogs)
		{
			System.out.println("Processing "+blog+"...");
			
			try {
				int offset = 0;

				while (true)
				{
					URL url = new URL("https://api.tumblr.com/v2/blog/"+blog+".tumblr.com/posts?api_key=XINbAiVLJDW5vyoLPwpYbzLMZVm7D4GPyYUpYKXrr7f8H49MHc&limit=50&offset="+offset);
					++requests;
					JsonObject json = (JsonObject)Jsoner.deserialize( new URLReader(url) );
					json = (JsonObject)json.get("response");

					JsonArray currentPosts = (JsonArray)json.get("posts");

					if ( currentPosts.isEmpty() || requests >= 1000 ) {
						break;
					}

					for (Object post: currentPosts)
					{					
						ps.setString(1, blog);
						ps.setString(2, Jsoner.serialize(post));
						ps.execute();
					}

					offset += 50;
				}
			} catch (FileNotFoundException fnfe) {
				fnfe.printStackTrace();
			}
		}
	}

	public void download() throws Exception
	{
		int offset = 0;

		String consumerKey = "myapikey";
		URL url = new URL("https://api.tumblr.com/v2/blog/"+m_user+".tumblr.com/posts?api_key="+consumerKey+"&limit=50&offset="+offset);
		JsonObject json = (JsonObject)Jsoner.deserialize( new URLReader(url) );
		json = (JsonObject)json.get("response");

		JsonArray posts = (JsonArray)json.get("posts");
		json = (JsonObject)json.get("blog");
		int total = json.getInteger("total_posts");

		for (Object o: posts)
		{
			JsonObject j = (JsonObject)o;

			String postUrl = j.getString("post_url");
			String sourceUrl = URLDecoder.decode( j.getString("source_url"), "UTF-8" );
			JsonArray tags = (JsonArray)j.get("tags");
			String body = new String();
			String type = j.getString("type");

			if ( type.equals("quote") ) {
				String source = extractBody( j.getString("source") );
				body = j.getString("text") + source;
			} else if ( type.equals("text") ) {
				body = extractBody( j.getString("body") );
			} else if ( type.equals("link") ) {
				body += j.getString("url");
				body = extractBody( j.getString("description") );
			} else if ( type.equals("photo") ) {
				JsonArray photos = (JsonArray)j.get("photos");
				JsonObject photo = (JsonObject)photos.get(0);
				photo = (JsonObject)photo.get("original_size");
				body = photo.getString("url");
				body += extractBody( j.getString("caption") );
			}
		}

		System.out.println(json.keySet().toString());
	}
}