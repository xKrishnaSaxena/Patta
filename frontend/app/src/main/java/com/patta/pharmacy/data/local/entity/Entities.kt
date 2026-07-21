package com.patta.pharmacy.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.patta.pharmacy.util.DEFAULT_STORE_ID

/*
 * Phase-1 entities. Every row carries sync-ready bookkeeping:
 *   storeId    — multi-store ready (one store for now)
 *   createdAt / updatedAt — epoch millis
 *   isDeleted  — soft delete (so cloud sync can propagate deletions later)
 *   syncedAt   — null until pushed to Supabase (Phase 6)
 *
 * Money is always PAISE (Long). Expiry is stored as Int yyyyMM (e.g. 202708) so
 * it sorts naturally for FEFO. Quantities are Double to allow loose sale (0.4 strip).
 */

@Entity(tableName = "stores")
data class StoreEntity(
    @PrimaryKey val id: String = DEFAULT_STORE_ID,
    val name: String,
    val drugLicenseNo: String = "",
    val gstin: String = "",
    val address: String = "",
    val phone: String = "",
    val logoUri: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
    val isDeleted: Boolean = false,
    val syncedAt: Long? = null,
)

@Entity(
    tableName = "medicines",
    indices = [Index("name"), Index("salt"), Index("barcode"), Index("storeId")]
)
data class MedicineEntity(
    @PrimaryKey val id: String,
    val storeId: String = DEFAULT_STORE_ID,
    val name: String,
    val salt: String = "",
    val company: String = "",
    val packType: String = "Strip",          // Strip / Bottle / Box / Tube
    val unitsPerPack: Int = 1,                // e.g. 15 tablets per strip
    val allowLooseSale: Boolean = false,
    val hsnCode: String = "",
    val gstPercent: Int = 12,
    val defaultMrpPaise: Long = 0,
    val purchaseRatePaise: Long = 0,
    val rackLocation: String = "",
    val reorderLevel: Int = 0,                // in packs
    val isScheduleH1: Boolean = false,
    val isFridgeItem: Boolean = false,
    val barcode: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
    val isDeleted: Boolean = false,
    val syncedAt: Long? = null,
)

@Entity(
    tableName = "batches",
    indices = [Index("medicineId"), Index("supplierId"), Index("storeId")]
)
data class BatchEntity(
    @PrimaryKey val id: String,
    val storeId: String = DEFAULT_STORE_ID,
    val medicineId: String,
    val batchNo: String,
    val expiryYm: Int,                        // yyyyMM, e.g. 202708
    val qtyPacks: Double,                     // stock in packs (strips); Double for loose
    val mrpPaise: Long = 0,
    val purchaseRatePaise: Long = 0,
    val landedCostPaise: Long = 0,
    val supplierId: String? = null,
    val receivedDate: Long,
    val createdAt: Long,
    val updatedAt: Long,
    val isDeleted: Boolean = false,
    val syncedAt: Long? = null,
)

@Entity(
    tableName = "stock_movements",
    indices = [Index("medicineId"), Index("batchId"), Index("dateTime")]
)
data class StockMovementEntity(
    @PrimaryKey val id: String,
    val storeId: String = DEFAULT_STORE_ID,
    val medicineId: String,
    val batchId: String?,
    val changeQty: Double,                    // + purchase/return-in, − sale/return-out
    val reason: String,                       // sale / purchase / return / adjust / opening
    val refId: String? = null,                // bill/purchase id
    val dateTime: Long,
    val syncedAt: Long? = null,
)

@Entity(tableName = "customers", indices = [Index("name"), Index("phone"), Index("storeId")])
data class CustomerEntity(
    @PrimaryKey val id: String,
    val storeId: String = DEFAULT_STORE_ID,
    val name: String,
    val phone: String = "",
    val address: String = "",
    val openingBalancePaise: Long = 0,        // + means customer owes shop
    val notes: String = "",
    val createdAt: Long,
    val updatedAt: Long,
    val isDeleted: Boolean = false,
    val syncedAt: Long? = null,
)

@Entity(tableName = "suppliers", indices = [Index("name"), Index("storeId")])
data class SupplierEntity(
    @PrimaryKey val id: String,
    val storeId: String = DEFAULT_STORE_ID,
    val name: String,
    val phone: String = "",
    val address: String = "",
    val gstin: String = "",
    val creditPeriodDays: Int = 0,
    val openingBalancePaise: Long = 0,        // + means shop owes supplier
    val createdAt: Long,
    val updatedAt: Long,
    val isDeleted: Boolean = false,
    val syncedAt: Long? = null,
)

@Entity(tableName = "bills", indices = [Index("customerId"), Index("dateTime"), Index("storeId")])
data class BillEntity(
    @PrimaryKey val id: String,
    val storeId: String = DEFAULT_STORE_ID,
    val billNo: String,
    val dateTime: Long,
    val customerId: String? = null,
    val subtotalPaise: Long,
    val gstPaise: Long,
    val discountPaise: Long,
    val totalPaise: Long,
    val paymentMode: String,                  // cash / upi / udhaar / split
    val amountPaidPaise: Long,
    val createdAt: Long,
    val updatedAt: Long,
    val isDeleted: Boolean = false,
    val syncedAt: Long? = null,
)

@Entity(tableName = "bill_items", indices = [Index("billId"), Index("medicineId"), Index("batchId")])
data class BillItemEntity(
    @PrimaryKey val id: String,
    val billId: String,
    val medicineId: String,
    val batchId: String?,
    val qty: Double,                          // in packs (fractional allowed for loose sale)
    val unit: String,                         // Strip / Tab (display label)
    val ratePaise: Long,
    val gstPercent: Int,
    val lineTotalPaise: Long,
    val returnedQty: Double = 0.0,            // packs already returned
)

@Entity(tableName = "purchases", indices = [Index("supplierId"), Index("invoiceDate"), Index("storeId")])
data class PurchaseEntity(
    @PrimaryKey val id: String,
    val storeId: String = DEFAULT_STORE_ID,
    val supplierId: String,
    val invoiceNo: String,
    val invoiceDate: Long,
    val dueDate: Long,
    val subtotalPaise: Long,
    val gstPaise: Long,
    val discountPaise: Long,
    val totalPaise: Long,
    val status: String = "received",          // received / partial
    val createdAt: Long,
    val updatedAt: Long,
    val isDeleted: Boolean = false,
    val syncedAt: Long? = null,
)

@Entity(tableName = "purchase_items", indices = [Index("purchaseId"), Index("medicineId")])
data class PurchaseItemEntity(
    @PrimaryKey val id: String,
    val purchaseId: String,
    val medicineId: String,
    val batchId: String?,
    val batchNo: String,
    val expiryYm: Int,
    val qtyPacks: Double,
    val freeQtyPacks: Double,
    val ratePaise: Long,                       // purchase rate per pack, ex-GST
    val schemeDiscPercent: Double,
    val gstPercent: Int,
    val landedCostPaise: Long,                 // real per-pack cost after scheme + free + GST
    val marginPercent: Double,
    val lineTotalPaise: Long,
)

@Entity(tableName = "missed_sales", indices = [Index("medicineName"), Index("lastAskedAt"), Index("storeId")])
data class MissedSaleEntity(
    @PrimaryKey val id: String,
    val storeId: String = DEFAULT_STORE_ID,
    val medicineId: String? = null,           // set when it matched a known medicine
    val medicineName: String,
    val salt: String = "",
    val timesAsked: Int = 1,
    val lastAskedAt: Long,
    val resolved: Boolean = false,            // true once ordered
    val createdAt: Long,
    val updatedAt: Long,
    val syncedAt: Long? = null,
)

@Entity(tableName = "purchase_orders", indices = [Index("supplierId"), Index("dateTime"), Index("storeId")])
data class PurchaseOrderEntity(
    @PrimaryKey val id: String,
    val storeId: String = DEFAULT_STORE_ID,
    val supplierId: String? = null,
    val supplierName: String = "",
    val dateTime: Long,
    val status: String = "draft",             // draft / sent
    val itemCount: Int,
    val estimatedValuePaise: Long,
    val createdAt: Long,
    val updatedAt: Long,
    val isDeleted: Boolean = false,
    val syncedAt: Long? = null,
)

@Entity(tableName = "purchase_order_items", indices = [Index("poId")])
data class PurchaseOrderItemEntity(
    @PrimaryKey val id: String,
    val poId: String,
    val medicineId: String? = null,
    val medicineName: String,
    val orderQty: Double,
    val reason: String,                       // low stock / missed sale / fast mover
    val estRatePaise: Long,
)

@Entity(
    tableName = "customer_ledger",
    indices = [Index("customerId"), Index("date")]
)
data class CustomerLedgerEntry(
    @PrimaryKey val id: String,
    val storeId: String = DEFAULT_STORE_ID,
    val customerId: String,
    val date: Long,
    val type: String,                         // bill / payment / adjustment
    val refBillId: String? = null,
    val amountPaise: Long,                    // + owes more, − paid
    val paymentMode: String = "",
    val runningBalancePaise: Long,
    val note: String = "",
    val syncedAt: Long? = null,
)

@Entity(
    tableName = "supplier_ledger",
    indices = [Index("supplierId"), Index("date")]
)
data class SupplierLedgerEntry(
    @PrimaryKey val id: String,
    val storeId: String = DEFAULT_STORE_ID,
    val supplierId: String,
    val date: Long,
    val type: String,                         // purchase / payment / creditNote
    val refId: String? = null,
    val amountPaise: Long,                    // + shop owes more, − paid/credited
    val runningBalancePaise: Long,
    val note: String = "",
    val syncedAt: Long? = null,
)
