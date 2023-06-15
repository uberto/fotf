package com.ubertob.fotf.exercises.chapter3

import com.ubertob.pesticide.core.DdtActor
import strikt.api.expectThat
import strikt.assertions.isEqualTo

data class CustomerActor(override val name: String) : DdtActor<CashierActions>() {
    fun `can add #qty #item`(qty: Int, item: Item) = step(qty, item.name) {
        addItem(name, qty, item)
    }

    fun `check total is #total`(total: Double) = step(total) {
        expectThat(totalFor(name)).isEqualTo(total)
    }
}