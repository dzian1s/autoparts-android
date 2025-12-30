package com.dzian1s.autopartsapp.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dzian1s.autopartsapp.data.CreateOrderItemDto
import com.dzian1s.autopartsapp.data.CreateOrderRequest
import com.dzian1s.autopartsapp.data.ProductDto
import com.dzian1s.autopartsapp.data.Repository
import kotlinx.coroutines.launch

@Composable
fun ProductRow(p: ProductDto, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() }
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(p.name, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text("Part: ${p.partNumber}   OEM: ${p.oemNumber}")
            Spacer(Modifier.height(4.dp))
            Text("Price: ${p.priceCents} cents")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(vm: CatalogViewModel, onOpenSearch: () -> Unit, onOpenDetails: (String) -> Unit) {
    LaunchedEffect(Unit) { vm.load() }

    val s = vm.state

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Catalog") },
                actions = {
                    TextButton(onClick = onOpenSearch) { Text("Search") }
                }
            )
        }
    ) { pad ->
        Box(Modifier.padding(pad).fillMaxSize()) {
            when {
                s.loading -> CircularProgressIndicator(Modifier.padding(24.dp))
                s.error != null -> Text("Error: ${s.error}", Modifier.padding(24.dp))
                else -> LazyColumn {
                    items(s.items) { p ->
                        ProductRow(p) { onOpenDetails(p.id) }
                        Divider()
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(vm: SearchViewModel, onBack: () -> Unit, onOpenDetails: (String) -> Unit) {

    LaunchedEffect(Unit){
        android.util.Log.d("SeachScreen", "ENTER SerachScreen")
    }
    SideEffect { android.util.Log.d("SeachScreen", "RECOMPOSE") }

    var text by rememberSaveable { mutableStateOf("") } //local state of the field
    val s = vm.state

    Scaffold(
        topBar = { TopAppBar(title = { Text("Search") }) }
    ) { pad ->
        Column(Modifier.padding(pad).fillMaxSize().padding(12.dp)) {
            OutlinedTextField(
                value = s.query,
                onValueChange = vm::onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Search (part/oem/name, typos ok)") }
            )
            Text("typed: $text")

            Spacer(Modifier.height(8.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = onBack) { Text("Back") }
                if (s.mode != null) Text("mode: ${s.mode}", style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(Modifier.height(8.dp))

            when {
                s.loading -> CircularProgressIndicator()
                s.error != null -> Text("Error: ${s.error}")
                else -> LazyColumn {
                    items(s.items) { p ->
                        ProductRow(p) { onOpenDetails(p.id) }
                        Divider()
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    id: String,
    vm: DetailsViewModel,
    cart: CartState,
    onBack: () -> Unit,
    onOpenCart: () -> Unit
) {
    LaunchedEffect(id) { vm.load(id) }
    val s = vm.state

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Product") },
                actions = { TextButton(onClick = onOpenCart) { Text("Cart") } }
            )
        }
    ) { pad ->
        Box(Modifier.padding(pad).fillMaxSize().padding(12.dp)) {
            when {
                s.loading -> CircularProgressIndicator()
                s.error != null -> Column {
                    Text("Error: ${s.error}")
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = onBack) { Text("Back") }
                }
                s.item != null -> {
                    val p = s.item
                    Column {
                        Text(p.name, style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(8.dp))
                        Text("Part: ${p.partNumber}")
                        Text("OEM: ${p.oemNumber}")
                        Spacer(Modifier.height(8.dp))
                        Text(p.description)
                        Spacer(Modifier.height(12.dp))
                        Text("Price: ${p.priceCents} cents")
                        Spacer(Modifier.height(12.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(onClick = { cart.add(p) }) { Text("Add to cart") }
                            OutlinedButton(onClick = onBack) { Text("Back") }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(cart: CartState, onBack: () -> Unit) {
    val items = cart.items // <-- важно: читает state каждый раз

    var name by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var comment by rememberSaveable { mutableStateOf("") }

    var sending by remember { mutableStateOf(false) }
    var sentOrderId by remember { mutableStateOf<String?>(null) }
    var sendError by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val repo = remember { Repository() }


    Scaffold(
        topBar = { TopAppBar(title = { Text("Cart") }) }
    ) { pad ->
        Column(Modifier.padding(pad).fillMaxSize().padding(12.dp)) {
            if (items.isEmpty()) {
                Text("Cart is empty")
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = onBack) { Text("Back") }
                return@Column
            }

            LazyColumn(Modifier.weight(1f)) {
                items(items, key = { it.product.id }) { itx ->
                    Row(
                        Modifier.fillMaxWidth().padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(itx.product.name, style = MaterialTheme.typography.titleMedium)
                            Text("Qty: ${itx.qty}  Price: ${itx.product.priceCents}")
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { cart.removeOne(itx.product) }) { Text("-") }
                            Button(onClick = { cart.add(itx.product) }) { Text("+") }
                        }
                    }
                    Divider()
                }
            }

            Spacer(Modifier.height(8.dp))
            Text("Total: ${cart.totalCents()} cents", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Name") }
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Phone") }
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = comment,
                onValueChange = { comment = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Comment") }
            )

            Spacer(Modifier.height(12.dp))

            if (sentOrderId != null) {
                Text("Order sent: $sentOrderId", style = MaterialTheme.typography.titleMedium)
            }

            if (sendError != null) {
                Text("Error: $sendError")
            }

            Button(
                enabled = !sending && items.isNotEmpty(),
                onClick = {
                    sending = true
                    sendError = null
                    sentOrderId = null

                    val req = CreateOrderRequest(
                        customerName = name.ifBlank { null },
                        customerPhone = phone.ifBlank { null },
                        customerComment = comment.ifBlank { null },
                        items = items.map { CreateOrderItemDto(it.product.id, it.qty) }
                    )

                    scope.launch {
                        runCatching { repo.createOrder(req) }
                            .onSuccess { resp ->
                                sentOrderId = resp.orderId
                                cart.clear()
                            }
                            .onFailure { e ->
                                sendError = e.message
                            }
                        sending = false
                    }
                }
            ) {
                Text(if (sending) "Sending..." else "Send order")
            }


            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = { cart.clear() }) { Text("Clear") }
                TextButton(onClick = onBack) { Text("Back") }
            }
        }
    }
}


