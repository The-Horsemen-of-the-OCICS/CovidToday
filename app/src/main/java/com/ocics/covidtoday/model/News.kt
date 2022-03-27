package com.ocics.covidtoday.model

import android.widget.SimpleAdapter


class News {
    private val newsList: MutableList<HashMap<String, String>> = ArrayList()

    public class NewsItem {
        private var title: String? = null
        private var link: String? = null

        public fun NewsItem(title: String?, link: String?) {
            this.title = title
            this.link = link
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
    }

    public fun getNewsList(): List<HashMap<String, String>> {
        val item = java.util.HashMap<String, String>()
        item["news_image"] = "https://cdn.cnn.com/cnnnext/dam/assets/220315134412-child-covid-vaccine-011922-super-tease.jpg"
        item["news_title"] = "US Surgeon General orders tech companies to reveal sources of COVID-19 misinformation"
        item["news_link"] = "https://www.engadget.com/us-surgeon-general-orders-tech-companies-to-reveal-sources-of-covid-19-misinformation-050540191.html"
        newsList.add(item)
        newsList.add(item)
        newsList.add(item)
        newsList.add(item)
        newsList.add(item)
        newsList.add(item)
        newsList.add(item)
        newsList.add(item)
        newsList.add(item)
        return newsList
    }

    //TODO Fetch data from API
    public fun fetchNewsFromAPI(adapter: SimpleAdapter){

    }
}