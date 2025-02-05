package studio.codescape.metronome.common

sealed interface State<T> {
    @JvmInline
    value class Ready<T>(val value: T) : State<T>

    data object Loading : State<Nothing>

    @JvmInline
    value class Failure<E>(val cause: E) : State<Nothing>
}