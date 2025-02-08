package studio.codescape.metronome.test

import org.junit.Before
import org.mockito.MockitoAnnotations

abstract class UnitTest {

    @Before
    open fun before() {
        MockitoAnnotations.openMocks(this)
    }

}