package ru.smalljinn.tiers.features.tier_lists

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.smalljinn.tiers.R
import ru.smalljinn.tiers.data.database.model.TierCategory
import ru.smalljinn.tiers.data.database.model.TierList
import ru.smalljinn.tiers.data.database.model.TierListWithCategories
import ru.smalljinn.tiers.features.components.TextOnColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TiersListScreen(
    modifier: Modifier = Modifier,
    viewModel: TiersListViewModel = hiltViewModel(),
    navigateToEdit: (Long) -> Unit,
    navigateToSettings: () -> Unit,
    shouldShowGrid: Boolean
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value

    val tiersScrollState = rememberLazyListState()
    val tiersGridScrollState = rememberLazyGridState()
    val createNewTierList = { viewModel.obtainEvent(TiersEvent.CreateNew) }
    val showAddButton by remember {
        derivedStateOf {
            tiersScrollState.canScrollForward || shouldShowGrid
        }
    }
    val scrollToTop = {
        scope.launch {
            if (shouldShowGrid) tiersGridScrollState.animateScrollToItem(0)
            else tiersScrollState.animateScrollToItem(0)
        }
    }
    val scrollBehavior = if (!shouldShowGrid) TopAppBarDefaults.enterAlwaysScrollBehavior()
    else TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    //TODO safe channel handling
    LaunchedEffect(key1 = Unit) {
        viewModel.eventsFlow.collectLatest { event ->
            when (event) {
                is ActionEvent.StartIntent -> {
                    context.startActivity(event.intent)
                }
            }
        }
    }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                modifier = Modifier.clickable { scrollToTop() },
                title = { Text(text = stringResource(id = R.string.app_name)) },
                actions = {
                    IconButton(onClick = navigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.go_to_settings_cd)
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            AnimatedVisibility(visible = showAddButton, exit = scaleOut(), enter = scaleIn()) {
                ExtendedFloatingActionButton(
                    onClick = createNewTierList,
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(R.string.create_new_tier_list_cd)
                        )
                    },
                    text = { Text(text = stringResource(R.string.create_tier_list_label)) },
                    elevation = FloatingActionButtonDefaults.elevation()
                )
            }

        }
    ) { innerPadding ->
        TiersListBody(
            modifier = Modifier.padding(innerPadding),
            uiState = uiState,
            onCreateNewClicked = createNewTierList,
            onDeleteTierList = { viewModel.obtainEvent(TiersEvent.Delete(it)) },
            tiersListState = tiersScrollState,
            tiersGridState = tiersGridScrollState,
            onClearSearchQuery = { viewModel.obtainEvent(TiersEvent.ClearSearch) },
            onSearchChanged = { query -> viewModel.obtainEvent(TiersEvent.Search(query)) },
            searchQuery = viewModel.searchQuery,
            onTierListClicked = { tierList: TierList -> navigateToEdit(tierList.id) },
            onShareTierClicked = { listId -> viewModel.obtainEvent(TiersEvent.ShareList(listId)) },
            shouldShowGrid = shouldShowGrid
        )
    }
}

@Composable
fun TiersListBody(
    modifier: Modifier = Modifier,
    uiState: TiersState,
    onCreateNewClicked: () -> Unit,
    onDeleteTierList: (TierList) -> Unit,
    tiersListState: LazyListState,
    tiersGridState: LazyGridState,
    onClearSearchQuery: () -> Unit,
    onSearchChanged: (String) -> Unit,
    searchQuery: String,
    onTierListClicked: (TierList) -> Unit,
    onShareTierClicked: (id: Long) -> Unit,
    shouldShowGrid: Boolean
) {
    var dialogVisible by rememberSaveable { mutableStateOf(false) }
    var tierListToDelete by remember { mutableStateOf<TierList?>(null) }
    if (dialogVisible && tierListToDelete != null)
        DeleteTierDialog(
            tierListName = tierListToDelete?.name ?: stringResource(R.string.get_name_error),
            onDismissRequest = {
                dialogVisible = false
                tierListToDelete = null
            }) {
            dialogVisible = false
            onDeleteTierList(tierListToDelete ?: return@DeleteTierDialog)
        }
    when (uiState) {
        TiersState.Empty -> EmptyBody(modifier) { onCreateNewClicked() }
        TiersState.Loading -> LoadingContent(modifier)
        is TiersState.Success -> if (shouldShowGrid) TiersGrid(
            modifier = modifier,
            tiersList = uiState.tiersList,
            onDeleteTierClicked = { tierList ->
                tierListToDelete = tierList
                dialogVisible = true
            },
            gridState = tiersGridState,
            onClearSearchQuery = onClearSearchQuery,
            onSearchChanged = onSearchChanged,
            searchQuery = searchQuery,
            onTierListClicked = onTierListClicked,
            onShareTierClicked = onShareTierClicked,
            searchEnabled = uiState.searchEnabled
        )
        else
            TiersColumn(
                modifier = modifier,
                tiersList = uiState.tiersList,
                onCreateNewClicked = onCreateNewClicked,
                onDeleteTierClicked = { tierList ->
                    tierListToDelete = tierList
                    dialogVisible = true
                },
                tiersListState = tiersListState,
                onClearSearchQuery = onClearSearchQuery,
                onSearchChanged = onSearchChanged,
                searchQuery = searchQuery,
                onTierListClicked = onTierListClicked,
                onShareTierClicked = onShareTierClicked,
                searchEnabled = uiState.searchEnabled
            )

        is TiersState.Error -> ErrorBody(message = uiState.message)
    }
}

@Composable
fun TiersGrid(
    modifier: Modifier = Modifier,
    tiersList: List<TierListWithCategories>,
    onDeleteTierClicked: (TierList) -> Unit,
    onClearSearchQuery: () -> Unit,
    onSearchChanged: (String) -> Unit,
    searchEnabled: Boolean,
    searchQuery: String,
    gridState: LazyGridState = rememberLazyGridState(),
    onTierListClicked: (TierList) -> Unit,
    onShareTierClicked: (id: Long) -> Unit
) {
    val spacing = dimensionResource(R.dimen.textfield_padding)
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 40.dp),
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {
        SearchElement(
            enabled = searchEnabled,
            onClearSearchQuery = onClearSearchQuery,
            onTextChanged = onSearchChanged,
            searchQuery = searchQuery,
            isSearchResultEmpty = searchQuery.isNotBlank() && tiersList.isEmpty()
        )
        LazyVerticalGrid(
            modifier = Modifier.weight(1f),
            columns = GridCells.Adaptive(minSize = 200.dp),
            verticalArrangement = Arrangement.spacedBy(spacing),
            horizontalArrangement = Arrangement.spacedBy(spacing, Alignment.CenterHorizontally),
            state = gridState
        ) {
            items(items = tiersList, key = { it.list.id }) { tierList ->
                TierListItem(
                    modifier = Modifier.animateItem(),
                    tierList = tierList,
                    onDeleteTierClicked = { onDeleteTierClicked(tierList.list) },
                    onTierListClicked = onTierListClicked,
                    onShareTierClicked = { onShareTierClicked(tierList.list.id) }
                )
            }
        }
    }
}

@Composable
private fun EmptySearchElement(
    modifier: Modifier = Modifier,
    searchQuery: String,
    tiersListSize: Int,
    onCreateNewClicked: () -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (searchQuery.isNotBlank() && tiersListSize == 0) NoFoundText(searchQuery)
        CreateNewTierButton { onCreateNewClicked() }
    }
}

@Composable
fun TiersColumn(
    modifier: Modifier = Modifier,
    tiersList: List<TierListWithCategories>,
    onCreateNewClicked: () -> Unit,
    onDeleteTierClicked: (TierList) -> Unit,
    onClearSearchQuery: () -> Unit,
    onSearchChanged: (String) -> Unit,
    searchEnabled: Boolean,
    searchQuery: String,
    tiersListState: LazyListState = rememberLazyListState(),
    onTierListClicked: (TierList) -> Unit,
    onShareTierClicked: (id: Long) -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
        contentPadding = PaddingValues(start = 40.dp, end = 40.dp, bottom = 15.dp),
        state = tiersListState
    ) {
        item {
            SearchElement(
                enabled = searchEnabled,
                onClearSearchQuery = onClearSearchQuery,
                onTextChanged = onSearchChanged,
                searchQuery = searchQuery
            )
        }
        items(items = tiersList, key = { it.list.id }) { tierList ->
            TierListItem(
                modifier = Modifier.animateItem(),
                tierList = tierList,
                onDeleteTierClicked = { onDeleteTierClicked(tierList.list) },
                onTierListClicked = onTierListClicked,
                onShareTierClicked = { onShareTierClicked(tierList.list.id) }
            )
        }
        item {
            EmptySearchElement(
                searchQuery = searchQuery,
                tiersListSize = tiersList.size
            ) { onCreateNewClicked() }
        }
    }
}

@Composable
private fun NoFoundText(searchQuery: String) {
    Text(
        text = stringResource(R.string.empty_search_results, searchQuery),
        textAlign = TextAlign.Center
    )
}

@Composable
fun SearchElement(
    modifier: Modifier = Modifier,
    onClearSearchQuery: () -> Unit,
    onTextChanged: (String) -> Unit,
    searchQuery: String,
    enabled: Boolean = true,
    isSearchResultEmpty: Boolean = false
) {
    Column(
        modifier.animateContentSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(
            dimensionResource(R.dimen.vertical_arrangement)
        )
    ) {
        OutlinedTextField(
            modifier = modifier.fillMaxWidth(),
            enabled = enabled,
            value = searchQuery,
            onValueChange = onTextChanged,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = stringResource(R.string.search_tier_list_by_name_cd)
                )
            },
            label = { Text(text = stringResource(R.string.tier_name_search_label)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            trailingIcon = {
                AnimatedVisibility(
                    visible = searchQuery.isNotBlank(),
                    exit = scaleOut(),
                    enter = scaleIn()
                ) {
                    IconButton(onClick = onClearSearchQuery) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = stringResource(R.string.clear_search_query_button_cd),
                        )
                    }
                }
            }
        )
        AnimatedVisibility(isSearchResultEmpty) {
            NoFoundText(searchQuery)
        }
    }
}

@Composable
fun TierListItem(
    modifier: Modifier = Modifier,
    tierList: TierListWithCategories,
    onDeleteTierClicked: () -> Unit,
    onTierListClicked: (TierList) -> Unit,
    onShareTierClicked: () -> Unit
) {
    Card(
        modifier = modifier
            .heightIn(100.dp, 120.dp)
            .clickable { onTierListClicked(tierList.list) },
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(modifier = Modifier) {
            ColorsColumn(categories = tierList.categories, modifier = Modifier.fillMaxHeight())
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(15.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth(),
                    text = tierList.list.name,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.weight(1f))
                TierManageButtons(
                    modifier = Modifier.fillMaxWidth(),
                    onDeleteTierClicked = onDeleteTierClicked,
                    onShareTierClicked = onShareTierClicked
                )
            }
        }
    }
}

@Composable
fun DeleteTierDialog(
    modifier: Modifier = Modifier,
    tierListName: String,
    onDismissRequest: () -> Unit,
    onConfirmDeletion: () -> Unit
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        icon = {
            Icon(imageVector = Icons.Default.Delete, contentDescription = null)
        },
        title = {
            Text(text = stringResource(R.string.delete_confirmation_title))
        },
        text = {
            Text(text = stringResource(R.string.tier_list_delete_text, tierListName))
        },
        confirmButton = {
            Button(onClick = onConfirmDeletion) {
                Text(text = stringResource(R.string.delete_btn_label))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = stringResource(R.string.cancel_btn_label))
            }

        }
    )
}

@Composable
fun TierManageButtons(
    modifier: Modifier = Modifier,
    onDeleteTierClicked: () -> Unit,
    onShareTierClicked: () -> Unit,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        FilledIconButton(onClick = onDeleteTierClicked) {
            Icon(imageVector = Icons.Outlined.Delete, contentDescription = "Delete tier list")
        }
        FilledIconButton(onClick = onShareTierClicked) {
            Icon(imageVector = Icons.Outlined.Share, contentDescription = "Share tier list")
        }
    }
}

@Composable
fun ColorsColumn(modifier: Modifier = Modifier, categories: List<TierCategory>) {
    Column(modifier = modifier.fillMaxHeight()) {
        categories.forEach { category ->
            key(category.id) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .width(50.dp)
                        .background(color = category.color),
                    contentAlignment = Alignment.Center
                ) {
                    TextOnColor(
                        text = category.name.trim().take(1),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
fun ErrorBody(modifier: Modifier = Modifier, message: String?) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = "Error loading tiers: $message",
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun EmptyBody(modifier: Modifier = Modifier, onCreateNewClicked: () -> Unit) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(dimensionResource(id = R.dimen.item_arrangement)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp, alignment = Alignment.CenterVertically)
    ) {
        Text(
            text = stringResource(R.string.empty_lists_label),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        CreateNewTierButton(onCreateNewClicked)
    }
}

@Composable
private fun CreateNewTierButton(onCreateNewClicked: () -> Unit) {
    Button(onClick = onCreateNewClicked) {
        Text(text = stringResource(R.string.create_tier_list))
    }
}

@Composable
fun LoadingContent(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Loading")
            Spacer(modifier = Modifier.width(20.dp))
            CircularProgressIndicator()
        }
    }
}

@Preview
@Composable
private fun DeleteDialogPreview() {
    DeleteTierDialog(tierListName = "Abra cadabra", onDismissRequest = { /*TODO*/ }) {

    }
}

@Preview(widthDp = 200, heightDp = 150)
@Composable
private fun TierListItemPreview() {
    TierListItem(
        onDeleteTierClicked = {},
        onTierListClicked = {},
        onShareTierClicked = {},
        tierList = TierListWithCategories(
            list = TierList(name = "Jojo"),
            categories = listOf(
                TierCategory(
                    tierListId = 0,
                    name = "A",
                    colorArgb = Color.Green.toArgb(),
                    position = 0
                ),
                TierCategory(
                    tierListId = 0,
                    name = "B",
                    colorArgb = Color.Yellow.toArgb(),
                    position = 1
                ),
                TierCategory(
                    tierListId = 0,
                    name = "C",
                    colorArgb = Color.Red.toArgb(),
                    position = 2
                ),
                TierCategory(
                    tierListId = 0,
                    name = "D",
                    colorArgb = Color.Black.toArgb(),
                    position = 3
                ),
            )
        )
    )
}