package io.github.aedev.flow.update

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class UpdateVersionTest {
    @Test
    fun `normalize strips v prefix and pre-release suffix`() {
        assertThat(UpdateVersion.normalize("v1.3.0")).isEqualTo("1.3.0")
        assertThat(UpdateVersion.normalize("1.3.0-playstore")).isEqualTo("1.3.0")
        assertThat(UpdateVersion.normalize("1.3.0-preview.1")).isEqualTo("1.3.0")
        assertThat(UpdateVersion.normalize(" 2.0.0 ")).isEqualTo("2.0.0")
    }

    @Test
    fun `isNewer detects a higher remote version`() {
        assertThat(UpdateVersion.isNewer("1.0.1", "1.0.0")).isTrue()
        assertThat(UpdateVersion.isNewer("2.0.0", "1.9.9")).isTrue()
    }

    @Test
    fun `isNewer is false for equal or older versions`() {
        assertThat(UpdateVersion.isNewer("1.0.0", "1.0.0")).isFalse()
        assertThat(UpdateVersion.isNewer("1.0.0", "1.0.1")).isFalse()
        assertThat(UpdateVersion.isNewer("1.0.0", "2.0.0")).isFalse()
    }

    @Test
    fun `isNewer pads missing parts with zero`() {
        assertThat(UpdateVersion.isNewer("1.0.1", "1.0")).isTrue()
        assertThat(UpdateVersion.isNewer("1.0", "1.0.0")).isFalse()
        assertThat(UpdateVersion.isNewer("1.1", "1.0.9")).isTrue()
    }

    @Test
    fun `isNewer tolerates non-numeric parts`() {
        assertThat(UpdateVersion.isNewer("1.0.x", "1.0.0")).isFalse()
        assertThat(UpdateVersion.isNewer("1.0.x", "0.9.0")).isTrue()
    }

    @Test
    fun `isNewer compares suffix-stripped tags`() {
        assertThat(UpdateVersion.isNewer("v2.0.0-beta", "1.9.0")).isTrue()
        assertThat(UpdateVersion.isNewer("v2.0.0-beta", "2.0.0")).isFalse()
    }
}
