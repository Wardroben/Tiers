package ru.smalljinn.tiers.presentation.ui.screens.edit

import android.content.ClipData
import android.content.ClipDescription
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickMultipleVisualMedia
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.draganddrop.dragAndDropSource
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import ru.smalljinn.tiers.R
import ru.smalljinn.tiers.data.database.model.TierCategory
import ru.smalljinn.tiers.data.database.model.TierCategoryWithElements
import ru.smalljinn.tiers.data.database.model.TierElement
import ru.smalljinn.tiers.presentation.ui.screens.components.TextOnColor
import kotlin.math.roundToInt

private const val DND_ELEMENT_ID_LABEL = "elementId"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TierEditScreen(
    modifier: Modifier = Modifier,
    viewModel: TierEditViewModel = viewModel(factory = TierEditViewModel.Factory)
) {
    val scope = rememberCoroutineScope()
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
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
                //TODO textField for name
                TopAppBar(
                    title = { Text(text = uiState.tierListName) },
                    actions = {
                        IconButton(onClick = { viewModel.obtainEvent(EditEvent.CreateNewCategory) }) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = stringResource(R.string.create_new_category_cd)
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
                    scope.launch { sheetState.hide() }.invokeOnCompletion { sheetVisible = false }
                }

                else -> sheetVisible = true
            }
        }
        if (sheetVisible) {
            CategoryModalBottomSheet(
                onDismissRequest = { viewModel.obtainEvent(EditEvent.HideSheet) },
                onSaveClicked = { viewModel.obtainEvent(EditEvent.EditCategory(it)) },
                onDeleteClicked = { viewModel.obtainEvent(EditEvent.RemoveCategory(it)) },
                tierCategory = when (uiState.sheetState) {
                    is SheetState.CategoryCreation -> uiState.sheetState.newCategory
                    is SheetState.CategoryEditing -> uiState.sheetState.category
                    else -> null
                },
                sheetState = sheetState,
            )
        }
        TierEditBody(
            modifier = Modifier.padding(innerPaddings),
            notAttachedElements = uiState.notAttachedElements,
            categories = uiState.categoriesWithElements,
            onAddImageClicked = { mediaLauncher.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly)) },
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
                viewModel.obtainEvent(EditEvent.UnattachElementFromCategory(elementId))
            }
        )
    }
}

@Composable
fun TierEditBody(
    modifier: Modifier = Modifier,
    notAttachedElements: List<TierElement>,
    categories: List<TierCategoryWithElements>,
    onAddImageClicked: () -> Unit,
    onCategoryClicked: (TierCategory) -> Unit,
    onTierElementDropped: (categoryId: Long, elementId: Long) -> Unit,
    onDeleteItemDropped: (Long) -> Unit,
    onElementUnpinDropped: (Long) -> Unit
) {
    Column(modifier = modifier.fillMaxSize()) {
        CategoriesList(
            modifier = Modifier.weight(1f),
            categories = categories,
            onCategoryClicked = onCategoryClicked,
            onTierElementDropped = onTierElementDropped
        )
        NotAttachedImages(
            images = notAttachedElements,
            onAddImageClicked = onAddImageClicked,
            onDeleteItemDropped = onDeleteItemDropped,
            onElementUnpinDropped = onElementUnpinDropped
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NotAttachedImages(
    modifier: Modifier = Modifier,
    images: List<TierElement>,
    onAddImageClicked: () -> Unit,
    onDeleteItemDropped: (Long) -> Unit,
    onElementUnpinDropped: (Long) -> Unit
) {
    val itemArrangement = dimensionResource(id = R.dimen.item_arrangement)
    val imageSize = dimensionResource(id = R.dimen.image_list_size)
    val haptic = LocalHapticFeedback.current

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
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .background(
                if (dragStarted) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surface
            )
            .dragAndDropTarget(
                shouldStartDragAndDrop = { event ->
                    event
                        .mimeTypes()
                        .contains(ClipDescription.MIMETYPE_TEXT_PLAIN)
                },
                target = dndCallback
            ),
        horizontalArrangement = Arrangement.spacedBy(itemArrangement),
        contentPadding = PaddingValues(itemArrangement)
    ) {
        item { AddDeleteImageItem(onDeleteItemDropped = onDeleteItemDropped) { onAddImageClicked() } }
        items(items = images, key = { it.id }) { element ->
            ElementImage(
                imageUrl = element.imageUrl,
                modifier = Modifier
                    .animateItemPlacement()
                    .size(imageSize)
                    .dragAndDropSource {
                        detectTapGestures(
                            onLongPress = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                startTransfer(
                                    DragAndDropTransferData(
                                        ClipData.newPlainText(
                                            DND_ELEMENT_ID_LABEL,
                                            element.id.toString()
                                        )
                                    )
                                )
                            }
                        )
                    }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CategoriesList(
    modifier: Modifier = Modifier,
    categories: List<TierCategoryWithElements>,
    onCategoryClicked: (TierCategory) -> Unit,
    onTierElementDropped: (categoryId: Long, elementId: Long) -> Unit
) {
    val itemArrangement = dimensionResource(id = R.dimen.item_arrangement)
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(itemArrangement),
        contentPadding = PaddingValues(start = itemArrangement, end = itemArrangement)
    ) {
        items(items = categories, key = { it.category.id }) { categoryWithElements ->
            CategoryItem(
                modifier = Modifier.animateItemPlacement(),
                categoryWithElements = categoryWithElements,
                onCategoryClicked = { onCategoryClicked(categoryWithElements.category) },
                onTierElementDropped = onTierElementDropped
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CategoryItem(
    modifier: Modifier = Modifier,
    categoryWithElements: TierCategoryWithElements,
    onCategoryClicked: () -> Unit,
    onTierElementDropped: (categoryId: Long, elementId: Long) -> Unit
) {
    var isReadyToDrop by remember { mutableStateOf(false) }
    val dndCallback = remember {
        object : DragAndDropTarget {
            override fun onDrop(event: DragAndDropEvent): Boolean {
                val draggedData = event.toAndroidDragEvent().clipData.getItemAt(0).text.toString()
                val elementIdToPin = draggedData.toLong()
                onTierElementDropped(categoryWithElements.category.id, elementIdToPin)
                return true
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
            }
        }
    }
    val itemArrangement = dimensionResource(id = R.dimen.item_arrangement)
    val categoryHeight = dimensionResource(id = R.dimen.image_category_size)
    val density = LocalDensity.current.density
    var cardHeight by remember { mutableIntStateOf(categoryHeight.value.toInt()) }
    val sortedElements =
        remember(categoryWithElements.elements) { categoryWithElements.elements.sortedBy { it.position } }
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
            //TODO positioning elements by element.position
            /*LazyRow(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                contentPadding = PaddingValues(start = 5.dp)
            ) {
                items(items = categoryWithElements.elements, key = { it.id }) { element ->
                    ElementImage(
                        modifier = Modifier
                            .animateItemPlacement()
                            .size(categoryHeight)
                            .dragAndDropSource {
                                detectTapGestures(
                                    onLongPress = {
                                        startTransfer(
                                            DragAndDropTransferData(
                                                clipData = ClipData.newPlainText(
                                                    "categoryElementId", element.id.toString()
                                                )
                                            )
                                        )
                                    }
                                )
                            },
                        imageUrl = element.imageUrl,
                    )
                }
            }*/
            LazyVerticalGrid(
                modifier = Modifier
                    .heightIn(min = categoryHeight, max = 600.dp)
                    .onSizeChanged { cardHeight = (it.height / density).toInt() },
                columns = GridCells.Adaptive(categoryHeight),
                userScrollEnabled = true,
                horizontalArrangement = Arrangement.spacedBy(itemArrangement),
                verticalArrangement = Arrangement.spacedBy(itemArrangement)
            ) {
                items(items = sortedElements, key = { it.id }) { element ->
                    ElementImage(
                        imageUrl = element.imageUrl,
                        modifier = Modifier
                            .animateItemPlacement()
                            .size(categoryHeight)
                            .dragAndDropSource {
                                detectTapGestures(
                                    onLongPress = {
                                        startTransfer(
                                            DragAndDropTransferData(
                                                clipData = ClipData.newPlainText(
                                                    "categoryElementId", element.id.toString()
                                                )
                                            )
                                        )
                                    }
                                )
                            },
                    )
                }
            }
            /*FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .onSizeChanged { cardHeight = (it.height / density).roundToInt() },
                horizontalArrangement = Arrangement.spacedBy(itemArrangement),
                verticalArrangement = Arrangement.spacedBy(itemArrangement)
            ) {
                categoryWithElements.elements.forEach { element ->
                    key(element.id) {
                        ElementImage(
                            modifier = Modifier
                                .size(categoryHeight)
                                .dragAndDropSource {
                                    detectTapGestures(
                                        onLongPress = {
                                            startTransfer(
                                                DragAndDropTransferData(
                                                    clipData = ClipData.newPlainText(
                                                        "categoryElementId", element.id.toString()
                                                    )
                                                )
                                            )
                                        }
                                    )
                                },
                            imageUrl = element.imageUrl,
                        )
                    }
                }
            }*/
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AddDeleteImageItem(onDeleteItemDropped: (Long) -> Unit, onAddClicked: () -> Unit) {
    var isDeleteState by remember { mutableStateOf(false) }
    var isElementEntered by remember { mutableStateOf(false) }
    val dndCallback = remember {
        object : DragAndDropTarget {
            override fun onDrop(event: DragAndDropEvent): Boolean {
                val elementId: Long =
                    event.toAndroidDragEvent().clipData.getItemAt(0).text.toString().toLong()
                onDeleteItemDropped(elementId)
                isDeleteState = false
                isElementEntered = false
                return true
            }

            override fun onStarted(event: DragAndDropEvent) {
                super.onStarted(event)
                isDeleteState = true
            }

            override fun onEnded(event: DragAndDropEvent) {
                super.onEnded(event)
                isDeleteState = false
            }

            override fun onEntered(event: DragAndDropEvent) {
                super.onEntered(event)
                isElementEntered = true
            }

            override fun onExited(event: DragAndDropEvent) {
                super.onExited(event)
                isElementEntered = false
            }
        }
    }
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(dimensionResource(id = R.dimen.image_list_size))
            .clip(RoundedCornerShape(dimensionResource(id = R.dimen.round_clip)))
            .background(
                if (isDeleteState) {
                    if (isElementEntered) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.errorContainer
                } else {
                    MaterialTheme.colorScheme.secondaryContainer
                }
            )
            .dragAndDropTarget(
                shouldStartDragAndDrop = { event ->
                    event
                        .mimeTypes()
                        .contains(ClipDescription.MIMETYPE_TEXT_PLAIN)
                },
                target = dndCallback
            )
            .clickable { onAddClicked() }
    ) {
        AnimatedContent(targetState = isDeleteState, label = "AddEditItemState") { deleteState ->
            if (deleteState) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = stringResource(R.string.delete_dragged_image_cd)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Add,
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
    onClick: () -> Unit
) {
    val categorySize = dimensionResource(id = R.dimen.image_category_size)
    Box(
        modifier = modifier
            .width(categorySize)
            .background(category.color)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        //TODO resizable text
        //Text(text = category.name, maxLines = 2, color = Color.Black)
        TextOnColor(text = category.name)
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryModalBottomSheet(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    tierCategory: TierCategory?,
    onSaveClicked: (TierCategory) -> Unit,
    onDeleteClicked: (TierCategory) -> Unit,
    sheetState: androidx.compose.material3.SheetState,
) {

    val category by remember {
        mutableStateOf(
            tierCategory ?: TierCategory(
                tierListId = 1,
                position = 0
            )
        )
    }

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
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                label = { Text(text = "Tier name") },
                isError = error
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
                IconButton(onClick = { onDeleteClicked(category) }) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Delete category ${category.name}"
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
                        contentDescription = "Save category ${category.name}"
                    )
                }
            }
        }
    }
}


@Composable
fun ElementImage(modifier: Modifier = Modifier, imageUrl: String) {
    //val context = LocalContext.current
    AsyncImage(
        model = imageUrl,
        modifier = modifier.clip(RoundedCornerShape(dimensionResource(id = R.dimen.round_clip))),
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
