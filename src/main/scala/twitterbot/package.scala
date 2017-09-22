/**
  * Created by salah on 6/23/16.
  */
import org.json4s._
import org.json4s.native.JsonMethods._
import scalaj.http._
import twitter4j.conf.ConfigurationBuilder
import twitter4j.{StatusUpdate, TwitterFactory}
import java.net.URL

package object twitterbot {
  def get_tweet = {
    def getTitle = {
      val url = "http://www.wikihow.com/api.php?action=query&list=random&rnnamespace=0&rnlimit=1&format=json"
      val r = Http(url).asString.body
      val json = parse(r)
      val title = json \ "query" \ "random" \ "title"
      title(0).values.toString()
    }

    def improveTitle(title: String) =
      title.split(" ").init.mkString("How to ", " ", " you FOOL!")

    def get_image_stream(title: String) = {
      val url = "http://www.wikihow.com/%s".format(title.replace(" ", "-"))
      val b = Http(url).asString
      val regex = """meta name="twitter:image:src" content="(.+?)"""".r
      val image_url = regex.findFirstMatchIn(b.body).map(_.group(1)).get
      new URL(image_url).openConnection().getInputStream()
    }

    val t = getTitle
    val tweet = improveTitle(t)
    (tweet, get_image_stream(t))
  }

  def sign_in_api = {
    val configuration = new ConfigurationBuilder()
      .setOAuthConsumerKey(sys.env("twitter_consumerKey"))
      .setOAuthConsumerSecret(sys.env("twitter_consumerSecret"))
      .setOAuthAccessToken(sys.env("twitter_accessToken"))
      .setOAuthAccessTokenSecret(sys.env("twitter_accessTokenSecret"))
      .build()
    new TwitterFactory(configuration).getInstance()
  }

  def main(args: Array[String]): Unit = {
    val (tweet, image) = get_tweet
    val status = new StatusUpdate(tweet)
    status.media("image.jpg", image)
    sign_in_api.updateStatus(status)
  }
}
