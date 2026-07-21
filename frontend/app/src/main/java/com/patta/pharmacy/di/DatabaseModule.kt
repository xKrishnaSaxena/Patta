package com.patta.pharmacy.di

import android.content.Context
import androidx.room.Room
import com.patta.pharmacy.data.local.PattaDatabase
import com.patta.pharmacy.data.local.dao.BatchDao
import com.patta.pharmacy.data.local.dao.BillDao
import com.patta.pharmacy.data.local.dao.BillItemDao
import com.patta.pharmacy.data.local.dao.CustomerDao
import com.patta.pharmacy.data.local.dao.CustomerLedgerDao
import com.patta.pharmacy.data.local.dao.MedicineDao
import com.patta.pharmacy.data.local.dao.PurchaseDao
import com.patta.pharmacy.data.local.dao.PurchaseItemDao
import com.patta.pharmacy.data.local.dao.StockMovementDao
import com.patta.pharmacy.data.local.dao.SupplierDao
import com.patta.pharmacy.data.local.dao.SupplierLedgerDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PattaDatabase =
        Room.databaseBuilder(context, PattaDatabase::class.java, "patta.db")
            .fallbackToDestructiveMigration()   // fine during Phase 1 dev; real migrations later
            .build()

    @Provides fun provideMedicineDao(db: PattaDatabase): MedicineDao = db.medicineDao()
    @Provides fun provideBatchDao(db: PattaDatabase): BatchDao = db.batchDao()
    @Provides fun provideStockMovementDao(db: PattaDatabase): StockMovementDao = db.stockMovementDao()
    @Provides fun provideBillDao(db: PattaDatabase): BillDao = db.billDao()
    @Provides fun provideBillItemDao(db: PattaDatabase): BillItemDao = db.billItemDao()
    @Provides fun provideSupplierDao(db: PattaDatabase): SupplierDao = db.supplierDao()
    @Provides fun provideSupplierLedgerDao(db: PattaDatabase): SupplierLedgerDao = db.supplierLedgerDao()
    @Provides fun providePurchaseDao(db: PattaDatabase): PurchaseDao = db.purchaseDao()
    @Provides fun providePurchaseItemDao(db: PattaDatabase): PurchaseItemDao = db.purchaseItemDao()
    @Provides fun provideCustomerDao(db: PattaDatabase): CustomerDao = db.customerDao()
    @Provides fun provideCustomerLedgerDao(db: PattaDatabase): CustomerLedgerDao = db.customerLedgerDao()
    @Provides fun provideStoreDao(db: PattaDatabase): com.patta.pharmacy.data.local.dao.StoreDao = db.storeDao()
    @Provides fun provideScheduleH1Dao(db: PattaDatabase): com.patta.pharmacy.data.local.dao.ScheduleH1Dao = db.scheduleH1Dao()
    @Provides fun provideMissedSaleDao(db: PattaDatabase): com.patta.pharmacy.data.local.dao.MissedSaleDao = db.missedSaleDao()
    @Provides fun providePurchaseOrderDao(db: PattaDatabase): com.patta.pharmacy.data.local.dao.PurchaseOrderDao = db.purchaseOrderDao()
    @Provides fun providePurchaseOrderItemDao(db: PattaDatabase): com.patta.pharmacy.data.local.dao.PurchaseOrderItemDao = db.purchaseOrderItemDao()
}
