package com.ocics.covidtoday.fragment

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.ocics.covidtoday.model.CovidStatics
import com.ocics.covidtoday.repository.CovidCasesRepository

class CovidStatsViewModel(application: Application) :
    AndroidViewModel(application) {
    private var covidCaseData: LiveData<Map<String, Map<String, CovidStatics>>>? = null
    private lateinit var historyCovidCaseData: LiveData<Map<String, Long>>
    private lateinit var mCovidCasesRepo: CovidCasesRepository
    fun initialize() {
        mCovidCasesRepo = CovidCasesRepository()
        covidCaseData = mCovidCasesRepo.getCovidCasesData()
        historyCovidCaseData = mCovidCasesRepo.getCovidHistoryData()
    }

    fun getHistoryStatsData(): LiveData<Map<String, Long>> {
        return historyCovidCaseData
    }

    fun requestHistoryData(country: String, province: String) {
        mCovidCasesRepo.getCovidHistory(country, province)
    }
}
