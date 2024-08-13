package utils

import logger
import java.io.File

private fun <R> fileWalk(
    rootWalking: WalkingDir<R>,
    walkDir: (dir: File, walkingDir: WalkingDir<R>) -> R,
    walkFile: (file: File, dir: WalkingDir<R>, value: R) -> Unit,
    walkingUnknownInEach: (File, WalkingDir<R>) -> Unit,
    walkingUnknown: (WalkingDir<R>) -> Unit
){
    val root = rootWalking.dir
    if (root.isDirectory) {
        for (f in root.listFiles()?: emptyArray()){
            if (f.isDirectory) {
                val value = walkDir(f, rootWalking)
                val dir = WalkingDir(f, value)
                fileWalk(dir, walkDir, walkFile, walkingUnknownInEach, walkingUnknown)
            } else if (f.isFile) {
                walkFile(f, rootWalking, rootWalking.value!!)
            } else {
                walkingUnknownInEach(f, rootWalking)
            }
        }
    } else {
        walkingUnknown(rootWalking)
    }
}

data class WalkingDir<R>(val dir: File, val value: R)

fun fileEachCommon(root: File, file: (File) -> Unit) {
    try {
        val files = root.listFiles()!!
        for (f in files) {
            if (f.isDirectory) {
                fileEachCommon(f, file)
            } else if (f.isFile) {
                file(f)
            } else {
                logger.error("无法访问文件: file: $f")
            }
        }
    } catch (e: Throwable) {
        logger.error(e.stackTraceToString() + "root: $root")
    }
}