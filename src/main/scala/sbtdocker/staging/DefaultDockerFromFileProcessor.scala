package sbtdocker.staging

import sbt._
import sbtdocker._

object DefaultDockerFromFileProcessor extends DockerFromFileProcessor {

  def apply(dockerFromfile: DockerFromFileInstructions, stageDir: File) = {
    dockerFromfile.instructions.filter(_.isInstanceOf[FileStagingInstruction])
      .foldLeft(StagedDockerfile.empty)(handleInstruction(stageDir))
  }

  private[sbtdocker] def handleInstruction(stageDir: File)(context: StagedDockerfile, instruction: Instruction): StagedDockerfile = {
    val i = instruction.asInstanceOf[FileStagingInstruction]
    context.stageFile(i.sources(0), stageDir / i.destination)
  }
}
