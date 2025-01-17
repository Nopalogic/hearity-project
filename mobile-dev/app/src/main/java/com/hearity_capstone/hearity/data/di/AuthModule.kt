package com.hearity_capstone.hearity.data.di

import com.hearity_capstone.hearity.data.api.TokenProvider
import com.hearity_capstone.hearity.data.api.TokenProviderImpl
import com.hearity_capstone.hearity.data.repository.authentication.AuthRepository
import com.hearity_capstone.hearity.data.repository.authentication.AuthRepositoryImpl
import com.hearity_capstone.hearity.data.storage.TokenStorage
import com.hearity_capstone.hearity.viewModel.AuthViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val authModule = module {
    // Provide Singleton TokenStorage
    single { TokenStorage(get()) }

    // Provide Singleton TokenProvider
    single<TokenProvider> { TokenProviderImpl(get()) }

    // Provide singleton AuthRepository
    single<AuthRepository>(qualifier = named("AuthRepositoryImpl")) { AuthRepositoryImpl(get()) }

    // Provide AuthViewModel
    viewModel {
        AuthViewModel(
            repository = get(qualifier = named("AuthRepositoryImpl")),
            tokenProvider = get()
        )
    }
}
