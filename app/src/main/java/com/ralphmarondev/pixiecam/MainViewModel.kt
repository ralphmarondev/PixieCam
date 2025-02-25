package com.ralphmarondev.pixiecam

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val messages = listOf(
        "PixieCam sees a cute person!",
        "Hi, cutie Ralph Maron Eda!",
        "Where is that cute person?"
    )

    private val _triggered = MutableStateFlow(false)
    val triggered = _triggered.asStateFlow()

    private val _isRotated = MutableStateFlow(false)

    private val _response = MutableStateFlow(messages[0])
    val response = _response.asStateFlow()

    fun toggleRotated() {
        _triggered.value = true
        viewModelScope.launch {
            if (!_isRotated.value) {
                delay(3000)
                _response.value = messages[0]
                delay(3000)
                _response.value = messages[1]
                delay(3000)
            } else {
                delay(3000)
                _response.value = messages[2]
            }
        }
        _isRotated.value = true
    }
}