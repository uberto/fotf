package com.ubertob.fotf.zettai.fp

import com.ubertob.fotf.zettai.domain.tooling.randomText
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import kotlin.random.Random

class MonadLawsTest {

    @Test
    fun `List left identity`() {

        val a = Random.nextInt()
        val f: (Int) -> List<Int> = { x -> listOf(x * 2) }
        val ma = listOf(a).flatMap(f)

        expectThat(ma).isEqualTo(f(a))

    }


    @Test
    fun `List right identity`() {

        val a = randomText(10)

        val ma = listOf(a).flatMap { listOf(it) }

        expectThat(ma).isEqualTo(listOf(a))

    }

    @Test
    fun `List associativity`() {
        val a = Random.nextInt()
        val f: (Int) -> List<Int> = { listOf(it * 2) }
        val g: (Int) -> List<Int> = { listOf(it + 5) }

        val ma1 = listOf(a).flatMap(f).flatMap(g)
        val ma2 = listOf(a).flatMap { x -> f(x).flatMap(g) }

        expectThat(ma1).isEqualTo(ma2)

    }


    data class TestError(override val msg: String) : OutcomeError

    @Test
    fun `Outcome left identity`() {

        val a = Random.nextInt()
        val f: (Int) -> Outcome<OutcomeError, Int> = { (it * 2).asSuccess() }

        val ma = a.asSuccess().bind(f)

        expectThat(ma).isEqualTo(f(a))

    }


    @Test
    fun `Outcome right identity`() {
        val a = randomText(10)

        val ma = a.asSuccess().bind { it.asSuccess() }

        expectThat(ma).isEqualTo(a.asSuccess())

    }

    @Test
    fun `Outcome associativity`() {
        val a = Random.nextInt()
        val f: (Int) -> Outcome<OutcomeError, Int> = { (it * 2).asSuccess() }
        val g: (Int) -> Outcome<OutcomeError, Int> = { (it + 5).asSuccess() }

        val ma1 = a.asSuccess().bind(f).bind(g)
        val ma2 = a.asSuccess().bind { x -> f(x).bind(g) }

        expectThat(ma1).isEqualTo(ma2)

    }

}