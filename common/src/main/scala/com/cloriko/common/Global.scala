package com.cloriko.common

import com.cloriko.protobuf.protocol.{ Directory, File, FileReference }
import com.google.protobuf.ByteString

object Global {
  val `./root/data`: String = "./root/data"
  val `~` = "~"
  val `/`: String = "/"

  implicit def dirUtils(dir: Directory): DirectoryUtils = DirectoryUtils(dir)
  case class DirectoryUtils(directory: Directory) {
    val absolutePath: String = `./root/data` + directory.path + / + directory.dirName
  }

  implicit def fileUtils(file: File): FileUtils = FileUtils(file)
  case class FileUtils(file: File) {
    val absolutePath: String = `./root/data` + file.path + `/` + file.fileName
  }

  implicit def fileReferenceUtils(fileRef: FileReference): FileReferenceUtils = FileReferenceUtils(fileRef)
  case class FileReferenceUtils(fileRef: FileReference) {
    val absolutePath: String = `./root/data` + fileRef.path + `/` + fileRef.fileName
    def asSlaveFile(data: ByteString): File = {
      File(fileRef.fileName, fileRef.path, data)
    }
  }
}
