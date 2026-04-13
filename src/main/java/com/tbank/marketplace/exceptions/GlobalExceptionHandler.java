package com.tbank.marketplace.exceptions;


import com.tbank.marketplace.exceptions.auth_exceptions.InvalidCredentialsException;
import com.tbank.marketplace.exceptions.auth_exceptions.InvalidRefreshTokenException;
import com.tbank.marketplace.exceptions.auth_exceptions.UserAlreadyExistsException;
import com.tbank.marketplace.exceptions.order_exceptions.*;
import com.tbank.marketplace.model.ErrorResponse;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExistsException(UserAlreadyExistsException e){
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setErrorCode("USER_ALREADY_EXISTS");
        errorResponse.setMessage("Ваш запрос невалиден: введите другой email");
        errorResponse.setDetails(JsonNullable.of("Данный email уже зарегистрирован в системе"));
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentialsException(InvalidCredentialsException e){
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setErrorCode("INVALID_CREDENTIALS");
        errorResponse.setMessage("Неверные данные для входа в систему");
        errorResponse.setDetails(JsonNullable.undefined());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRefreshException(InvalidRefreshTokenException e){
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setErrorCode("REFRESH_TOKEN_INVALID");
        errorResponse.setMessage("Отправлен невалидный refresh-токен");
        errorResponse.setDetails(JsonNullable.of(e.getMessage()));

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(RateLimitingCreateOrderException.class)
    public ResponseEntity<ErrorResponse> handleRateLimitingCreateOrderException(RateLimitingCreateOrderException e){
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setErrorCode("ORDER_LIMIT_EXCEEDED");
        errorResponse.setMessage(e.getMessage());
        errorResponse.setDetails(JsonNullable.undefined());

        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(errorResponse);
    }

    @ExceptionHandler(OrderHasActiveException.class)
    public ResponseEntity<ErrorResponse> handleHasActiveOrderException(OrderHasActiveException e){
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setErrorCode("ORDER_HAS_ACTIVE");
        errorResponse.setMessage(e.getMessage());
        errorResponse.setDetails(JsonNullable.undefined());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(ProductInactiveException.class)
    public ResponseEntity<ErrorResponse> handleProductInactiveException(ProductInactiveException e){
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setErrorCode("ORDER_HAS_ACTIVE");
        errorResponse.setMessage(e.getMessage());
        errorResponse.setDetails(JsonNullable.undefined());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(PromoCodeInvalidException.class)
    public ResponseEntity<ErrorResponse> handlePromoCodeInvalidException(PromoCodeInvalidException e){
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setErrorCode("ORDER_HAS_ACTIVE");
        errorResponse.setMessage(e.getMessage());
        errorResponse.setDetails(JsonNullable.undefined());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(PromoCodeMinAmountException.class)
    public ResponseEntity<ErrorResponse> handlePromoCodeMinAmountException(PromoCodeMinAmountException e){
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setErrorCode("ORDER_HAS_ACTIVE");
        errorResponse.setMessage(e.getMessage());
        errorResponse.setDetails(JsonNullable.undefined());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientStockException(InsufficientStockException e){
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setErrorCode("ORDER_HAS_ACTIVE");
        errorResponse.setMessage(e.getMessage());
        errorResponse.setDetails(JsonNullable.undefined());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }
}
