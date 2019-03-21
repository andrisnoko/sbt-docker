package sbtdocker.staging

import java.io.File

import sbtdocker.DockerFromFileInstructions

trait DockerFromFileProcessor {
  def apply(dockerFromFile: DockerFromFileInstructions, stageDir: File): StagedDockerfile
}
