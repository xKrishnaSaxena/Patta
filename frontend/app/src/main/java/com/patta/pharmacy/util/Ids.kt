package com.patta.pharmacy.util

import java.util.UUID

/** UUID string ids — stable across local DB and future cloud sync. */
fun newId(): String = UUID.randomUUID().toString()

/** The single store for now. Multi-store later just swaps this per active store. */
const val DEFAULT_STORE_ID = "store-local"
