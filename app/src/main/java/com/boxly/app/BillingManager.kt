package com.boxly.app

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.acknowledgePurchase
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/** מזהה המוצר ב-Play Console: מוצר חד-פעמי (In-app product) בשם boxly_pro, מחיר ₪20. */
const val PRO_PRODUCT_ID = "boxly_pro"

class BillingManager(
    context: Context,
    private val scope: CoroutineScope,
    private val onProChanged: (Boolean) -> Unit,
) {
    var priceText by mutableStateOf<String?>(null)
        private set
    var message by mutableStateOf<String?>(null)

    private var productDetails: ProductDetails? = null

    private val client: BillingClient = BillingClient.newBuilder(context.applicationContext)
        .setListener { result, purchases ->
            when (result.responseCode) {
                BillingClient.BillingResponseCode.OK -> purchases?.forEach { scope.launch { handle(it) } }
                BillingClient.BillingResponseCode.USER_CANCELED -> Unit
                else -> message = "הרכישה נכשלה (${result.debugMessage})"
            }
        }
        .enablePendingPurchases()
        .build()

    fun connect() {
        if (client.isReady) return
        client.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    scope.launch { loadProduct(); restore() }
                }
            }
            override fun onBillingServiceDisconnected() { /* ינסה שוב בחיבור הבא */ }
        })
    }

    private suspend fun loadProduct() {
        val params = QueryProductDetailsParams.newBuilder().setProductList(
            listOf(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(PRO_PRODUCT_ID)
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build()
            )
        ).build()
        val details = client.queryProductDetails(params).productDetailsList?.firstOrNull()
        productDetails = details
        priceText = details?.oneTimePurchaseOfferDetails?.formattedPrice
    }

    /** שחזור רכישה קיימת (למשל אחרי התקנה מחדש). */
    suspend fun restore() {
        val result = client.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build()
        )
        val owned = result.purchasesList.any { it.isPro() && it.purchaseState == Purchase.PurchaseState.PURCHASED }
        result.purchasesList.filter { it.isPro() }.forEach { handle(it) }
        if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) onProChanged(owned)
    }

    fun launchPurchase(activity: Activity) {
        val details = productDetails
        if (details == null) {
            message = "המוצר לא זמין. בדוק שהוגדר ב-Play Console והאפליקציה הותקנה מ-Google Play."
            connect()
            return
        }
        val params = BillingFlowParams.newBuilder().setProductDetailsParamsList(
            listOf(BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(details).build())
        ).build()
        client.launchBillingFlow(activity, params)
    }

    private suspend fun handle(purchase: Purchase) {
        if (!purchase.isPro() || purchase.purchaseState != Purchase.PurchaseState.PURCHASED) return
        // TODO לפני פרסום רחב: לאמת את purchase.purchaseToken בשרת מול Google Play Developer API.
        if (!purchase.isAcknowledged) {
            client.acknowledgePurchase(
                AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
            )
        }
        onProChanged(true)
    }

    private fun Purchase.isPro() = products.contains(PRO_PRODUCT_ID)

    fun disconnect() { client.endConnection() }
}
