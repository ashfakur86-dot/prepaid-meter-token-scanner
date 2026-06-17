package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.TokenDatabase
import com.example.data.TokenEntity
import com.example.data.TokenRepository
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TokenViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TokenRepository

    init {
        val database = TokenDatabase.getDatabase(application)
        repository = TokenRepository(database.tokenDao())
    }

    val tokens: StateFlow<List<TokenEntity>> = repository.allTokens
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun processImage(uri: Uri, onSuccess: (Int) -> Unit, onFailure: (String) -> Unit) {
        _isLoading.value = true
        _errorMessage.value = null

        val context = getApplication<Application>().applicationContext
        try {
            val inputImage = InputImage.fromFilePath(context, uri)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

            recognizer.process(inputImage)
                .addOnSuccessListener { visionText ->
                    val recognizedText = visionText.text
                    val allNumbers = recognizedText.replace(Regex("[^0-9]"), "")

                    if (allNumbers.isEmpty()) {
                        _isLoading.value = false
                        onFailure(context.getString(com.example.R.string.no_tokens))
                        return@addOnSuccessListener
                    }

                    val tempTokens = mutableListOf<TokenEntity>()
                    val timestamp = System.currentTimeMillis()
                    var count = 0
                    for (i in 0 until allNumbers.length step 20) {
                        val end = if (i + 20 < allNumbers.length) i + 20 else allNumbers.length
                        val rawToken = allNumbers.substring(i, end)
                        val formatted = StringBuilder()
                        for (j in 0 until rawToken.length) {
                            formatted.append(rawToken[j])
                            if ((j + 1) % 4 == 0 && (j + 1) % 20 != 0 && j != rawToken.length - 1) {
                                formatted.append(" ")
                            }
                        }
                        if (formatted.isNotEmpty()) {
                            tempTokens.add(
                                TokenEntity(
                                    text = formatted.toString(),
                                    isEntered = false,
                                    timestamp = timestamp + count
                                )
                            )
                            count++
                        }
                    }

                    viewModelScope.launch {
                        repository.insertAll(tempTokens)
                        _isLoading.value = false
                        onSuccess(tempTokens.size)
                    }
                }
                .addOnFailureListener { e ->
                    _isLoading.value = false
                    val errorMsg = e.localizedMessage ?: "OCR extraction failed"
                    _errorMessage.value = errorMsg
                    onFailure(errorMsg)
                }
        } catch (e: Exception) {
            _isLoading.value = false
            val errorMsg = e.localizedMessage ?: "File opening error"
            _errorMessage.value = errorMsg
            onFailure(errorMsg)
        }
    }

    fun toggleEntered(token: TokenEntity) {
        viewModelScope.launch {
            repository.updateToken(token.copy(isEntered = !token.isEntered))
        }
    }

    fun deleteToken(token: TokenEntity) {
        viewModelScope.launch {
            repository.deleteTokenById(token.id)
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            repository.clearAllTokens()
        }
    }
}
