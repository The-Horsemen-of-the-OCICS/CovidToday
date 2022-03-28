package com.ocics.covidtoday.model

import android.annotation.SuppressLint
import android.util.Log
import android.widget.SimpleAdapter
import com.kwabenaberko.newsapilib.NewsApiClient
import com.kwabenaberko.newsapilib.NewsApiClient.ArticlesResponseCallback
import com.kwabenaberko.newsapilib.models.request.EverythingRequest
import com.kwabenaberko.newsapilib.models.response.ArticleResponse
import java.text.DateFormat
import java.text.SimpleDateFormat


class News(val country: String, val province: String) {
    private val TAG = "News"
    private val newsList: MutableList<HashMap<String, String>> = ArrayList()

    public class NewsItem {
        private var title: String? = null
        private var link: String? = null
        private var author: String? = null

        public fun NewsItem(title: String?, link: String?, author: String?) {
            this.title = title
            this.link = link
            this.author = author
        }

        public fun getLink(): String? {
            return this.link
        }

        public fun setLink(link: String?) {
            this.link = link
        }

        public fun getTitle(): String? {
            return this.title
        }

        public fun setTitle(title: String?) {
            this.title = title
        }

        public fun getAuthor(): String? {
            return this.author
        }

        public fun setAuthor(link: String?) {
            this.author = author
        }
    }

    public fun getNewsList(): List<HashMap<String, String>> {
//        val item = java.util.HashMap<String, String>()
//        item["news_image"] =
//            "https://s.yimg.com/os/creatr-uploaded-images/2021-08/a6250ad0-004d-11ec-bf21-a29998f28b50"
//        item["news_title"] =
//            "US Surgeon General orders tech companies to reveal sources of COVID-19 misinformation"
//        item["news_link"] =
//            "https://www.engadget.com/us-surgeon-general-orders-tech-companies-to-reveal-sources-of-covid-19-misinformation-050540191.html"
//        newsList.add(item)
//        newsList.add(item)
//        newsList.add(item)
//        newsList.add(item)
//        newsList.add(item)
//        newsList.add(item)
//        newsList.add(item)
//        newsList.add(item)
//        newsList.add(item)
        return newsList
    }

    private fun getQueryKeyword(): String {
        var result = ""
        result = if (province == "All") {
            if (country == "Canada") {
                "(Canada OR CAN OR Canadian OR CA)"
            } else {
                "(United+States OR USA OR US OR U.S.)"
            }
        } else {
            "($province)"
        }
        result += " AND (COVID OR vaccine OR coronavirus)"
        Log.d(TAG, "getAreaKeyword, $result")
        return result
    }

    public fun fetchNewsFromAPI(adapter: SimpleAdapter) {
        val newsApiClient = NewsApiClient("41aa6852e4f84fcf8e2507d708596d2a")
        newsApiClient.getEverything(
            EverythingRequest.Builder()
                .q(getQueryKeyword())
                .language("en")
                .build(),
            object : ArticlesResponseCallback {
                override fun onSuccess(response: ArticleResponse) {
                    val articleList = response.articles
                    for (article in articleList) {
                        if (article.title == null || article.urlToImage == null) continue
                        if (article.title.contains("COVID")
                            || article.title.contains("vaccine")
                            || article.title.contains("lockdown")
                            || article.title.contains("pandemic")
                            || article.title.contains("outbreak")
                            || article.title.contains("coronavirus")
                        ) {
                            val item = java.util.HashMap<String, String>()
                            item["news_image"] = article.urlToImage
                            item["news_title"] = article.title
                            item["news_link"] = article.url
                            item["news_author"] =
                                (if (article.author !== null) article.author else '-').toString()

                            item["news_published_at"] = formatDate(article.publishedAt)
                            newsList.add(item)

                        }
                    }
                    adapter.notifyDataSetChanged()
                }

                override fun onFailure(throwable: Throwable) {
                    println(throwable.message)
                }
            }
        )
    }

    @SuppressLint("SimpleDateFormat")
    private fun formatDate(dataString: String): String {
        val dateFormat: DateFormat =
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
        val formatter: DateFormat =
            SimpleDateFormat("yyyy-MM-dd HH:mm")
        return formatter.format(dateFormat.parse(dataString))
    }
}