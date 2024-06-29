provider "aws" {
  region     = "us-east-1"
}

terraform {
  backend "s3" {
    region     = "us-east-1"
    bucket = "terraform-jenkins-cicd"
    key = "cicd-prod.fstate"
  }
}

module "ec2" {
  source = "../modules/ec2module"
  instance_type = "t2.medium"
  aws_common_tag = {
    Name = "ec2-prod-cicd"
  }
  sg_name = "cicd-prod-sg"
  eip_name = "prod-jenkins"
}