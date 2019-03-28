package ru.ku88.account.core

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.test.web.servlet.ResultActions
import ru.ku88.account.core.controller.AccountDetails
import ru.ku88.account.core.controller.CreateAccountResponse
import java.math.BigDecimal
import java.math.RoundingMode

fun <T> ResultActions.getEntity(entityClass: Class<T>): T {
    return jacksonObjectMapper().readValue(this.andReturn().response.contentAsString, entityClass)
}

fun ResultActions.getCreateAccountResponse(): CreateAccountResponse {
    return getEntity(CreateAccountResponse::class.java)
}

fun ResultActions.getAccountList(): List<AccountDetails> {
    return jacksonObjectMapper().readValue(this.andReturn().response.contentAsString, object : TypeReference<List<AccountDetails>>(){})
}

fun Double.toCurrencyBigDecimal(): BigDecimal {
    return this.toString().toCurrencyBigDecimal()
}

fun String.toCurrencyBigDecimal(): BigDecimal {
    return BigDecimal(this).setScale(2, RoundingMode.DOWN)
}