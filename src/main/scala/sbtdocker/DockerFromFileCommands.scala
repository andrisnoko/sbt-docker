package sbtdocker

import java.io.File

import sbtdocker.Instructions.{DockerfileStaging, ResourceFileStaging}
import sbtdocker.staging.{CopyFile, SourceFile}

trait DockerFromFileInstructions extends DockerFromFileCommands {
  type T <: DockerFromFileInstructions
  def instructions: Seq[Instruction]
}

trait DockerFromFileCommands {
  type T <: DockerFromFileCommands
  def addInstruction(instruction: Instruction): T

  def dockerFilePath(filePath: File): T = addInstruction(DockerfilePath(filePath))

  def addResource(directory: String, include: String): T
  = addInstruction(ResourcesFile(CopyFile( new File(directory + "/" + include)), include))

}

case class DockerfilePath(dockerfilePath: File) extends DockerfileStaging

case class ResourcesFile(source: SourceFile, include: String) extends ResourceFileStaging
