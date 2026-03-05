package fr.pokenity.data.model

enum class TradeStatus {
    PENDING,
    WAITING_CONFIRMATION,
    COMPLETED,
    CANCELED,
    DECLINED;

    companion object {
        fun fromString(value: String): TradeStatus {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
                ?: PENDING
        }
    }
}
