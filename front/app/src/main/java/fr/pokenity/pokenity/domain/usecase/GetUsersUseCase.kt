package fr.pokenity.pokenity.domain.usecase

import fr.pokenity.data.model.UserProfile
import fr.pokenity.data.repository.SocialRepository

class GetUsersUseCase(
    private val socialRepository: SocialRepository
) {
    suspend operator fun invoke(): List<UserProfile> {
        return socialRepository.getUsers()
    }
}
