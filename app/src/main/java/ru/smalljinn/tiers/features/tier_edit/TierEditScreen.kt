package ru.smalljinn.tiers.features.tier_edit

import android.content.ClipData
import android.content.ClipDescription
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.util.Log
import android.view.HapticFeedbackConstants
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickMultipleVisualMedia
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.draganddrop.dragAndDropSource
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.DragAndDropTransferData
import androidx.compose.ui.draganddrop.mimeTypes
import androidx.compose.ui.draganddrop.toAndroidDragEvent
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.smalljinn.tiers.R
import ru.smalljinn.tiers.data.database.model.TierCategory
import ru.smalljinn.tiers.data.database.model.TierCategoryWithElements
import ru.smalljinn.tiers.data.database.model.TierElement
import ru.smalljinn.tiers.data.network.observer.ConnectivityObserver
import ru.smalljinn.tiers.features.components.TextOnColor
import ru.smalljinn.tiers.features.components.keyboardAsState
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyGridState
import kotlin.math.roundToInt

private const val DND_ELEMENT_ID_LABEL = "elementId"
private const val DELAY_BEFORE_KEYBOARD_SHOWN = 250L

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TierEditScreen(
    modifier: Modifier = Modifier,
    viewModel: TierEditViewModel = hiltViewModel(),
    sideControls: Boolean
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    val settings = viewModel.settingsStream.collectAsStateWithLifecycle().value
    val connectionStatus =
        viewModel.connectionStatusStream.collectAsStateWithLifecycle(
            initialValue = ConnectivityObserver.ConnectionStatus.UNAVAILABLE
        ).value
    val focusManager = LocalFocusManager.current
    val isKeyboardOpened = keyboardAsState()
    val mediaLauncher =
        rememberLauncherForActivityResult(contract = PickMultipleVisualMedia()) { uris ->
            if (uris.isNotEmpty()) {
                viewModel.obtainEvent(EditEvent.AddImages(uris))
            }
        }
    Scaffold(
        modifier = modifier,
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        if (!isKeyboardOpened.value) focusManager.clearFocus()
                        EditableTierName(uiState.tierListName) { text ->
                            viewModel.obtainEvent(EditEvent.ChangeTierName(text))
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            mediaLauncher.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
                        }) {
                            Icon(
                                painter = painterResource(R.drawable.outline_add_photo_alternate_24),
                                contentDescription = stringResource(R.string.search_images_via_internet)
                            )
                        }
                    }
                )
                AnimatedVisibility(visible = uiState.isPhotoProcessing) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
        }
    ) { innerPaddings ->
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        var sheetVisible by rememberSaveable { mutableStateOf(false) }
        LaunchedEffect(key1 = uiState.sheetState) {
            when (uiState.sheetState) {
                SheetState.Hidden -> {
                    scope.launch { sheetState.hide() }
                    sheetVisible = false
                }

                else -> sheetVisible = true
            }
        }
        if (sheetVisible) {
            when (uiState.sheetState) {
                is SheetState.CategoryCreation -> CategoryModalBottomSheet(
                    onDismissRequest = { viewModel.obtainEvent(EditEvent.HideSheet) },
                    category = uiState.sheetState.newCategory,
                    onSaveClicked = { viewModel.obtainEvent(EditEvent.EditCategory(it)) },
                    onDeleteClicked = { viewModel.obtainEvent(EditEvent.RemoveCategory(it)) },
                    isCategoryCreation = true,
                    sheetState = sheetState
                )

                is SheetState.CategoryEditing -> CategoryModalBottomSheet(
                    onDismissRequest = { viewModel.obtainEvent(EditEvent.HideSheet) },
                    category = uiState.sheetState.category,
                    onSaveClicked = { viewModel.obtainEvent(EditEvent.EditCategory(it)) },
                    onDeleteClicked = { viewModel.obtainEvent(EditEvent.RemoveCategory(it)) },
                    sheetState = sheetState
                )

                is SheetState.SearchImages -> GoogleImageModalBottomSheet(
                    onDismissRequest = { viewModel.obtainEvent(EditEvent.HideSheet) },
                    searchQuery = uiState.sheetState.query,
                    onQueryChanged = { viewModel.obtainEvent(EditEvent.SearchEdited(it)) },
                    images = uiState.sheetState.images,
                    sheetState = sheetState,
                    onImageAdd = { index, bitmap ->
                        viewModel.obtainEvent(
                            EditEvent.SaveInternetImage(
                                index, bitmap
                            )
                        )
                    },
                    onSearchClicked = { query ->
                        if (connectionStatus == ConnectivityObserver.ConnectionStatus.AVAILABLE)
                            viewModel.obtainEvent(EditEvent.SearchImages(query))
                        else Toast.makeText(
                            context,
                            context.getString(R.string.no_internet_message),
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    imagesLoading = uiState.sheetState.loading,
                    errorMessage = uiState.sheetState.errorMessage
                )

                else -> {}
            }
        }
        TierEditBody(
            modifier = Modifier.padding(innerPaddings),
            notAttachedElements = uiState.notAttachedElements,
            categories = uiState.categoriesWithElements,
            onSearchImageClicked = {
                if (connectionStatus == ConnectivityObserver.ConnectionStatus.AVAILABLE)
                    viewModel.obtainEvent(EditEvent.OpenSearchSheet)
                else Toast.makeText(
                    context,
                    context.getString(R.string.no_internet_message),
                    Toast.LENGTH_SHORT
                ).show()

            },
            onCategoryClicked = { category ->
                viewModel.obtainEvent(EditEvent.SelectCategory(category))
            },
            onTierElementDropped = { categoryId, elementId ->
                viewModel.obtainEvent(EditEvent.AttachElementToCategory(categoryId, elementId))
            },
            onDeleteItemDropped = { elementId ->
                viewModel.obtainEvent(EditEvent.RemoveElement(elementId))
            },
            onElementUnpinDropped = { elementId ->
                viewModel.obtainEvent(EditEvent.UnpinElementFromCategory(elementId))
            },
            onReorderElements = { firstId, secondId ->
                viewModel.obtainEvent(
                    EditEvent.ReorderElements(
                        firstId = firstId,
                        secondId = secondId
                    )
                )
            },
            onCreateNewCategory = { viewModel.obtainEvent(EditEvent.CreateNewCategory) },
            vibrationEnabled = settings.vibrationEnabled,
            sideControls = sideControls
        )
    }
}

@Composable
private fun EditableTierName(
    listName: String,
    onNameChanged: (String) -> Unit,
) {
    BasicTextField(
        value = listName,
        onValueChange = onNameChanged,
        textStyle = MaterialTheme.typography.titleLarge.copy(color = LocalContentColor.current),
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences,
            imeAction = ImeAction.Done
        ),
        singleLine = true,
        cursorBrush = SolidColor(LocalContentColor.current),
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = dimensionResource(id = R.dimen.textfield_padding))
    )
}

@Composable
fun TierEditBody(
    modifier: Modifier = Modifier,
    notAttachedElements: List<TierElement>,
    categories: List<TierCategoryWithElements>,
    onSearchImageClicked: () -> Unit,
    onCategoryClicked: (TierCategory) -> Unit,
    onTierElementDropped: (categoryId: Long, elementId: Long) -> Unit,
    onDeleteItemDropped: (Long) -> Unit,
    onElementUnpinDropped: (Long) -> Unit,
    onReorderElements: (firstId: Long, secondId: Long) -> Unit,
    onCreateNewCategory: () -> Unit,
    vibrationEnabled: Boolean,
    sideControls: Boolean
) {
    if (!sideControls) {
        Column(modifier = modifier.fillMaxSize()) {
            CategoriesList(
                modifier = Modifier.weight(1f),
                categories = categories,
                onCategoryClicked = onCategoryClicked,
                onTierElementDropped = onTierElementDropped,
                onReorderElements = onReorderElements,
                onCreateNewCategory = onCreateNewCategory,
                vibrationEnabled = vibrationEnabled
            )
            UnpinnedHorizontalImages(
                images = notAttachedElements,
                onSearchImageClicked = onSearchImageClicked,
                onDeleteItemDropped = onDeleteItemDropped,
                onElementUnpinDropped = onElementUnpinDropped
            )
        }
    } else {
        Row(modifier = modifier.fillMaxHeight()) {
            CategoriesList(
                modifier = Modifier.weight(1f),
                categories = categories,
                onCategoryClicked = onCategoryClicked,
                onTierElementDropped = onTierElementDropped,
                onReorderElements = onReorderElements,
                onCreateNewCategory = onCreateNewCategory,
                vibrationEnabled = vibrationEnabled
            )
            UnpinnedVerticalImages(
                images = notAttachedElements,
                onSearchImageClicked = onSearchImageClicked,
                onDeleteItemDropped = onDeleteItemDropped,
                onElementUnpinDropped = onElementUnpinDropped
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UnpinnedHorizontalImages(
    modifier: Modifier = Modifier,
    images: List<TierElement>,
    onSearchImageClicked: () -> Unit,
    onDeleteItemDropped: (Long) -> Unit,
    onElementUnpinDropped: (Long) -> Unit
) {
    val itemArrangement = dimensionResource(id = R.dimen.item_arrangement)
    val imageSize = dimensionResource(id = R.dimen.image_list_size)

    var dragStarted by remember { mutableStateOf(false) }
    val dndCallback = remember {
        object : DragAndDropTarget {
            override fun onDrop(event: DragAndDropEvent): Boolean {
                val elementId =
                    event.toAndroidDragEvent().clipData.getItemAt(0).text.toString().toLong()
                onElementUnpinDropped(elementId)
                dragStarted = false
                return true
            }

            override fun onStarted(event: DragAndDropEvent) {
                super.onStarted(event)
                dragStarted = true
            }

            override fun onEnded(event: DragAndDropEvent) {
                super.onEnded(event)
                dragStarted = false
            }
        }
    }
    val animatedColor by animateColorAsState(
        targetValue = if (dragStarted) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface,
        label = "NotAttachedColor"
    )
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        AddDeleteImageItem(
            modifier = Modifier.padding(horizontal = itemArrangement),
            onDeleteItemDropped = onDeleteItemDropped
        ) { onSearchImageClicked() }
        LazyRow(
            modifier = modifier
                .fillMaxWidth()
                .height(imageSize)
                .clip(RoundedCornerShape(dimensionResource(R.dimen.round_clip)))
                .drawBehind { drawRect(animatedColor) }
                .dragAndDropTarget(
                    shouldStartDragAndDrop = { event ->
                        event
                            .mimeTypes()
                            .contains(ClipDescription.MIMETYPE_TEXT_PLAIN)
                    },
                    target = dndCallback
                ),
            horizontalArrangement = Arrangement.spacedBy(itemArrangement),
            contentPadding = PaddingValues(end = itemArrangement),
        ) {
            items(items = images, key = { it.elementId }) { element ->
                ElementImage(
                    imageUrl = element.imageUrl,
                    modifier = Modifier
                        .animateItem()
                        .sizeIn(
                            minWidth = imageSize - 1.dp,
                            maxHeight = imageSize + 1.dp,
                            minHeight = imageSize - 1.dp,
                            maxWidth = imageSize + 1.dp
                        )
                        .dragAndDropSource {
                            detectVerticalDragGestures { _, dragAmount ->
                                if (dragAmount < 5) startTransfer(
                                    DragAndDropTransferData(
                                        ClipData.newPlainText(
                                            DND_ELEMENT_ID_LABEL,
                                            element.elementId.toString()
                                        )
                                    )
                                )
                            }
                        }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UnpinnedVerticalImages(
    modifier: Modifier = Modifier,
    images: List<TierElement>,
    onSearchImageClicked: () -> Unit,
    onDeleteItemDropped: (Long) -> Unit,
    onElementUnpinDropped: (Long) -> Unit
) {
    val itemArrangement = dimensionResource(id = R.dimen.item_arrangement)
    val imageSize = dimensionResource(id = R.dimen.image_list_size)

    var dragStarted by remember { mutableStateOf(false) }
    val dndCallback = remember {
        object : DragAndDropTarget {
            override fun onDrop(event: DragAndDropEvent): Boolean {
                val elementId =
                    event.toAndroidDragEvent().clipData.getItemAt(0).text.toString().toLong()
                onElementUnpinDropped(elementId)
                dragStarted = false
                return true
            }

            override fun onStarted(event: DragAndDropEvent) {
                super.onStarted(event)
                dragStarted = true
            }

            override fun onEnded(event: DragAndDropEvent) {
                super.onEnded(event)
                dragStarted = false
            }
        }
    }
    val animatedColor by animateColorAsState(
        targetValue = if (dragStarted) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface,
        label = "NotAttachedColor"
    )
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(end = itemArrangement),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LazyColumn(
            modifier = modifier
                .weight(1f)
                .width(imageSize)
                .clip(RoundedCornerShape(dimensionResource(R.dimen.round_clip)))
                .drawBehind { drawRect(animatedColor) }
                .dragAndDropTarget(
                    shouldStartDragAndDrop = { event ->
                        event
                            .mimeTypes()
                            .contains(ClipDescription.MIMETYPE_TEXT_PLAIN)
                    },
                    target = dndCallback
                ),
            verticalArrangement = Arrangement.spacedBy(itemArrangement),
            reverseLayout = false
        ) {
            items(items = images, key = { it.elementId }) { element ->
                ElementImage(
                    imageUrl = element.imageUrl,
                    modifier = Modifier
                        .animateItem()
                        .sizeIn(
                            minWidth = imageSize - 1.dp,
                            maxHeight = imageSize + 1.dp,
                            minHeight = imageSize - 1.dp,
                            maxWidth = imageSize + 1.dp
                        )
                        .dragAndDropSource {
                            detectHorizontalDragGestures { _, dragAmount ->
                                if (dragAmount < 5) startTransfer(
                                    DragAndDropTransferData(
                                        ClipData.newPlainText(
                                            DND_ELEMENT_ID_LABEL,
                                            element.elementId.toString()
                                        )
                                    )
                                )
                            }
                        }
                )
            }
        }
        AddDeleteImageItem(
            modifier = Modifier.padding(top = itemArrangement),
            onDeleteItemDropped = onDeleteItemDropped
        ) { onSearchImageClicked() }
    }
}

@Composable
fun CategoriesList(
    modifier: Modifier = Modifier,
    categories: List<TierCategoryWithElements>,
    onCategoryClicked: (TierCategory) -> Unit,
    onTierElementDropped: (categoryId: Long, elementId: Long) -> Unit,
    onReorderElements: (firstId: Long, secondId: Long) -> Unit,
    onCreateNewCategory: () -> Unit,
    vibrationEnabled: Boolean
) {
    val itemArrangement = dimensionResource(id = R.dimen.item_arrangement)
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(itemArrangement),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(start = itemArrangement, end = itemArrangement)
    ) {
        items(items = categories, key = { it.category.id }) { categoryWithElements ->
            CategoryItem(
                modifier = Modifier.animateItem(),
                categoryWithElements = categoryWithElements,
                onCategoryClicked = { onCategoryClicked(categoryWithElements.category) },
                onTierElementDropped = onTierElementDropped,
                onReorderElements = onReorderElements,
                vibrationEnabled = vibrationEnabled
            )
        }
        item {
            TextButton(onClick = onCreateNewCategory) {
                Text(stringResource(R.string.create_new_category))
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CategoryItem(
    modifier: Modifier = Modifier,
    categoryWithElements: TierCategoryWithElements,
    onCategoryClicked: (TierCategory) -> Unit,
    onTierElementDropped: (categoryId: Long, elementId: Long) -> Unit,
    onReorderElements: (firstId: Long, secondId: Long) -> Unit,
    vibrationEnabled: Boolean
) {
    val haptic = LocalHapticFeedback.current
    var isReadyToDrop by remember { mutableStateOf(false) }
    val vibrate = { haptic.performHapticFeedback(HapticFeedbackType.LongPress) }

    val dndCallback = remember {
        object : DragAndDropTarget {
            override fun onDrop(event: DragAndDropEvent): Boolean {
                val draggedData = event.toAndroidDragEvent().clipData.getItemAt(0).text.toString()
                val elementIdToPin = draggedData.toLong()
                onTierElementDropped(categoryWithElements.category.id, elementIdToPin)
                return true
            }

            override fun onStarted(event: DragAndDropEvent) {
                super.onStarted(event)
                if (vibrationEnabled) {
                    vibrate()
                }
            }

            override fun onEntered(event: DragAndDropEvent) {
                super.onEntered(event)
                isReadyToDrop = true
            }

            override fun onExited(event: DragAndDropEvent) {
                super.onExited(event)
                isReadyToDrop = false
            }

            override fun onEnded(event: DragAndDropEvent) {
                super.onEnded(event)
                isReadyToDrop = false
                if (vibrationEnabled) {
                    vibrate()
                }
            }
        }
    }
    val itemArrangement = dimensionResource(id = R.dimen.item_arrangement)
    val categoryHeight = dimensionResource(id = R.dimen.image_category_size)
    val density = LocalDensity.current.density
    var cardHeight by remember { mutableIntStateOf(categoryHeight.value.toInt()) }

    val lazyGridState = rememberLazyGridState()
    val view = LocalView.current
    val listReorderChannel = remember { Channel<Unit>() }
    val reorderableLazyGridState =
        rememberReorderableLazyGridState(lazyGridState = lazyGridState) { from, to ->
            listReorderChannel.tryReceive()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                if (vibrationEnabled) {
                    view.performHapticFeedback(HapticFeedbackConstants.SEGMENT_FREQUENT_TICK)
                }
            }
            onReorderElements(from.key as Long, to.key as Long)
            listReorderChannel.receive()
        }
    LaunchedEffect(key1 = categoryWithElements.elements) {
        listReorderChannel.trySend(Unit)
    }
    val interactionSource = remember { MutableInteractionSource() }
    Card(
        modifier = modifier
            .defaultMinSize(minHeight = categoryHeight)
            .dragAndDropTarget(
                shouldStartDragAndDrop = { event ->
                    event
                        .mimeTypes()
                        .contains(ClipDescription.MIMETYPE_TEXT_PLAIN)
                },
                target = dndCallback
            ),
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.round_clip)),
        border = if (isReadyToDrop) BorderStroke(
            dimensionResource(id = R.dimen.border_width),
            color = MaterialTheme.colorScheme.primary
        ) else null
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            //category color with name
            TierCategoryInfo(
                modifier = Modifier.height(cardHeight.dp),
                category = categoryWithElements.category,
                onClick = onCategoryClicked
            )
            LazyVerticalGrid(
                modifier = Modifier
                    .heightIn(min = categoryHeight, max = 800.dp)
                    .onSizeChanged { cardHeight = (it.height / density).toInt() },
                columns = GridCells.Adaptive(categoryHeight),
                userScrollEnabled = true,
                horizontalArrangement = Arrangement.spacedBy(
                    itemArrangement,
                    Alignment.CenterHorizontally
                ),
                verticalArrangement = Arrangement.spacedBy(itemArrangement),
                state = lazyGridState
            ) {
                items(
                    items = categoryWithElements.elements,
                    key = { element -> element.elementId }) { element ->
                    ReorderableItem(
                        state = reorderableLazyGridState,
                        key = element.elementId
                    ) {
                        ElementImage(
                            imageUrl = element.imageUrl,
                            modifier = Modifier
                                .dragAndDropSource {
                                    detectTapGestures(
                                        onLongPress = {
                                            startTransfer(
                                                DragAndDropTransferData(
                                                    clipData = ClipData.newPlainText(
                                                        "categoryElementId",
                                                        element.elementId.toString()
                                                    )
                                                )
                                            )
                                        }
                                    )
                                }
                                .draggableHandle(
                                    onDragStarted = {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                                            if (vibrationEnabled) view.performHapticFeedback(
                                                HapticFeedbackConstants.DRAG_START
                                            )
                                        }
                                        Log.i("DRAG", "drag started $element")
                                    },
                                    onDragStopped = {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                            if (vibrationEnabled) view.performHapticFeedback(
                                                HapticFeedbackConstants.GESTURE_END
                                            )
                                        }
                                        Log.i("DRAG", "drag stopped $element")
                                    },
                                    interactionSource = interactionSource
                                )
                                .sizeIn(
                                    minWidth = categoryHeight - 1.dp,
                                    maxHeight = categoryHeight + 1.dp,
                                    minHeight = categoryHeight - 1.dp,
                                    maxWidth = categoryHeight + 1.dp
                                )

                        )
                    }
                }
            }
        }
    }
}

enum class DragState {
    STOPPED, STARTED, ELEMENT_IN
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AddDeleteImageItem(
    modifier: Modifier = Modifier,
    onDeleteItemDropped: (Long) -> Unit,
    onSearchClicked: () -> Unit
) {
    var dragState by remember { mutableStateOf(DragState.STOPPED) }
    val dndCallback = remember {
        object : DragAndDropTarget {
            override fun onDrop(event: DragAndDropEvent): Boolean {
                val elementId: Long =
                    event.toAndroidDragEvent().clipData.getItemAt(0).text.toString().toLong()
                onDeleteItemDropped(elementId)
                dragState = DragState.STOPPED
                return true
            }

            override fun onStarted(event: DragAndDropEvent) {
                super.onStarted(event)
                dragState = DragState.STARTED
            }

            override fun onEnded(event: DragAndDropEvent) {
                super.onEnded(event)
                dragState = DragState.STOPPED
            }

            override fun onEntered(event: DragAndDropEvent) {
                super.onEntered(event)
                dragState = DragState.ELEMENT_IN
            }

            override fun onExited(event: DragAndDropEvent) {
                super.onExited(event)
                dragState = DragState.STARTED
            }
        }
    }
    val animatedColor by animateColorAsState(
        targetValue = when (dragState) {
            DragState.STOPPED -> MaterialTheme.colorScheme.secondaryContainer
            DragState.STARTED -> MaterialTheme.colorScheme.errorContainer
            DragState.ELEMENT_IN -> MaterialTheme.colorScheme.error
        },
        label = "NotAttachedColor"
    )
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .dragAndDropTarget(
                shouldStartDragAndDrop = { event ->
                    event
                        .mimeTypes()
                        .contains(ClipDescription.MIMETYPE_TEXT_PLAIN)
                },
                target = dndCallback
            )
            .size(dimensionResource(id = R.dimen.image_list_size))
            .clip(RoundedCornerShape(dimensionResource(id = R.dimen.round_clip)))
            .drawBehind { drawRect(animatedColor) }
            .clickable { onSearchClicked() }
    ) {
        AnimatedContent(
            targetState = dragState != DragState.STOPPED,
            label = "AddEditItemState"
        ) { deleteState ->
            if (deleteState) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = stringResource(R.string.delete_dragged_image_cd)
                )
            } else {
                Icon(
                    painter = painterResource(R.drawable.baseline_image_search_24),
                    contentDescription = stringResource(R.string.add_image_from_device_cd)
                )
            }
        }
    }
}

@Composable
private fun TierCategoryInfo(
    modifier: Modifier = Modifier,
    category: TierCategory,
    onClick: (TierCategory) -> Unit
) {
    val categorySize = dimensionResource(id = R.dimen.image_category_size)
    Box(
        modifier = modifier
            .width(categorySize)
            .background(category.color)
            .clickable { onClick(category) },
        contentAlignment = Alignment.Center
    ) {
        //TODO resizable text
        TextOnColor(text = category.name)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoogleImageModalBottomSheet(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    searchQuery: String,
    imagesLoading: Boolean,
    onQueryChanged: (String) -> Unit,
    images: List<String>,
    errorMessage: String?,
    onImageAdd: (index: Int, image: Bitmap) -> Unit,
    sheetState: androidx.compose.material3.SheetState,
    onSearchClicked: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val loader = ImageLoader(context)
    val focus = remember { FocusRequester() }

    val itemSpacing = dimensionResource(id = R.dimen.item_spacing)
    val roundCornerSize = dimensionResource(id = R.dimen.round_clip)
    val minImageSize = remember { 80.dp }
    val spacing = remember { 20.dp }

    LaunchedEffect(key1 = Unit) {
        if (images.isNotEmpty()) return@LaunchedEffect
        delay(DELAY_BEFORE_KEYBOARD_SHOWN)
        focus.requestFocus()
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = sheetState
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(spacing),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing)
        ) {
            LocalOutlinedTextField(
                modifier = Modifier
                    .focusRequester(focus)
                    .fillMaxWidth(),
                value = searchQuery,
                onValueChanged = onQueryChanged,
                labelText = stringResource(R.string.search_images_label),
                imeAction = ImeAction.Search,
                onSearchClicked = { query -> onSearchClicked(query) }
            )
            AnimatedVisibility(visible = images.isEmpty()) {
                Text(
                    text = stringResource(R.string.here_will_be_images_from_google_search),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }
            AnimatedVisibility(visible = !errorMessage.isNullOrEmpty()) {
                Text(
                    text = errorMessage ?: "no errors",
                    style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.error)
                )
            }
            AnimatedVisibility(visible = imagesLoading) { if (imagesLoading) CircularProgressIndicator() }
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Adaptive(minSize = minImageSize),
                horizontalArrangement = Arrangement.spacedBy(itemSpacing),
                verticalItemSpacing = itemSpacing
            ) {
                itemsIndexed(items = images, key = { _, url -> url }) { index, url ->
                    val request = ImageRequest.Builder(context = context)
                        .allowHardware(false)
                        .data(url)
                        .crossfade(true)
                        .build()
                    AsyncImage(
                        model = request,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .animateItem()
                            .sizeIn(
                                minHeight = minImageSize,
                                minWidth = minImageSize,
                            )
                            .clip(RoundedCornerShape(roundCornerSize))
                            .clickable {
                                scope.launch {
                                    val result = (loader.execute(request) as SuccessResult).drawable
                                    val bitmap = (result as BitmapDrawable).bitmap
                                    onImageAdd(index, bitmap ?: return@launch)
                                }
                            },
                    )
                }
            }
        }
    }
}

@Composable
fun LocalOutlinedTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChanged: (String) -> Unit,
    error: Boolean = false,
    labelText: String,
    imeAction: ImeAction = ImeAction.Default,
    onSearchClicked: (String) -> Unit = {}
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    OutlinedTextField(
        modifier = modifier,
        value = value,
        onValueChange = onValueChanged,
        singleLine = true,
        label = { Text(text = labelText) },
        isError = error,
        keyboardOptions = KeyboardOptions(imeAction = imeAction),
        keyboardActions = KeyboardActions(
            onSearch = {
                keyboardController?.hide()
                onSearchClicked(value)
            }
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryModalBottomSheet(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    category: TierCategory,
    onSaveClicked: (TierCategory) -> Unit,
    onDeleteClicked: (TierCategory) -> Unit,
    isCategoryCreation: Boolean = false,
    sheetState: androidx.compose.material3.SheetState,
) {
    var name by rememberSaveable { mutableStateOf(category.name) }
    val error by remember(name) { mutableStateOf(name.isBlank()) }
    var color by remember { mutableStateOf(category.color) }

    ModalBottomSheet(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        sheetState = sheetState
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.padding(bottom = 10.dp)
        ) {
            //Category name
            LocalOutlinedTextField(
                value = name,
                onValueChanged = { name = it },
                labelText = stringResource(R.string.category_name_label),
                error = error
            )
            //color picker
            ColorPicker(color = color, categoryName = name) { color = it }
            //buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                //Delete button
                if (!isCategoryCreation)
                    IconButton(onClick = { onDeleteClicked(category) }) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = stringResource(
                                R.string.delete_category_cd,
                                category.name
                            )
                        )
                    }

                //Save button
                Button(
                    onClick = {
                        onSaveClicked(category.copy(name = name, colorArgb = color.toArgb()))
                    },
                    enabled = !error
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.outline_save_24),
                        contentDescription = stringResource(
                            R.string.save_category_cd,
                            category.name
                        )
                    )
                }
            }
        }
    }
}


@Composable
fun ElementImage(modifier: Modifier = Modifier, imageUrl: String) {
    val context = LocalContext.current
    AsyncImage(
        model = ImageRequest.Builder(context)
            .allowHardware(false)
            .data(imageUrl)
            .crossfade(false)
            .build(),
        modifier = modifier
            .clip(RoundedCornerShape(dimensionResource(id = R.dimen.round_clip))),
        contentDescription = null,
        contentScale = ContentScale.Crop
    )
}

@Composable
fun ColorPicker(
    modifier: Modifier = Modifier,
    color: Color,
    categoryName: String,
    onColorChanged: (Color) -> Unit
) {
    var colorState by remember(color) { mutableStateOf(color) }
    LaunchedEffect(key1 = colorState) { onColorChanged(colorState) }

    Column(modifier = modifier.padding(start = 30.dp, end = 30.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(RoundedCornerShape(dimensionResource(id = R.dimen.round_clip)))
                .background(color = color),
            contentAlignment = Alignment.Center
        ) {
            TextOnColor(text = categoryName)
        }
        ColorSlider(name = "Red", value = colorState.red) { colorState = colorState.copy(red = it) }
        ColorSlider(name = "Green", value = colorState.green) {
            colorState = colorState.copy(green = it)
        }
        ColorSlider(name = "Blue", value = colorState.blue) {
            colorState = colorState.copy(blue = it)
        }
    }
}

@Composable
fun ColorSlider(
    modifier: Modifier = Modifier,
    name: String,
    value: Float,
    onValueChanged: (Float) -> Unit
) {
    val labelWidth = remember { 60.dp }
    val spacerWidth = remember { 10.dp }
    val intColorValue = remember(value) { (value * 255).roundToInt().toString() }

    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(text = name, modifier = Modifier.width(labelWidth))
        Spacer(modifier = Modifier.width(spacerWidth))
        Slider(
            modifier = Modifier.weight(1f),
            value = value,
            onValueChange = { onValueChanged(it) },
            valueRange = 0f..1f
        )
        Spacer(modifier = Modifier.width(spacerWidth))
        Text(text = intColorValue, modifier = Modifier.width(labelWidth))
    }
}
