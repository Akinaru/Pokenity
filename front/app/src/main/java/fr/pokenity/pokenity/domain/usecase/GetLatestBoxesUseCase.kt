package fr.pokenity.pokenity.domain.usecase

import fr.pokenity.data.model.LootBox
import fr.pokenity.data.repository.BoxRepository

class GetLatestBoxesUseCase(
    private val boxRepository: BoxRepository
) {
    suspend operator fun invoke(limit: Int = 2): List<LootBox> {
        return boxRepository.getBoxes().take(limit.coerceAtLeast(0))
    }
}
