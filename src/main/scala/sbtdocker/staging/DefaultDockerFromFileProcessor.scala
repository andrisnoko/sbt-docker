package sbtdocker.staging

import sbt._
import sbtdocker.Instructions.ResourceFileStaging
import sbtdocker._

object DefaultDockerFromFileProcessor extends DockerFromFileProcessor{

  def apply(dockerFromfile: DockerFromFileInstructions, stageDir: File) = {
    dockerFromfile.instructions.filter(_.isInstanceOf[ResourcesFile])
      .foldLeft(StagedDockerfile.empty)(handleInstruction(stageDir))
  }

  private[sbtdocker] def handleInstruction(stageDir: File)(context: StagedDockerfile, instruction: Instruction): StagedDockerfile = {
    instruction match {
      case instruction: ResourceFileStaging =>
        context.stageFile(instruction.source, stageDir / instruction.include)
    }
  }
}
