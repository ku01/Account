package ru.ku88.account.core.jpa

import java.math.BigDecimal
import java.math.RoundingMode
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.validation.constraints.DecimalMax
import javax.validation.constraints.Digits

@Entity
open class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long = 0

    @Digits(integer = 10, fraction = 2)
    @DecimalMax(value = maxBalanceValue.toString())
    var balance: BigDecimal = BigDecimal("0").setScale(2, RoundingMode.DOWN)
}

const val maxBalanceValue = 9999999999.99