package ch.skyfy.tinyeconomyrenewed.utils

import ch.skyfy.tinyeconomyrenewed.TinyEconomyRenewedMod
import ch.skyfy.tinyeconomyrenewed.exceptions.TinyEconomyModException
import kotlin.io.path.createDirectory
import kotlin.io.path.exists

fun setupConfigDirectory(){
    try {
        if(!TinyEconomyRenewedMod.CONFIG_DIRECTORY.exists()) TinyEconomyRenewedMod.CONFIG_DIRECTORY.createDirectory()
    } catch (e: java.lang.Exception) {
        TinyEconomyRenewedMod.LOGGER.fatal("An exception occurred. Could not create the root folder that should contain the configuration files")
        throw TinyEconomyModException(e)
    }
}