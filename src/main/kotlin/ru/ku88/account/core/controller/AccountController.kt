package ru.ku88.account.core.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import ru.ku88.account.core.service.account.AccountService
import javax.validation.Valid

@RestController
@RequestMapping("/accounts")
class AccountController @Autowired constructor(private val accountService: AccountService) {

    @PostMapping("/{accountId}/transfer")
    fun transfer(@PathVariable accountId: Long,
                 @Valid @RequestBody transferRequest: TransferRequest) {
        accountService.transfer(accountId, transferRequest)
    }

    @PostMapping("/{accountId}/deposit")
    fun deposit(@PathVariable accountId: Long,
                @Valid @RequestBody depositRequest: DepositRequest) {
        accountService.deposit(accountId, depositRequest)
    }

    @PostMapping("/{accountId}/withdraw")
    fun withdraw(@PathVariable accountId: Long,
                 @Valid @RequestBody withdrawRequest: WithdrawRequest) {
        return accountService.withdraw(accountId, withdrawRequest)
    }

    @GetMapping
    fun getAllAccounts(): List<AccountDetails> {
        return accountService.getAllAccounts()
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createAccount(): CreateAccountResponse {
        return accountService.createAccount()
    }
}