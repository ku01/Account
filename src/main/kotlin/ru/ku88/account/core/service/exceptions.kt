package ru.ku88.account.core.service

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "Entity is not found")
class NotFoundException : RuntimeException()

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "Reference entity is not found")
class ReferenceNotFoundException : RuntimeException()

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "Insufficient funds")
class InsufficientFundsException : RuntimeException()

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "Maximum value exceeded")
class MaxValueViolationException : RuntimeException()