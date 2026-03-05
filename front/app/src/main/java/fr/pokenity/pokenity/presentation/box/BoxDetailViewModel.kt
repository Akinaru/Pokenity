package fr.pokenity.pokenity.presentation.box

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import fr.pokenity.data.core.AppContainer
import fr.pokenity.data.model.BoxOpenResult
import fr.pokenity.pokenity.domain.usecase.GetBoxByIdUseCase
import fr.pokenity.pokenity.domain.usecase.OpenBoxUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.random.Random

class BoxDetailViewModel(
    private val getBoxByIdUseCase: GetBoxByIdUseCase,
    private val openBoxUseCase: OpenBoxUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(BoxDetailUiState())
    val uiState: StateFlow<BoxDetailUiState> = _uiState.asStateFlow()

    fun loadBox(boxId: String) {
        if (boxId.isBlank()) {
            _uiState.value = BoxDetailUiState(
                isLoading = false,
                errorMessage = "Identifiant box invalide."
            )
            return
        }

        _uiState.value = _uiState.value.copy(
            isLoading = true,
            errorMessage = null,
            openingErrorMessage = null,
            showRewardDialog = false,
            pendingReward = null,
            rouletteWinningIndex = null,
            isSpinning = false
        )

        viewModelScope.launch(Dispatchers.IO) {
            runCatching { getBoxByIdUseCase(boxId) }
                .onSuccess { box ->
                    val orderedEntries = box.entries
                        .map { it.toUi() }
                        .sortedWith(compareBy<BoxPokemonUi> { it.dropRate }.thenBy { it.resourceName })
                    val previewRoulette = buildPreviewRoulette(orderedEntries)

                    _uiState.value = BoxDetailUiState(
                        isLoading = false,
                        box = box,
                        orderedEntries = orderedEntries,
                        rouletteItems = previewRoulette
                    )
                }
                .onFailure {
                    _uiState.value = BoxDetailUiState(
                        isLoading = false,
                        errorMessage = it.message ?: "Impossible de charger cette box."
                    )
                }
        }
    }

    fun openBox() {
        val currentState = _uiState.value
        val currentBox = currentState.box ?: return
        if (currentState.isLoading || currentState.isOpening || currentState.isSpinning) {
            return
        }

        val previewRoulette = buildPreviewRoulette(currentState.orderedEntries)

        _uiState.value = currentState.copy(
            isOpening = true,
            openingErrorMessage = null,
            showRewardDialog = false,
            pendingReward = null,
            rouletteWinningIndex = null,
            rouletteItems = previewRoulette
        )

        viewModelScope.launch(Dispatchers.IO) {
            runCatching { openBoxUseCase(currentBox.id) }
                .onSuccess { openResult ->
                    val reward = openResult.toRewardUi()
                    val pool = _uiState.value.orderedEntries.ifEmpty {
                        currentBox.entries
                            .map { it.toUi() }
                            .sortedWith(compareBy<BoxPokemonUi> { it.dropRate }.thenBy { it.resourceName })
                    }.ifEmpty {
                        listOf(reward)
                    }

                    val flashSequence = buildFlashSequence(pool, reward)
                    val previousSpinRequestId = _uiState.value.spinRequestId

                    _uiState.value = _uiState.value.copy(
                        isOpening = false,
                        isSpinning = true,
                        openingErrorMessage = null,
                        pendingReward = reward,
                        showRewardDialog = false,
                        rouletteItems = flashSequence,
                        rouletteWinningIndex = flashSequence.lastIndex,
                        spinRequestId = previousSpinRequestId + 1L
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isOpening = false,
                        isSpinning = false,
                        openingErrorMessage = it.message ?: "Impossible d'ouvrir cette box."
                    )
                }
        }
    }

    fun onSpinAnimationCompleted() {
        val currentState = _uiState.value
        if (!currentState.isSpinning) {
            return
        }

        _uiState.value = currentState.copy(
            isSpinning = false,
            showRewardDialog = currentState.pendingReward != null
        )
    }

    fun dismissRewardDialog() {
        val currentState = _uiState.value
        val previewRoulette = buildPreviewRoulette(currentState.orderedEntries)

        _uiState.value = currentState.copy(
            showRewardDialog = false,
            pendingReward = null,
            rouletteWinningIndex = null,
            rouletteItems = previewRoulette
        )
    }

    private fun buildPreviewRoulette(entries: List<BoxPokemonUi>): List<BoxPokemonUi> {
        if (entries.isEmpty()) {
            return emptyList()
        }
        val sampleSize = 30
        val preview = ArrayList<BoxPokemonUi>(sampleSize)
        var lastResourceId: Int? = null

        repeat(sampleSize) {
            val next = pickWeightedItem(
                items = entries,
                avoidResourceId = lastResourceId
            )
            preview += next
            lastResourceId = next.resourceId
        }

        if (preview.size <= 1) return preview
        val pivot = Random.nextInt(preview.size)
        return preview.drop(pivot) + preview.take(pivot)
    }

    private fun buildFlashSequence(
        pool: List<BoxPokemonUi>,
        reward: BoxPokemonUi
    ): List<BoxPokemonUi> {
        val flashesBeforeReward = 30 + Random.nextInt(from = 0, until = 10)
        val flashes = ArrayList<BoxPokemonUi>(flashesBeforeReward + 1)
        var lastResourceId: Int? = null
        repeat(flashesBeforeReward) {
            val next = pickWeightedItem(
                items = pool,
                avoidResourceId = lastResourceId
            )
            flashes += next
            lastResourceId = next.resourceId
        }
        flashes += reward
        return flashes
    }

    private fun pickWeightedItem(
        items: List<BoxPokemonUi>,
        avoidResourceId: Int? = null
    ): BoxPokemonUi {
        if (items.isEmpty()) {
            return BoxPokemonUi(
                resourceType = "pokemon",
                resourceId = 0,
                resourceName = "unknown",
                dropRate = 100.0
            )
        }

        val candidateItems = if (avoidResourceId != null && items.size > 1) {
            items.filter { it.resourceId != avoidResourceId }.ifEmpty { items }
        } else {
            items
        }

        val weightedPool = candidateItems.map { item ->
            item to max(item.dropRate, 0.01)
        }

        val totalWeight = weightedPool.sumOf { it.second }
        var threshold = Random.nextDouble(from = 0.0, until = totalWeight)

        for ((item, weight) in weightedPool) {
            threshold -= weight
            if (threshold <= 0.0) {
                return item
            }
        }

        return weightedPool.last().first
    }

    private fun fr.pokenity.data.model.LootBoxEntry.toUi(): BoxPokemonUi {
        return BoxPokemonUi(
            resourceType = resourceType,
            resourceId = resourceId,
            resourceName = resourceName,
            dropRate = dropRate
        )
    }

    private fun BoxOpenResult.toRewardUi(): BoxPokemonUi {
        return BoxPokemonUi(
            resourceType = reward.resourceType,
            resourceId = reward.resourceId,
            resourceName = reward.resourceName,
            dropRate = reward.dropRate
        )
    }

    companion object {
        val factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return BoxDetailViewModel(
                    getBoxByIdUseCase = GetBoxByIdUseCase(AppContainer.boxRepository),
                    openBoxUseCase = OpenBoxUseCase(AppContainer.boxRepository)
                ) as T
            }
        }
    }
}
