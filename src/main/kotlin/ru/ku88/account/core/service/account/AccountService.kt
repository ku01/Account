package ru.ku88.account.core.service.account

import ru.ku88.account.core.controller.*

interface AccountService {
    fun getAllAccounts(): List<AccountDetails>
    fun transfer(accountId: Long, transferRequest: TransferRequest)
    fun deposit(accountId: Long, depositRequest: DepositRequest)
    fun withdraw(accountId: Long, withdrawRequest: WithdrawRequest)
    fun createAccount(): CreateAccountResponse
}