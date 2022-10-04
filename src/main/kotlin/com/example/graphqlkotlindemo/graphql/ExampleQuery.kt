package com.example.graphqlkotlindemo.graphql

import com.expediagroup.graphql.server.operations.Query
import org.springframework.stereotype.Component

@Component
class ExampleQuery: Query {
    fun example(): List<ExampleType> = (1..20).map { ExampleType() }
}
