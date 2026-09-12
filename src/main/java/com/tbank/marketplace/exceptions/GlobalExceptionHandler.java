package com.tbank.marketplace.exceptions;


import com.tbank.marketplace.exceptions.auth_exceptions.InvalidCredentialsException;
import com.tbank.marketplace.exceptions.auth_exceptions.InvalidRefreshTokenException;
import com.tbank.marketplace.exceptions.auth_exceptions.UserAlreadyExistsException;
import com.tbank.marketplace.exceptions.order_exceptions.*;
import com.tbank.marketplace.exceptions.product_exceptions.ProductInactiveException;
import com.tbank.marketplace.exceptions.product_exceptions.ProductNotFoundException;
import com.tbank.marketplace.exceptions.promo_exceptions.PromoCodeAlreadyExistsException;
import com.tbank.marketplace.exceptions.promo_exceptions.PromoCodeInvalidException;
import com.tbank.marketplace.exceptions.promo_exceptions.PromoCodeMinAmountException;
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

    @ExceptionHandler({RateLimitingCreateOrderException.class, RateLimitingUpdateOrderException.class})
    public ResponseEntity<ErrorResponse> handleRateLimitingCreateOrderException(Exception e){
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
        errorResponse.setErrorCode("PRODUCT_INACTIVE");
        errorResponse.setMessage(e.getMessage());
        errorResponse.setDetails(JsonNullable.undefined());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(PromoCodeInvalidException.class)
    public ResponseEntity<ErrorResponse> handlePromoCodeInvalidException(PromoCodeInvalidException e){
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setErrorCode("PROMO_CODE_INVALID");
        errorResponse.setMessage(e.getMessage());
        errorResponse.setDetails(JsonNullable.undefined());

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT).body(errorResponse);
    }

    @ExceptionHandler(PromoCodeMinAmountException.class)
    public ResponseEntity<ErrorResponse> handlePromoCodeMinAmountException(PromoCodeMinAmountException e){
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setErrorCode("PROMO_CODE_MIN_AMOUNT");
        errorResponse.setMessage(e.getMessage());
        errorResponse.setDetails(JsonNullable.undefined());

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT).body(errorResponse);
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientStockException(InsufficientStockException e){
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setErrorCode("INSUFFICIENT_STOCK");
        errorResponse.setMessage(e.getMessage());
        errorResponse.setDetails(JsonNullable.of(e.getErrors()));

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleProductNotFoundException(ProductNotFoundException e){
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setErrorCode("PRODUCT_NOT_FOUND");
        errorResponse.setMessage(e.getMessage());
        errorResponse.setDetails(JsonNullable.undefined());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleOrderNotFoundException(OrderNotFoundException e){
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setErrorCode("ORDER_NOT_FOUND");
        errorResponse.setMessage(e.getMessage());
        errorResponse.setDetails(JsonNullable.undefined());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(OrderOwnershipViolationException.class)
    public ResponseEntity<ErrorResponse> handleOrderOwnershipViolationException(OrderOwnershipViolationException e){
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setErrorCode("ORDER_OWNERSHIP_VIOLATION");
        errorResponse.setMessage(e.getMessage());
        errorResponse.setDetails(JsonNullable.undefined());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    @ExceptionHandler(InvalidStateTransitionException.class)
    public ResponseEntity<ErrorResponse> handleInvalidStateTransitionException(InvalidStateTransitionException e){
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setErrorCode("INVALID_STATE_TRANSITION");
        errorResponse.setMessage(e.getMessage());
        errorResponse.setDetails(JsonNullable.undefined());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(PromoCodeAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handlePromoCodeAlreadyExistsException(PromoCodeAlreadyExistsException e){
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setErrorCode("PROMOCODE_ALREADY_EXISTS");
        errorResponse.setMessage(e.getMessage());
        errorResponse.setDetails(JsonNullable.undefined());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e){
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setErrorCode("BAD_REQUEST");
        errorResponse.setMessage(e.getMessage());
        errorResponse.setDetails(JsonNullable.undefined());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ErrorResponse> handleSecurityException(SecurityException e){
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setErrorCode("PRODUCT_OWNERSHIP_VIOLATION");
        errorResponse.setMessage(e.getMessage());
        errorResponse.setDetails(JsonNullable.undefined());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    private ErrorResponse createErrorResponse(String code, String message, JsonNullable<Object> details){
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setErrorCode(code);
        errorResponse.setMessage(message);
        errorResponse.setDetails(details);

        return errorResponse;
    };

}
