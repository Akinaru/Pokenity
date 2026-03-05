package fr.pokenity.pokenity.domain.usecase

import fr.pokenity.data.model.LootBox
import fr.pokenity.data.repository.BoxRepository

class GetBoxesUseCase(
    private val boxRepository: BoxRepository
) {
    suspend operator fun invoke(): List<LootBox> {
        return boxRepository.getBoxes()
    }
}
