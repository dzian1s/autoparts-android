package com.dzian1s.autopartsapp.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.dzian1s.autopartsapp.R
import androidx.compose.ui.unit.dp
import com.dzian1s.autopartsapp.data.CreateOrderItemDto
import com.dzian1s.autopartsapp.data.CreateOrderRequest
import com.dzian1s.autopartsapp.data.ProductDto
import com.dzian1s.autopartsapp.data.Repository
import com.dzian1s.autopartsapp.data.UserPrefs
import kotlinx.coroutines.launch
import kotlin.math.abs

import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType


// ----------------------------
// Helpers
// ----------------------------

private fun formatEur(cents: Int): String {
    val sign = if (cents < 0) "-" else ""
    val a = abs(cents)
    val euros = a / 100
    val rest = a % 100
    return "$sign€$euros.${rest.toString().padStart(2, '0')}"
}

@Composable
private fun CenterBox(content: @Composable ColumnScope.() -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, content = content)
    }
}

@Composable
private fun LoadingView(text: String = "Loading...") {
    CenterBox {
        CircularProgressIndicator()
        Spacer(Modifier.height(12.dp))
        Text(text)
    }
}

@Composable
private fun ErrorView(error: Throwable?, onRetry: (() -> Unit)? = null) {
    val context = LocalContext.current
    val msg = if (error == null) stringResource(R.string.something_went_wrong) else context.toUiError(error)

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(msg)
            if (onRetry != null) {
                Spacer(Modifier.height(12.dp))
                Button(onClick = onRetry) { Text(stringResource(R.string.retry)) }
            }
        }
    }
}


@Composable
private fun EmptyView(text: String) {
    CenterBox { Text(text) }
}

// ----------------------------
// UI
// ----------------------------

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
            Text(stringResource(R.string.part_label, p.partNumber))
            Text(stringResource(R.string.oem_label, p.oemNumber))
            Spacer(Modifier.height(4.dp))
            Text(stringResource(R.string.price_label, formatEur(p.priceCents)))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    cart: CartState,
    onOpenCatalog: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenCart: () -> Unit,
    onOpenOrders: () -> Unit
) {
    val cartCount by remember {
        derivedStateOf { cart.items.sumOf { it.qty } }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.home_title)) }) }
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onOpenCatalog,
                modifier = Modifier.fillMaxWidth()
            ) { Text(stringResource(R.string.home_catalog)) }

            OutlinedButton(
                onClick = onOpenSearch,
                modifier = Modifier.fillMaxWidth()
            ) { Text(stringResource(R.string.home_search)) }

            ElevatedButton(
                onClick = onOpenCart,
                modifier = Modifier.fillMaxWidth()
            ) {
                val label = if (cartCount > 0)
                    stringResource(R.string.home_cart_with_count, cartCount)
                else
                    stringResource(R.string.home_cart)

                Text(label)
            }

            ElevatedButton(
                onClick = onOpenOrders,
                modifier = Modifier.fillMaxWidth()
            ) { Text(stringResource(R.string.home_orders)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.orders_title)) },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text(stringResource(R.string.back)) }
                }
            )
        }
    ) { pad ->
        Column(
            Modifier
                .padding(pad)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(stringResource(R.string.orders_coming))
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
                title = { Text(stringResource(R.string.catalog_title)) },
                actions = {
                    TextButton(onClick = onOpenSearch) { Text(stringResource(R.string.open_search)) }
                }
            )
        }
    ) { pad ->
        Box(Modifier.padding(pad).fillMaxSize()) {
            when {
                s.loading -> LoadingView(stringResource(R.string.loading_catalog))
                s.error != null -> ErrorView(s.error) { vm.load() }
                s.items.isEmpty() -> EmptyView(stringResource(R.string.no_products))
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
    val s = vm.state

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.search_title)) }) }
    ) { pad ->
        Column(Modifier.padding(pad).fillMaxSize().padding(12.dp)) {
            OutlinedTextField(
                value = s.query,
                onValueChange = vm::onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.search_label)) }
            )

            Spacer(Modifier.height(8.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = onBack) { Text(stringResource(R.string.back)) }
                s.mode?.let { Text(stringResource(R.string.mode_label, it), style = MaterialTheme.typography.bodyMedium) }
            }

            Spacer(Modifier.height(8.dp))

            when {
                s.query.trim().isEmpty() -> EmptyView(stringResource(R.string.type_to_search))
                s.loading -> LoadingView(stringResource(R.string.loading_search))
                s.error != null -> ErrorView(s.error) { vm.retry() }
                s.items.isEmpty() -> EmptyView(stringResource(R.string.no_results))
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
                title = { Text(stringResource(R.string.product_title)) },
                actions = { TextButton(onClick = onOpenCart) { Text(stringResource(R.string.cart)) } }
            )
        }
    ) { pad ->
        Box(Modifier.padding(pad).fillMaxSize().padding(12.dp)) {
            when {
                s.loading -> LoadingView(stringResource(R.string.loading_product))
                s.error != null -> Column {
                    ErrorView(s.error) { vm.load(id) }
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = onBack) { Text(stringResource(R.string.back)) }
                }
                s.item != null -> {
                    val p = s.item
                    Column {
                        Text(p.name, style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(8.dp))
                        Text(stringResource(R.string.part_label, p.partNumber))
                        Text(stringResource(R.string.oem_label, p.oemNumber))
                        Spacer(Modifier.height(8.dp))
                        Text(p.description)
                        Spacer(Modifier.height(12.dp))
                        Text(stringResource(R.string.price_label, formatEur(p.priceCents)))
                        Spacer(Modifier.height(12.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(onClick = { cart.add(p) }) { Text(stringResource(R.string.add_to_cart)) }
                            OutlinedButton(onClick = onBack) { Text(stringResource(R.string.back)) }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(cart: CartState, onBack: () -> Unit, onOpenPolicy: () -> Unit) {
    val items = cart.items

    var name by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var comment by rememberSaveable { mutableStateOf("") }
    var agree by rememberSaveable { mutableStateOf(false) }

    var sending by remember { mutableStateOf(false) }
    var sentOrderId by remember { mutableStateOf<String?>(null) }
    var sendError by remember { mutableStateOf<Throwable?>(null) }

    val scope = rememberCoroutineScope()
    val repo = remember { Repository() }
    val context = LocalContext.current

    var triedSend by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Cart") }) }
    ) { pad ->
        Column(Modifier.padding(pad).fillMaxSize().padding(12.dp)) {

            // After successful order we clear the cart,
            // so show confirmation even if items become empty.
            if (items.isEmpty() && sentOrderId == null) {
                EmptyView(stringResource(R.string.cart_empty))
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = onBack) { Text(stringResource(R.string.back)) }
                return@Column
            }

            if (sentOrderId != null) {
                Text(stringResource(R.string.order_sent, sentOrderId!!), style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = onBack) { Text(stringResource(R.string.continue_shopping)) }
                Spacer(Modifier.height(12.dp))
            }

            sendError?.let { err ->
                Text(LocalContext.current.toUiError(err))
                Spacer(Modifier.height(8.dp))
            }

            if (items.isNotEmpty()) {
                LazyColumn(Modifier.weight(1f)) {
                    items(items, key = { it.product.id }) { itx ->
                        val unit = itx.product.priceCents
                        val lineTotal = unit * itx.qty

                        Row(
                            Modifier.fillMaxWidth().padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(itx.product.name, style = MaterialTheme.typography.titleMedium)
                                Text(stringResource(R.string.qty_each, itx.qty, formatEur(unit)))
                                Text(stringResource(R.string.line_total, formatEur(lineTotal)))
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    enabled = !sending,
                                    onClick = { cart.removeOne(itx.product) }
                                ) { Text("-") }

                                Button(
                                    enabled = !sending,
                                    onClick = { cart.add(itx.product) }
                                ) { Text("+") }
                            }
                        }
                        Divider()
                    }
                }

                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(stringResource(R.string.total), style = MaterialTheme.typography.titleMedium)
                    Text(formatEur(cart.totalCents()), style = MaterialTheme.typography.titleMedium)
                }
                Spacer(Modifier.height(12.dp))
            }

            // Form
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.name_required)) },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            if (triedSend && name.isBlank()) {
                Text(
                    text = stringResource(R.string.err_name_required),
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.phone_required)) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Done
                )
            )

            if (triedSend && phone.isBlank()) {
                Text(
                    text = stringResource(R.string.err_phone_required),
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = comment,
                onValueChange = { comment = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.comment)) }
            )

            Spacer(Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = agree, onCheckedChange = { agree = it })
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.agree_pd))
                Spacer(Modifier.width(8.dp))
                TextButton(onClick = onOpenPolicy) { Text(stringResource(R.string.policy)) }
            }

            if (triedSend && !agree) {
                Text(
                    text = stringResource(R.string.err_consent_required),
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(Modifier.height(12.dp))

            val canSend = items.isNotEmpty() &&
                    name.isNotBlank() &&
                    phone.isNotBlank() &&
                    agree &&
                    !sending

            Button(
                enabled = canSend,
                onClick = {
                    sending = true
                    sendError = null
                    sentOrderId = null
                    triedSend = true

                    val clientId = UserPrefs.getOrCreateClientId(context)
                    val req = CreateOrderRequest(
                        clientId = clientId,
                        customerName = name.trim(),
                        customerPhone = phone.trim(),
                        customerComment = comment.trim().ifBlank { null },
                        items = items.map { CreateOrderItemDto(it.product.id, it.qty) }
                    )

                    scope.launch {
                        try {
                            val resp = repo.createOrder(req)
                            sentOrderId = resp.orderId
                            cart.clear()
                        } catch (e: Exception) {
                            sendError = e
                        } finally {
                            sending = false
                        }
                    }
                }
            ) {
                Text(if (sending) stringResource(R.string.sending) else stringResource(R.string.send_order))
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(enabled = !sending, onClick = { cart.clear() }) { Text(stringResource(R.string.clear)) }
                TextButton(onClick = onBack) { Text(stringResource(R.string.back)) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.privacy_policy_title)) },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text(stringResource(R.string.back)) }
                }
            )
        }
    ) { pad ->
        Column(Modifier.padding(pad).padding(12.dp)) {
            Text(stringResource(R.string.what_we_collect), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.collect_items))

            Spacer(Modifier.height(12.dp))
            Text(stringResource(R.string.why_title), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.why_text))

            Spacer(Modifier.height(12.dp))
            Text(stringResource(R.string.storage_title), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.storage_text))

            Spacer(Modifier.height(12.dp))
            Text(stringResource(R.string.control_title), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.control_text))
        }
    }
}