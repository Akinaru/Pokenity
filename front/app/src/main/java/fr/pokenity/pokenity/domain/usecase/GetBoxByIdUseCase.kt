package fr.pokenity.pokenity.domain.usecase

import fr.pokenity.data.model.LootBox
import fr.pokenity.data.repository.BoxRepository

class GetBoxByIdUseCase(
    private val boxRepository: BoxRepository
) {
    suspend operator fun invoke(boxId: String): LootBox {
        return boxRepository.getBoxById(boxId)
    }
}
