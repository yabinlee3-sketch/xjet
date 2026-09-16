package io.github.xjet.spi

import java.util.concurrent.CopyOnWriteArrayList
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ExceptionInterceptorTest {

    data class Captured(val source: String, val throwable: Throwable, val severity: ErrorSeverity)

    private class RecordingInterceptor : ExceptionInterceptor {
        val captured = CopyOnWriteArrayList<Captured>()
        override fun onError(source: String, throwable: Throwable, severity: ErrorSeverity) {
            captured += Captured(source, throwable, severity)
        }
    }

    @Test
    fun tryCatchReportsAndReturnsNullOnFailure() {
        val interceptor = RecordingInterceptor()
        val result = interceptor.tryCatch("vm.load") {
            error("boom")
        }
        assertNull(result)
        assertEquals(1, interceptor.captured.size)
        assertEquals("vm.load", interceptor.captured[0].source)
        assertEquals(ErrorSeverity.ERROR, interceptor.captured[0].severity)
        assertEquals("boom", interceptor.captured[0].throwable.message)
    }

    @Test
    fun tryCatchReturnsValueWhenBlockSucceeds() {
        val interceptor = RecordingInterceptor()
        val result = interceptor.tryCatch("cache.read", ErrorSeverity.WARNING) { 42 }
        assertEquals(42, result)
        assertEquals(0, interceptor.captured.size)
    }

    @Test
    fun logInterceptorAcceptsAndCatches() {
        val result = LogExceptionInterceptor().tryCatch("log.only") { throw IllegalStateException("handled") }
        assertNull(result)
        val ok = LogExceptionInterceptor().tryCatch("log.ok") { "value" }
        assertNotNull(ok)
    }
}
