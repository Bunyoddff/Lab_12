package task15

enum class FailureReason {
    WRONG_PASSWORD, USER_NOT_FOUND, NO_NETWORK, TOO_MANY_ATTEMPTS
}

sealed interface LoginResult {
    data class Success(val userName: String) : LoginResult
    data class Failure(val reason: FailureReason) : LoginResult
    data class Locked(val minutes: Int) : LoginResult
    data object Loading : LoginResult
}

fun login(user: String, password: String): LoginResult = when {
    user.isBlank() -> LoginResult.Failure(FailureReason.USER_NOT_FOUND)
    password != "1234" -> LoginResult.Failure(FailureReason.WRONG_PASSWORD)
    else -> LoginResult.Success(user)
}

fun message(result: LoginResult): String = when (result) {
    is LoginResult.Success -> "Welcome, ${result.userName}!"
    is LoginResult.Failure -> when (result.reason) {
        FailureReason.WRONG_PASSWORD -> "Wrong password"
        FailureReason.USER_NOT_FOUND -> "No such user"
        FailureReason.NO_NETWORK -> "Check your connection"
        FailureReason.TOO_MANY_ATTEMPTS -> "Too many attempts. Please try again later."
    }
    is LoginResult.Locked -> "Try again in ${result.minutes} minutes"
    LoginResult.Loading -> "Please wait..."
}

fun main() {
    println(message(login("ali", "1234")))
    println(message(login("ali", "0000")))
    println(message(login("", "1234")))
    println(message(LoginResult.Loading))
    println(message(LoginResult.Locked(5)))
}

