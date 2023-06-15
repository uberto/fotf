package com.ubertob.fotf.zettai.fp

import com.ubertob.fotf.zettai.domain.tooling.expectFailure
import com.ubertob.fotf.zettai.fp.Outcome.Companion.transform2Failures
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class OutcomeTest {

    data class Error1(override val msg: String) : OutcomeError
    data class Error2(override val msg: String) : OutcomeError
    data class Error3(override val msg: String) : OutcomeError


    @Test
    fun `combine failures`() {

        val err1 = Error1("e1").asFailure()
        val err2 = Error2("e2").asFailure()

        val err3 = transform2Failures(err1, err2) { e1, e2 -> Error3(e1.msg + e2.msg) }.expectFailure()

        expectThat(err3.msg).isEqualTo("e1e2")


//        un <E1 : OutcomeError, E2 : OutcomeError, E3 : OutcomeError, T> transform2Failures(
//            first: Outcome<E1, T>,
//            second: Outcome<E2, T>,
//            f: (E1, E2) -> E3): Outcome<E3, T> =
//            first.bindFailure { a ->
//                second.transformFailure { b ->
//                    f(a, b)
//                }
//            }
    }


    @Test
    fun `combine failure with success`() {

        val suc: Outcome<Error1, Int> = 42.asSuccess()
        val err = Error2("err").asFailure()

        val sucErr = transform2Failures(suc, err) { e1, e2 -> Error3(e1.msg + e2.msg) }.expectFailure()

        expectThat(sucErr.msg).isEqualTo("err")

        val errSuc = transform2Failures(err, suc) { e1, e2 -> Error3(e1.msg + e2.msg) }.expectFailure()

        expectThat(errSuc.msg).isEqualTo("err")

    }
}