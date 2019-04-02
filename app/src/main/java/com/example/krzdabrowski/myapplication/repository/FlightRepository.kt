package com.example.krzdabrowski.myapplication.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.krzdabrowski.myapplication.model.Flight
import com.example.krzdabrowski.myapplication.retrofit.SpaceXService
import io.objectbox.Box
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import timber.log.Timber

class FlightRepository(private val service: SpaceXService) : BaseRepository<Flight> {

    override fun fetchData(): LiveData<List<Flight>> {
        val result = MutableLiveData<List<Flight>>()

        CoroutineScope(Dispatchers.IO).launch {
            val request = service.getNextFlightsAsync()
            withContext(Dispatchers.Main) {
                try {
                    val response = request.await()
                    if (response.isSuccessful) {
                        result.value = response.body()
                    } else {
                        Timber.e("Error occurred with code ${response.code()}")
                    }
                } catch (e: HttpException) {
                    Timber.e("Error: ${e.message()}")
                } catch (e: Throwable) {
                    Timber.e("Error: ${e.message}")
                }
            }
        }

        return result
    }

    override fun saveToDatabase(box: Box<Flight>, data: List<Flight>) {
        CoroutineScope(Dispatchers.IO).launch {
            box.put(data)
        }
    }
}