package us.dragonma.backgrounds.update

internal class ValidateException : Exception {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}
