package yichenhsiaonz;

import bsky4j.BlueskyFactory;
import bsky4j.api.entity.atproto.server.ServerCreateSessionRequest;
import bsky4j.api.entity.atproto.server.ServerCreateSessionResponse;
import bsky4j.api.entity.bsky.feed.FeedPostRequest;
import bsky4j.api.entity.bsky.feed.FeedPostResponse;
import bsky4j.api.entity.share.Response;
import bsky4j.domain.Service;
import io.github.redouane59.twitter.TwitterClient;
import io.github.redouane59.twitter.dto.tweet.Tweet;
import io.github.redouane59.twitter.signature.TwitterCredentials;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;

public class Main {
  public static void main(String[] args) {
    try{
      // configure twitter client
      TwitterClient twitterClient = getTwitterClient();

      // configure bluesky client
      String blueskyAccessToken = getBlueskyAccessToken();

      BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
      System.out.println("Enter the text you want to post: ");

      String content = reader.readLine();

      if(!(content == null || content.isEmpty())) {
        // post tweet
        Tweet tweet = twitterClient.postTweet(content);
        System.out.println("Tweet posted: " + tweet.getText());

        // post to bluesky
        Response<FeedPostResponse> response = BlueskyFactory
          .getInstance(Service.BSKY_SOCIAL.getUri())
          .feed().post(
            FeedPostRequest.builder()
              .accessJwt(blueskyAccessToken)
              .text(content)
              .build()
          );
        System.out.println("Posted: " + response.getJson());
      }


    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static TwitterClient getTwitterClient() {
    File twtConfigFile = new File("twtConfig.json");
    if (!twtConfigFile.exists()) {
      System.out.println("Please create a twtConfig.json file in the root directory of the project.");
    } else {
      System.out.println("twtConfig.json file found.");
      TwitterCredentials credentials = TwitterClient.getAuthentication(twtConfigFile);
      return new TwitterClient(credentials);
    }
    return null;
  }

  private static String getBlueskyAccessToken() throws Exception {
    // get bluesky access token
    File bsConfigFile = new File("bskyConfig.json");
    if (!bsConfigFile.exists()) {
      System.out.println("Please create a bskyConfig.json file in the root directory of the project.");
    } else {
      System.out.println("bskyConfig.json file found.");
    }
    JSONObject bsConfig = new JSONObject(Files.readString(bsConfigFile.toPath()));
    Response<ServerCreateSessionResponse> response = BlueskyFactory
      .getInstance(Service.BSKY_SOCIAL.getUri())
      .server().createSession(
        ServerCreateSessionRequest.builder()
          .identifier(bsConfig.getString("handle"))
          .password(bsConfig.getString("password"))
          .build()
      );
    return response.get().getAccessJwt();
  }
}