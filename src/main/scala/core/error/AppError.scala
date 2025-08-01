package test.ice
package core.error

final case class AppError(message: String)

// Basic predefined types for error handling
object AppError {
  val NotFound: AppError = AppError("Not found")
  val InvalidState: AppError = AppError("Invalid state")
  val Success: AppError = AppError("Success")
}
