package com.chatgptlite.wanted

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Used to communicate between screens.
 */
class MainViewModel : ViewModel() {
    private val _drawerShouldBeOpened = MutableStateFlow(false)
    val drawerShouldBeOpened = _drawerShouldBeOpened.asStateFlow()
    private var _settingsScreenOpen = MutableStateFlow(false)
    var settingsScreenOpen = _settingsScreenOpen.asStateFlow()
    private var _agentScreenOpen = MutableStateFlow(false)
    var agentScreenOpen = _agentScreenOpen.asStateFlow()
    fun openDrawer() {
        _drawerShouldBeOpened.value = true
    }

    fun agentsScreenOpen(){
        _agentScreenOpen.value = true
    }
    fun agentsScreenClose(){
        _agentScreenOpen.value = false
    }
    fun settingsScreenOpen() {
        _settingsScreenOpen.value = true
    }

    fun closeSettingsScreen() {
        _settingsScreenOpen.value = false
    }
    fun resetOpenDrawerAction() {
        _drawerShouldBeOpened.value = false
    }
}