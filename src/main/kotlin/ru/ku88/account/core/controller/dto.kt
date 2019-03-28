package ru.ku88.account.core.controller

import java.math.BigDecimal
import javax.validation.constraints.DecimalMin
import javax.validation.constraints.Digits
import javax.validation.constraints.NotNull

data class AccountDetails(
        val id: Long,
        val balance: BigDecimal
)

data class TransferRequest(
        @field:NotNull
        val accountId: Long,

        @field:DecimalMin("0.01")
        @field:Digits(integer = 10, fraction = 2)
        val sum: BigDecimal
)

data class DepositRequest(
        @field:DecimalMin("0.01")
        @field:Digits(integer = 10, fraction = 2)
        val sum: BigDecimal
)

data class WithdrawRequest(
        @field:DecimalMin("0.01")
        @field:Digits(integer = 10, fraction = 2)
        val sum: BigDecimal
)

data class CreateAccountResponse(
        val id: Long
)