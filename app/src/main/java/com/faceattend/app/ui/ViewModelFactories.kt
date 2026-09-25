package com.faceattend.app.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.faceattend.app.AppContainer
import com.faceattend.app.appContainer

/** Bridges the manually-wired [AppContainer] into `by viewModels()`. */
inline fun <reified VM : ViewModel> Fragment.appViewModels(
    crossinline create: (AppContainer) -> VM
): Lazy<VM> = viewModels {
    viewModelFactory {
        initializer { create(requireContext().appContainer) }
    }
}
