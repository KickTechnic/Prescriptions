package com.prescription.tracker.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetailsResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.acknowledgePurchase
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import com.prescription.tracker.data.SettingsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BillingManager(
    context: Context,
    private val settingsManager: SettingsManager
) : PurchasesUpdatedListener {

    companion object {
        const val PRODUCT_REMOVE_ADS = "remove_ads"
    }

    private val scope = CoroutineScope(Dispatchers.IO)

    private val billingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build()
        )
        .build()

    fun startConnection() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    scope.launch { restorePurchases() }
                }
            }

            override fun onBillingServiceDisconnected() {
                // Will retry on next launchPurchaseFlow if needed
            }
        })
    }

    private suspend fun restorePurchases() {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()
        val result = billingClient.queryPurchasesAsync(params)
        val purchased = result.purchasesList.any { purchase ->
            purchase.products.contains(PRODUCT_REMOVE_ADS) &&
                purchase.purchaseState == Purchase.PurchaseState.PURCHASED
        }
        if (purchased) {
            settingsManager.setAdsRemoved(true)
        }
    }

    fun launchPurchaseFlow(activity: Activity) {
        scope.launch {
            val productList = listOf(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(PRODUCT_REMOVE_ADS)
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build()
            )
            val params = QueryProductDetailsParams.newBuilder()
                .setProductList(productList)
                .build()

            val result: ProductDetailsResult = billingClient.queryProductDetails(params)
            val productDetails = result.productDetailsList?.firstOrNull() ?: return@launch

            val flowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(
                    listOf(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(productDetails)
                            .build()
                    )
                )
                .build()

            activity.runOnUiThread {
                billingClient.launchBillingFlow(activity, flowParams)
            }
        }
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: List<Purchase>?) {
        if (result.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                if (purchase.products.contains(PRODUCT_REMOVE_ADS) &&
                    purchase.purchaseState == Purchase.PurchaseState.PURCHASED
                ) {
                    settingsManager.setAdsRemoved(true)
                    scope.launch { acknowledgePurchaseIfNeeded(purchase) }
                }
            }
        }
    }

    private suspend fun acknowledgePurchaseIfNeeded(purchase: Purchase) {
        if (!purchase.isAcknowledged) {
            val params = com.android.billingclient.api.AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
            billingClient.acknowledgePurchase(params)
        }
    }

    fun destroy() {
        billingClient.endConnection()
    }
}
