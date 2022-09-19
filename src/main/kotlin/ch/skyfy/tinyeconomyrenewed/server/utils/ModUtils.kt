package ch.skyfy.tinyeconomyrenewed.server.utils

import ch.skyfy.tinyeconomyrenewed.both.TinyEconomyRenewedMod.Companion.CONFIG_DIRECTORY
import ch.skyfy.tinyeconomyrenewed.both.TinyEconomyRenewedMod.Companion.LOGGER
import kotlin.io.path.createDirectory
import kotlin.io.path.exists

/**
 * Just a fun to create a folder named by the [TinyEconomyRenewedMod.MOD_ID] where all others files will be located
 */
fun setupConfigDirectory(){
    try {
        if(!CONFIG_DIRECTORY.exists()) CONFIG_DIRECTORY.createDirectory()
    } catch (e: java.lang.Exception) {
        LOGGER.fatal("An exception occurred. Could not create the root folder that should contain the configuration files")
        throw RuntimeException(e)
    }
}