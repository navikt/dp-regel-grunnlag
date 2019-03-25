package no.nav.dagpenger.regel.grunnlag

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
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
}