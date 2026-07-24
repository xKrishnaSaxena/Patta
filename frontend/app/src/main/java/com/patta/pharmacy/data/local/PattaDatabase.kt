package com.patta.pharmacy.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.patta.pharmacy.data.local.dao.BatchDao
import com.patta.pharmacy.data.local.dao.BillDao
import com.patta.pharmacy.data.local.dao.BillItemDao
import com.patta.pharmacy.data.local.dao.CustomerDao
import com.patta.pharmacy.data.local.dao.CustomerLedgerDao
import com.patta.pharmacy.data.local.dao.MedicineDao
import com.patta.pharmacy.data.local.dao.MissedSaleDao
import com.patta.pharmacy.data.local.dao.PurchaseDao
import com.patta.pharmacy.data.local.dao.PurchaseItemDao
import com.patta.pharmacy.data.local.dao.PurchaseOrderDao
import com.patta.pharmacy.data.local.dao.PurchaseOrderItemDao
import com.patta.pharmacy.data.local.dao.StockMovementDao
import com.patta.pharmacy.data.local.dao.SupplierDao
import com.patta.pharmacy.data.local.dao.SupplierLedgerDao
import com.patta.pharmacy.data.local.entity.BatchEntity
import com.patta.pharmacy.data.local.entity.BillEntity
import com.patta.pharmacy.data.local.entity.BillItemEntity
import com.patta.pharmacy.data.local.entity.CustomerEntity
import com.patta.pharmacy.data.local.entity.CustomerLedgerEntry
import com.patta.pharmacy.data.local.entity.MedicineEntity
import com.patta.pharmacy.data.local.entity.MissedSaleEntity
import com.patta.pharmacy.data.local.entity.PurchaseEntity
import com.patta.pharmacy.data.local.entity.PurchaseItemEntity
import com.patta.pharmacy.data.local.entity.PurchaseOrderEntity
import com.patta.pharmacy.data.local.entity.PurchaseOrderItemEntity
import com.patta.pharmacy.data.local.entity.StockMovementEntity
import com.patta.pharmacy.data.local.entity.StoreEntity
import com.patta.pharmacy.data.local.entity.SupplierEntity
import com.patta.pharmacy.data.local.entity.SupplierLedgerEntry

@Database(
    entities = [
        StoreEntity::class,
        MedicineEntity::class,
        BatchEntity::class,
        StockMovementEntity::class,
        CustomerEntity::class,
        SupplierEntity::class,
        BillEntity::class,
        BillItemEntity::class,
        CustomerLedgerEntry::class,
        SupplierLedgerEntry::class,
        PurchaseEntity::class,
        PurchaseItemEntity::class,
        MissedSaleEntity::class,
        PurchaseOrderEntity::class,
        PurchaseOrderItemEntity::class,
        com.patta.pharmacy.data.local.entity.ScheduleH1Entry::class,
    ],
    version = 6,
    exportSchema = true,
)
abstract class PattaDatabase : RoomDatabase() {
    abstract fun medicineDao(): MedicineDao
    abstract fun batchDao(): BatchDao
    abstract fun stockMovementDao(): StockMovementDao
    abstract fun billDao(): BillDao
    abstract fun billItemDao(): BillItemDao
    abstract fun supplierDao(): SupplierDao
    abstract fun supplierLedgerDao(): SupplierLedgerDao
    abstract fun purchaseDao(): PurchaseDao
    abstract fun purchaseItemDao(): PurchaseItemDao
    abstract fun customerDao(): CustomerDao
    abstract fun customerLedgerDao(): CustomerLedgerDao
    abstract fun storeDao(): com.patta.pharmacy.data.local.dao.StoreDao
    abstract fun scheduleH1Dao(): com.patta.pharmacy.data.local.dao.ScheduleH1Dao
    abstract fun missedSaleDao(): MissedSaleDao
    abstract fun purchaseOrderDao(): PurchaseOrderDao
    abstract fun purchaseOrderItemDao(): PurchaseOrderItemDao
}
