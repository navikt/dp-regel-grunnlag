package no.nav.dagpenger.regel.grunnlag

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.math.MathContext
import java.math.RoundingMode
import java.time.LocalDate
import java.time.Month
import java.time.YearMonth

class FinnGrunnbeløpTest{

    @Test
    fun ` Skal returnere grunnbeløp på 96883 for måned april 2019 `(){

        assertEquals( 96883.toBigDecimal(), getGrunnbeløpForMåned(YearMonth.of(2019, Month.APRIL)))

    }

    @Test
    fun ` Skal returnere grunnbeløp på 93634 for måned mars 2018 `(){

        assertEquals( 93634.toBigDecimal(), getGrunnbeløpForMåned(YearMonth.of(2018, Month.MARCH)))

    }

    @Test
    fun ` Skal returnere grunnbeløp på 92576 for måned mai 2016  `(){

        assertEquals( 92576.toBigDecimal(), getGrunnbeløpForMåned(YearMonth.of(2016, Month.MAY)))

    }

    @Test
    fun ` Skal returnere grunnbeløp på 90068 for måned mars 2015 `(){

        assertEquals( 90068.toBigDecimal(), getGrunnbeløpForMåned(YearMonth.of(2015, Month.AUGUST)))

    }

    @Test
    fun `Skal finne faktoren mellom to Grunnbeløp `(){

        val grunnbeløp = Grunnbeløp(LocalDate.now(), 1000.toBigDecimal())
        val gjeldendeGrunnbeløp = Grunnbeløp(LocalDate.now(), 2000.toBigDecimal())

        assertEquals(2.toBigDecimal(), gjeldendeGrunnbeløp.faktorMellom(grunnbeløp))

    }

    @Test
    fun `Skal finne faktoren mellom to Grunnbeløp med desimaler`(){

        val grunnbeløp = Grunnbeløp(LocalDate.now(), 93634.toBigDecimal())
        val gjeldendeGrunnbeløp = Grunnbeløp(LocalDate.now(), 96883.toBigDecimal())

        assertEquals(1.034699.toBigDecimal(), gjeldendeGrunnbeløp.faktorMellom(grunnbeløp))

    }
}