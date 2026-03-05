package fr.pokenity.pokenity.domain.usecase

import fr.pokenity.data.model.BoxOpenResult
import fr.pokenity.data.repository.BoxRepository

class OpenBoxUseCase(
    private val boxRepository: BoxRepository
) {
    suspend operator fun invoke(boxId: String): BoxOpenResult {
        return boxRepository.openBox(boxId)
    }
}
