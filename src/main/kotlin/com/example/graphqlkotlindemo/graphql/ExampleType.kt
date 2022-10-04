package com.example.graphqlkotlindemo.graphql

import kotlin.random.Random

data class ExampleType(
    val number: Int = Random.nextInt()
) {
    fun addition(a: Int, b: Int) = a + b
}
