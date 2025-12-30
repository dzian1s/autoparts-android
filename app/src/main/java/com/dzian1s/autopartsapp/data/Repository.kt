package com.dzian1s.autopartsapp.data

class Repository(
    private val api: ApiService = Api.service
) {
    suspend fun products(): List<ProductDto> = api.getProducts()
    suspend fun product(id: String): ProductDto = api.getProduct(id)
    suspend fun search(q: String): SearchResponse = api.search(q)
    suspend fun createOrder(req: CreateOrderRequest): CreateOrderResponse =
        Api.service.createOrder(req)
}
