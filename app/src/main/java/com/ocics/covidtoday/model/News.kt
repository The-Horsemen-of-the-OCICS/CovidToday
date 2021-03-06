package com.ocics.covidtoday.model

import android.util.Log
import android.widget.SimpleAdapter
import com.kwabenaberko.newsapilib.NewsApiClient
import com.kwabenaberko.newsapilib.NewsApiClient.ArticlesResponseCallback
import com.kwabenaberko.newsapilib.models.request.EverythingRequest
import com.kwabenaberko.newsapilib.models.response.ArticleResponse
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class News(val country: String, val province: String) {
    private val TAG = "News"
    private val newsList: MutableList<HashMap<String, String>> = ArrayList()

    fun getNewsList(): List<HashMap<String, String>> {
        return newsList
    }

    private fun getQueryKeyword(): String {
        var result = if (province == "All") {
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

    fun fetchNewsFromAPI(adapter: SimpleAdapter) {
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

    private fun formatDate(dateString: String): String {
        val odt = OffsetDateTime.parse(dateString)
        val dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.US)

        return dtf.format(odt)
    }
}