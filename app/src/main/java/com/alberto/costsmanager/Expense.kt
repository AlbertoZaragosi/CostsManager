package com.alberto.costsmanager

data class Expense(
    var name: String,
    var daily: Double,
    var monthly: Double,
    var annual: Double,
    var isHidden: Boolean = false
)
