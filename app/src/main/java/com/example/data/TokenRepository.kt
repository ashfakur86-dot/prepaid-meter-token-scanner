package com.example.data

import kotlinx.coroutines.flow.Flow

class TokenRepository(private val tokenDao: TokenDao) {
    val allTokens: Flow<List<TokenEntity>> = tokenDao.getAllTokens()

    suspend fun insertToken(token: TokenEntity) {
        tokenDao.insertToken(token)
    }

    suspend fun insertAll(tokens: List<TokenEntity>) {
        tokenDao.insertAll(tokens)
    }

    suspend fun updateToken(token: TokenEntity) {
        tokenDao.updateToken(token)
    }

    suspend fun deleteTokenById(id: Int) {
        tokenDao.deleteTokenById(id)
    }

    suspend fun clearAllTokens() {
        tokenDao.clearAllTokens()
    }
}
