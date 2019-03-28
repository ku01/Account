package ru.ku88.account.core.service.account

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.ku88.account.core.controller.*
import ru.ku88.account.core.jpa.Account
import ru.ku88.account.core.jpa.maxBalanceValue
import ru.ku88.account.core.jpa.repository.AccountRepository
import ru.ku88.account.core.service.InsufficientFundsException
import ru.ku88.account.core.service.MaxValueViolationException
import ru.ku88.account.core.service.NotFoundException
import ru.ku88.account.core.service.ReferenceNotFoundException
import java.math.BigDecimal

@Service
@Transactional
class AccountServiceImpl @Autowired constructor(private val accountRepository: AccountRepository) : AccountService {

    override fun transfer(accountId: Long, transferRequest: TransferRequest) {
        val accounts = accountRepository.findByIdIn(setOf(accountId, transferRequest.accountId))
        val accountFrom = accounts.firstOrNull { it.id == accountId } ?: throw NotFoundException()
        val accountTo = accounts.firstOrNull { it.id == transferRequest.accountId } ?: throw ReferenceNotFoundException()
        if (accountFrom.balance < transferRequest.sum) {
            throw InsufficientFundsException()
        }
        val newValue = accountTo.balance + transferRequest.sum
        if (newValue > BigDecimal(maxBalanceValue)) {
            throw MaxValueViolationException()
        }
        accountTo.balance = newValue
        accountFrom.balance -= transferRequest.sum
        accountRepository.save(accountFrom)
        accountRepository.save(accountTo)
    }

    override fun deposit(accountId: Long, depositRequest: DepositRequest) {
        val account = accountRepository.findById(accountId).orElseThrow { NotFoundException() }
        val newValue = account.balance + depositRequest.sum
        if (newValue > BigDecimal(maxBalanceValue)) {
            throw MaxValueViolationException()
        }
        account.balance = newValue
        accountRepository.save(account)
    }

    override fun withdraw(accountId: Long, withdrawRequest: WithdrawRequest) {
        val account = accountRepository.findById(accountId).orElseThrow { NotFoundException() }
        val withdrawSum = withdrawRequest.sum
        if (account.balance < withdrawSum) {
            throw InsufficientFundsException()
        }
        account.balance -= withdrawSum
        accountRepository.save(account)
    }

    override fun createAccount(): CreateAccountResponse {
        return CreateAccountResponse(accountRepository.save(Account()).id)
    }

    override fun getAllAccounts(): List<AccountDetails> {
        val accounts = accountRepository.findAll()
        return accounts.map { AccountDetails(it.id, it.balance) }
    }
}
