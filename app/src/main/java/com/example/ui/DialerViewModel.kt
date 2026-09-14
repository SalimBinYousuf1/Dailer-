package com.example.ui

import android.app.Application
import android.content.Context
import android.telecom.PhoneAccountHandle
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.DialerApplication
import com.example.data.local.BlockedNumberEntity
import com.example.data.local.DialerSettingsEntity
import com.example.data.model.CallLogItem
import com.example.data.model.CallType
import com.example.data.model.ContactItem
import com.example.data.model.CountryCode
import com.example.data.repository.CallLogRepository
import com.example.data.repository.ContactsRepository
import com.example.data.repository.DialerSettingsRepository
import com.example.telecom.ActiveCallInfo
import com.example.telecom.AppInCallService
import com.example.telecom.DialerAudioAndHaptics
import com.example.telecom.DialerCaller
import com.example.telecom.OngoingCallManager
import com.example.util.PhoneUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ScreenTab {
    DIALER,
    RECENTS,
    CONTACTS,
    SETTINGS
}

data class ContactSuggestion(
    val name: String,
    val number: String,
    val isGradientCard: Boolean,
    val avatarType: AvatarMonsterType,
    val lookupKey: String? = null
)

enum class AvatarMonsterType {
    BLUE_PURPLE,
    PINK
}

class DialerViewModel(application: Application) : AndroidViewModel(application) {

    private val contactsRepo = ContactsRepository(application)
    private val callLogRepo = CallLogRepository(application)
    private val settingsRepo = DialerSettingsRepository(
        (application as DialerApplication).database.dialerDao()
    )
    val audioHaptics = DialerAudioAndHaptics(application)

    private val _dialerInput = MutableStateFlow("")
    val dialerInput: StateFlow<String> = _dialerInput.asStateFlow()

    private val _selectedCountry = MutableStateFlow(PhoneUtils.getDefaultCountryCode(application))
    val selectedCountry: StateFlow<CountryCode> = _selectedCountry.asStateFlow()

    private val _activeTab = MutableStateFlow(ScreenTab.DIALER)
    val activeTab: StateFlow<ScreenTab> = _activeTab.asStateFlow()

    private val _allContacts = MutableStateFlow<List<ContactItem>>(emptyList())
    val allContacts: StateFlow<List<ContactItem>> = _allContacts.asStateFlow()

    private val _callLogs = MutableStateFlow<List<CallLogItem>>(emptyList())
    val callLogs: StateFlow<List<CallLogItem>> = _callLogs.asStateFlow()

    private val _contactSearchQuery = MutableStateFlow("")
    val contactSearchQuery: StateFlow<String> = _contactSearchQuery.asStateFlow()

    val settings: StateFlow<DialerSettingsEntity> = settingsRepo.settings
        .combine(MutableStateFlow(DialerSettingsEntity())) { saved, default ->
            saved ?: default
        }
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            DialerSettingsEntity()
        )

    val blockedNumbers: StateFlow<List<BlockedNumberEntity>> = settingsRepo.blockedNumbers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeCall: StateFlow<ActiveCallInfo?> = OngoingCallManager.callState

    // Dynamic contact matching for typed digits
    val matchedContacts: StateFlow<List<ContactItem>> = combine(_dialerInput, _allContacts) { input, contacts ->
        if (input.isEmpty()) {
            emptyList()
        } else {
            contacts.filter { contact ->
                PhoneUtils.matchesT9(input, contact.displayName, contact.primaryNumber) ||
                    contact.numbers.any { PhoneUtils.matchesT9(input, contact.displayName, it) }
            }.take(10)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Two floating suggestion cards matching the visual reference
    val suggestionCards: StateFlow<Pair<ContactSuggestion, ContactSuggestion>> = combine(
        _dialerInput,
        _allContacts,
        _callLogs
    ) { input, contacts, logs ->
        generateSuggestions(input, contacts, logs)
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        Pair(
            ContactSuggestion("Recent Calls", "Tap to grant contacts", true, AvatarMonsterType.BLUE_PURPLE),
            ContactSuggestion("Frequent Contacts", "Ready to dial", false, AvatarMonsterType.PINK)
        )
    )

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            try {
                val contacts = contactsRepo.getContacts()
                _allContacts.value = contacts
            } catch (_: Exception) {
            }

            try {
                val logs = callLogRepo.getCallLogs()
                _callLogs.value = logs
            } catch (_: Exception) {
            }
        }
    }

    private fun generateSuggestions(
        input: String,
        contacts: List<ContactItem>,
        logs: List<CallLogItem>
    ): Pair<ContactSuggestion, ContactSuggestion> {
        // If user is currently typing digits, match contacts
        if (input.isNotEmpty()) {
            val matches = contacts.filter { contact ->
                PhoneUtils.matchesT9(input, contact.displayName, contact.primaryNumber) ||
                    contact.numbers.any { PhoneUtils.matchesT9(input, contact.displayName, it) }
            }

            val card1 = matches.firstOrNull()?.let {
                ContactSuggestion(
                    name = it.displayName,
                    number = it.primaryNumber,
                    isGradientCard = true,
                    avatarType = AvatarMonsterType.BLUE_PURPLE,
                    lookupKey = it.lookupKey
                )
            } ?: ContactSuggestion(
                name = "Direct Dial",
                number = "${selectedCountry.value.code} $input",
                isGradientCard = true,
                avatarType = AvatarMonsterType.BLUE_PURPLE
            )

            val card2 = matches.getOrNull(1)?.let {
                ContactSuggestion(
                    name = it.displayName,
                    number = it.primaryNumber,
                    isGradientCard = false,
                    avatarType = AvatarMonsterType.PINK,
                    lookupKey = it.lookupKey
                )
            } ?: if (contacts.isNotEmpty()) {
                val other = contacts.firstOrNull { it.lookupKey != matches.firstOrNull()?.lookupKey }
                    ?: contacts.first()
                ContactSuggestion(
                    name = other.displayName,
                    number = other.primaryNumber,
                    isGradientCard = false,
                    avatarType = AvatarMonsterType.PINK,
                    lookupKey = other.lookupKey
                )
            } else {
                ContactSuggestion(
                    name = "Add Contact",
                    number = "Save new number",
                    isGradientCard = false,
                    avatarType = AvatarMonsterType.PINK
                )
            }

            return Pair(card1, card2)
        }

        // When input is empty: display real recent calls or favorites
        val recentLog1 = logs.firstOrNull()
        val recentLog2 = logs.getOrNull(1)

        val card1 = when {
            recentLog1 != null -> {
                ContactSuggestion(
                    name = recentLog1.cachedName ?: recentLog1.number.ifEmpty { "Recent Call" },
                    number = recentLog1.number.ifEmpty { "Incoming / Outgoing" },
                    isGradientCard = true,
                    avatarType = AvatarMonsterType.BLUE_PURPLE
                )
            }
            contacts.isNotEmpty() -> {
                val first = contacts.first()
                ContactSuggestion(
                    name = first.displayName,
                    number = first.primaryNumber,
                    isGradientCard = true,
                    avatarType = AvatarMonsterType.BLUE_PURPLE,
                    lookupKey = first.lookupKey
                )
            }
            else -> {
                ContactSuggestion(
                    name = "Tap to sync contacts",
                    number = "Permissions enabled",
                    isGradientCard = true,
                    avatarType = AvatarMonsterType.BLUE_PURPLE
                )
            }
        }

        val card2 = when {
            recentLog2 != null -> {
                ContactSuggestion(
                    name = recentLog2.cachedName ?: recentLog2.number.ifEmpty { "Recent Call" },
                    number = recentLog2.number.ifEmpty { "Call details" },
                    isGradientCard = false,
                    avatarType = AvatarMonsterType.PINK
                )
            }
            contacts.size > 1 -> {
                val second = contacts[1]
                ContactSuggestion(
                    name = second.displayName,
                    number = second.primaryNumber,
                    isGradientCard = false,
                    avatarType = AvatarMonsterType.PINK,
                    lookupKey = second.lookupKey
                )
            }
            contacts.size == 1 -> {
                ContactSuggestion(
                    name = "Quick Call",
                    number = contacts.first().primaryNumber,
                    isGradientCard = false,
                    avatarType = AvatarMonsterType.PINK,
                    lookupKey = contacts.first().lookupKey
                )
            }
            else -> {
                ContactSuggestion(
                    name = "Favorites",
                    number = "Starred contacts list",
                    isGradientCard = false,
                    avatarType = AvatarMonsterType.PINK
                )
            }
        }

        return Pair(card1, card2)
    }

    fun onDigitPress(char: Char) {
        audioHaptics.playTone(char, settings.value.dtmfToneEnabled)
        audioHaptics.performHaptic(settings.value.hapticsEnabled)

        // If in an active call, send DTMF tone
        if (activeCall.value != null && activeCall.value?.state == android.telecom.Call.STATE_ACTIVE) {
            OngoingCallManager.sendDtmf(char)
        }

        _dialerInput.value = _dialerInput.value + char
    }

    fun onZeroLongPress() {
        audioHaptics.performHaptic(settings.value.hapticsEnabled)
        _dialerInput.value = _dialerInput.value + "+"
    }

    fun onBackspace() {
        audioHaptics.performHaptic(settings.value.hapticsEnabled)
        if (_dialerInput.value.isNotEmpty()) {
            _dialerInput.value = _dialerInput.value.dropLast(1)
        }
    }

    fun onClearDigits() {
        audioHaptics.performHaptic(settings.value.hapticsEnabled)
        _dialerInput.value = ""
    }

    fun setNumber(raw: String) {
        _dialerInput.value = raw
    }

    fun selectCountry(country: CountryCode) {
        _selectedCountry.value = country
        viewModelScope.launch {
            settingsRepo.saveSettings(settings.value.copy(countryCode = country.code))
        }
    }

    fun switchTab(tab: ScreenTab) {
        _activeTab.value = tab
    }

    fun setContactSearchQuery(query: String) {
        _contactSearchQuery.value = query
    }

    fun makeCall(context: Context, explicitNumber: String? = null, accountHandle: PhoneAccountHandle? = null) {
        val numberToCall = explicitNumber ?: run {
            val input = _dialerInput.value.trim()
            if (input.isEmpty()) return
            if (input.startsWith("+")) {
                input
            } else {
                "${selectedCountry.value.code}$input"
            }
        }

        audioHaptics.performHaptic(settings.value.hapticsEnabled)
        DialerCaller.placeCall(context, numberToCall, accountHandle)
    }

    fun deleteCallLogItem(id: Long) {
        viewModelScope.launch {
            callLogRepo.deleteCallLog(id)
            _callLogs.value = callLogRepo.getCallLogs()
        }
    }

    fun clearAllCallHistory() {
        viewModelScope.launch {
            callLogRepo.clearAllCallLogs()
            _callLogs.value = emptyList()
        }
    }

    fun blockNumber(number: String, reason: String = "Spam / Unwanted") {
        viewModelScope.launch {
            settingsRepo.blockNumber(number, reason)
        }
    }

    fun unblockNumber(number: String) {
        viewModelScope.launch {
            settingsRepo.unblockNumber(number)
        }
    }

    fun toggleHaptics(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepo.saveSettings(settings.value.copy(hapticsEnabled = enabled))
        }
    }

    fun toggleDtmf(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepo.saveSettings(settings.value.copy(dtmfToneEnabled = enabled))
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            settingsRepo.saveSettings(settings.value.copy(themeMode = mode))
        }
    }

    // In-Call actions
    fun answerCall() {
        audioHaptics.performHaptic(settings.value.hapticsEnabled)
        OngoingCallManager.answer()
    }

    fun declineCall() {
        audioHaptics.performHaptic(settings.value.hapticsEnabled)
        OngoingCallManager.decline()
    }

    fun endCall() {
        audioHaptics.performHaptic(settings.value.hapticsEnabled)
        OngoingCallManager.endCall()
    }

    fun toggleMute() {
        audioHaptics.performHaptic(settings.value.hapticsEnabled)
        OngoingCallManager.toggleMute(AppInCallService.instance)
    }

    fun toggleSpeaker() {
        audioHaptics.performHaptic(settings.value.hapticsEnabled)
        OngoingCallManager.toggleSpeaker(AppInCallService.instance)
    }

    fun toggleHold() {
        audioHaptics.performHaptic(settings.value.hapticsEnabled)
        OngoingCallManager.toggleHold()
    }

    fun sendInCallDtmf(char: Char) {
        audioHaptics.playTone(char, settings.value.dtmfToneEnabled)
        audioHaptics.performHaptic(settings.value.hapticsEnabled)
        OngoingCallManager.sendDtmf(char)
    }

    override fun onCleared() {
        super.onCleared()
        audioHaptics.release()
    }
}
