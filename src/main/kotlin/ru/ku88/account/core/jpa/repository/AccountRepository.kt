package ru.ku88.account.core.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import ru.ku88.account.core.jpa.Account
import java.util.*
import javax.persistence.LockModeType

interface AccountRepository : JpaRepository<Account, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    override fun findById(id: Long): Optional<Account>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun findByIdIn(id: Set<Long>): List<Account>
}