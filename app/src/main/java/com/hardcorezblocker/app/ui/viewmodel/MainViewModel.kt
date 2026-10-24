package com.hardcorezblocker.app.ui.viewmodel

import android.app.Application
import android.app.role.RoleManager
import android.content.Context
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hardcorezblocker.app.data.model.BlockRule
import com.hardcorezblocker.app.data.model.BlockSettings
import com.hardcorezblocker.app.data.model.BlockedLog
import com.hardcorezblocker.app.data.model.BlockingMode
import com.hardcorezblocker.app.data.model.RuleType
import com.hardcorezblocker.app.data.model.SmsMessageItem
import com.hardcorezblocker.app.data.model.TargetChannel
import com.hardcorezblocker.app.data.repository.BlockRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = BlockRepository.getInstance(application)

    val settings: StateFlow<BlockSettings> = repository.settingsFlow
    val rules: StateFlow<List<BlockRule>> = repository.rulesFlow
    val logs: StateFlow<List<BlockedLog>> = repository.logsFlow

    private val _inboxMessages = MutableStateFlow<List<SmsMessageItem>>(emptyList())
    val inboxMessages: StateFlow<List<SmsMessageItem>> = _inboxMessages.asStateFlow()

    private val _isRoleHeld = MutableStateFlow(false)
    val isRoleHeld: StateFlow<Boolean> = _isRoleHeld.asStateFlow()

    private val _isDefaultSms = MutableStateFlow(false)
    val isDefaultSms: StateFlow<Boolean> = _isDefaultSms.asStateFlow()

    private val _isOnboardingCompleted = MutableStateFlow(repository.isOnboardingCompleted())
    val isOnboardingCompleted: StateFlow<Boolean> = _isOnboardingCompleted.asStateFlow()

    init {
        checkCallScreeningRole()
        checkDefaultSmsRole()
        refreshInbox()
    }

    fun refreshInbox() {
        viewModelScope.launch(Dispatchers.IO) {
            val list = repository.readInboxSms(getApplication())
            _inboxMessages.value = list
        }
    }

    fun checkCallScreeningRole() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getApplication<Application>().getSystemService(Context.ROLE_SERVICE) as? RoleManager
            _isRoleHeld.value = roleManager?.isRoleHeld(RoleManager.ROLE_CALL_SCREENING) == true
        } else {
            _isRoleHeld.value = true
        }
    }

    fun checkDefaultSmsRole() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getApplication<Application>().getSystemService(Context.ROLE_SERVICE) as? RoleManager
            _isDefaultSms.value = roleManager?.isRoleHeld(RoleManager.ROLE_SMS) == true
        } else {
            val defaultPkg = android.provider.Telephony.Sms.getDefaultSmsPackage(getApplication())
            _isDefaultSms.value = defaultPkg == getApplication<Application>().packageName
        }
    }

    fun deleteSmsBatch(ids: List<Long>, onComplete: (Int) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val count = repository.deleteSmsBatch(getApplication(), ids)
            refreshInbox()
            launch(Dispatchers.Main) {
                onComplete(count)
            }
        }
    }

    fun setProtectionActive(active: Boolean) {
        val current = settings.value
        repository.updateSettings(current.copy(isProtectionActive = active))
    }

    fun setBlockingMode(mode: BlockingMode) {
        val current = settings.value
        repository.updateSettings(current.copy(mode = mode))
    }

    fun setBlockForeignNumbers(enabled: Boolean) {
        val current = settings.value
        repository.updateSettings(current.copy(blockForeignNumbers = enabled))
    }

    fun setBlockSuspiciousLinks(enabled: Boolean) {
        val current = settings.value
        repository.updateSettings(current.copy(blockSuspiciousLinksInSms = enabled))
    }

    fun setSendNotificationOnBlock(enabled: Boolean) {
        val current = settings.value
        repository.updateSettings(current.copy(sendNotificationOnBlock = enabled))
    }

    fun setThemeMode(mode: com.hardcorezblocker.app.data.model.AppThemeMode) {
        val current = settings.value
        repository.updateSettings(current.copy(themeMode = mode))
    }

    fun setLanguage(language: com.hardcorezblocker.app.data.model.AppLanguage) {
        val current = settings.value
        repository.updateSettings(current.copy(language = language))
    }

    fun addRule(pattern: String, type: RuleType, channel: TargetChannel, label: String) {
        repository.addRule(
            BlockRule(
                pattern = pattern.trim(),
                type = type,
                channel = channel,
                label = label.trim()
            )
        )
    }

    fun toggleRule(ruleId: String, isEnabled: Boolean) {
        repository.toggleRule(ruleId, isEnabled)
    }

    fun deleteRule(ruleId: String) {
        repository.deleteRule(ruleId)
    }

    fun clearLogs(channel: TargetChannel? = null) {
        repository.clearLogs(channel)
    }

    fun testSimulation(input: String, isSms: Boolean): com.hardcorezblocker.app.data.repository.BlockRepository.SimulationResult {
        return repository.testRuleSimulation(input, isSms, getApplication())
    }

    fun exportRules(): String {
        return repository.exportRulesJson()
    }

    fun importRules(jsonString: String, replaceExisting: Boolean): Int {
        return repository.importRulesJson(jsonString, replaceExisting)
    }

    fun completeOnboarding() {
        repository.setOnboardingCompleted(true)
        _isOnboardingCompleted.value = true
    }

    fun triggerTutorial() {
        _isOnboardingCompleted.value = false
    }

    fun isSplashShown(): Boolean = repository.isSplashShown()

    fun markSplashShown() {
        repository.setSplashShown(true)
    }
}
