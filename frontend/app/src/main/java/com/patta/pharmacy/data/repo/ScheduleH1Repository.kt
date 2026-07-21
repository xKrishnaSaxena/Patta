package com.patta.pharmacy.data.repo

import com.patta.pharmacy.data.local.dao.ScheduleH1Dao
import com.patta.pharmacy.data.local.entity.ScheduleH1Entry
import com.patta.pharmacy.util.newId
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScheduleH1Repository @Inject constructor(
    private val dao: ScheduleH1Dao,
) {
    fun recent(): Flow<List<ScheduleH1Entry>> = dao.observeRecent()

    /** Records every Schedule-H1 line of a bill against the patient + doctor. */
    suspend fun recordForBill(
        billNo: String,
        lines: List<CartLine>,
        patientName: String,
        doctorName: String,
    ) {
        val h1Lines = lines.filter { it.isScheduleH1 }
        if (h1Lines.isEmpty()) return
        val now = System.currentTimeMillis()
        dao.insertAll(
            h1Lines.map { l ->
                ScheduleH1Entry(
                    id = newId(),
                    billNo = billNo,
                    medicineId = l.medicineId,
                    medicineName = l.name,
                    batchNo = l.batchNo,
                    qty = l.packsSold,
                    patientName = patientName.trim(),
                    doctorName = doctorName.trim(),
                    date = now,
                    createdAt = now,
                )
            }
        )
    }

    suspend fun addManual(medicineName: String, qty: Double, patientName: String, doctorName: String) {
        val now = System.currentTimeMillis()
        dao.insertAll(
            listOf(
                ScheduleH1Entry(
                    id = newId(), medicineName = medicineName.trim(), qty = qty,
                    patientName = patientName.trim(), doctorName = doctorName.trim(),
                    date = now, createdAt = now,
                )
            )
        )
    }
}
