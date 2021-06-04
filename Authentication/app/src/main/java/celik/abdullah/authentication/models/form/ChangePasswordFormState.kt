package celik.abdullah.authentication.models.form

/**
 * Holds the state of our change password procedure.
 * */
data class ChangePasswordFormState(
    val emailError: Int? = null,
    val newPasswordError: Int? = null,
    val isDataValid: Boolean = false
)