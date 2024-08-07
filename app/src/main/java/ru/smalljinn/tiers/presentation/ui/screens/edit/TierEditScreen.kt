package ru.smalljinn.tiers.presentation.ui.screens.edit

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickMultipleVisualMedia
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.launch
import ru.smalljinn.tiers.R
import ru.smalljinn.tiers.data.database.model.TierCategory
import ru.smalljinn.tiers.data.database.model.TierCategoryWithElements
import ru.smalljinn.tiers.data.database.model.TierElement
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TierEditScreen(
    modifier: Modifier = Modifier,
    viewModel: TierEditViewModel = viewModel(factory = TierEditViewModel.Factory)
) {
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
        if (uiState.selectedCategory != null) {
            CategoryModalBottomSheet(
                onDismissRequest = { viewModel.obtainEvent(EditEvent.SelectCategory(null)) },
                onSaveClicked = { viewModel.obtainEvent(EditEvent.EditCategory(it)) },
                onDeleteClicked = { viewModel.obtainEvent(EditEvent.RemoveCategory(it)) },
                category = uiState.selectedCategory
            )
        }
        TierEditBody(
            modifier = Modifier.padding(innerPaddings),
            notAttachedElements = uiState.notAttachedElements,
            categories = uiState.listWithCategoriesAndElements?.categories ?: emptyList(),
            onAddImageClicked = { mediaLauncher.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly)) },
            onCategoryClicked = { category ->
                viewModel.obtainEvent(EditEvent.SelectCategory(category))
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryModalBottomSheet(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    category: TierCategory,
    onSaveClicked: (TierCategory) -> Unit,
    onDeleteClicked: (TierCategory) -> Unit
) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var name by rememberSaveable { mutableStateOf(category.name) }
    val error by remember(name) { mutableStateOf(name.isBlank()) }
    var color by remember { mutableStateOf(category.color) }

    val hideSheet = { scope.launch { sheetState.hide() } }
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
            //TODO color picker
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
                        hideSheet().invokeOnCompletion {
                            onDismissRequest()
                        }
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
            Text(text = categoryName)
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

@Composable
fun TierEditBody(
    modifier: Modifier = Modifier,
    notAttachedElements: List<TierElement>,
    categories: List<TierCategoryWithElements>,
    onAddImageClicked: () -> Unit,
    onCategoryClicked: (TierCategory) -> Unit
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        CategoriesList(
            categories = categories,
            modifier = Modifier.weight(1f),
            onCategoryClicked = onCategoryClicked
        )
        NotAttachedImages(images = notAttachedElements, onAddImageClicked = onAddImageClicked)
    }
}

@Composable
fun NotAttachedImages(
    modifier: Modifier = Modifier,
    images: List<TierElement>,
    onAddImageClicked: () -> Unit
) {
    val itemArrangement = dimensionResource(id = R.dimen.item_arrangement)
    val imageSize = dimensionResource(id = R.dimen.image_list_size)
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(itemArrangement),
        contentPadding = PaddingValues(itemArrangement)
    ) {
        item { AddImageItem(imageSize) { onAddImageClicked() } }
        items(items = images, key = { it.id }) { element ->
            ElementImage(imageUrl = element.imageUrl, modifier = Modifier.size(imageSize))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CategoriesList(
    modifier: Modifier = Modifier,
    categories: List<TierCategoryWithElements>,
    onCategoryClicked: (TierCategory) -> Unit
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
                onCategoryClicked = { onCategoryClicked(categoryWithElements.category) })
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryItem(
    modifier: Modifier = Modifier,
    categoryWithElements: TierCategoryWithElements,
    onCategoryClicked: () -> Unit
) {
    val itemArrangement = dimensionResource(id = R.dimen.item_arrangement)
    Card(
        modifier = modifier
            .defaultMinSize(minHeight = dimensionResource(id = R.dimen.min_category_height))
            .fillMaxWidth(),
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.round_clip))
    ) {
        Row {
            with(categoryWithElements) {
                //category color with name
                TierCategoryInfo(category = category, onClick = onCategoryClicked)
                //TODO positioning elements by element.position
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(itemArrangement),
                    verticalArrangement = Arrangement.spacedBy(itemArrangement)
                ) {
                    elements.forEach { element ->
                        ElementImage(imageUrl = element.imageUrl)
                    }
                }
            }
        }
    }

}

@Composable
fun ElementImage(modifier: Modifier = Modifier, imageUrl: String) {
    val context = LocalContext.current
    AsyncImage(
        modifier = modifier.clip(RoundedCornerShape(dimensionResource(id = R.dimen.round_clip))),
        model = ImageRequest.Builder(context)
            .data(imageUrl)
            .crossfade(true)
            .build(),
        contentDescription = null,
        contentScale = ContentScale.Crop
    )
}

@Composable
private fun AddImageItem(imageSize: Dp, onAddClicked: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(imageSize)
            .clip(RoundedCornerShape(dimensionResource(id = R.dimen.round_clip)))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .clickable { onAddClicked() }
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = stringResource(R.string.add_image_from_device_cd)
        )
    }
}

@Composable
private fun TierCategoryInfo(
    modifier: Modifier = Modifier,
    category: TierCategory,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .size(dimensionResource(id = R.dimen.image_category_size))
            .background(category.color)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        //TODO resizable text
        Text(text = category.name, maxLines = 2)
    }
}
