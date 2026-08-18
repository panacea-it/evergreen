package com.prod.evergreen.api

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.prod.evergreen.models.AllTasks

import com.prod.evergreen.models.Countdata

class SharedViewModel : ViewModel() {
    private val _sharedData = MutableLiveData<AllTasks>()
    val sharedData: LiveData<AllTasks> get() = _sharedData

    fun setSharedData(data: AllTasks) {
        _sharedData.value = data
    }

    private val _sharedDatacounter = MutableLiveData<Countdata>()
    val sharedDatacounter: LiveData<Countdata> get() = _sharedDatacounter

    fun setSharedDataCounter(data: Countdata) {
        _sharedDatacounter.value = data
    }
}