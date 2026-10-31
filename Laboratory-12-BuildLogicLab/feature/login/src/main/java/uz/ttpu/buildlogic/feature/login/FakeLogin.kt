package uz.ttpu.buildlogic.feature.login

import kotlinx.coroutines.delay

suspend fun fakeLogin(): Boolean {
    delay(500)
    return true
}

