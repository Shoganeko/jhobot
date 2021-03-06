package dev.shog.chad.framework.handle

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import kotlin.random.Random

/**
 * This class works by caching previously requested posts, and then using them when they're requested.
 *
 * When a subreddit is requested, the contents is saved and that will be used whenever it's requested again. This is very helpful to limit requests to Reddit.
 *
 * Data is purged every day, to keep them memes fresh
 *
 * @author sho
 */
object Reddit {
    /**
     * The type of post
     */
    enum class PostType {
        HOT, NEW, TOP
    }

    /**
     * When there's a mass refresh going on, to not start another one until this one is complete.
     */
    var massRefresh = false

    /**
     * The subreddit's posts
     */
    @JvmStatic
    val subreddits = ConcurrentHashMap<String, ConcurrentHashMap<PostType, ArrayList<JSONObject>>>()

    /**
     * Gets a post from multiple subreddits
     */
    @JvmStatic
    fun getPost(subs: ArrayList<String>, postType: PostType, saveAll: Boolean = true): JSONObject? {
        val loc = Random.nextInt(subs.size)
        val sub = subs[loc]

        // Saves all subreddits: This makes retrieving posts from a command like Meme much faster, but uses a lot of resources and possibly can get ratelimited.
        if (saveAll) {
            GlobalScope.launch {
                for (subr in subs) {
                    launch { refreshSubreddit(subr, postType) }
                }
            }
        }

        return getPost(sub, postType)
    }

    /**
     * Gets a post from a single subreddit
     */
    @JvmStatic
    fun getPost(subr: String, postType: PostType): JSONObject? {
        val subreddit = subr.toLowerCase()

        if (!subreddits.containsKey(subreddit) || !subreddits[subreddit]!!.containsKey(postType))
            refreshSubreddit(subreddit, postType)

        val posts = subreddits[subreddit]!![postType]!!

        if (posts.isEmpty() && !refreshSubreddit(subreddit, postType))
            return null

        val loc = Random.nextInt(posts.size)
        val post = posts[loc]

        posts.remove(post)

        return post
    }

    /**
     * Refreshes a subreddit's posts
     */
    @JvmStatic
    private fun refreshSubreddit(subreddit: String, postType: PostType): Boolean {
        if (!subreddits.containsKey(subreddit))
            subreddits[subreddit] = ConcurrentHashMap()

        if (!subreddits[subreddit]!!.containsKey(postType))
            subreddits[subreddit]!![postType] = ArrayList()

        try {
            val json = when (postType) {
                PostType.HOT -> JsonHandler.read("https://reddit.com/r/$subreddit/hot.json?limit=100")
                PostType.NEW -> JsonHandler.read("https://reddit.com/r/$subreddit/new.json?limit=100")
                PostType.TOP -> JsonHandler.read("https://reddit.com/r/$subreddit/top.json?limit=100")
            } ?: return false

            if (json.getJSONObject("data").getJSONArray("children").isEmpty)
                return false

            subreddits[subreddit]!![postType]!!.clear()

            GlobalScope.launch {
                for (post in json.getJSONObject("data").getJSONArray("children")) {
                    launch {
                        try {
                            val postJson = post as JSONObject
                            if (!postJson.getJSONObject("data").getBoolean("stickied") && !(postJson.getJSONObject("data").getString("url").contains(postJson.getJSONObject("data").getString("permalink"))))
                                subreddits[subreddit]!![postType]!!.add(postJson)
                        } catch (ex: Exception) { }
                    }
                }
            }

            TimeUnit.MILLISECONDS.sleep(500)

            return true
        } catch (ex: Exception) {
            return false
        }
    }
}