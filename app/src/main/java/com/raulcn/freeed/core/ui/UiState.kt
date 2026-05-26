package com.raulcn.freeed.core.ui

data class UiMessage(
    val title: String,
    val body: String
)

data class AsyncState<T>(
    val isLoading: Boolean = false,
    val data: T? = null,
    val message: UiMessage? = null
)

