package tumblr;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tumblr.jumblr.JumblrClient;
import com.tumblr.jumblr.types.Blog;
import com.tumblr.jumblr.types.Post;

public class App {

	public App() {
	}
	
	public void start()
	{
		JumblrClient client = new JumblrClient("consumerKey", "consumerSecret");
		client.setToken("token", "tokenSecret");
		
		Blog blog = client.blogInfo("blog.tumblr.com");
		Map<String, Integer> options = new HashMap<String, Integer>();
		options.put("limit", 50);
		List<Post> posts = blog.posts(options);
		System.out.println("size: " + posts.size());
		System.out.println( "count: " + blog.getPostCount() );
		
		for (Post p: posts)
		{
			System.out.println( p.toString() );
		}
		
		options.put("offset", 50);
		
		blog.posts(options);
		System.out.println("size: " + posts.size());
		System.out.println( "count: " + blog.getPostCount() );
		
		for (Post p: posts)
		{
			System.out.println( p.toString() );
		}
	}
	
	public void download() throws Exception {
		Connection c = DriverManager.getConnection("jdbc:sqlite:blogs.db");
		c.setAutoCommit(false);
		
		PreparedStatement ps = c.prepareStatement("INSERT INTO blogs (user,json)"+" VALUES (?,?)");
		
		try {
			BlogDownloader bd = new BlogDownloader();
			bd.collect(c);
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			c.rollback();
		}
		
		ps.close();
		c.close();
	}

	public static void main(String[] args) {
		//new App().start();
		try {
			new App().download();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
