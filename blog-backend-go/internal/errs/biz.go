package errs

import "fmt"

// BusinessError is a domain error carrying an HTTP-like code and a message.
type BusinessError struct {
	Code    int
	Message string
}

func (e *BusinessError) Error() string {
	return e.Message
}

// New creates a new BusinessError.
func New(code int, message string) *BusinessError {
	return &BusinessError{Code: code, Message: message}
}

// Newf creates a new BusinessError with formatted message.
func Newf(code int, format string, args ...any) *BusinessError {
	return &BusinessError{Code: code, Message: fmt.Sprintf(format, args...)}
}
