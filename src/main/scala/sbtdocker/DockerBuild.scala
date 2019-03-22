package sbtdocker

import java.io.{FileInputStream, FileOutputStream}

import sbt._
import sbtdocker.Instructions.DockerfileStaging
import sbtdocker.staging.{DockerFromFileProcessor, DockerfileProcessor, StagedDockerfile}

import scala.sys.process.{Process, ProcessLogger}

object DockerBuild {
  /**
    * Build a Dockerfile using a provided docker binary.
    *
    * @param dockerfile   Dockerfile to build
    * @param processor    processor to create a staging directory for the Dockerfile
    * @param imageNames   names of the resulting image
    * @param stageDir     stage dir
    * @param dockerPath   path to the docker binary
    * @param buildOptions options for the build command
    * @param log          logger
    */
  def apply(dockerfile: DockerfileLike, processor: DockerfileProcessor, imageNames: Seq[ImageName],
                      buildOptions: BuildOptions, stageDir: File, dockerPath: String,
                      log: Logger): ImageId = {
    val staged = processor(dockerfile, stageDir)

    apply(staged, imageNames, buildOptions, stageDir, dockerPath, log)
  }

  /**
    * Build a Dockerfile using a provided docker binary.
    *
    * @param staged       a staged Dockerfile to build.
    * @param imageNames   names of the resulting image
    * @param stageDir     stage dir
    * @param dockerPath   path to the docker binary
    * @param buildOptions options for the build command
    * @param log          logger
    */
  def apply(staged: StagedDockerfile, imageNames: Seq[ImageName], buildOptions: BuildOptions,
            stageDir: File, dockerPath: String, log: Logger, dockerTextFile: Option[File] =
            Option.empty)
  : ImageId
  = {
    log.debug(s"Preparing stage directory '${stageDir.getPath}'")

    clean(stageDir)
    if (!dockerTextFile.isEmpty) {
      copyDockerfile(dockerTextFile.get, stageDir)
    } else {
      log.debug("Building Dockerfile:\n" + staged.instructionsString)
      createDockerfile(staged, stageDir)
    }
    prepareFiles(staged)
    buildAndTag(imageNames, stageDir, dockerPath, buildOptions, log)
  }

  def apply(dockerFromFile: DockerFromFileInstructions, processor: DockerFromFileProcessor,
            imageNames: Seq[ImageName],
            buildOptions: BuildOptions,
            stageDir: File,
            dockerPath: String,
            log: Logger)
  : ImageId = {
    val staged = processor(dockerFromFile, stageDir)
    val dockerfilePath = dockerfile(dockerFromFile.instructions)
    apply(staged, imageNames, buildOptions, stageDir, dockerPath, log, Option(dockerfilePath))
  }


  private[sbtdocker] def dockerfile (instructions: Seq[Instruction]) = {
    val i = instructions.find( _.isInstanceOf[DockerfileStaging])
    val d = i.get.asInstanceOf[FromFile]
    d.dockerfile
  }

  private[sbtdocker] def copyDockerfile(dockerFile: File, stageDir: File) = {
    stageDir.mkdir
    IO.transferAndClose(new FileInputStream(dockerFile), new FileOutputStream(stageDir/
      "Dockerfile"))
  }

  private[sbtdocker] def clean(stageDir: File) = {
    IO.delete(stageDir)
  }

  private[sbtdocker] def createDockerfile(staged: StagedDockerfile, stageDir: File) = {
    IO.write(stageDir / "Dockerfile", staged.instructionsString)
  }

  private[sbtdocker] def prepareFiles(staged: StagedDockerfile) = {
    staged.stageFiles.foreach {
      case (source, destination) =>
        source.stage(destination)
    }
  }

  private val SuccessfullyBuilt = "^Successfully built ([0-9a-f]+)$".r

  private[sbtdocker] def buildAndTag(imageNames: Seq[ImageName], stageDir: File,
                                     dockerPath: String, buildOptions: BuildOptions, log: Logger): ImageId
  = {
    val processLogger = ProcessLogger({ line =>
      log.info(line)
    }, { line =>
      log.info(line)
    })

    val imageId = build(stageDir, dockerPath, buildOptions, log, processLogger)

    imageNames.foreach { name =>
      DockerTag(imageId, name, dockerPath, log)
    }
    
    imageId
  }

  private[sbtdocker] def build(stageDir: File, dockerPath: String, buildOptions: BuildOptions,
                               log: Logger, processLogger: ProcessLogger): ImageId = {
    val flags = buildFlags(buildOptions)

    val command = dockerPath :: "build" :: flags ::: "." :: Nil

    log.info(s"Running command: '${command.mkString(" ")}' in '${stageDir.absString}'")

    val processOutput = Process(command, stageDir).lines(processLogger)
    processOutput.foreach { line =>
      log.info(line)
    }

    val imageId = processOutput.collect {
      case SuccessfullyBuilt(id) => ImageId(id)
    }.lastOption

    imageId match {
      case Some(id) =>
        id
      case None =>
        sys.error("Could not parse image id")
    }
  }

  private[sbtdocker] def buildFlags(buildOptions: BuildOptions): List[String] = {
    val cacheFlag = "--no-cache=" + !buildOptions.cache
    val removeFlag = {
      buildOptions.removeIntermediateContainers match {
        case BuildOptions.Remove.Always =>
          "--force-rm=true"
        case BuildOptions.Remove.Never =>
          "--rm=false"
        case BuildOptions.Remove.OnSuccess =>
          "--rm=true"
      }
    }
    val pullFlag = {
      val value = buildOptions.pullBaseImage match {
        case BuildOptions.Pull.Always => true
        case BuildOptions.Pull.IfMissing => false
      }
      "--pull=" + value
    }

    cacheFlag :: removeFlag :: pullFlag :: Nil
  }
}
