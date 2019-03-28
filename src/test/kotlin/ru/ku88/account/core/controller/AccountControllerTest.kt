package ru.ku88.account.core.controller

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultMatcher
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import ru.ku88.account.AccountApplication
import ru.ku88.account.core.getAccountList
import ru.ku88.account.core.getCreateAccountResponse
import ru.ku88.account.core.jpa.repository.AccountRepository
import ru.ku88.account.core.toCurrencyBigDecimal

@RunWith(SpringRunner::class)
@SpringBootTest(classes = [AccountApplication::class])
@AutoConfigureMockMvc
class AccountControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var accountRepository: AccountRepository

    @Before
    fun setup() {
        accountRepository.deleteAll()
    }

    @Test
    fun testPostAndGetAccounts() {
        val accountsBefore = getAccountList()
        assertTrue(accountsBefore.isEmpty())

        val createAccountResponse = createAccount()

        val accountsAfter = getAccountList()
        assertEquals(1, accountsAfter.size)
        assertEquals(createAccountResponse.id, accountsAfter[0].id)
        assertEquals("0.00", accountsAfter[0].balance.toString())
    }

    @Test
    fun testDeposit() {
        val accountId = createAccount().id

        deposit(accountId, 120.0)
        assertEquals("120.00", getAccountList()[0].balance.toString())

        deposit(accountId, 10.0)
        assertEquals("130.00", getAccountList()[0].balance.toString())
    }

    @Test
    fun testDepositNegativeSum() {
        val accountId = createAccount().id
        deposit(accountId, -10.0, status().isBadRequest)
    }

    @Test
    fun testWithdraw() {
        val accountId = createAccount().id

        deposit(accountId, 100.0)
        assertEquals("100.00", getAccountList()[0].balance.toString())
        withdraw(accountId, 30.0)
        assertEquals("70.00", getAccountList()[0].balance.toString())
    }

    @Test
    fun testWithdrawNegativeSum() {
        val accountId = createAccount().id

        deposit(accountId, 100.0)
        assertEquals("100.00", getAccountList()[0].balance.toString())
        withdraw(accountId, -30.0, status().isBadRequest)
    }

    @Test
    fun testTransfer() {
        val account1Id = createAccount().id
        val account2Id = createAccount().id
        deposit(account1Id, 100.0)

        val accountsInit = getAccountList()
        assertEquals("100.00", accountsInit.first { it.id == account1Id }.balance.toString())
        assertEquals("0.00", accountsInit.first { it.id == account2Id }.balance.toString())

        transfer(account1Id, account2Id, 30.0)

        val accountsAfter1Transfer = getAccountList()
        assertEquals("70.00", accountsAfter1Transfer.first { it.id == account1Id }.balance.toString())
        assertEquals("30.00", accountsAfter1Transfer.first { it.id == account2Id }.balance.toString())

        transfer(account2Id, account1Id, 10.0)

        val accountsAfter2Transfer = getAccountList()
        assertEquals("80.00", accountsAfter2Transfer.first { it.id == account1Id }.balance.toString())
        assertEquals("20.00", accountsAfter2Transfer.first { it.id == account2Id }.balance.toString())
    }

    @Test
    fun testTransferNegativeSum() {
        val account1Id = createAccount().id
        val account2Id = createAccount().id
        deposit(account1Id, 100.0)
        transfer(account1Id, account2Id, -1.0,
                status().isBadRequest)
    }

    @Test
    fun testAccountNotFound() {
        deposit(10, 100.0, status().isNotFound)
        withdraw(11, 100.0, status().isNotFound)
        transfer(12, 13, 100.0, status().isNotFound)
    }

    @Test
    fun testReferenceNotFound() {
        val account1Id = createAccount().id
        deposit(account1Id, 100.0)
        transfer(account1Id, -100, 100.0, status().isBadRequest)
    }

    @Test
    fun testWithdrawMoreThanAccountHas() {
        val accountId = createAccount().id

        deposit(accountId, 100.0)
        assertEquals("100.00", getAccountList()[0].balance.toString())
        withdraw(accountId, 120.0, status().isBadRequest)
        assertEquals("100.00", getAccountList()[0].balance.toString())
    }

    @Test
    fun testTransferMoreThanAccountHas() {
        val account1Id = createAccount().id
        val account2Id = createAccount().id
        deposit(account1Id, 100.0)
        transfer(account1Id, account2Id, 100.01, status().isBadRequest)
    }

    @Test
    fun testFloatingPointCalculationsTransfer() {
        assertEquals(61, 103 - 42)
        assertNotEquals(0.61, 1.03 - 0.42, 0.0) // floating point calculations accuracy problem
        assertTrue(0.61 < 1.03 - 0.42)

        val account1Id = createAccount().id
        val account2Id = createAccount().id
        deposit(account1Id, 1.03)

        transfer(account1Id, account2Id, 0.42)
        val accounts = getAccountList()
        assertEquals("0.61", accounts.first { it.id == account1Id }.balance.toString())
        assertEquals("0.42", accounts.first { it.id == account2Id }.balance.toString())
    }

    @Test
    fun testFloatingPointCalculationsDeposit() {
        assertEquals(47, 46 + 1)
        assertNotEquals(4.7, 4.6 + 0.1, 0.0) // floating point calculations accuracy problem
        assertTrue(4.7 > 4.6 + 0.1)

        val accountId = createAccount().id
        deposit(accountId, 4.6)
        deposit(accountId, 0.1)

        assertEquals("4.70", getAccountList()[0].balance.toString())
    }

    @Test
    fun testDigitsAfterDecimalPoint() {
        val accountId = createAccount().id
        mockMvc.perform(post("/accounts/$accountId/deposit")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"sum\":200.7399}"))
                .andDo(print())
                .andExpect(status().isBadRequest)
    }

    @Test
    fun testSetOverMaxBalance() {
        val account1Id = createAccount().id
        val bigValue = "9999999999.00"
        deposit(account1Id, bigValue.toDouble())
        assertEquals(bigValue, getAccountList()[0].balance.toString())
        deposit(account1Id, 1.0, status().isBadRequest)
        assertEquals(bigValue, getAccountList()[0].balance.toString())

        val account2Id = createAccount().id
        deposit(account2Id, 100.0)
        transfer(account1Id, account2Id, bigValue.toDouble(), status().isBadRequest)

        val accounts = getAccountList()
        assertEquals(bigValue, accounts.first { it.id == account1Id }.balance.toString())
        assertEquals("100.00", accounts.first { it.id == account2Id }.balance.toString())
    }

    @Test
    fun testConcurrentDeposit() {
        val accountId = createAccount().id

        val threads = List(50) {
            Thread { deposit(accountId, 100.0) }
        }
        threads.forEach { it.start() }
        threads.forEach { it.join() }

        assertEquals("5000.00", getAccountList().first().balance.toString())
    }

    @Test
    fun testConcurrentDepositAndWithdraw() {
        val accountId = createAccount().id
        deposit(accountId, 10000.0)

        val threads = MutableList(25) {
            Thread { deposit(accountId, 100.0) }
        }
        threads += List(20) {
            Thread { withdraw(accountId, 100.0) }
        }
        threads.forEach { it.start() }
        threads.forEach { it.join() }

        assertEquals("10500.00", getAccountList().first().balance.toString())
    }

    @Test
    fun testConcurrentTransfers() {
        val account1Id = createAccount().id
        val account2Id = createAccount().id

        deposit(account1Id, 1000.0)
        deposit(account2Id, 1000.0)

        val threads = MutableList(25) {
            Thread { transfer(account1Id, account2Id, 10.0) }
        }
        threads += List(20) {
            Thread { transfer(account2Id, account1Id, 10.0) }
        }
        threads.forEach { it.start() }
        threads.forEach { it.join() }

        val accounts = getAccountList()
        assertEquals("950.00", accounts.first { it.id == account1Id }.balance.toString())
        assertEquals("1050.00", accounts.first { it.id == account2Id }.balance.toString())
    }

    private fun getAccountList(): List<AccountDetails> {
        return mockMvc.perform(get("/accounts"))
                .andDo(print())
                .andExpect(status().isOk)
                .getAccountList()
    }

    private fun createAccount(): CreateAccountResponse {
        return mockMvc.perform(post("/accounts"))
                .andDo(print())
                .andExpect(status().isCreated)
                .getCreateAccountResponse()
    }

    private fun deposit(accountId: Long, sum: Double, resultMatcher: ResultMatcher = status().isOk) {
        mockMvc.perform(post("/accounts/$accountId/deposit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jacksonObjectMapper().writeValueAsString(DepositRequest(sum.toCurrencyBigDecimal()))))
                .andDo(print())
                .andExpect(resultMatcher)
    }

    private fun withdraw(accountId: Long, sum: Double, resultMatcher: ResultMatcher = status().isOk) {
        mockMvc.perform(post("/accounts/$accountId/withdraw")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jacksonObjectMapper().writeValueAsString(DepositRequest(sum.toCurrencyBigDecimal()))))
                .andDo(print())
                .andExpect(resultMatcher)
    }

    private fun transfer(account1Id: Long, account2Id: Long, sum: Double, resultMatcher: ResultMatcher = status().isOk) {
        mockMvc.perform(post("/accounts/$account1Id/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jacksonObjectMapper().writeValueAsString(TransferRequest(account2Id, sum.toCurrencyBigDecimal()))))
                .andDo(print())
                .andExpect(resultMatcher)
    }
}