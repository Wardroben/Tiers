package ru.smalljinn.tiers.presentation.ui.screens.edit

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickMultipleVisualMedia
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import ru.smalljinn.tiers.R
import ru.smalljinn.tiers.data.database.model.TierCategory
import ru.smalljinn.tiers.data.database.model.TierCategoryWithElements
import ru.smalljinn.tiers.data.database.model.TierElement

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
            //TODO textField for name
            TopAppBar(title = { Text(text = uiState.tierListName) })
        }
    ) { innerPaddings ->
        TierEditBody(
            modifier = Modifier.padding(innerPaddings),
            notAttachedElements = uiState.notAttachedElements,
            categories = uiState.listWithCategoriesAndElements?.categories ?: emptyList(),
            onAddImageClicked = { mediaLauncher.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly)) }
        )
    }
}

@Composable
fun TierEditBody(
    modifier: Modifier = Modifier,
    notAttachedElements: List<TierElement>,
    categories: List<TierCategoryWithElements>,
    onAddImageClicked: () -> Unit
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        CategoriesList(categories = categories, modifier = Modifier.weight(1f))
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

@Composable
fun CategoriesList(modifier: Modifier = Modifier, categories: List<TierCategoryWithElements>) {
    val itemArrangement = dimensionResource(id = R.dimen.item_arrangement)
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(itemArrangement),
        contentPadding = PaddingValues(start = itemArrangement, end = itemArrangement)
    ) {
        items(items = categories, key = { it.category.id }) { categoryWithElements ->
            CategoryItem(categoryWithElements = categoryWithElements)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryItem(modifier: Modifier = Modifier, categoryWithElements: TierCategoryWithElements) {
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
                TierCategoryInfo(category)
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
            contentDescription = "Add image"
        )
    }
}

@Composable
private fun TierCategoryInfo(category: TierCategory) {
    Box(
        modifier = Modifier
            .size(dimensionResource(id = R.dimen.image_category_size))
            .background(category.color),
        contentAlignment = Alignment.Center
    ) {
        //TODO resizable text
        Text(text = category.name, maxLines = 2)
    }
}
