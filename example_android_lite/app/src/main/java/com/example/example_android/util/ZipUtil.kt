package com.example.example_android.util

import android.content.Context
import android.content.ContextWrapper
import android.os.Environment
import android.text.TextUtils
import android.util.Log
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.Enumeration
import java.util.zip.CRC32
import java.util.zip.CheckedOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object ZipUtil {

    private val BUFF_SIZE = 4 * 1024
    private var zip: Zip? = null

    /**
     * Copy the ZIP file from the raw directory to the app private directory or public storage
     */
    fun copyRawZipFile(context: Context, rawZipFileName: String,FileExtension:String): File? {
        val inputStream: InputStream = context.resources.openRawResource(
            context.resources.getIdentifier(
                rawZipFileName,
                "raw",
                context.packageName
            )
        )

        val outputDir: File = context.filesDir


        val outputFile = File(outputDir, "${rawZipFileName}${FileExtension}")

        try {
            val outputStream = FileOutputStream(outputFile)

            val buffer = ByteArray(1024)
            var length: Int
            while (inputStream.read(buffer).also { length = it } > 0) {
                outputStream.write(buffer, 0, length)
            }

            outputStream.close()
            inputStream.close()

            return outputFile
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("tag","copyRawZipFile =="+e)
        }

        return null
    }


    /**
     * Unzip a Zip file
     *
     * @param desPath Destination directory for extraction
     * @param zipFile zip file name (including the path)
     * @return true if extraction succeeded,false if it failed.
     */
    fun unpackCopyZip(desPath: String, zipFile: String): Boolean {
        if (!zipFile.endsWith(".zip")) {
            return false
        }
        val des = File(desPath)
        if (!des.exists()) {
            des.mkdirs()
        }
        var `is`: InputStream? = null
        var zis: ZipInputStream? = null
        var fout: FileOutputStream? = null
        try {
            var filename: String
            `is` = FileInputStream(zipFile)
            zis = ZipInputStream(BufferedInputStream(`is`))
            var ze: ZipEntry
            val buffer = ByteArray(1024)
            var count: Int
            while (zis.nextEntry.also { ze = it } != null) {
                val zeName = ze.name
                if (zeName.contains(File.separator)) {
                    filename = zeName.substring(0, zeName.lastIndexOf(File.separator))
                    val file = File(desPath + File.separator + filename)
                    if (!file.exists()) {
                        file.mkdirs()
                    }
                } else {
                    filename = zeName
                }

                // Need to create directories if not exists, or
                // it will generate an Exception...
                if (ze.isDirectory) {
                    val fmd = File(desPath + ze)
                    fmd.mkdirs()
                    continue
                }
                fout = FileOutputStream(desPath + zeName)
                while (zis.read(buffer).also { count = it } != -1) {
                    fout.write(buffer, 0, count)
                }
                fout.close()
                zis.closeEntry()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        } finally {
            IOUtil.close(`is`)
            IOUtil.close(zis)
            IOUtil.close(fout)
        }
        return true
    }

    /**
     * Recursively zip a folder
     *
     * @param srcRootDir Sub-path of the root directory of the folder being zipped
     * @param file       File or directory object currently being zipped recursively
     * @param zos        Zip output stream object
     * @throws Exception
     */
    @Throws(Exception::class)
    private fun zip(srcRootDir: String, file: File?, zos: ZipOutputStream) {
        if (file == null) {
            return
        }

        //If it is a file, zip it directly
        if (file.isFile) {
            var count: Int
            val bufferLen = 1024
            val data = ByteArray(bufferLen)

            //Get the file path relative to the root directory of the folder being zipped
            var subPath = file.absolutePath
            val index = subPath.indexOf(srcRootDir)
            if (index != -1) {
                subPath = subPath.substring(srcRootDir.length)
            }
            val entry = ZipEntry(subPath)
            zos.putNextEntry(entry)
            val bis = BufferedInputStream(FileInputStream(file))
            while (bis.read(data, 0, bufferLen).also { count = it } != -1) {
                zos.write(data, 0, count)
            }
            bis.close()
            zos.closeEntry()
        } else {
            //Zip the files or subdirectories in the directory
            val childFileList = file.listFiles()
            for (n in childFileList.indices) {
                childFileList[n].absolutePath.indexOf(file.absolutePath)
                zip(srcRootDir, childFileList[n], zos)
            }
        }
    }

    /**
     * Zip a file or directory
     *
     * @param srcPath     Source path to zip. For a single file, its full path; for a directory, the path of its top-level directory
     * @param zipPath     Path where the zip file is saved. Note: zipPath must not be a subfolder of srcPath
     * @param zipFileName Zip file name
     * @throws Exception
     */
    @Throws(Exception::class)
    fun zip(srcPath: String, zipPath: String, zipFileName: String) {
        if (TextUtils.isEmpty(srcPath) || TextUtils.isEmpty(zipPath) || TextUtils.isEmpty(
                zipFileName
            )
        ) {
            throw Exception("para is empty")
        }
        var cos: CheckedOutputStream? = null
        var zos: ZipOutputStream? = null
        try {
            val srcFile = File(srcPath)

            //Check whether the zip save path is a subfolder of the source path; if so, throw an exception (prevents infinite recursive zipping)
            if (srcFile.isDirectory && zipPath.indexOf(srcPath) != -1) {
                throw Exception("zipPath must not be the child directory of srcPath.")
            }

            //Check whether the zip save path exists; if not, create the directory
            val zipDir = File(zipPath)
            if (!zipDir.exists() || !zipDir.isDirectory) {
                zipDir.mkdirs()
            }

            //Create the file object for the saved zip file
            val zipFilePath = zipPath + File.separator + zipFileName
            val zipFile = File(zipFilePath)
            if (zipFile.exists()) {
                //Check whether the file may be deleted; if not, this throws SecurityException
                val securityManager = SecurityManager()
                securityManager.checkDelete(zipFilePath)
                //Delete the existing target file
                zipFile.delete()
            }
            cos = CheckedOutputStream(FileOutputStream(zipFile), CRC32())
            zos = ZipOutputStream(cos)

            //If only a single file is zipped, take its parent directory
            var srcRootDir = srcPath
            if (srcFile.isFile) {
                val index = srcPath.lastIndexOf(File.separator)
                if (index != -1) {
                    srcRootDir = srcPath.substring(0, index)
                }
            }
            //Call the recursive zip method to zip the directory or file
            zip(srcRootDir, srcFile, zos)
            zos.flush()
        } catch (e: Exception) {
            throw e
        } finally {
            try {
                zos?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Unzip
     *
     * @param zipFilePath Zip file
     * @param unzipPath   Extraction path
     * @return return true if success
     */
    fun unzip(zipFilePath: String, unzipPath: String): Boolean {
        return try {
            val zipFile = ZipFile(zipFilePath)
            val emu: Enumeration<*> = zipFile.entries()
            while (emu.hasMoreElements()) {
                val entry = emu.nextElement() as ZipEntry
                val zipFileName = entry.name.replace("../", "")
                if (entry.isDirectory) {
                    File("$unzipPath/$zipFileName").mkdirs()
                    continue
                }
                val bis = BufferedInputStream(zipFile.getInputStream(entry))
                val file = File("$unzipPath/$zipFileName")
                val parent = file.parentFile
                if (parent != null && !parent.exists()) {
                    parent.mkdirs()
                }
                val fos = FileOutputStream(file)
                val bos = BufferedOutputStream(fos)
                var count: Int
                val data = ByteArray(BUFF_SIZE)
                while (bis.read(data, 0, BUFF_SIZE).also { count = it } != -1) {
                    bos.write(data, 0, count)
                }
                bos.flush()
                bos.close()
                bis.close()
            }
            zipFile.close()
            true
        } catch (e: Exception) {
            Log.e("ZipUtil", "unzip error! zip file:$zipFilePath unzip to path:$unzipPath")
            e.printStackTrace()
            false
        }
    }

    fun unzip(inputStream: InputStream?, outPathString: String): Boolean {
        try {
            ZipInputStream(inputStream).use { inZip ->
                var zipEntry: ZipEntry
                var szName = ""
                while (inZip.nextEntry.also { zipEntry = it } != null) {
                    szName = zipEntry.name.replace("../", "")
                    if (zipEntry.isDirectory) {
                        //Get the folder name of the entry
                        szName = szName.substring(0, szName.length - 1)
                        val folder =
                            File(outPathString + File.separator + szName)
                        folder.mkdirs()
                    } else {
                        val file =
                            File(outPathString + File.separator + szName)
                        if (!file.exists()) {
                            file.parentFile.mkdirs()
                            file.createNewFile()
                        }
                        // Get the output stream of the file
                        val out = FileOutputStream(file)
                        var len: Int
                        val buffer = ByteArray(1024)
                        // Read bytes into the buffer
                        while (inZip.read(buffer).also { len = it } != -1) {
                            // Write the bytes from buffer position (0)
                            out.write(buffer, 0, len)
                            out.flush()
                        }
                        //Close the stream
                        IOUtil.close(out)
                    }
                }
                //Close the stream
                IOUtil.close(inputStream)
                IOUtil.close(inZip)
                return true
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return false
    }

    /**
     * @throws Exception
     */
    @Throws(Exception::class)
    fun ZipFolder(srcFileString: String?, zipFileString: String?) {
        Log.v("XZip", "ZipFolder(String, String)")
        val outZip = ZipOutputStream(FileOutputStream(zipFileString))
        val file = File(srcFileString)
        ZipFiles(file.parent + File.separator, file.name, outZip)
        outZip.finish()
        outZip.close()
        zip!!.onFinish()
    } // run_go of func


    /**
     * @param folderString
     * @param fileString
     * @param zipOutputSteam
     * @throws Exception
     */
    @Throws(Exception::class)
    private fun ZipFiles(
        folderString: String,
        fileString: String,
        zipOutputSteam: ZipOutputStream?
    ) {
        Log.v("XZip", "ZipFiles(String, String, ZipOutputStream)")
        if (zipOutputSteam == null) return
        val file = File(folderString + fileString)
        if (file.isFile) {
            val zipEntry = ZipEntry(fileString)
            val inputStream = FileInputStream(file)
            zipOutputSteam.putNextEntry(zipEntry)
            var len: Int
            val buffer = ByteArray(4096)
            while (inputStream.read(buffer).also { len = it } != -1) {
                zipOutputSteam.write(buffer, 0, len)
            }
            zipOutputSteam.closeEntry()
        } else {
            val fileList = file.list()
            if (fileList.size <= 0) {
                val zipEntry = ZipEntry(fileString + File.separator)
                zipOutputSteam.putNextEntry(zipEntry)
                zipOutputSteam.closeEntry()
            }
            for (i in fileList.indices) {
                ZipFiles(folderString, fileString + File.separator + fileList[i], zipOutputSteam)
            } // run_go of for
        } // run_go of if
    } // run_go of func


    fun OnZip(zip: Zip?) {
        this.zip = zip
    }


    interface Zip {
        fun onFinish()
    }

}