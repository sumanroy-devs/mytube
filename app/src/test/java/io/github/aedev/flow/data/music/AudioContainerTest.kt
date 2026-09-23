package io.github.aedev.flow.data.music

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AudioContainerTest {
    @Test
    fun `video mp4 stream maps to m4a`() {
        assertThat(audioContainerFor("""video/mp4; codecs="mp4a.40.2""""))
            .isEqualTo(AudioContainer("m4a", "audio/mp4"))
    }

    @Test
    fun `audio mp4 stream maps to m4a`() {
        assertThat(audioContainerFor("""audio/mp4; codecs="mp4a.40.2""""))
            .isEqualTo(AudioContainer("m4a", "audio/mp4"))
    }

    @Test
    fun `webm stream maps to webm`() {
        assertThat(audioContainerFor("""audio/webm; codecs="opus""""))
            .isEqualTo(AudioContainer("webm", "audio/webm"))
    }

    @Test
    fun `mp4 match wins over codec parameters`() {
        assertThat(audioContainerFor("""video/mp4; codecs="mp4a.40.5"; bitrate=128000"""))
            .isEqualTo(AudioContainer("m4a", "audio/mp4"))
    }

    @Test
    fun `unknown mime falls back to m4a defaults`() {
        assertThat(audioContainerFor(""))
            .isEqualTo(AudioContainer("m4a", "audio/mp4"))
    }
}
