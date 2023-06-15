import java.time.LocalDate
import java.util.logging.Logger

fun plusOne(a: Int) = a + 1

val intFun = ::plusOne

fun plusTwo(a: Int) = intFun(intFun(a))

val names = listOf("Bob", "Mary", "Ann", "Fred")

val aName = names.first { it.startsWith("A") }

val plusOneL0 = fun(x: Int): Int { return x + 1 }

val plusOneL1: (Int) -> Int = { x: Int -> x + 1 }

val plusOneL2: (Int) -> Int = { it + 1 }

fun Int.next() = this + 1

val nextFn: Int.() -> Int = Int::next

fun plusX(x: Int): (Int) -> Int = { x + it }

fun plusXv(x: Int): (Int) -> Int {
    return fun(y: Int): Int = x + y
}

class User(
    val name: String,
    val surname: String
) {

    fun initials() = "${name.first()}${surname.first()}"

    companion object {
        fun fromString(fullName: String) =
            fullName.split(" ")
                .let { User(it.get(0), it.get(1)) }
    }
}

open class UserO(
    open val name: String,
    open val surname: String
) {

    open fun initials() = "${name.first()}${surname.first()}"
}

class JapaneseUser(
    override val name: String,
    override val surname: String
) : UserO(name, surname) {

    override fun initials() = "${surname.first()}${name.first()}"
}

data class UserD(
    val name: String,
    val surname: String
)

interface UserStore {
    fun saveUser(user: User)
}

class UserStoreDb(val conn: String) : UserStore {
    override fun saveUser(user: User) {
        TODO()
    }
}

class UserStoreLog(
    private val store: UserStore,
    val logger: Logger
) : UserStore by store {

    override fun saveUser(user: User) {
        logger.info("saving $user")
        store.saveUser(user)
        logger.info("saved  $user")
    }

}

fun isTheAnswer(x: Int) =
    when (x) {
        42 -> "the answer"
        else -> "not the answer"
    }

typealias Bookings = Map<User, List<LocalDate>>

typealias UserId = String

fun findUser(id: UserId): User = TODO()

sealed class MyError()
class GenericError(val msg: String) : MyError()
class HttpError(val status: Int, val response: String) : MyError()
object UnexpectedError : MyError()

fun checkError(e: MyError): String =
    when (e) {
        is GenericError -> e.msg
        is HttpError -> "${e.status}"
        UnexpectedError -> "Unexpected Error!"
    }


tailrec fun recursiveSum(acc: Long, x: Long): Long =
    if (x <= 1) acc else recursiveSum(acc + x, x - 1)

val powersOfTwo = generateSequence(2) { it * 2 }

val maybeAString: String? = null

data class Pizza(val name: String, val price: Double)

infix fun String.costs(price: Double): Pizza = Pizza(this, price)

data class Order(val pizzas: List<Pizza> = emptyList()) {

    operator fun plus(pizza: Pizza): Order {
        return Order(pizzas + pizza)
    }

}

fun main() {

    val mari = "Marinara" costs 5.8
    val capri = "Capricciosa" costs 8.5
    val order = Order() + mari + capri

    println(order)
}