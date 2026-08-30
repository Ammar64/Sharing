import java.io.ByteArrayOutputStream

object Utils {
    fun getVersionNumber(): Int {
        val command = listOf("git", "--no-pager", "log", "-1", "--format=%at")

        val process = ProcessBuilder(command)
            .redirectErrorStream(false)
            .start()

        val output = ByteArrayOutputStream()
        process.inputStream.copyTo(output)
        process.waitFor()

        val versionNumber = output.toString().trim().toLong() - 1779530803L

        return versionNumber.toInt();
    }
}
