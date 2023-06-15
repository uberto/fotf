package com.ubertob.fotf.exercises.chapter3

import com.ubertob.pesticide.core.DdtActions
import com.ubertob.pesticide.core.DdtProtocol

interface CashierActions : DdtActions<DdtProtocol> {
    fun setupPrices(prices: Map<Item, Double>)
    fun totalFor(actorName: String): Double
    fun addItem(actorName: String, qty: Int, item: Item)
    fun setup3x2(item: Item)
}