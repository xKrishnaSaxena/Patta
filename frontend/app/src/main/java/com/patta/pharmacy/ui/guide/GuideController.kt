package com.patta.pharmacy.ui.guide

import androidx.lifecycle.ViewModel
import com.patta.pharmacy.ui.navigation.Routes
import com.patta.pharmacy.ui.navigation.TopDest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/** The three hands-on steps a first-time user is walked through. */
enum class GuideStep(
    val index: Int,
    val route: String,
    val title: String,
    val instruction: String,
) {
    SHOP_PROFILE(1, "shop_profile", "Step 1 — Apni dukaan",
        "Dukaan ka naam aur license bharo, phir neeche 'Save' dabao. Yeh bill pe chhpega."),
    ADD_MEDICINE(2, Routes.ADD_MEDICINE, "Step 2 — Pehli dawai",
        "Ek dawai ka naam aur MRP bharo. 'Opening Stock' mein qty + expiry bhi daalo, phir 'Save Medicine'."),
    MAKE_BILL(3, TopDest.Billing.route, "Step 3 — Pehla bill",
        "Upar search mein wahi dawai likho ya bolo, tap karke cart mein daalo, phir 'CASH' dabao. Bas ho gaya!");

    fun next(): GuideStep? = entries.firstOrNull { it.index == index + 1 }

    companion object {
        const val TOTAL = 3
    }
}

/**
 * Shared, app-wide state for the interactive first-run guide. Feature ViewModels
 * call [complete] after the relevant save; the nav graph observes [step] to drive
 * navigation and show the guide banner.
 */
@Singleton
class GuideController @Inject constructor() {
    private val _step = MutableStateFlow<GuideStep?>(null)
    val step: StateFlow<GuideStep?> = _step.asStateFlow()

    private val _justFinished = MutableStateFlow(false)
    val justFinished: StateFlow<Boolean> = _justFinished.asStateFlow()

    val isActive: Boolean get() = _step.value != null

    fun start() { if (_step.value == null) _step.value = GuideStep.SHOP_PROFILE }

    /** Advance only if the completed step is the one currently in progress. */
    fun complete(done: GuideStep) {
        if (_step.value != done) return
        val nxt = done.next()
        _step.value = nxt
        if (nxt == null) _justFinished.value = true
    }

    fun skip() { _step.value = null }

    fun clearFinished() { _justFinished.value = false }
}

/** Thin wrapper so composables can reach the singleton via hiltViewModel(). */
@HiltViewModel
class GuideViewModel @Inject constructor(val controller: GuideController) : ViewModel()
