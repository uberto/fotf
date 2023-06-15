package com.ubertob.fotf.exercises.chapter3

class Cashier {

    private val prices = mutableMapOf<Item, Double>()
    private val customerTotal = mutableMapOf<String, Double>()
    private val offers3x2 = mutableListOf<Item>()


    fun putAll(offers: Map<Item, Double>) {
        prices.putAll(offers)
        customerTotal.clear()
        offers3x2.clear()
    }

    fun totalFor(actorName: String): Double =
        customerTotal[actorName] ?: 0.0

    fun addItem(actorName: String, qty: Int, item: Item) {
        customerTotal[actorName] = totalFor(actorName) + qty * price(item) - discount(qty, item)
    }

    private fun price(item: Item) = prices[item] ?: 0.0

    private fun discount(qty: Int, item: Item): Double =
        if (item in offers3x2)
            (qty / 3) * price(item)
        else
            0.0


    fun setup3x2(item: Item) {
        offers3x2.add(item)
    }


}
