package sbtdocker

import sbt.File
import sbtdocker.Instructions.DockerfileStaging
import sbtdocker.staging.CopyFile

trait DockerFromFileInstructions extends DockerFromFileCommands {
  type T <: DockerFromFileInstructions
  def instructions: Seq[Instruction]
}

trait DockerFromFileCommands {
  type T <: DockerFromFileCommands
  def addInstruction(instruction: Instruction): T

  /**
    *
    * @param dockerFile Dockerfile to stage
    */
  def fromFile(dockerFile: File): T = addInstruction(FromFile(dockerFile))

  /**
    * Stage a file. The file will be copied to the stage directory when the Dockerfile is built.
    *
    *
    * The `target` file must be unique for this Dockerfile. Otherwise later staged files will overwrite previous
    * files on the same target.
    *
    * @param source File to copy into stage dir.
    * @param target Path to copy file to, should be relative to the stage dir.
    */
  def stageFile(source: File, target: String): T = {
    addInstruction(Instructions.StageFiles(CopyFile(source), target))
  }
}

  /**
    *
    * @param dockerfile Dockerfile to stage
    */
case class FromFile(dockerfile: File) extends DockerfileStaging
