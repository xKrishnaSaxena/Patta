package com.patta.pharmacy.ui.onboarding

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.patta.pharmacy.ui.components.PattaPrimaryButton
import kotlinx.coroutines.launch

private data class Slide(val emoji: String, val title: String, val body: String)

private val slides = listOf(
    Slide("🙏", "Patta mein aapka swaagat", "Aapki dukaan ka poora hisaab — stock, billing, udhaar, supplier — sab ek jagah, bina internet ke bhi. Chaliye ek-ek karke dekhte hain."),
    Slide("🏠", "Home", "Roz kholte hi 'Aaj ki Sale' (cash/UPI/udhaar), expire hone wali aur kam stock dawaiyan, aur neeche 🎤 mic — jisse bolke poocho 'aaj ka collection?'"),
    Slide("🧾", "Billing (bolke bhi!)", "Dawai search karo ya bolo 'Dolo do strip'. Purani expiry wali batch apne aap chunni hai. Cash / UPI / Udhaar — aur bill WhatsApp/PDF pe bhejo. Barcode bhi scan hota hai."),
    Slide("💊", "Dosage bill pe", "Har dawai ke saath 'kaise leni hai' likho (🌅🌙 subah-raat, khaana ke baad). Ye bill pe chhpega — customer ko doctor jaisi salah."),
    Slide("📦", "Stock", "Saari dawaiyan, batch aur expiry ke saath. Kam stock laal, near-expiry amber. Kisi pe tap karke edit karo. '+' se nayi dawai (dosage, barcode, rack bhi)."),
    Slide("🤝", "Supplier & Purchase", "Supplier ka maal 'Purchase Entry' se ghusao — landed cost aur asli margin apne aap. Supplier ledger mein kisko kitna dena hai, payment sab track."),
    Slide("📒", "Khata (Udhaar)", "Kis customer ka kitna udhaar baaki. Udhaar bill apne aap khaate se judta hai. Payment mila ek tap. WhatsApp pe reminder bhejo."),
    Slide("⏰", "Expiry & Return", "30/60/90 din mein expire hone wali dawaiyan supplier-wise. Ek tap 'Return' — stock hatega aur supplier ke khaate se credit note apne aap."),
    Slide("📊", "Reports & Day Close", "Din ke end mein cash milaao (system vs asli), profit (asli landed cost pe), aur GST summary — CA ko WhatsApp."),
    Slide("🎤", "Voice + Missed Sale + PO", "Bolke billing/query/purchase. Jo dawai na ho use 'Missed Sale' mein note karo — phir Purchase Order apne aap ban ke supplier ko WhatsApp."),
    Slide("✅", "Bas ho gaya!", "Sabse pehle 'More → Shop Profile' bhar dena (naam, license) — wo bill pe chhpega. Ye tour More menu se dobara dekh sakte ho. Chaliye shuru karein!"),
)

@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { slides.size })
    val scope = rememberCoroutineScope()
    val isLast = pagerState.currentPage == slides.lastIndex

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.End) {
            if (!isLast) TextButton(onClick = onFinish) { Text("Skip") }
        }

        HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
            val slide = slides[page]
            Column(
                Modifier.fillMaxSize().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(slide.emoji, style = MaterialTheme.typography.displayMedium)
                Spacer(Modifier.height(24.dp))
                Text(slide.title, style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Spacer(Modifier.height(16.dp))
                Text(slide.body, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            }
        }

        Row(
            Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            slides.indices.forEach { i ->
                val active = pagerState.currentPage == i
                val width by animateFloatAsState(if (active) 22f else 8f, label = "dot")
                Box(
                    Modifier
                        .padding(horizontal = 3.dp)
                        .height(8.dp)
                        .size(width = width.dp, height = 8.dp)
                        .background(
                            if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            CircleShape,
                        )
                )
            }
        }

        PattaPrimaryButton(
            text = if (isLast) "Shuru karo" else "Aage",
            onClick = {
                if (isLast) onFinish()
                else scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
            },
            modifier = Modifier.padding(16.dp),
        )
    }
}
