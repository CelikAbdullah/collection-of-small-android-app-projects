package celik.abdullah.authentication.models.form

/**
 * Holds the state of our login procedure.
 * */
data class LoginFormState(
    val emailError: Int? = null,
    val passwordError:Int? = null,
    val isDataValid:Boolean = false,
)