package sbtdocker

import sbt.AutoPlugin

object DockerFromFilePlugin extends AutoPlugin {
  object autoImport {
    val DockerKeys = sbtdocker.DockerKeys

    val docker = DockerKeys.docker
    val dockerPath = DockerKeys.dockerPath

    val dockerFromFile = DockerKeys.dockerFromFile

    @deprecated("Use imageNames instead.", "1.0.0")
    val imageName = DockerKeys.imageName
    val imageNames = DockerKeys.imageNames
    val buildOptions = DockerKeys.buildOptions

    type DockerFromFile =  sbtdocker.DockerFromFile
    val ImageId = sbtdocker.ImageId
    type ImageId = sbtdocker.ImageId
    val ImageName = sbtdocker.ImageName
    type ImageName = sbtdocker.ImageName
    val BuildOptions = sbtdocker.BuildOptions
    type BuildOptions = sbtdocker.BuildOptions
  }

  override def projectSettings = DockerSettings.baseDockerFileSettings

}
